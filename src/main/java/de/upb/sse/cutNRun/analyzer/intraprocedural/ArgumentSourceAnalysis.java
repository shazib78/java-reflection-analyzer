package de.upb.sse.cutNRun.analyzer.intraprocedural;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import sootup.analysis.intraprocedural.BackwardFlowAnalysis;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.LinePosition;
import sootup.core.views.View;
import sootup.java.core.jimple.basic.JavaLocal;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import static de.upb.sse.cutNRun.analyzer.helper.AnalysisHelper.getJVirtualInvokeExpr;
import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.INTRAPROCEDURAL;
import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.UNKOWN;

@Slf4j
public class ArgumentSourceAnalysis extends BackwardFlowAnalysis<Set<Result>> {
    private boolean isStartStatementReached;
    private Stmt startStmt;
    @Getter
    private Result result;
    private View view;
    @Getter
    private StringConcatenationSource stringConcatenationSource;

    public <B extends BasicBlock<B>> ArgumentSourceAnalysis(StmtGraph<B> graph, Stmt startStmt, View view) {
        super(graph);
        this.isStartStatementReached = false;
        this.startStmt = startStmt;
        this.result = Result.builder()
                            .argumentSource(UNKOWN)
                            .build();
        stringConcatenationSource = StringConcatenationSource.builder().build();
        this.view = view;
    }

    @Override
    protected void flowThrough(@Nonnull Set<Result> in, Stmt stmt,
                               @Nonnull Set<Result> out) {
        log.info(stmt.toString());
        if (!isStartStatementReached && stmt.equivTo(startStmt)) {
            isStartStatementReached = true;
        }

        if (isStartStatementReached) {
            if (stmt.equivTo(startStmt)) {
                JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(stmt);
                Immediate argument = jVirtualInvokeExpr.getArg(0);
                if (argument instanceof StringConstant) {
                    LinePosition linePosition = (LinePosition) stmt.getPositionInfo().getStmtPosition();
                    result.setStatementLineNumber(linePosition.getFirstLine());
                    result.setArgumentSource(INTRAPROCEDURAL);
                    out.add(Result.builder()
                                  .statementLineNumber(linePosition.getFirstLine())
                                  .argumentSource(INTRAPROCEDURAL)
                                  .build());
                } else if (argument instanceof JavaLocal) {
                    result.setTrackVariable((JavaLocal) argument);
                }
            } else if (stmt instanceof JAssignStmt) {
                JAssignStmt jAssignStmt = (JAssignStmt) stmt;
                Value leftOp = jAssignStmt.getLeftOp();
                Value rightOp = jAssignStmt.getRightOp();
                //To handle aliases and string concatenation
                if (leftOp.equivTo(result.getTrackVariable())) {
                    if (rightOp instanceof JavaLocal) {
                        result.setTrackVariable((JavaLocal) rightOp);
                    } else if (rightOp instanceof JDynamicInvokeExpr) {
                        StringConcatenationProcessor stringConcatenationProcessor = new StringConcatenationProcessor(view, result, out, stmt,
                                                                                                                     stringConcatenationSource);
                        stringConcatenationProcessor.process(rightOp);
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    protected Set<Result> newInitialFlow() {
        return new HashSet<>();
    }

    @Override
    protected void merge(@Nonnull Set<Result> in1, @Nonnull Set<Result> in2, @Nonnull Set<Result> out) {
        if (in1.size() > 1 || in2.size() > 1) {
            throw new RuntimeException("Unexpected");
        } else if (in1.isEmpty() && !in2.isEmpty()) {
            out.addAll(in2);
        } else if (!in1.isEmpty() && in2.isEmpty()) {
            out.addAll(in1);
        } else if (!in1.isEmpty() && !in2.isEmpty()) {
            if (in1.equals(in2)) {
                out.addAll(in1);
            }
        }
    }

    @Override
    protected void copy(@Nonnull Set<Result> source, @Nonnull Set<Result> destination) {
        destination.addAll(source);
    }

    public void execute() {
        super.execute();
    }
}
