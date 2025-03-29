package de.upb.sse.cutNRun.analyzer.interprocedural.flowFunction.normal;

//import analysis.data.DFF;
import heros.FlowFunction;
/*import soot.Value;
import soot.jimple.internal.JArrayRef;*/
import de.upb.sse.cutNRun.analyzer.interprocedural.DFF;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.views.View;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ArrayLoadFF implements FlowFunction<DFF> {

    private DFF zeroValue;
    private AliasHandler aliasHandler;
    private JArrayRef arrayRef;
    private Value lhs;
    private View view;

    public ArrayLoadFF(JArrayRef arrayRef, Value lhs, DFF zeroValue, AliasHandler aliasHandler, View view) {
        this.arrayRef = arrayRef;
        this.lhs = lhs;
        this.zeroValue = zeroValue;
        this.aliasHandler = aliasHandler;
        this.view = view;
    }


    @Override
    public Set<DFF> computeTargets(DFF source) {
        if(source.equals(zeroValue)){
            return Collections.singleton(source);
        }
        Set<DFF> res = new HashSet<>();
        res.add(source);
        if(DFF.asDFF(arrayRef, view).equals(source)){
            res.add(DFF.asDFF(lhs, view));
            aliasHandler.handleAliases(res);
        }
        return res;
    }
}
