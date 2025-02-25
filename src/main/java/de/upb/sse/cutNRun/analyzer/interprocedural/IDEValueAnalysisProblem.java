package de.upb.sse.cutNRun.analyzer.interprocedural;

import de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource;
import de.upb.sse.cutNRun.analyzer.intraprocedural.Result;
import de.upb.sse.cutNRun.analyzer.intraprocedural.StringConcatenationProcessor;
import de.upb.sse.cutNRun.analyzer.intraprocedural.StringConcatenationSource;
import heros.*;
import heros.edgefunc.AllBottom;
import heros.edgefunc.AllTop;
import heros.edgefunc.EdgeIdentity;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import sootup.analysis.interprocedural.ide.DefaultJimpleIDETabulationProblem;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
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
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.NullType;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

import java.util.*;

import static de.upb.sse.cutNRun.analyzer.helper.AnalysisHelper.*;
import static de.upb.sse.cutNRun.analyzer.helper.AnalysisHelper.isModernFieldReflection;
import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.*;
import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.LOCAL;

@Slf4j
public class IDEValueAnalysisProblem extends DefaultJimpleIDETabulationProblem<Local, String, InterproceduralCFG<Stmt, SootMethod>> {
    private boolean isTraditionalReflection;
    private boolean isTraditionalNewInstanceReflection;
    private boolean isTraditionalMethodReflection;
    private boolean isTraditionalFieldReflection;
    private boolean isModernNewInstanceReflection;
    private boolean isModernMethodReflection;
    private boolean isModernFieldReflection;
    @Getter
    private StringConcatenationSource stringConcatenationSource;
    protected InterproceduralCFG<Stmt, SootMethod> icfg;
    private final List<MethodSignature> entryPoints;
    private final Stmt startStmt;
    protected final JavaView view;
    protected final static String TOP = "";
    protected final static String BOTTOM = "<<TOP>>";
    private final static EdgeFunction<String> ALL_BOTTOM = new AllBottom<>(BOTTOM);
    private final Local hardCoddedResult;

    public IDEValueAnalysisProblem(InterproceduralCFG<Stmt, SootMethod> icfg, List<MethodSignature> entryPoints, Stmt startStmt, JavaView view) {
        super(icfg);
        this.icfg = icfg;
        this.entryPoints = entryPoints;
        this.startStmt = startStmt;
        this.view = view;
        this.hardCoddedResult = JavaJimple.newLocal("hardCoddedResult",
                                                    view.getIdentifierFactory().getClassType("java.lang.String"));
        this.isTraditionalReflection = false;
        this.isTraditionalNewInstanceReflection = false;
        this.isTraditionalMethodReflection = false;
        this.isTraditionalFieldReflection = false;
        this.isModernNewInstanceReflection = false;
        this.isModernMethodReflection = false;
        this.isModernFieldReflection = false;
        this.stringConcatenationSource = StringConcatenationSource.builder().build();
    }
    @Override
    public int numThreads() {
        return 1;
    }

    //TODO: Everything is copied, change logic

    protected static class EdgeFunctionComposer implements EdgeFunction<String> {

        private final EdgeFunction<String> F;
        private final EdgeFunction<String> G;

        public EdgeFunctionComposer(EdgeFunction<String> F, EdgeFunction<String> G) {
            this.F = F;
            this.G = G;
        }

        @Override
        public String computeTarget(String source) {
            return F.computeTarget(G.computeTarget(source));
        }

        @Override
        public EdgeFunction<String> composeWith(EdgeFunction<String> secondFunction) {
            return G.composeWith(F.composeWith(secondFunction));
        }

        @Override
        public EdgeFunction<String> meetWith(EdgeFunction<String> otherFunction) {
            // FIXME: needs improvement, but is good enough to analyze the current target programs
            if (this == ALL_BOTTOM && otherFunction != ALL_BOTTOM) {
                return otherFunction;
            } else if (this != ALL_BOTTOM && otherFunction == ALL_BOTTOM) {
                return this;
            } else {
                return this;
            }
        }

