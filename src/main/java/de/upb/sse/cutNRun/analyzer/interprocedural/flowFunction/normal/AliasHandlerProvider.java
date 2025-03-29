package de.upb.sse.cutNRun.analyzer.interprocedural.flowFunction.normal;


import sootup.callgraph.CallGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;

import java.util.Collection;

public class AliasHandlerProvider {

    public static AliasHandler get(SootMethod method, Stmt curr, Value rhs,
                                   View view, CallGraph callGraph, Collection<JavaSootMethod> entryPoints) {
        if (rhs instanceof JInstanceFieldRef) {
            return new FieldStoreAliasHandler(method, curr, rhs, view, callGraph, entryPoints);
        } else if (rhs instanceof JArrayRef) {
            return new ArrayStoreAliasHandler(method, curr, rhs, view, callGraph, entryPoints);
        } else {
            return new AliasHandler() {
            };
        }
    }


}
