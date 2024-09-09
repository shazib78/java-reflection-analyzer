package de.upb.sse.cutNRun.analyzer.intraprocedural;

import lombok.extern.slf4j.Slf4j;
import sootup.analysis.intraprocedural.BackwardFlowAnalysis;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.LinePosition;
import sootup.java.core.jimple.basic.JavaLocal;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.INTRAPROCEDURAL;
import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.UNKOWN;

@Slf4j
public class ArgumentSourceAnalysis extends BackwardFlowAnalysis<Set<UnsoundStatementArgumentSource>> {
    private boolean isStartStatementReached;
    private Stmt startStmt;
    private ArgumentSource argumentSource;
    private JavaLocal trackVariable;

    public <B extends BasicBlock<B>> ArgumentSourceAnalysis(StmtGraph<B> graph, Stmt startStmt) {
        super(graph);
        this.isStartStatementReached = false;
        this.startStmt = startStmt;
        this.argumentSource = UNKOWN;
    }

    @Override
    protected void flowThrough(@Nonnull Set<UnsoundStatementArgumentSource> in, Stmt stmt,
                               @Nonnull Set<UnsoundStatementArgumentSource> out) {
        log.info(stmt.toString());
        if(!isStartStatementReached && stmt.equivTo(startStmt)){
            isStartStatementReached = true;
        }

        if(isStartStatementReached){
            if(stmt.equivTo(startStmt)) {
                JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(stmt);
                Immediate argument = jVirtualInvokeExpr.getArg(0);
                if(argument instanceof StringConstant){
                    LinePosition linePosition = (LinePosition) stmt.getPositionInfo().getStmtPosition();
                    argumentSource = INTRAPROCEDURAL;
                    out.add(UnsoundStatementArgumentSource.builder()
                                    .statementLineNumber(linePosition.getFirstLine())
                                    .argumentSource(ArgumentSource.INTRAPROCEDURAL)
                                                          .build());
                } else if (argument instanceof JavaLocal) {
                    trackVariable = (JavaLocal) argument;
                }
            } else if (stmt instanceof JAssignStmt) {
                JAssignStmt jAssignStmt = (JAssignStmt) stmt;
                Value leftOp = jAssignStmt.getLeftOp();
                Value rightOp = jAssignStmt.getRightOp();
                if(leftOp.equivTo(trackVariable)){
                    if(rightOp instanceof JavaLocal){
                        trackVariable = (JavaLocal) rightOp;
                    } else if () {
                        //TODO: logic for assignment
                    }
                }
            }
        }
    }

    private JVirtualInvokeExpr getJVirtualInvokeExpr(Stmt stmt) {
        if (stmt instanceof JInvokeStmt) {
            JInvokeStmt jInvokeStmt = (JInvokeStmt) stmt;
            AbstractInvokeExpr abstractInvokeExpr = jInvokeStmt.getInvokeExpr().orElse(null);
            if (abstractInvokeExpr instanceof JVirtualInvokeExpr) {
                return (JVirtualInvokeExpr) abstractInvokeExpr;
            }
        } else if (stmt instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) stmt;
            Value rightOp = jAssignStmt.getRightOp();
            if (rightOp instanceof JVirtualInvokeExpr) {
                return (JVirtualInvokeExpr) rightOp;
            }
        }
        return null;
    }

    @Nonnull
    @Override
    protected Set<UnsoundStatementArgumentSource> newInitialFlow() {
        return new HashSet<>();
    }

    @Override
    protected void merge(@Nonnull Set<UnsoundStatementArgumentSource> in1, @Nonnull Set<UnsoundStatementArgumentSource> in2, @Nonnull Set<UnsoundStatementArgumentSource> out) {
        if (in1.size() > 1 || in2.size() > 1) {
            throw new RuntimeException("Unexpected");
        } else if(in1.isEmpty() && !in2.isEmpty()) {
            out.addAll(in2);
        } else if (!in1.isEmpty() && in2.isEmpty()) {
            out.addAll(in1);
        } else if (!in1.isEmpty() && !in2.isEmpty()) {
            if(in1.equals(in2)){
                out.addAll(in1);
            }
        }
    }

    @Override
    protected void copy(@Nonnull Set<UnsoundStatementArgumentSource> source, @Nonnull Set<UnsoundStatementArgumentSource> destination) {
        destination.addAll(source);
    }

    public void execute(){
        super.execute();
    }

    public ArgumentSource getArgumentSource() {
        return argumentSource;
    }
}
