package de.upb.sse.cutNRun.analyzer.interprocedural.flowFunction.normal;


import boomerang.scope.Val;
import boomerang.scope.sootup.jimple.JimpleUpVal;
import boomerang.util.AccessPath;
import de.upb.sse.cutNRun.analyzer.interprocedural.DFF;
import de.upb.sse.cutNRun.analyzer.interprocedural.SparseAliasManager;
import sootup.callgraph.CallGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;
import sparse.SparsificationStrategy;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class FieldStoreAliasHandler implements AliasHandler {

    private JInstanceFieldRef fieldRef;
    private Stmt curr;
    private SootMethod method;
    private View view;
    private CallGraph callGraph;
    private Collection<JavaSootMethod> entryPoints;

    public FieldStoreAliasHandler(SootMethod method, Stmt curr, Value lhs,
                                  View view, CallGraph callGraph, Collection<JavaSootMethod> entryPoints) {
        if(lhs instanceof JInstanceFieldRef){
            this.fieldRef = (JInstanceFieldRef) lhs;
        }
        this.curr = curr;
        this.method = method;
        this.view = view;
        this.callGraph = callGraph;
        this.entryPoints = entryPoints;
    }


    @Override
    public void handleAliases(Set<DFF> res) {
        if(this.fieldRef!=null) {
            SparseAliasManager aliasManager = SparseAliasManager.getInstance(SparsificationStrategy.NONE, true,
                                                                             view, callGraph, entryPoints);
            Set<AccessPath> aliases = aliasManager.getAliases((Stmt) curr, method, fieldRef.getBase());
            for (AccessPath alias : aliases) {
                Val base = alias.getBase();
                if (base instanceof JimpleUpVal) {
                    JimpleUpVal jval = (JimpleUpVal) base;
                    Value delegate = jval.getDelegate();
                    if(!delegate.equals(fieldRef.getBase())){
                        res.add(new DFF(delegate, curr, Collections.singletonList(view.getField(fieldRef.getFieldSignature()).get()), view));
                    }
                }
            }
        }
    }

}