        @Override
        public boolean equalTo(EdgeFunction<String> other) {
            return F.equalTo(other);
        }

    }

    @Override
    protected EdgeFunction<String> createAllTopFunction() {
        return new AllTop<>(TOP);
    }

    @Override
    protected MeetLattice<String> createMeetLattice() {
        return new MeetLattice<String>() {
            @Override
            public String topElement() {
                return TOP;
            }

            @Override
            public String bottomElement() {
                return BOTTOM;
            }

            @Override
            public String meet(String left, String right) {
                if (left == TOP && right != BOTTOM) {
                    return right;
                } else if (right == TOP && left != BOTTOM) {
                    return left;
                } else {
                    return BOTTOM;
                }
            }
        };
    }

    @Override
    protected EdgeFunctions<Stmt, Local, SootMethod, String> createEdgeFunctionsFactory() {
        return new EdgeFunctions<Stmt, Local, SootMethod, String>() {
            @Override
            public EdgeFunction<String> getNormalEdgeFunction(Stmt src, Local srcNode, Stmt tgt, Local tgtNode) {
                log.info("EDGE getNormalEdgeFunction src: " + src.toString());
                return EdgeIdentity.v();
            }

            @Override
            public EdgeFunction<String> getCallEdgeFunction(Stmt callStmt, Local srcNode, SootMethod destinationMethod, Local destNode) {
                log.info("EDGE getCallEdgeFunction callStmt: " + callStmt.toString());
                return EdgeIdentity.v();
            }

            @Override
            public EdgeFunction<String> getReturnEdgeFunction(Stmt callSite, SootMethod calleeMethod, Stmt exitStmt, Local exitNode, Stmt returnSite, Local retNode) {
                log.info("EDGE getReturnEdgeFunction callSite: " + callSite.toString());
                return EdgeIdentity.v();
            }

            @Override
            public EdgeFunction<String> getCallToReturnEdgeFunction(Stmt callStmt, Local callNode, Stmt returnSite, Local returnSideNode) {
                log.info("EDGE getCallToReturnEdgeFunction callStmt: " + callStmt.toString());
                if(hardCoddedResult.equivTo(returnSideNode)) {
                    JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(callStmt);
                    Immediate argument = jVirtualInvokeExpr.getArg(0);
                    if (argument instanceof StringConstant) {
                        StringConstant hardcodedValue = (StringConstant) argument;
                        return new EdgeFunction<String>() {
                            @Override
                            public String computeTarget(String source) {
                                return hardcodedValue.getValue();
                            }

                            @Override
                            public EdgeFunction<String> composeWith(EdgeFunction<String> secondFunction) {
                                return new EdgeFunctionComposer(secondFunction, this);
                            }

                            @Override
                            public EdgeFunction<String> meetWith(EdgeFunction<String> otherFunction) {
                                if (this == ALL_BOTTOM && otherFunction != ALL_BOTTOM) {
                                    return otherFunction;
                                } else if (this != ALL_BOTTOM && otherFunction == ALL_BOTTOM) {
                                    return this;
                                } else {
                                    return this;
                                }
                            }

                            @Override
                            public boolean equalTo(EdgeFunction<String> other) {
                                return this == other;
                            }
                        };
                    }
                }
                return EdgeIdentity.v();
            }
        };
    }

