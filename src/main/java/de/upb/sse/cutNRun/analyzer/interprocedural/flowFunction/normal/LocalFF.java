package de.upb.sse.cutNRun.analyzer.interprocedural.flowFunction.normal;

//import analysis.data.DFF;
import heros.FlowFunction;
/*import soot.Local;
import soot.Value;
import soot.jimple.FieldRef;
import soot.jimple.internal.JArrayRef;*/
import de.upb.sse.cutNRun.analyzer.interprocedural.DFF;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.views.View;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Assignment from a single local
 */
public class LocalFF implements FlowFunction<DFF> {

    private Value left;
    private Value rhs;
    private DFF zeroValue;
    private AliasHandler aliasHandler;
    private View view;

    public LocalFF(Value left, Value rhs, DFF zeroValue, AliasHandler aliasHandler, View view) {
        this.left = left;
        this.rhs = rhs;
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
        if (DFF.asDFF(left, view).equals(source)) {
            res.add(DFF.asDFF(rhs, view));
            aliasHandler.handleAliases(res);
        }
        // for arrays
        if(source.getValue() instanceof JArrayRef){
            JArrayRef arrayRef = (JArrayRef) source.getValue();
            if(arrayRef.getBase().equals(left)){
                if(!(rhs instanceof JFieldRef)){
                    JArrayRef newRef = new JArrayRef((Local) rhs, arrayRef.getIndex());
                    res.add(DFF.asDFF(newRef, view));
                    aliasHandler.handleAliases(res);
                }
            }
        }
        return res;
    }


}
