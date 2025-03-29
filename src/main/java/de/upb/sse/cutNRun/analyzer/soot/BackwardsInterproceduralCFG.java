package de.upb.sse.cutNRun.analyzer.soot;

import sootup.analysis.interprocedural.icfg.BiDiInterproceduralCFG;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.callgraph.CallGraph;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

import java.util.*;

import static de.upb.sse.cutNRun.analyzer.helper.AnalysisHelper.*;

/**
 * Same as {@link JimpleBasedInterproceduralCFG} but based on inverted Stmt graphs. This should be used for backward
 * analyses.
 */
public class BackwardsInterproceduralCFG implements BiDiInterproceduralCFG<Stmt, SootMethod> {

  protected final BiDiInterproceduralCFG<Stmt, SootMethod> delegate;
  final View view;

  public BackwardsInterproceduralCFG(BiDiInterproceduralCFG<Stmt, SootMethod> fwICFG, View view) {
    delegate = fwICFG;
    this.view = view;
  }

  // swapped
  @Override
  public List<Stmt> getSuccsOf(Stmt n) {
    return delegate.getPredsOf(n);
  }

  // swapped
  @Override
  public Collection<Stmt> getStartPointsOf(SootMethod m) {
    return delegate.getEndPointsOf(m);
  }

  // swapped
  @Override
  public List<Stmt> getReturnSitesOfCallAt(Stmt n) {
    return delegate.getPredsOfCallAt(n);
  }

  // swapped
  @Override
  public boolean isExitStmt(Stmt stmt) {
    return delegate.isStartPoint(stmt);
  }

  // swapped
  @Override
  public boolean isStartPoint(Stmt stmt) {
    return delegate.isExitStmt(stmt);
  }

  // swapped
  @Override
  public Set<Stmt> allNonCallStartNodes() {
    return delegate.allNonCallEndNodes();
  }

  // swapped
  @Override
  public List<Stmt> getPredsOf(Stmt u) {
    return delegate.getSuccsOf(u);
  }

  // swapped
  @Override
  public Collection<Stmt> getEndPointsOf(SootMethod m) {
    return delegate.getStartPointsOf(m);
  }

  // swapped
  @Override
  public List<Stmt> getPredsOfCallAt(Stmt u) {
    return delegate.getSuccsOf(u);
  }

  // swapped
  @Override
  public Set<Stmt> allNonCallEndNodes() {
    return delegate.allNonCallStartNodes();
  }

  // same
  @Override
  public SootMethod getMethodOf(Stmt n) {
    return delegate.getMethodOf(n);
  }

  // same
  @Override
  public Collection<SootMethod> getCalleesOfCallAt(Stmt n) {
    /*Collection<Stmt> callerStmts = delegate.getCallersOf(delegate.getMethodOf(n));
    return callerStmts.stream()
                     .map(callerStmt -> delegate.getMethodOf(callerStmt))
                     .collect(Collectors.toSet());*/

    //To find interface implementation
    System.out.println("getCalleesOfCallAt statement class:" +n.getClass());
    /*JVirtualInvokeExpr jVirtualInvokeExpr = getJVirtualInvokeExpr(n);
    JInterfaceInvokeExpr jInterfaceInvokeExpr = getInterfaceInvokeExpr(n);
    if (jInterfaceInvokeExpr != null) {
      Collection<SootMethod> targetMethods = new HashSet<>();
      ClassType interfaceClassType = jInterfaceInvokeExpr.getMethodSignature().getDeclClassType();
      Set<CallGraph.Call> calls = ((JimpleBasedInterproceduralCFG) delegate).getCg().callsFrom(delegate.getMethodOf(n).getSignature());
      for (CallGraph.Call call : calls) {
        MethodSignature targetMethodSignature = call.getTargetMethodSignature();
        SootClass targetMethodClass = view.getClass(targetMethodSignature.getDeclClassType()).orElse(null);
        if ((targetMethodClass != null && targetMethodClass.implementsInterface(interfaceClassType))
                && targetMethodSignature.getSubSignature().equals(jInterfaceInvokeExpr.getMethodSignature().getSubSignature())) {
          Optional<? extends SootMethod> targetMethod = view.getMethod(targetMethodSignature);
          if (targetMethod.isPresent()) {
            targetMethods.add(targetMethod.get());
          }
        }
      }
      return targetMethods;
    } *//*else if(jVirtualInvokeExpr != null && !isMethodHasBody(jVirtualInvokeExpr, view) && isAbstractMethod(jVirtualInvokeExpr, view)){
      Collection<SootMethod> targetMethods = new HashSet<>();
      ClassType abstarctClassType = jVirtualInvokeExpr.getMethodSignature().getDeclClassType();
      Set<CallGraph.Call> calls = ((JimpleBasedInterproceduralCFG) delegate).getCg().callsFrom(delegate.getMethodOf(n).getSignature());
      for (CallGraph.Call call : calls) {
        MethodSignature targetMethodSignature = call.getTargetMethodSignature();
        SootClass targetMethodClass = view.getClass(targetMethodSignature.getDeclClassType()).orElse(null);
        if ((targetMethodClass != null && isSubClass(targetMethodClass, abstarctClassType, view))
                && targetMethodSignature.getSubSignature().equals(jVirtualInvokeExpr.getMethodSignature().getSubSignature())) {
          Optional<? extends SootMethod> targetMethod = view.getMethod(targetMethodSignature);
          if (targetMethod.isPresent()) {
            targetMethods.add(targetMethod.get());
          }
        }
      }
      return targetMethods;
    }*//*else {*/
      return delegate.getCalleesOfCallAt(n);
   // }
  }

  // same
  @Override
  public Collection<Stmt> getCallersOf(SootMethod m) {
    /*Set<Stmt> stmts = delegate.getCallsFromWithin(m);
    return stmts.stream()
         .flatMap(stmt -> delegate.getCalleesOfCallAt(stmt).stream())
         .map(sootMethod -> sootMethod.getBody().getStmtGraph().getStartingStmt())
            .collect(Collectors.toSet());*/
    return delegate.getCallersOf(m);
  }

  // same
  @Override
  public Set<Stmt> getCallsFromWithin(SootMethod m) {
    return delegate.getCallsFromWithin(m);
  }

  // same
  @Override
  public boolean isCallStmt(Stmt stmt) {
    //return isExitStmt(stmt);
    return delegate.isCallStmt(stmt);
  }

  // same
  @Override
  public StmtGraph<?> getOrCreateStmtGraph(SootMethod m) {
    return delegate.getOrCreateStmtGraph(m);
  }

  // same
  @Override
  public List<Value> getParameterRefs(SootMethod m) {
    return delegate.getParameterRefs(m);
  }

  @Override
  public boolean isFallThroughSuccessor(Stmt stmt, Stmt succ) {
    throw new UnsupportedOperationException("not implemented because semantics unclear");
  }

  @Override
  public boolean isBranchTarget(Stmt stmt, Stmt succ) {
    throw new UnsupportedOperationException("not implemented because semantics unclear");
  }

  // swapped
  @Override
  public boolean isReturnSite(Stmt n) {
    for (Stmt pred : getSuccsOf(n)) {
      if (isCallStmt(pred)) {
        return true;
      }
    }
    return false;
  }

  // same
  @Override
  public boolean isReachable(Stmt u) {
    return delegate.isReachable(u);
  }

}
