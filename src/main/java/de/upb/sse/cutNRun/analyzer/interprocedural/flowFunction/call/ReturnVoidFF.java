package de.upb.sse.cutNRun.analyzer.interprocedural.flowFunction.call;

import heros.FlowFunction;
import heros.solver.Pair;
/*import soot.Value;
import soot.jimple.FieldRef;*/

import de.upb.sse.cutNRun.analyzer.interprocedural.DFF;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.types.ReferenceType;
import sootup.core.views.View;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReturnVoidFF implements FlowFunction<DFF> {
    private Stmt callsite;
    private SootMethod method;

    public ReturnVoidFF(Stmt callsite, SootMethod method) {
        this.callsite = callsite;
        this.method = method;
    }


    @Override
    public Set<DFF> computeTargets(DFF source) {
        callsite.toString();
        Set<DFF> res = new HashSet<>();
        Value d = source.getValue();
        if(d instanceof JInstanceFieldRef){
            if(callsite instanceof JInvokeStmt){
                /*JInvokeStmt invoke = (JInvokeStmt) callsite;
                List<Value> args = invoke.getInvokeExpr().getArgs();
                JInstanceFieldRef fieldRef = (JInstanceFieldRef) d;
                Value base = fieldRef.getBase();
                int argIndex = 0;
                for (Value arg : args) {
                    Pair<Value, String> mArg = new Pair<>(arg, argIndex);
                    if(isSameParam(method, mArg, base)){
                        JInstanceFieldRef mapRef = new JInstanceFieldRef(arg, fieldRef.getFieldRef());
                        res.add(DFF.asDFF(mapRef));
                    }
                    argIndex++;
                }*/
            }
        }
        if(d instanceof JInstanceFieldRef){
            res.add(source);
        }
        return res;
    }

    boolean isSameParam(SootMethod method, Pair<Value, String> actualParam, Value formalParam){
        if(actualParam.getO1().getType() instanceof ReferenceType){
            Body activeBody = method.getBody();
            List<Stmt> stmts = activeBody.getStmts();
            int idIndex = -1; // @this
            for (Stmt stmt : stmts) {
                if(stmt instanceof JIdentityStmt){
                    JIdentityStmt id = (JIdentityStmt) stmt;
                    Value rightOp = id.getRightOp();
                    Value leftOp = id.getLeftOp();
                    if(rightOp.getType().equals(actualParam.getO1().getType()) && leftOp.equals(formalParam) && actualParam.getO2().equals(idIndex)){
                        return true;
                    }
                    idIndex++;
                }
            }
        }
        return false;
    }

}