    @Override
    protected FlowFunctions<Stmt, Local, SootMethod> createFlowFunctionsFactory() {
        return new FlowFunctions<Stmt, Local, SootMethod>() {
            @Override
            public FlowFunction<Local> getNormalFlowFunction(Stmt curr, Stmt succ) {
                log.info("getNormalFlowFunction: "+curr.toString());
                if (isTraditionalReflection) {
                    if (isTraditionalMethodReflection || isTraditionalFieldReflection) {
                        return analyzeMethodAndFieldReflection(null /*out*/, curr, false);
                    } else if (isTraditionalNewInstanceReflection) {
                        //TODO: write logic
                        //return analyzeNewInstanceReflection(out, stmt, false);
                    }
                } else {
                    if (isModernMethodReflection || isModernFieldReflection) {
                        return analyzeMethodAndFieldReflection(null /*out*/, curr, true);
                    } else if (isModernNewInstanceReflection) {
                        //TODO: write logic
                        //return analyzeNewInstanceReflection(out, stmt, true);
                    }
                }
                return Identity.v();
            }

            @Override
            public FlowFunction<Local> getCallFlowFunction(Stmt callStmt, SootMethod dest) {
                log.info("getCallFlowFunction: "+callStmt.toString());
                return  Identity.v();
            }

            @Override
            public FlowFunction<Local> getReturnFlowFunction(Stmt callSite, SootMethod calleeMethod, Stmt exitStmt, Stmt returnSite) {
                log.info("getReturnFlowFunction: " + returnSite);
                return  Identity.v();
            }

            @Override
            public FlowFunction<Local> getCallToReturnFlowFunction(Stmt callSite, Stmt returnSite) {
                log.info("getCallToReturnFlowFunction: "+callSite.toString());
                if(callSite.equivTo(startStmt)){
                    /*JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(callSite);
                    Immediate argument = jVirtualInvokeExpr.getArg(0);
                    if (argument instanceof StringConstant) {
                        return new Gen(hardCoddedResult, zeroValue());
                    }*/
                    //Determine the type of reflection
                    JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(callSite);
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

                    if (isTraditionalReflection) {
                        if (isTraditionalMethodReflection || isTraditionalFieldReflection) {
                            return analyzeMethodAndFieldReflection(null /*out*/, callSite, false);
                        } else if (isTraditionalNewInstanceReflection) {
                            //TODO: write logic
                            //return analyzeNewInstanceReflection(out, stmt, false);
                        }
                    } else {
                        if (isModernMethodReflection || isModernFieldReflection) {
                            return analyzeMethodAndFieldReflection(null /*out*/, callSite, true);
                        } else if (isModernNewInstanceReflection) {
                            //TODO: write logic
                            //return analyzeNewInstanceReflection(out, stmt, true);
                        }
                    }
                }
                return Identity.v();
            }
        };
    }

