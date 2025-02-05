package de.upb.sse.cutNRun.analyzer.intraprocedural;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import sootup.analysis.intraprocedural.BackwardFlowAnalysis;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.LinePosition;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.upb.sse.cutNRun.analyzer.helper.AnalysisHelper.*;
import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.*;

@Slf4j
public class ArgumentSourceAnalysis extends BackwardFlowAnalysis<Set<Result>> {
    private boolean isStartStatementReached;
    private boolean isTraditionalReflection;
    private boolean isTraditionalNewInstanceReflection;
    private boolean isTraditionalMethodReflection;
    private boolean isTraditionalFieldReflection;
    private boolean isModernNewInstanceReflection;
    private boolean isModernMethodReflection;
    private boolean isModernFieldReflection;
    private Stmt startStmt;
    @Getter
    private Result result;
    private View view;
    @Getter
    private StringConcatenationSource stringConcatenationSource;
    private boolean isBranching;

    public <B extends BasicBlock<B>> ArgumentSourceAnalysis(StmtGraph<B> graph, Stmt startStmt, View view) {
        super(graph);
        this.isStartStatementReached = false;
        this.isTraditionalReflection = false;
        this.isTraditionalNewInstanceReflection = false;
        this.isTraditionalMethodReflection = false;
        this.isTraditionalFieldReflection = false;
        this.isModernNewInstanceReflection = false;
        this.isModernMethodReflection = false;
        this.isModernFieldReflection = false;
        this.startStmt = startStmt;
        this.result = Result.builder()
                            .argumentSource(UNKNOWN)
                            .build();
        stringConcatenationSource = StringConcatenationSource.builder().build();
        this.view = view;
        this.isBranching = false;
    }

    @Override
    protected void flowThrough(@Nonnull Set<Result> in, Stmt stmt,
                               @Nonnull Set<Result> out) {
        log.info(stmt.toString());
        if (!in.isEmpty()) {
            out.addAll(in);
        }
        if (!isStartStatementReached && stmt.equivTo(startStmt)) {
            isStartStatementReached = true;
        }

        if (isStartStatementReached) {
            if (stmt.equivTo(startStmt)) {
                JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(stmt);
                if (isTraditionalReflection(jVirtualInvokeExpr.getMethodSignature(), view)) {
                    isTraditionalReflection = true;
                    if (isTraditionalNewInstanceReflection(jVirtualInvokeExpr.getMethodSignature(), view)) {
                        isTraditionalNewInstanceReflection = true;
                    } else if (isTraditionalMethodReflection(jVirtualInvokeExpr.getMethodSignature(), view)) {
                        isTraditionalMethodReflection = true;
                    } else if (isTraditionalFieldReflection(jVirtualInvokeExpr.getMethodSignature(), view)) {
                        isTraditionalFieldReflection = true;
                    }
                } else {
                    if (isModernNewInstanceReflection(jVirtualInvokeExpr.getMethodSignature(), view)) {
                        isModernNewInstanceReflection = true;
                    } else if (isModernMethodReflection(jVirtualInvokeExpr.getMethodSignature(), view)) {
                        isModernMethodReflection = true;
                    } else if (isModernFieldReflection(jVirtualInvokeExpr.getMethodSignature(), view)) {
                        isModernFieldReflection = true;
                    }
                }
            }

            if (isTraditionalReflection) {
                if (isTraditionalMethodReflection || isTraditionalFieldReflection) {
                    analyzeMethodAndFieldReflection(out, stmt, false);
                } else if (isTraditionalNewInstanceReflection) {
                    analyzeNewInstanceReflection(out, stmt, false);
                }
            } else {
                if (isModernMethodReflection || isModernFieldReflection) {
                    analyzeMethodAndFieldReflection(out, stmt, true);
                } else if (isModernNewInstanceReflection) {
                    analyzeNewInstanceReflection(out, stmt, true);
                }
            }
        }
    }

