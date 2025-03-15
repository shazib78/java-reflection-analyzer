package de.upb.sse.cutNRun.analyzer.interprocedural;

import heros.EdgeFunction;
import heros.edgefunc.EdgeIdentity;

import java.util.Set;

import static de.upb.sse.cutNRun.analyzer.interprocedural.IDEValueAnalysisProblem.ALL_BOTTOM;

public class StringAssignEdgeFunction implements EdgeFunction<Set<String>> {

    private Set<String> value;

    public StringAssignEdgeFunction(Set<String> value) {
        this.value = value;
    }

    public Set<String> getValue() {
        return value;
    }

    @Override
    public Set<String> computeTarget(Set<String> strings) {
        return value;
    }

    @Override
    public EdgeFunction<Set<String>> composeWith(EdgeFunction<Set<String>> secondFunction) {
        if(secondFunction instanceof EdgeIdentity){
            return this;
        }else if(secondFunction instanceof StringAssignEdgeFunction){
            return secondFunction; //TODO: not sure of the logic
        }/*else if(secondFunction instanceof IntegerBinop){
            // Heros paper advises inplace composition for fast execution
            BinopExpr binop = ((IntegerBinop) secondFunction).getBinop();
            Value lop = binop.getOp1();
            Value rop = binop.getOp2();
            if(lop instanceof IntConstant){
                int val = ((IntConstant) lop).value;
                String op = binop.getSymbol();
                int res = IntegerBinop.executeBinOperation(op, value, val);
                return new IntegerAssign(res, analysisLogger);
            }else if(rop instanceof IntConstant){
                int val = ((IntConstant) rop).value;
                String op = binop.getSymbol();
                int res = IntegerBinop.executeBinOperation(op, value, val);
                return new IntegerAssign(res, analysisLogger);
            }
            analysisLogger.log();
            throw new RuntimeException("neither lop nor rop is constant: " + this.getValue() + System.lineSeparator() + ((IntegerBinop) secondFunction).getBinop());
        }*/
        return this;
    }

    @Override
    public EdgeFunction<Set<String>> meetWith(EdgeFunction<Set<String>> otherFunction) {
        if(otherFunction instanceof EdgeIdentity){
            return this;
        }else if(otherFunction instanceof StringAssignEdgeFunction){
            Set<String> valueFromOtherBranch = ((StringAssignEdgeFunction) otherFunction).getValue();
            Set<String> valueFromThisBranch = this.getValue();
            if(valueFromOtherBranch.equals(valueFromThisBranch)){
                return this;
            }else{
                valueFromThisBranch.addAll(valueFromOtherBranch);
                return new StringAssignEdgeFunction(valueFromThisBranch);
            }
        }else if(ALL_BOTTOM.equalTo(otherFunction)){
            return otherFunction;
        }
        throw new RuntimeException("can't meeet: " + this.toString() + " and " + otherFunction.toString());
    }

    @Override
    public boolean equalTo(EdgeFunction<Set<String>> other) {
        return this == other;
    }
}
