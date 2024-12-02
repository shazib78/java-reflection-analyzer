package de.upb.sse.cutNRun.analyzer;

import de.upb.sse.cutNRun.analyzer.interprocedural.IDEValueAnalysisProblem;
import de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource;
import de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSourceAnalysis;
import de.upb.sse.cutNRun.analyzer.intraprocedural.Result;
import de.upb.sse.cutNRun.analyzer.intraprocedural.StringConcatenationSource;
import de.upb.sse.cutNRun.analyzer.methodSignature.ModernReflectionMethodSignature;
import de.upb.sse.cutNRun.analyzer.methodSignature.ReflectionMethodSignature;
import de.upb.sse.cutNRun.analyzer.methodSignature.UnsoundMethodSignatureCategory;
import de.upb.sse.cutNRun.analyzer.soot.BackwardsInterproceduralCFG;
import heros.InterproceduralCFG;
import lombok.extern.slf4j.Slf4j;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.analysis.interprocedural.ide.JimpleIDESolver;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.UNKOWN;

@Slf4j
public class ProgramAnalyzerAdaptor implements ProgramAnalyzerPort {
    private View view;
    private List<UnsoundMethodSignatureCategory> unsoundMethodSignatureCategories;

    public ProgramAnalyzerAdaptor(View view) {
        this.view = view;
        this.unsoundMethodSignatureCategories = List.of(new ReflectionMethodSignature(view),
                                                        new ModernReflectionMethodSignature(view));
    }

    @Override
    public void analyze() {
        int totalSourcesOfUnsoundnessCount = 0;
        for (SootClass sootClass : view.getClasses().toList()) {
            for (SootMethod sootMethod : sootClass.getMethods()) {
                System.out.println("method: " + sootMethod.getSignature());
                System.out.println(sootMethod.getBody());
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
                    //TODO: uncomment for interprocedural Analysis
                    /*unsoundStatements.stream()
                            .forEach(stmt -> performInterProceduralAnalysis(sootMethod, stmt));*/
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

    private void performInterProceduralAnalysis(SootMethod entryPointMethod, Stmt startStmt) {
        /*final ClassType classType = view.getIdentifierFactory().getClassType(targetTestClassName);
        final Optional<JavaSootClass> aClass = view.getClass(classType);
        if (!aClass.isPresent()) {
            throw new IllegalArgumentException("Entrypoint class is not in the View.");
        }*/
        //entryPointMethod = aClass.get().getMethods().stream().filter(SootMethod::hasBody).filter(ms -> ms.getName().equals("entryPoint")).findAny().get();
        log.info("----------------------------------------");
        log.info("Starting inter-procedural analysis");
        log.info("Entry point:" +entryPointMethod.toString());
        final List<MethodSignature> entryPoints = Collections.singletonList(entryPointMethod.getSignature());
        JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG(view, entryPoints, false, true);
        BackwardsInterproceduralCFG backwardICFG = new BackwardsInterproceduralCFG(icfg);
        /*
        //TODO: testing start
        List<Stmt> startStmt = (List<Stmt>) backwardICFG.getStartPointsOf(entryPointMethod);
        Stmt temp = backwardICFG.getSuccsOf(startStmt.get(0)).get(0);
        log.info(temp.toString());
        do{
            if(!backwardICFG.isExitStmt(temp)) {
                temp = backwardICFG.getSuccsOf(temp).get(0);
                log.info(temp.toString());
            }else{
                log.info(temp.toString());
                break;
            }
        }while (true);
        //TODO: testing end
        */

        IDEValueAnalysisProblem problem = new IDEValueAnalysisProblem(backwardICFG, entryPoints, startStmt, (JavaView) view);
        JimpleIDESolver<Local, String, InterproceduralCFG<Stmt, SootMethod>> solver = new JimpleIDESolver<>(problem);
        solver.solve();
        Map<Local, String> result = solver.resultsAt(backwardICFG.getEndPointsOf(entryPointMethod).stream().findFirst().get());
        log.info("RESULT: {} = {}", result.keySet().stream().findFirst().get(), result.values().stream().findFirst().get());
        log.info("End of inter-procedural analysis");
    }

    private void performIntraProceduralAnalysis(SootMethod sootMethod, Stmt startStmt) {
        StmtGraph<?> stmtGraph = sootMethod.getBody().getStmtGraph();
        ArgumentSourceAnalysis argumentSourceAnalysis = new ArgumentSourceAnalysis(stmtGraph, startStmt, view);
        argumentSourceAnalysis.execute();
        Result result = argumentSourceAnalysis.getResult();
        ArgumentSource argumentSource = result.getArgumentSource();
        StringConcatenationSource stringConcatResult = argumentSourceAnalysis.getStringConcatenationSource();
        if (argumentSource == UNKOWN && !stringConcatResult.isEmpty()) {
            argumentSource = stringConcatResult.getSource();
            log.info("String Concatenation sources: {}", stringConcatResult.getArgumentSources());
        }
        log.info("Argument Source: {} for Statement: {}", argumentSource, startStmt);
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