    private FlowFunction<Local> analyzeMethodAndFieldReflection(Set<Result> out, Stmt stmt, boolean isModernReflection) {
        if (stmt.equivTo(startStmt)) {
            JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(stmt);
            Immediate argument;
            if (isModernReflection) {
                argument = jVirtualInvokeExpr.getArg(1);
            } else {
                argument = jVirtualInvokeExpr.getArg(0);
            }
            if (argument instanceof StringConstant) {
                return new Gen(hardCoddedResult, zeroValue()); //setResultArgumentSource(LOCAL, stmt, out);
            } else if (argument instanceof Local) {
                return new Gen(argument, zeroValue()); //result.setTrackVariable((Local) argument);
            }
        } else if (stmt instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) stmt;
            Value leftOp = jAssignStmt.getLeftOp();
            Value rightOpTemp = jAssignStmt.getRightOp();
            if (rightOpTemp instanceof JCastExpr) {
                JCastExpr jCastExpr = (JCastExpr) rightOpTemp;
                rightOpTemp = jCastExpr.getOp();
            }
            if (rightOpTemp instanceof JArrayRef) {
                JArrayRef jArrayRef = (JArrayRef) rightOpTemp;
                rightOpTemp = jArrayRef.getBase();
            }
            final Value rightOp = rightOpTemp;
            List<Local> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
            //To handle aliases, string concatenation, method return value source
            return new FlowFunction<Local>() {
                @Override
                public Set<Local> computeTargets(Local source) {
                    Set<Local> res = new HashSet<>();
                    res.add(source);
                    if (source != zeroValue() && (leftOp.equivTo(source) ||
                            (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp)))) {

                        if (rightOp instanceof Local) {
                            res.add((Local) rightOp); //result.setTrackVariable((Local) rightOp);
                        } else if (rightOp instanceof JDynamicInvokeExpr) {
                            StringConcatenationProcessor stringConcatenationProcessor =
                                    new StringConcatenationProcessor(view, leftOp, out, stmt, stringConcatenationSource);
                            stringConcatenationProcessor.process(rightOp);
                        } else if ((rightOp instanceof AbstractInstanceInvokeExpr || rightOp instanceof JStaticInvokeExpr)
                                && stringConcatenationSource.isEmpty()) {
                            //TODO: write logic
                            //setResultArgumentSource(RETURN_FROM_METHOD, stmt, out);
                        } else if (rightOp instanceof JFieldRef && stringConcatenationSource.isEmpty()) {
                            //TODO: write logic
                            //setResultArgumentSource(FIELD, stmt, out);
                        } else if (rightOp instanceof StringConstant && stringConcatenationSource.isEmpty()) {
                            res.add((Local) rightOp); //setResultArgumentSource(LOCAL, stmt, out);
                        } else if (!stringConcatenationSource.isEmpty()) {
                            updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                        }
                    }
                    return res;
                }
            };

        } else if (stmt instanceof JIdentityStmt) {
            JIdentityStmt jIdentityStmt = (JIdentityStmt) stmt;
            Value leftOp = jIdentityStmt.getLeftOp();
            Value rightOp = jIdentityStmt.getRightOp();
            List<Local> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
            return new FlowFunction<Local>() {
                @Override
                public Set<Local> computeTargets(Local source) {
                    Set<Local> res = new HashSet<>();
                    res.add(source);
                    if (source != zeroValue() && (leftOp.equivTo(source) ||
                            (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp)))) {
                        if (rightOp instanceof JParameterRef && stringConcatenationSource.isEmpty()) {
                            //TODO: write logic
                            //setResultArgumentSource(METHOD_PARAMETER, stmt, out);
                        } else if (!stringConcatenationSource.isEmpty()) {
                            updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                        }
                    }
                    return res;
                }
            };
        } else if (stmt instanceof JInvokeStmt){
            JInvokeStmt jInvokeStmt = (JInvokeStmt) stmt;
            AbstractInvokeExpr abstractInvokeExpr = jInvokeStmt.getInvokeExpr().orElse(null);
            if(abstractInvokeExpr instanceof JSpecialInvokeExpr){
                JSpecialInvokeExpr jSpecialInvokeExpr = (JSpecialInvokeExpr) abstractInvokeExpr;
                Local baseVariable = jSpecialInvokeExpr.getBase();
                List<Local> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
                return new FlowFunction<Local>() {
                    @Override
                    public Set<Local> computeTargets(Local source) {
                        Set<Local> res = new HashSet<>();
                        res.add(source);
                        if (source != zeroValue() && (baseVariable.equivTo(source) ||
                                (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(baseVariable)))) {
                            MethodSignature specialInvokeMethodSignature = jSpecialInvokeExpr.getMethodSignature();
                            if(isNewStringObjectCreationSignature(specialInvokeMethodSignature, view)){
                                Value parameter = jSpecialInvokeExpr.getArg(0);
                                if (parameter instanceof Local){
                                    res.add((Local) parameter); //result.setTrackVariable((Local) parameter);
                                } else if(parameter instanceof StringConstant && stringConcatenationSource.isEmpty()) {
                                    res.add((Local) parameter); //setResultArgumentSource(LOCAL, stmt, out);
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
                        return res;
                    }
                };
            }
        }
        return Identity.v();
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

    @Override
    protected Local createZeroValue() {
        return new Local("<<zero>>", NullType.getInstance());
    }

    @Override
    public Map<Stmt, Set<Local>> initialSeeds() {
        /*for (MethodSignature methodSignature : entryPoints) {
            SootMethod m = view.getMethod(methodSignature).get();
            if (!m.hasBody()) {
                continue;
            }
            return DefaultSeeds.make(Collections.singleton(icfg.getStartPointsOf(m).stream().findFirst().get()), zeroValue());
        }
        throw new IllegalStateException("View does not contain entryPoint " + entryPoints);*/
        return DefaultSeeds.make(Collections.singleton(startStmt), zeroValue());
    }

    @Override
    public boolean followReturnsPastSeeds() {
        return true;
    }
}
