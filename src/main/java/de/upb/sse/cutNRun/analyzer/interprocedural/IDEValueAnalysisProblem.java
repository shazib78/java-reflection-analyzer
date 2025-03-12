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
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.LinePosition;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.NullType;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

import java.util.*;

import static de.upb.sse.cutNRun.analyzer.helper.AnalysisHelper.*;
import static de.upb.sse.cutNRun.analyzer.helper.AnalysisHelper.isModernFieldReflection;
import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.*;
import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.LOCAL;

@Slf4j
public class IDEValueAnalysisProblem extends DefaultJimpleIDETabulationProblem<Value, String, InterproceduralCFG<Stmt, SootMethod>> {
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
    private SootMethod methodConsistingResult;

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
        this.methodConsistingResult = null;
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
                }/* else if ((left != TOP && left != BOTTOM) && (right != TOP && right != BOTTOM)) {
                    return left + ", " + right;
                }*/ else {
                    return BOTTOM;
                }
            }
        };
    }

    @Override
    protected EdgeFunctions<Stmt, Value, SootMethod, String> createEdgeFunctionsFactory() {
        return new EdgeFunctions<Stmt, Value, SootMethod, String>() {
            @Override
            public EdgeFunction<String> getNormalEdgeFunction(Stmt src, Value srcNode, Stmt tgt, Value tgtNode) {
                log.info("EDGE getNormalEdgeFunction src: " + src.toString());
                if (isTraditionalReflection) {
                    if (isTraditionalMethodReflection || isTraditionalFieldReflection) {
                        return createEdgeFunctionsForMethodAndFieldReflection(src, srcNode, tgtNode);
                    } else if (isTraditionalNewInstanceReflection) {
                        return createEdgeFunctionsForNewInstanceReflection(src, srcNode, tgtNode);
                    }
                } else {
                    if (isModernMethodReflection || isModernFieldReflection) {
                        return createEdgeFunctionsForMethodAndFieldReflection(src, srcNode, tgtNode);
                    } else if (isModernNewInstanceReflection) {
                        return createEdgeFunctionsForNewInstanceReflection(src, srcNode, tgtNode);
                    }
                }
                return EdgeIdentity.v();
            }

            @Override
            public EdgeFunction<String> getCallEdgeFunction(Stmt callStmt, Value srcNode, SootMethod destinationMethod, Value destNode) {
                log.info("EDGE getCallEdgeFunction callStmt: " + callStmt.toString());
                if (callStmt != null) {
                    if (callStmt instanceof AbstractDefinitionStmt) {
                        AbstractDefinitionStmt defnStmt = (AbstractDefinitionStmt) callStmt;
                        Value leftOp = defnStmt.getLeftOp();
                        if (leftOp.equivTo(srcNode)) {
                            Stmt destMethodFirstStmt = icfg.getStartPointsOf(destinationMethod).stream().findFirst().get();//dest.getBody().getStmtGraph().

                            if (destMethodFirstStmt instanceof JReturnStmt) {
                                JReturnStmt returnStmt = (JReturnStmt) destMethodFirstStmt;
                                Value returnStmtOpValue = returnStmt.getOp();
                            /*return new FlowFunction<Local>() {
                                @Override
                                public Set<Local> computeTargets(Local source) {
                                    if (source == leftOp) {
                                        if(returnStmtOpValue instanceof StringConstant) {
                                            return Collections.singleton(hardCoddedResult);
                                        }
                                    }
                                }
                            };*/
                                if (hardCoddedResult.equivTo(destNode)) { //this condition would by default mean returnStmtOpValue is StringConstant
                                    if (returnStmtOpValue instanceof StringConstant) {
                                        methodConsistingResult = destinationMethod;
                                        StringConstant hardcodedValue = (StringConstant) returnStmtOpValue;
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
                                    } else if (returnStmtOpValue instanceof ClassConstant) {
                                        methodConsistingResult = destinationMethod;
                                        ClassConstant hardcodedValue = (ClassConstant) returnStmtOpValue;
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
                            }
                        }
                    }
                }
                return EdgeIdentity.v();
            }

            @Override
            public EdgeFunction<String> getReturnEdgeFunction(Stmt callSite, SootMethod calleeMethod, Stmt exitStmt,
                                                              Value exitNode, Stmt returnSite, Value retNode) {
                log.info("EDGE getReturnEdgeFunction callSite: " + callSite.toString());
                if (callSite != null) {
                    AbstractInvokeExpr ie = getAbstractInvokeExpr(callSite);//s.getInvokeExpr();
                    final List<Immediate> callArgs = ie.getArgs();
                    final List<Immediate> paramLocals = new ArrayList<>(callArgs.size());
                    for (int i = 0; i < calleeMethod.getParameterCount(); i++) {
                        paramLocals.add(calleeMethod.getBody().getParameterLocal(i));
                    }
                    //ignore implicit calls to static initializers
                    if (/*dest.getName().equals("<clinit>") &&*/ callArgs.size() == 0) {
                        //TODO: write logic
                        //return Collections.emptySet();
                    }
                    Set<Local> res = new HashSet<>();
                    for (int i = 0; i < paramLocals.size(); i++) {
                        // Special case: check if function is called with integer literals as params
                                /*if (paramLocals.get(i) instanceof StringConstant && source == zeroValue()) {
                                    res.add((Local) callArgs.get(i));
                                }*/
                        // Ordinary case: just perform the mapping
                        if (paramLocals.get(i) == exitNode) {
                            if (!(callArgs.get(i) instanceof StringConstant || callArgs.get(i) instanceof ClassConstant)) {
                                return EdgeIdentity.v();
                            } else {
                                // Special case: check if function is called with integer literals as params
                                if (hardCoddedResult.equivTo(retNode)) {
                                    if (callArgs.get(i) instanceof StringConstant) {
                                        methodConsistingResult = icfg.getMethodOf(callSite);
                                        StringConstant hardcodedValue = (StringConstant) callArgs.get(i);
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
                                    } else if (callArgs.get(i) instanceof ClassConstant) {
                                        methodConsistingResult = icfg.getMethodOf(callSite);
                                        ClassConstant hardcodedValue = (ClassConstant) callArgs.get(i);
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
                            }
                        }
                    }
                    //return res;
                }
                return EdgeIdentity.v();
            }

            @Override
            public EdgeFunction<String> getCallToReturnEdgeFunction(Stmt callStmt, Value callNode, Stmt returnSite, Value returnSideNode) {
                log.info("EDGE getCallToReturnEdgeFunction callStmt: " + callStmt.toString());
                /*if (hardCoddedResult.equivTo(returnSideNode) && callStmt.equivTo(startStmt)) {
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
                    }*/

                Immediate argument = null;
                if (callStmt.equivTo(startStmt)) {
                    JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(callStmt);
                    if (isTraditionalReflection) {
                        if (isTraditionalMethodReflection || isTraditionalFieldReflection) {
                            //return analyzeMethodAndFieldReflection(null /*out*/, callSite, false);
                            /*if (false*//*isModernReflection*//*) {
                                argument = jVirtualInvokeExpr.getArg(1);
                            } else {*/
                            argument = jVirtualInvokeExpr.getArg(0);
                            /*}
                            if (argument instanceof StringConstant) {
                                return new Gen(hardCoddedResult, zeroValue()); //setResultArgumentSource(LOCAL, stmt, out);
                            } else if (argument instanceof Local) {
                                return new Gen(argument, zeroValue()); //result.setTrackVariable((Local) argument);
                            }*/
                        } else if (isTraditionalNewInstanceReflection) {
                            //TODO: write logic - may not be required
                            //return analyzeNewInstanceReflection(out, stmt, false);
                            /*if (isModernReflection) {
                                immediate = jVirtualInvokeExpr.getArg(0);
                            } else {*/
                                argument = jVirtualInvokeExpr.getBase();
                            //}
                        }
                    } else {
                        if (isModernMethodReflection || isModernFieldReflection) {
                            argument = jVirtualInvokeExpr.getArg(1); //return analyzeMethodAndFieldReflection(null /*out*/, callSite, true);
                        } else if (isModernNewInstanceReflection) {
                            //TODO: write logic  - may not be required
                            //return analyzeNewInstanceReflection(out, stmt, true);
                            /*if (isModernReflection) {*/
                                argument = jVirtualInvokeExpr.getArg(0);
                            /*} else {
                                immediate = jVirtualInvokeExpr.getBase();
                            }*/
                        }
                    }


                    if (argument instanceof StringConstant) {
                        methodConsistingResult = icfg.getMethodOf(callStmt);
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
                    } else if (argument instanceof ClassConstant) {
                        methodConsistingResult = icfg.getMethodOf(callStmt);
                        ClassConstant hardcodedValue = (ClassConstant) argument;
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
                } else {
                    if (isTraditionalReflection) {
                        if (isTraditionalMethodReflection || isTraditionalFieldReflection) {
                            return createEdgeFunctionsForMethodAndFieldReflection(callStmt, callNode, returnSideNode);
                        } else if (isTraditionalNewInstanceReflection) {
                            return createEdgeFunctionsForNewInstanceReflection(callStmt, callNode, returnSideNode);
                        }
                    } else {
                        if (isModernMethodReflection || isModernFieldReflection) {
                            return createEdgeFunctionsForMethodAndFieldReflection(callStmt, callNode, returnSideNode);
                        } else if (isModernNewInstanceReflection) {
                            return createEdgeFunctionsForNewInstanceReflection(callStmt, callNode, returnSideNode);
                        }
                    }
                }

                return EdgeIdentity.v();
            }
        };
    }

    private EdgeFunction<String> createEdgeFunctionsForMethodAndFieldReflection(Stmt stmt, Value srcNode, Value tgtNode) {
        if (hardCoddedResult.equivTo(tgtNode)) {
            if (stmt instanceof JAssignStmt) {
                JAssignStmt jAssignStmt = (JAssignStmt) stmt;
                Value leftOpTemp = jAssignStmt.getLeftOp();
                Value rightOpTemp = jAssignStmt.getRightOp();
                if (rightOpTemp instanceof JCastExpr) {
                    JCastExpr jCastExpr = (JCastExpr) rightOpTemp;
                    rightOpTemp = jCastExpr.getOp();
                }
                if (rightOpTemp instanceof JArrayRef) {
                    JArrayRef jArrayRef = (JArrayRef) rightOpTemp;
                    rightOpTemp = jArrayRef.getBase();
                }
                if(rightOpTemp instanceof JInstanceFieldRef) {
                    JInstanceFieldRef jInstanceFieldRef = (JInstanceFieldRef) rightOpTemp;
                    rightOpTemp = new JInstanceFieldRef(new Local("<<dummyBase>>", NullType.getInstance())
                            , jInstanceFieldRef.getFieldSignature());
                }
                if(leftOpTemp instanceof JInstanceFieldRef) {
                    JInstanceFieldRef jInstanceFieldRef = (JInstanceFieldRef) leftOpTemp;
                    leftOpTemp = new JInstanceFieldRef(new Local("<<dummyBase>>", NullType.getInstance())
                            , jInstanceFieldRef.getFieldSignature());
                }
                final Value leftOp = leftOpTemp;
                final Value rightOp = rightOpTemp;
                List<Local> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
                //To handle aliases, string concatenation, method return value source
                if (leftOp.equivTo(srcNode)) {
                    if (srcNode != zeroValue() && (leftOp.equivTo(srcNode) ||
                            (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp)))) {

                        /*if (rightOp instanceof Local) {
                            //res.add((Local) rightOp); //result.setTrackVariable((Local) rightOp);
                        } else if (rightOp instanceof JDynamicInvokeExpr) {
                           *//* StringConcatenationProcessor stringConcatenationProcessor =
                                    new StringConcatenationProcessor(view, leftOp, out, stmt, stringConcatenationSource);
                            stringConcatenationProcessor.process(rightOp);*//*
                        } else if ((rightOp instanceof AbstractInstanceInvokeExpr || rightOp instanceof JStaticInvokeExpr)
                                && stringConcatenationSource.isEmpty()) {
                            //TODO: write logic
                            //setResultArgumentSource(RETURN_FROM_METHOD, stmt, out);
                        } else if (rightOp instanceof JFieldRef && stringConcatenationSource.isEmpty()) {
                            //JFieldRef jFieldRef = (JFieldRef) rightOp;
                            //res.add(rightOp); //setResultArgumentSource(FIELD, stmt, out);
                        } else */if (rightOp instanceof StringConstant && stringConcatenationSource.isEmpty()) {
                            //res.add((Local) rightOp); //setResultArgumentSource(LOCAL, stmt, out);
                            methodConsistingResult = icfg.getMethodOf(stmt);
                            StringConstant hardcodedValue = (StringConstant) rightOp;
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
                        }/* else if (!stringConcatenationSource.isEmpty()) {
                            //updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                        }*/
                    }
                }
                //return res;
                /*}
            };*/

            } else if (stmt instanceof JIdentityStmt) {
                /*JIdentityStmt jIdentityStmt = (JIdentityStmt) stmt;
                Value leftOp = jIdentityStmt.getLeftOp();
                Value rightOp = jIdentityStmt.getRightOp();
                List<Local> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
                return new FlowFunction<Value>() {
                    @Override
                    public Set<Value> computeTargets(Value source) {
                        Set<Value> res = new HashSet<>();
                        res.add(source);
                        if (source != zeroValue() && (leftOp.equivTo(source) ||
                                (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp)))) {
                            if (rightOp instanceof JParameterRef && stringConcatenationSource.isEmpty()) {
                                //res.add((Local) rightOp);//setResultArgumentSource(METHOD_PARAMETER, stmt, out);
                            } else if (!stringConcatenationSource.isEmpty()) {
                                updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                            }
                        }
                        return res;
                    }
                };*/
            } else if (stmt instanceof JInvokeStmt) {
                JInvokeStmt jInvokeStmt = (JInvokeStmt) stmt;
                AbstractInvokeExpr abstractInvokeExpr = jInvokeStmt.getInvokeExpr().orElse(null);
                if (abstractInvokeExpr instanceof JSpecialInvokeExpr) {
                    JSpecialInvokeExpr jSpecialInvokeExpr = (JSpecialInvokeExpr) abstractInvokeExpr;
                    Local baseVariable = jSpecialInvokeExpr.getBase();
                    List<Local> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
                    if (baseVariable == srcNode) {
                    /*return new FlowFunction<Value>() {
                        @Override
                        public Set<Value> computeTargets(Value source) {
                            Set<Value> res = new HashSet<>();
                            res.add(source);*/
                        if (srcNode != zeroValue() && (baseVariable.equivTo(srcNode) ||
                                (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(baseVariable)))) {
                            MethodSignature specialInvokeMethodSignature = jSpecialInvokeExpr.getMethodSignature();
                            if (isNewStringObjectCreationSignature(specialInvokeMethodSignature, view)) {
                                Value parameter = jSpecialInvokeExpr.getArg(0);
                                if (parameter instanceof Local) {
                                    //res.add((Local) parameter); //result.setTrackVariable((Local) parameter);
                                } else if (parameter instanceof StringConstant && stringConcatenationSource.isEmpty()) {
                                    //res.add((Local) parameter); //setResultArgumentSource(LOCAL, stmt, out);
                                    StringConstant hardcodedValue = (StringConstant) parameter;
                                    methodConsistingResult = icfg.getMethodOf(stmt);
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
                                } else if (!stringConcatenationSource.isEmpty()) {
                                    //updateStringConcatenationSource(baseVariable, parameter, stmt, out);
                                }
                            }
                    /*if (rightOp instanceof JParameterRef && stringConcatenationSource.isEmpty()) {
                        setResultArgumentSource(METHOD_PARAMETER, stmt, out);
                    } else if (!stringConcatenationSource.isEmpty()) {
                        updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                    }*/
                        }
                        //return res;
                       /* }
                    };*/
                    }
                }
            }
        }
        return EdgeIdentity.v();
    }

    private EdgeFunction<String> createEdgeFunctionsForNewInstanceReflection(Stmt stmt, Value srcNode, Value tgtNode) {
        if (hardCoddedResult.equivTo(tgtNode)) {
            if (stmt instanceof JAssignStmt) {
                JAssignStmt jAssignStmt = (JAssignStmt) stmt;
                Value leftOpTemp = jAssignStmt.getLeftOp();
                Value rightOpTemp = jAssignStmt.getRightOp();
                if (rightOpTemp instanceof JCastExpr) {
                    JCastExpr jCastExpr = (JCastExpr) rightOpTemp;
                    rightOpTemp = jCastExpr.getOp();
                }
                if(rightOpTemp instanceof JInstanceFieldRef) {
                    JInstanceFieldRef jInstanceFieldRef = (JInstanceFieldRef) rightOpTemp;
                    rightOpTemp = new JInstanceFieldRef(new Local("<<dummyBase>>", NullType.getInstance())
                            , jInstanceFieldRef.getFieldSignature());
                }
                if(leftOpTemp instanceof JInstanceFieldRef) {
                    JInstanceFieldRef jInstanceFieldRef = (JInstanceFieldRef) leftOpTemp;
                    leftOpTemp = new JInstanceFieldRef(new Local("<<dummyBase>>", NullType.getInstance())
                            , jInstanceFieldRef.getFieldSignature());
                }
                final Value leftOp = leftOpTemp;
                final Value rightOp = rightOpTemp;
                //List<JavaLocal> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
                //To handle aliases, string concatenation, method return value source
                        if (srcNode != zeroValue() && leftOp.equivTo(srcNode) /*||
                    (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp))*/) {


                           /* if (rightOp instanceof Local) {
                                res.add((Local) rightOp);//result.setTrackVariable((Local) rightOp);
                            } *//*else if (rightOp instanceof JDynamicInvokeExpr) {
                    StringConcatenationProcessor stringConcatenationProcessor =
                            new StringConcatenationProcessor(view, leftOp, out, stmt, stringConcatenationSource);
                    stringConcatenationProcessor.process(rightOp);
                } else*/ if (/*(*/rightOp instanceof AbstractInstanceInvokeExpr || rightOp instanceof JStaticInvokeExpr/*)
                        && stringConcatenationSource.isEmpty()*/) {
                                AbstractInvokeExpr abstractInvokeExpr = (AbstractInvokeExpr) rightOp;
                                //MethodSignature getConstructorMethodSignature = buildGetConstructorMethodSignature();
                                MethodSignature classForNameMethodSignature = buildClassForNameMethodSignature();
                                /*if (getConstructorMethodSignature.equals(abstractInvokeExpr.getMethodSignature())) {
                                    Local local = getJVirtualInvokeExpr(stmt).getBase();
                                    if (local instanceof Local) {
                                        res.add((Local) local);//result.setTrackVariable((Local) local);
                                    }
                                } else*/ if (classForNameMethodSignature.equals(abstractInvokeExpr.getMethodSignature())) {
                                    Immediate local = ((JStaticInvokeExpr) getAbstractInvokeExpr(stmt)).getArg(0);
                                    /*if (local instanceof Local) {
                                        res.add((Local) local);
                                    } else */if (local instanceof StringConstant) {
                                        methodConsistingResult = icfg.getMethodOf(stmt);
                                        StringConstant hardcodedValue = (StringConstant) local;
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
                                } else {
                                    //TODO: write logic
                                    //setResultArgumentSource(RETURN_FROM_METHOD, stmt, out);
                                }
                            }/* else if (rightOp instanceof JFieldRef && stringConcatenationSource.isEmpty()) {
                                res.add(rightOp);//setResultArgumentSource(FIELD, stmt, out);
                            }*/ else if (rightOp instanceof ClassConstant /*&& stringConcatenationSource.isEmpty()*/) {
                                //res.add(hardCoddedResult);//setResultArgumentSource(LOCAL, stmt, out);
                                methodConsistingResult = icfg.getMethodOf(stmt);
                                ClassConstant hardcodedValue = (ClassConstant) rightOp;
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
                            }/* else if (!stringConcatenationSource.isEmpty()) {
                    updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                }*/
                        }
                        //return res;
                    /*}
                };*/
            } else if (stmt instanceof JIdentityStmt) {
                JIdentityStmt jIdentityStmt = (JIdentityStmt) stmt;
                Value leftOp = jIdentityStmt.getLeftOp();
                Value rightOp = jIdentityStmt.getRightOp();
                /*List<JavaLocal> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();*/
                /*if (leftOp.equivTo(result.getTrackVariable()) *//*||
                    (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp))*//*) {
                if (rightOp instanceof JParameterRef *//*&& stringConcatenationSource.isEmpty()*//*) {
                    setResultArgumentSource(METHOD_PARAMETER, stmt, out);
                }*//* else if (!stringConcatenationSource.isEmpty()) {
                    updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                }*//*
            }*/
            }
        }
        return EdgeIdentity.v();
    }

    @Override
    protected FlowFunctions<Stmt, Value, SootMethod> createFlowFunctionsFactory() {
        return new FlowFunctions<Stmt, Value, SootMethod>() {
            @Override
            public FlowFunction<Value> getNormalFlowFunction(Stmt curr, Stmt succ) {
                log.info("getNormalFlowFunction: " + curr.toString());
                if (isTraditionalReflection) {
                    if (isTraditionalMethodReflection || isTraditionalFieldReflection) {
                        return analyzeMethodAndFieldReflection(null /*out*/, curr, false);
                    } else if (isTraditionalNewInstanceReflection) {
                        return analyzeNewInstanceReflection(null /*out*/, curr, false);
                    }
                } else {
                    if (isModernMethodReflection || isModernFieldReflection) {
                        return analyzeMethodAndFieldReflection(null /*out*/, curr, true);
                    } else if (isModernNewInstanceReflection) {
                        return analyzeNewInstanceReflection(null /*out*/, curr, true);
                    }
                }
                return Identity.v();
            }

            @Override
            public FlowFunction<Value> getCallFlowFunction(Stmt callStmt, SootMethod dest) {
                log.info("getCallFlowFunction: " + callStmt.toString());
                if (callStmt != null) {
                    if (callStmt instanceof AbstractDefinitionStmt) {
                        AbstractDefinitionStmt defnStmt = (AbstractDefinitionStmt) callStmt;
                        Value leftOp = defnStmt.getLeftOp();
                        Stmt destMethodFirstStmt = icfg.getStartPointsOf(dest).stream().findFirst().get();//dest.getBody().getStmtGraph().
                        AbstractInvokeExpr ie = getAbstractInvokeExpr(callStmt);
                        if (destMethodFirstStmt instanceof JReturnStmt) {
                            JReturnStmt returnStmt = (JReturnStmt) destMethodFirstStmt;
                            Value returnStmtOpValue = returnStmt.getOp();
                            return new FlowFunction<Value>() {
                                @Override
                                public Set<Value> computeTargets(Value source) {
                                    if (source == leftOp) {
                                        if (returnStmtOpValue instanceof StringConstant) {
                                            return Collections.singleton(hardCoddedResult);
                                        } else if (returnStmtOpValue instanceof ClassConstant) {
                                            return Collections.singleton(hardCoddedResult);
                                        } else {
                                            return Collections.singleton((Local) returnStmtOpValue);
                                        }
                                    }
                                    return Collections.emptySet();
                                }
                            };
                        }
                    }
                }
                return Identity.v();
            }

            @Override
            public FlowFunction<Value> getReturnFlowFunction(Stmt callSite, SootMethod calleeMethod, Stmt exitStmt, Stmt returnSite) {
                log.info("getReturnFlowFunction: " + returnSite);
                //Stmt s = callStmt;
                if (callSite != null) {
                    AbstractInvokeExpr ie = getAbstractInvokeExpr(callSite);//s.getInvokeExpr();
                    final List<Immediate> callArgs = ie.getArgs();
                    final List<Immediate> paramLocals = new ArrayList<>(callArgs.size());
                    for (int i = 0; i < calleeMethod.getParameterCount(); i++) {
                        paramLocals.add(calleeMethod.getBody().getParameterLocal(i));
                    }
                    //ignore implicit calls to static initializers
                    if (/*dest.getName().equals("<clinit>") &&*/ callArgs.size() == 0) {
                        //TODO: write logic
                        //return Collections.emptySet();
                    } else {
                        return new FlowFunction<Value>() {
                            @Override
                            public Set<Value> computeTargets(Value source) {
                                Set<Value> res = new HashSet<>();
                                for (int i = 0; i < paramLocals.size(); i++) {
                                    // Special case: check if function is called with integer literals as params
                                /*if (paramLocals.get(i) instanceof StringConstant && source == zeroValue()) {
                                    res.add((Local) callArgs.get(i));
                                }*/
                                    // Ordinary case: just perform the mapping
                                    if (paramLocals.get(i) == source) {
                                        if (!(callArgs.get(i) instanceof StringConstant || callArgs.get(i) instanceof ClassConstant)) {
                                            res.add((Local) callArgs.get(i));
                                        } else {
                                            // Special case: check if function is called with integer literals as params
                                            res.add(hardCoddedResult);
                                        }
                                    }
                                }
                                return res;
                            }
                        };
                    }
                }
                return Identity.v();
            }

            @Override
            public FlowFunction<Value> getCallToReturnFlowFunction(Stmt callSite, Stmt returnSite) {
                log.info("getCallToReturnFlowFunction: " + callSite.toString());
                if (callSite.equivTo(startStmt)) {
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
                }

                if (isTraditionalReflection) {
                    if (isTraditionalMethodReflection || isTraditionalFieldReflection) {
                        return analyzeMethodAndFieldReflection(null /*out*/, callSite, false);
                    } else if (isTraditionalNewInstanceReflection) {
                        return analyzeNewInstanceReflection(null /*out*/, callSite, false);
                    }
                } else {
                    if (isModernMethodReflection || isModernFieldReflection) {
                        return analyzeMethodAndFieldReflection(null /*out*/, callSite, true);
                    } else if (isModernNewInstanceReflection) {
                        return analyzeNewInstanceReflection(null /*out*/, callSite, true);
                    }
                }
                return Identity.v();
            }
        };
    }

    private FlowFunction<Value> analyzeMethodAndFieldReflection(Set<Result> out, Stmt stmt, boolean isModernReflection) {
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
            Value leftOpTemp = jAssignStmt.getLeftOp();
            Value rightOpTemp = jAssignStmt.getRightOp();
            if (rightOpTemp instanceof JCastExpr) {
                JCastExpr jCastExpr = (JCastExpr) rightOpTemp;
                rightOpTemp = jCastExpr.getOp();
            }
            if (rightOpTemp instanceof JArrayRef) {
                JArrayRef jArrayRef = (JArrayRef) rightOpTemp;
                rightOpTemp = jArrayRef.getBase();
            }
            if(rightOpTemp instanceof JInstanceFieldRef) {
                JInstanceFieldRef jInstanceFieldRef = (JInstanceFieldRef) rightOpTemp;
                rightOpTemp = new JInstanceFieldRef(new Local("<<dummyBase>>", NullType.getInstance())
                        , jInstanceFieldRef.getFieldSignature());
            }
            if(leftOpTemp instanceof JInstanceFieldRef) {
                JInstanceFieldRef jInstanceFieldRef = (JInstanceFieldRef) leftOpTemp;
                leftOpTemp = new JInstanceFieldRef(new Local("<<dummyBase>>", NullType.getInstance())
                        , jInstanceFieldRef.getFieldSignature());
            }
            final Value leftOp = leftOpTemp;
            final Value rightOp = rightOpTemp;
            List<Local> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
            //To handle aliases, string concatenation, method return value source
            return new FlowFunction<Value>() {
                @Override
                public Set<Value> computeTargets(Value source) {
                    Set<Value> res = new HashSet<>();
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
                            //JFieldRef jFieldRef = (JFieldRef) rightOp;
                            res.add(rightOp); //setResultArgumentSource(FIELD, stmt, out);
                        } else if (rightOp instanceof StringConstant && stringConcatenationSource.isEmpty()) {
                            res.add(hardCoddedResult);//res.add((Local) rightOp); //setResultArgumentSource(LOCAL, stmt, out);
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
            return new FlowFunction<Value>() {
                @Override
                public Set<Value> computeTargets(Value source) {
                    Set<Value> res = new HashSet<>();
                    res.add(source);
                    if (source != zeroValue() && (leftOp.equivTo(source) ||
                            (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp)))) {
                        if (rightOp instanceof JParameterRef && stringConcatenationSource.isEmpty()) {
                            //res.add((Local) rightOp);//setResultArgumentSource(METHOD_PARAMETER, stmt, out);
                        } else if (!stringConcatenationSource.isEmpty()) {
                            updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                        }
                    }
                    return res;
                }
            };
        } else if (stmt instanceof JInvokeStmt) {
            JInvokeStmt jInvokeStmt = (JInvokeStmt) stmt;
            AbstractInvokeExpr abstractInvokeExpr = jInvokeStmt.getInvokeExpr().orElse(null);
            if (abstractInvokeExpr instanceof JSpecialInvokeExpr) {
                JSpecialInvokeExpr jSpecialInvokeExpr = (JSpecialInvokeExpr) abstractInvokeExpr;
                Local baseVariable = jSpecialInvokeExpr.getBase();
                List<Local> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
                return new FlowFunction<Value>() {
                    @Override
                    public Set<Value> computeTargets(Value source) {
                        Set<Value> res = new HashSet<>();
                        res.add(source);
                        if (source != zeroValue() && (baseVariable.equivTo(source) ||
                                (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(baseVariable)))) {
                            MethodSignature specialInvokeMethodSignature = jSpecialInvokeExpr.getMethodSignature();
                            if (isNewStringObjectCreationSignature(specialInvokeMethodSignature, view)) {
                                Value parameter = jSpecialInvokeExpr.getArg(0);
                                if (parameter instanceof Local) {
                                    res.add((Local) parameter); //result.setTrackVariable((Local) parameter);
                                } else if (parameter instanceof StringConstant && stringConcatenationSource.isEmpty()) {
                                    res.add(hardCoddedResult);//res.add((Local) parameter); //setResultArgumentSource(LOCAL, stmt, out);
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

    private FlowFunction<Value> analyzeNewInstanceReflection(Set<Result> out, Stmt stmt, boolean isModernReflection) {
        if (stmt.equivTo(startStmt)) {
            JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(stmt);
            Immediate immediate;
            if (isModernReflection) {
                immediate = jVirtualInvokeExpr.getArg(0);
            } else {
                immediate = jVirtualInvokeExpr.getBase();
            }

            if (immediate instanceof ClassConstant) {
                return new Gen(hardCoddedResult, zeroValue());//setResultArgumentSource(LOCAL, stmt, out);
            } else if (immediate instanceof Local) {
                return new Gen(immediate, zeroValue());//result.setTrackVariable((Local) immediate);
            }
        } else if (stmt instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) stmt;
            Value leftOpTemp = jAssignStmt.getLeftOp();
            Value rightOpTemp = jAssignStmt.getRightOp();
            if (rightOpTemp instanceof JCastExpr) {
                JCastExpr jCastExpr = (JCastExpr) rightOpTemp;
                rightOpTemp = jCastExpr.getOp();
            }
            if(rightOpTemp instanceof JInstanceFieldRef) {
                JInstanceFieldRef jInstanceFieldRef = (JInstanceFieldRef) rightOpTemp;
                rightOpTemp = new JInstanceFieldRef(new Local("<<dummyBase>>", NullType.getInstance())
                        , jInstanceFieldRef.getFieldSignature());
            }
            if(leftOpTemp instanceof JInstanceFieldRef) {
                JInstanceFieldRef jInstanceFieldRef = (JInstanceFieldRef) leftOpTemp;
                leftOpTemp = new JInstanceFieldRef(new Local("<<dummyBase>>", NullType.getInstance())
                        , jInstanceFieldRef.getFieldSignature());
            }
            final Value leftOp = leftOpTemp;
            final Value rightOp = rightOpTemp;
            //List<JavaLocal> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();
            //To handle aliases, string concatenation, method return value source
            return new FlowFunction<Value>() {
                @Override
                public Set<Value> computeTargets(Value source) {
                    Set<Value> res = new HashSet<>();
                    res.add(source);
                    if (source != zeroValue() && leftOp.equivTo(source) /*||
                    (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp))*/) {


                        if (rightOp instanceof Local) {
                            res.add((Local) rightOp);//result.setTrackVariable((Local) rightOp);
                        } /*else if (rightOp instanceof JDynamicInvokeExpr) {
                    StringConcatenationProcessor stringConcatenationProcessor =
                            new StringConcatenationProcessor(view, leftOp, out, stmt, stringConcatenationSource);
                    stringConcatenationProcessor.process(rightOp);
                }*/ else if (/*(*/rightOp instanceof AbstractInstanceInvokeExpr || rightOp instanceof JStaticInvokeExpr/*)
                        && stringConcatenationSource.isEmpty()*/) {
                            AbstractInvokeExpr abstractInvokeExpr = (AbstractInvokeExpr) rightOp;
                            MethodSignature getConstructorMethodSignature = buildGetConstructorMethodSignature();
                            MethodSignature classForNameMethodSignature = buildClassForNameMethodSignature();
                            if (getConstructorMethodSignature.equals(abstractInvokeExpr.getMethodSignature())) {
                                Local local = getJVirtualInvokeExpr(stmt).getBase();
                                if (local instanceof Local) {
                                    res.add((Local) local);//result.setTrackVariable((Local) local);
                                }
                            } else if (classForNameMethodSignature.equals(abstractInvokeExpr.getMethodSignature())) {
                                Immediate local = ((JStaticInvokeExpr) getAbstractInvokeExpr(stmt)).getArg(0);
                                if (local instanceof Local) {
                                    res.add((Local) local);
                                } else if (local instanceof StringConstant) {
                                    res.add(hardCoddedResult);
                                }
                            } else {
                                //TODO: write logic
                                //setResultArgumentSource(RETURN_FROM_METHOD, stmt, out);
                            }
                        } else if (rightOp instanceof JFieldRef /*&& stringConcatenationSource.isEmpty()*/) {
                            res.add(rightOp);//setResultArgumentSource(FIELD, stmt, out);
                        } else if (rightOp instanceof ClassConstant /*&& stringConcatenationSource.isEmpty()*/) {
                            res.add(hardCoddedResult);//setResultArgumentSource(LOCAL, stmt, out);
                        }/* else if (!stringConcatenationSource.isEmpty()) {
                    updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                }*/
                    }
                    return res;
                }
            };
        } else if (stmt instanceof JIdentityStmt) {
            JIdentityStmt jIdentityStmt = (JIdentityStmt) stmt;
            Value leftOp = jIdentityStmt.getLeftOp();
            Value rightOp = jIdentityStmt.getRightOp();
            /*List<JavaLocal> stringConcatVariablesToTrack = stringConcatenationSource.getNextVariablesToTrack();*/
            /*if (leftOp.equivTo(result.getTrackVariable()) *//*||
                    (stringConcatVariablesToTrack != null && stringConcatVariablesToTrack.contains(leftOp))*//*) {
                if (rightOp instanceof JParameterRef *//*&& stringConcatenationSource.isEmpty()*//*) {
                    setResultArgumentSource(METHOD_PARAMETER, stmt, out);
                }*//* else if (!stringConcatenationSource.isEmpty()) {
                    updateStringConcatenationSource(leftOp, rightOp, stmt, out);
                }*//*
            }*/
        }
        return Identity.v();
    }

    private MethodSignature buildGetConstructorMethodSignature() {
        ClassType classType = view.getIdentifierFactory().getClassType("java.lang.Class");
        return view.getIdentifierFactory()
                   .getMethodSignature(classType, "getConstructor", "java.lang.reflect.Constructor", Arrays.asList("java.lang.Class[]"));
    }

    private MethodSignature buildClassForNameMethodSignature() {
        ClassType classType = view.getIdentifierFactory().getClassType("java.lang.Class");
        return view.getIdentifierFactory()
                   .getMethodSignature(classType, "forName", "java.lang.Class", Arrays.asList("java.lang.String"));
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
            } catch (Exception e) {
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
    public Map<Stmt, Set<Value>> initialSeeds() {
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

    public SootMethod getMethodConsistingResult(){
        return methodConsistingResult;
    }
}
