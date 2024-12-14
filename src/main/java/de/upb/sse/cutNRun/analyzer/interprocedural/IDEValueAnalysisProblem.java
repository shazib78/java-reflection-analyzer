package de.upb.sse.cutNRun.analyzer.interprocedural;

import heros.*;
import heros.edgefunc.AllBottom;
import heros.edgefunc.AllTop;
import heros.edgefunc.EdgeIdentity;
import heros.flowfunc.Gen;
import heros.flowfunc.Identity;
import lombok.extern.slf4j.Slf4j;
import sootup.analysis.interprocedural.ide.DefaultJimpleIDETabulationProblem;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.NullType;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

import java.util.*;

import static de.upb.sse.cutNRun.analyzer.helper.AnalysisHelper.getJVirtualInvokeExpr;

@Slf4j
public class IDEValueAnalysisProblem extends DefaultJimpleIDETabulationProblem<Local, String, InterproceduralCFG<Stmt, SootMethod>> {
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
                return Identity.v();
            }

            @Override
            public FlowFunction<Local> getCallFlowFunction(Stmt callStmt, SootMethod dest) {
                log.info("getCallFlowFunction: "+callStmt.toString());
                return  Identity.v();
            }

            @Override
            public FlowFunction<Local> getReturnFlowFunction(Stmt callSite, SootMethod calleeMethod, Stmt exitStmt, Stmt returnSite) {
                log.info("getReturnFlowFunction: "+returnSite.toString());
                return  Identity.v();
            }

            @Override
            public FlowFunction<Local> getCallToReturnFlowFunction(Stmt callSite, Stmt returnSite) {
                log.info("getCallToReturnFlowFunction: "+callSite.toString());
                if(callSite.equivTo(startStmt)){
                    JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(callSite);
                    Immediate argument = jVirtualInvokeExpr.getArg(0);
                    if (argument instanceof StringConstant) {
                        return new Gen(hardCoddedResult, zeroValue());
                    }
                }
                return Identity.v();
            }
        };
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
}
