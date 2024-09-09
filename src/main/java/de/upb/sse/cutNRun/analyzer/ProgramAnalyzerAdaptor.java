package de.upb.sse.cutNRun.analyzer;

import de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSourceAnalysis;
import de.upb.sse.cutNRun.analyzer.methodSignature.ReflectionMethodSignature;
import de.upb.sse.cutNRun.analyzer.methodSignature.UnsoundMethodSignatureCategory;
import lombok.extern.slf4j.Slf4j;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.views.View;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ProgramAnalyzerAdaptor implements ProgramAnalyzerPort {
    private View view;
    private List<UnsoundMethodSignatureCategory> unsoundMethodSignatureCategories;

    public ProgramAnalyzerAdaptor(View view) {
        this.view = view;
        this.unsoundMethodSignatureCategories = List.of(new ReflectionMethodSignature(view));
    }

    @Override
    public void analyze() {
        int totalSourcesOfUnsoundnessCount = 0;
        for (SootClass sootClass : view.getClasses().toList()) {
            for (SootMethod sootMethod : sootClass.getMethods()) {
                //System.out.println("method: " + sootMethod.getSignature());
                //System.out.println(sootMethod.getBody());
                List<Stmt> statements = sootMethod.hasBody() ? sootMethod.getBody().getStmts() : Collections.emptyList();
                List<Stmt> unsoundStatements = statements.stream()
                                                         .filter(statement -> isSourceOfUnsoundness(statement))
                                                         .collect(Collectors.toList());
                if (!unsoundStatements.isEmpty()) {
                    log.debug("------------START--------------");
                    log.debug("method: " + sootMethod.getSignature());
                    log.debug("Sources of Unsoundness - Statements:");
                    unsoundStatements.stream().forEach(stmt -> log.debug(stmt.toString()));
                    log.debug("Sources of Unsoundness - Count: " + unsoundStatements.size());
                    log.debug("------------END--------------");
                    totalSourcesOfUnsoundnessCount = totalSourcesOfUnsoundnessCount + unsoundStatements.size();
                    unsoundStatements.stream()
                            .forEach(stmt -> performIntraProceduralAnalysis(sootMethod, stmt));
                }
            }
            /*for(MethodSignature  : methodSignaturesToSearch)
            if (!sootClass.getMethod(methodSignature.getSubSignature()).isPresent()) {
                System.out.println("Method not found!");
                return;  // Exit if the method is not found
            }*/
        }
        log.info("Sources of Unsoundness - Total Count: " + totalSourcesOfUnsoundnessCount);
    }

    private void performIntraProceduralAnalysis(SootMethod sootMethod, Stmt startStmt) {
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();
        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt);
        argumentSourceAnalysis.execute();
        log.info("Argument Source: {} for Statement: {}", argumentSourceAnalysis.getArgumentSource(), startStmt);
    }

    private boolean isSourceOfUnsoundness(Stmt statement) {
        if (statement instanceof JInvokeStmt) {
            JInvokeStmt jInvokeStmt = (JInvokeStmt) statement;
            AbstractInvokeExpr abstractInvokeExpr = jInvokeStmt.getInvokeExpr().orElse(null);
            if (abstractInvokeExpr instanceof JVirtualInvokeExpr) {
                /*JVirtualInvokeExpr jVirtualInvokeExpr = (JVirtualInvokeExpr) statement.getInvokeExpr();*/
            /*boolean isUnsoundStatement = unsoundMethodSignatureTypes.stream()
                                       .map(methodSignatureType -> methodSignatureType.getSignatures())
                    .anyMatch(methodSignature -> methodSignature.contains(jVirtualInvokeExpr.getMethodSignature()));*/
                return isInvokeExpressionUnsound((JVirtualInvokeExpr) abstractInvokeExpr);
            }
        } else if (statement instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) statement;
            Value rightOp = jAssignStmt.getRightOp();
            if (rightOp instanceof JVirtualInvokeExpr) {
                return isInvokeExpressionUnsound((JVirtualInvokeExpr) rightOp);
            }
        }
        return false;
    }

    private boolean isInvokeExpressionUnsound(JVirtualInvokeExpr jVirtualInvokeExpr) {
        return unsoundMethodSignatureCategories
                .stream()
                .anyMatch(category -> category.isSourceOfUnsoundness(jVirtualInvokeExpr.getMethodSignature()));
    }
}