    private void analyzeNewInstanceReflection(Set<Result> out, Stmt stmt, boolean isModernReflection) {
        if (stmt.equivTo(startStmt)) {
            JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(stmt);
            Immediate immediate;
            if (isModernReflection) {
                immediate = jVirtualInvokeExpr.getArg(0);
            } else {
                immediate = jVirtualInvokeExpr.getBase();
            }

            if (immediate instanceof ClassConstant) {
                setResultArgumentSource(LOCAL, stmt, out);
            } else if (immediate instanceof Local) {
                result.setTrackVariable((Local) immediate);
            }
        } else if (stmt instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) stmt;
            Value leftOp = jAssignStmt.getLeftOp();
            Value rightOp = jAssignStmt.getRightOp();
            //List<JavaLocal> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
            //To handle aliases, string concatenation, method return value source
            if (leftOp.equivTo(result.getTrackVariable()) /*||
                    (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp))*/) {

                if (rightOp instanceof JCastExpr) {
                    JCastExpr jCastExpr = (JCastExpr) rightOp;
                    rightOp = jCastExpr.getOp();
                }

                if (rightOp instanceof Local) {
                    result.setTrackVariable((Local) rightOp);
                } /*else if (rightOp instanceof JDynamicInvokeExpr) {
                    StringConcatenationProcessor stringConcatenationProcessor =
                            new StringConcatenationProcessor(view, leftOp, out, stmt, stringConcatenationSource);
                    stringConcatenationProcessor.process(rightOp);
                }*/ else if (/*(*/rightOp instanceof AbstractInstanceInvokeExpr || rightOp instanceof JStaticInvokeExpr/*)
                        && stringConcatenationSource.isEmpty()*/) {
                    AbstractInvokeExpr abstractInvokeExpr = (AbstractInvokeExpr) rightOp;
                    MethodSignature getConstructorMethodSignature = buildGetConstructorMethodSignature();
                    if (getConstructorMethodSignature.equals(abstractInvokeExpr.getMethodSignature())) {
                        Local local = getJVirtualInvokeExpr(stmt).getBase();
                        if (local instanceof Local) {
                            result.setTrackVariable((Local) local);
                        }
                    } else {
                        setResultArgumentSource(RETURN_FROM_METHOD, stmt, out);
                    }
                } else if (rightOp instanceof JFieldRef /*&& stringConcatenationSource.isEmpty()*/) {
                    setResultArgumentSource(FIELD, stmt, out);
                } else if (rightOp instanceof ClassConstant /*&& stringConcatenationSource.isEmpty()*/) {
                    setResultArgumentSource(LOCAL, stmt, out);
                }/* else if (!stringConcatenationSource.isEmpty()) {
                    updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                }*/
            }
        } else if (stmt instanceof JIdentityStmt) {
            JIdentityStmt jIdentityStmt = (JIdentityStmt) stmt;
            Value leftOp = jIdentityStmt.getLeftOp();
            Value rightOp = jIdentityStmt.getRightOp();
            /*List<JavaLocal> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();*/
            if (leftOp.equivTo(result.getTrackVariable()) /*||
                    (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp))*/) {
                if (rightOp instanceof JParameterRef /*&& stringConcatenationSource.isEmpty()*/) {
                    setResultArgumentSource(METHOD_PARAMETER, stmt, out);
                }/* else if (!stringConcatenationSource.isEmpty()) {
                    updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                }*/
            }
        }
    }

    private MethodSignature buildGetConstructorMethodSignature() {
        ClassType classType = view.getIdentifierFactory().getClassType("java.lang.Class");
        return view.getIdentifierFactory()
                   .getMethodSignature(classType, "getConstructor", "java.lang.reflect.Constructor", Arrays.asList("java.lang.Class[]"));
    }

    private void analyzeMethodAndFieldReflection(Set<Result> out, Stmt stmt, boolean isModernReflection) {
        if (stmt.equivTo(startStmt)) {
            JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(stmt);
            Immediate argument;
            if (isModernReflection) {
                argument = jVirtualInvokeExpr.getArg(1);
            } else {
                argument = jVirtualInvokeExpr.getArg(0);
            }
            if (argument instanceof StringConstant) {
                setResultArgumentSource(LOCAL, stmt, out);
            } else if (argument instanceof Local) {
                result.setTrackVariable((Local) argument);
            }
        } else if (stmt instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) stmt;
            Value leftOp = jAssignStmt.getLeftOp();
            Value rightOp = jAssignStmt.getRightOp();
            List<Local> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
            //To handle aliases, string concatenation, method return value source
            if (leftOp.equivTo(result.getTrackVariable()) ||
                    (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp))) {

                if (rightOp instanceof JCastExpr) {
                    JCastExpr jCastExpr = (JCastExpr) rightOp;
                    rightOp = jCastExpr.getOp();
                }
                if (rightOp instanceof JArrayRef) {
                    JArrayRef jArrayRef = (JArrayRef) rightOp;
                    rightOp = jArrayRef.getBase();
                }

                if (rightOp instanceof Local) {
                    result.setTrackVariable((Local) rightOp);
                } else if (rightOp instanceof JDynamicInvokeExpr) {
                    StringConcatenationProcessor stringConcatenationProcessor =
                            new StringConcatenationProcessor(view, leftOp, out, stmt, stringConcatenationSource);
                    stringConcatenationProcessor.process(rightOp);
                } else if ((rightOp instanceof AbstractInstanceInvokeExpr || rightOp instanceof JStaticInvokeExpr)
                        && stringConcatenationSource.isEmpty()) {
                    setResultArgumentSource(RETURN_FROM_METHOD, stmt, out);
                } else if (rightOp instanceof JFieldRef && stringConcatenationSource.isEmpty()) {
                    setResultArgumentSource(FIELD, stmt, out);
                } else if (rightOp instanceof StringConstant && stringConcatenationSource.isEmpty()) {
                    setResultArgumentSource(LOCAL, stmt, out);
                } else if (!stringConcatenationSource.isEmpty()) {
                    updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                }
            }
        } else if (stmt instanceof JIdentityStmt) {
            JIdentityStmt jIdentityStmt = (JIdentityStmt) stmt;
            Value leftOp = jIdentityStmt.getLeftOp();
            Value rightOp = jIdentityStmt.getRightOp();
            List<Local> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
            if (leftOp.equivTo(result.getTrackVariable()) ||
                    (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp))) {
                if (rightOp instanceof JParameterRef && stringConcatenationSource.isEmpty()) {
                    setResultArgumentSource(METHOD_PARAMETER, stmt, out);
                } else if (!stringConcatenationSource.isEmpty()) {
                    updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                }
            }
        } else if (stmt instanceof JInvokeStmt){
            JInvokeStmt jInvokeStmt = (JInvokeStmt) stmt;
            AbstractInvokeExpr abstractInvokeExpr = jInvokeStmt.getInvokeExpr().orElse(null);
            if(abstractInvokeExpr instanceof JSpecialInvokeExpr){
                JSpecialInvokeExpr jSpecialInvokeExpr = (JSpecialInvokeExpr) abstractInvokeExpr;
                Local baseVariable = jSpecialInvokeExpr.getBase();
                List<Local> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
                if (baseVariable.equivTo(result.getTrackVariable()) ||
                        (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(baseVariable))) {
                    MethodSignature specialInvokeMethodSignature = jSpecialInvokeExpr.getMethodSignature();
                    if(isNewStringObjectCreationSignature(specialInvokeMethodSignature, view)){
                        Value parameter = jSpecialInvokeExpr.getArg(0);
                        if (parameter instanceof Local){
                            result.setTrackVariable((Local) parameter);
                        } else if(parameter instanceof StringConstant && stringConcatenationSource.isEmpty()) {
                            setResultArgumentSource(LOCAL, stmt, out);
                        } else if (!stringConcatenationSource.isEmpty()) {
                            updateStringConcatenationSource(baseVariable, parameter, stmt, out);
                        }
                    }
                    /*if (rightOp instanceof JParameterRef && stringConcatenationSource.isEmpty()) {
                        setResultArgumentSource(METHOD_PARAMETER, stmt, out);
                    } else if (!stringConcatenationSource.isEmpty()) {
                        updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                    }*/
                }
            }
        }
    }

    private void updateStringConcatenationSource(Value leftOp, Value rightOp, Stmt stmt, Set<Result> out) {

        if (rightOp instanceof AbstractInstanceInvokeExpr || rightOp instanceof JStaticInvokeExpr) {
            setStringConcatenationArgumentSource(RETURN_FROM_METHOD, leftOp, stmt, out);
        } else if (rightOp instanceof JFieldRef) {
            setStringConcatenationArgumentSource(FIELD, leftOp, stmt, out);
        } else if (rightOp instanceof JParameterRef) {
            setStringConcatenationArgumentSource(METHOD_PARAMETER, leftOp, stmt, out);
        } else if (rightOp instanceof StringConstant) {
            setStringConcatenationArgumentSource(LOCAL, leftOp, stmt, out);
        }
    }

    private void setStringConcatenationArgumentSource(ArgumentSource argumentSource, Value leftOp, Stmt stmt, Set<Result> out) {
        stringConcatenationSource.getArgumentSources().add(argumentSource);
        if (!CollectionUtils.isEmpty(stringConcatenationSource.getNextVariablesToTrack())) {
            List<Local> nextVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
            //result.setTrackVariable(nextVariablesToTrack.get(0));
            stringConcatenationSource.getNextVariablesToTrack().remove(leftOp/*result.getTrackVariable()*/);
        } else {
            //
            LinePosition linePosition;// = (LinePosition) stmt.getPositionInfo().getStmtPosition();
            try {
                linePosition = (LinePosition) stmt.getPositionInfo().getStmtPosition();
            } catch (Exception e){
                linePosition = null;
                e.printStackTrace();
            }
            out.add(Result.builder()
                          .statementLineNumber(linePosition == null ? -1 : linePosition.getFirstLine())
                          .argumentSource(argumentSource)
                          .build());
        }
    }

    private void setResultArgumentSource(ArgumentSource argumentSource, Stmt stmt, Set<Result> out) {
        LinePosition linePosition;// = (LinePosition) stmt.getPositionInfo().getStmtPosition();
        try {
            linePosition = (LinePosition) stmt.getPositionInfo().getStmtPosition();
        } catch (Exception e){
            linePosition = null;
            e.printStackTrace();
        }
        result.setStatementLineNumber(linePosition == null ? -1 : linePosition.getFirstLine());
        result.setArgumentSource(argumentSource);
        out.add(Result.builder()
                      .statementLineNumber(linePosition == null ? -1 : linePosition.getFirstLine())
                      .argumentSource(argumentSource)
                      .build());
    }

    @Nonnull
    @Override
    protected Set<Result> newInitialFlow() {
        return new HashSet<>();
    }

    @Override
    protected void merge(@Nonnull Set<Result> in1, @Nonnull Set<Result> in2, @Nonnull Set<Result> out) {
        log.info("Merge method");
        if (isStartStatementReached) {
            isBranching = true;
        }
        /*if (in1.size() > 1 || in2.size() > 1) {
            throw new RuntimeException("Unexpected");
        } else*/
        if (in1.isEmpty() && !in2.isEmpty()) {
            out.addAll(in2);
        } else if (!in1.isEmpty() && in2.isEmpty()) {
            out.addAll(in1);
        } else if (!in1.isEmpty() && !in2.isEmpty()) {
            if (in1.equals(in2)) {
                out.addAll(in1);
            } else {
                out.addAll(in1);
                for (Result result : in2) {
                    if (!out.contains(result)) {
                        out.add(result);
                    }
                }
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

    public boolean isBranching() {
        return isBranching;
    }
}
