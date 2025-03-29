package de.upb.sse.cutNRun.analyzer.interprocedural.flowFunction.call;

import de.upb.sse.cutNRun.analyzer.interprocedural.flowFunction.normal.FieldStoreAliasHandler;
import heros.FlowFunction;
/*import soot.Value;
import soot.jimple.FieldRef;*/

import de.upb.sse.cutNRun.analyzer.interprocedural.DFF;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.views.View;

import java.util.HashSet;
import java.util.Set;

public class ReturnFF implements FlowFunction<DFF> {

    private Value retLocal;
    private Value tgtLocal;
    private FieldStoreAliasHandler aliasHandler;
    private View view;

    public ReturnFF(Value retLocal, Value tgtLocal, FieldStoreAliasHandler aliasHandler, View view) {
        this.retLocal = retLocal;
        this.tgtLocal = tgtLocal;
        this.aliasHandler = aliasHandler;
        this.view = view;
    }


    @Override
    public Set<DFF> computeTargets(DFF source) {
        Set<DFF> res = new HashSet<>();
        if (source.equals(DFF.asDFF(tgtLocal, view))) {
            res.add(DFF.asDFF(retLocal, view));
            aliasHandler.handleAliases(res);
        }
        if(source.getValue() instanceof JStaticFieldRef){
            res.add(source);
        }
        return res;
    }
}
