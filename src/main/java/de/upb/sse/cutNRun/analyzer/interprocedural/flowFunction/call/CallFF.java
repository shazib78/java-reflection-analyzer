package de.upb.sse.cutNRun.analyzer.interprocedural.flowFunction.call;



import de.upb.sse.cutNRun.analyzer.interprocedural.DFF;
import heros.FlowFunction;
/*import soot.Value;
import soot.jimple.FieldRef;*/

import de.upb.sse.cutNRun.analyzer.interprocedural.DFF;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.model.SootMethod;
import sootup.core.views.View;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CallFF implements FlowFunction<DFF> {


    private List<Immediate> paramLocals;
    private SootMethod dest;
    private DFF zeroValue;
    private List<Immediate> callArgs;
    private View view;
    private Local hardCoddedResult;

    public CallFF(List<Immediate> paramLocals, SootMethod dest, DFF zeroValue, List<Immediate> callArgs, Local hardCoddedResult, View view) {
        this.paramLocals = paramLocals;
        this.dest = dest;
        this.zeroValue = zeroValue;
        this.callArgs = callArgs;
        this.view = view;
        this.hardCoddedResult = hardCoddedResult;
    }


    @Override
    public Set<DFF> computeTargets(DFF source) {
        //ignore implicit calls to static initializers
        if (dest.getName().equals("<clinit>") && dest.getParameterCount() == 0) {
            return Collections.emptySet();
        }
        Set<DFF> res = new HashSet<>();
        if(source==zeroValue || source.getValue() instanceof JStaticFieldRef || source.getValue() instanceof JInstanceFieldRef){
            res.add(source);
        }
        for (int i = 0; i < paramLocals.size(); i++) {
            // Special case: check if function is called with integer literals as params
            //TODO: this could be used for removing hardcodded variable
            /*if (paramLocals.get(i) instanceof IntConstant && source == zeroValue) {
                res.add(DFF.asDFF(callArgs.get(i)));
            }*/
            // Ordinary case: just perform the mapping
            if (DFF.asDFF(paramLocals.get(i), view).equals(source)) {
                if (!(callArgs.get(i) instanceof StringConstant || callArgs.get(i) instanceof ClassConstant)) {
                    //res.add((Local) callArgs.get(i));
                    res.add(DFF.asDFF(callArgs.get(i), view));
                } else {
                    // Special case: check if function is called with integer literals as params
                    res.add(DFF.asDFF(hardCoddedResult, view));
                }
            }
        }
        return res;
    }
}
