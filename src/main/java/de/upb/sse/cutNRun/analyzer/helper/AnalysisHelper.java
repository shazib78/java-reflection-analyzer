package de.upb.sse.cutNRun.analyzer.helper;

import de.upb.sse.cutNRun.analyzer.methodSignature.ModernReflectionMethodSignature;
import de.upb.sse.cutNRun.analyzer.methodSignature.TraditionalReflectionMethodSignature;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

public class AnalysisHelper {
    public static JVirtualInvokeExpr getJVirtualInvokeExpr(Stmt stmt) {
        if (stmt instanceof JInvokeStmt) {
            JInvokeStmt jInvokeStmt = (JInvokeStmt) stmt;
            AbstractInvokeExpr abstractInvokeExpr = jInvokeStmt.getInvokeExpr().orElse(null);
            if (abstractInvokeExpr instanceof JVirtualInvokeExpr) {
                return (JVirtualInvokeExpr) abstractInvokeExpr;
            }
        } else if (stmt instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) stmt;
            Value rightOp = jAssignStmt.getRightOp();
            if (rightOp instanceof JVirtualInvokeExpr) {
                return (JVirtualInvokeExpr) rightOp;
            }
        }
        return null;
    }

    public static boolean isTraditionalNewInstanceReflection(MethodSignature methodSignature, View view){
        TraditionalReflectionMethodSignature traditionalReflection = new TraditionalReflectionMethodSignature(view);
        return traditionalReflection.isNewInstanceReflection(methodSignature);
    }

    public static boolean isTraditionalMethodReflection(MethodSignature methodSignature, View view){
        TraditionalReflectionMethodSignature traditionalReflection = new TraditionalReflectionMethodSignature(view);
        return traditionalReflection.isMethodReflection(methodSignature);
    }

    public static boolean isTraditionalFieldReflection(MethodSignature methodSignature, View view){
        TraditionalReflectionMethodSignature traditionalReflection = new TraditionalReflectionMethodSignature(view);
        return traditionalReflection.isFieldReflection(methodSignature);
    }

    public static boolean isTraditionalReflection(MethodSignature methodSignature, View view){
        TraditionalReflectionMethodSignature traditionalReflection = new TraditionalReflectionMethodSignature(view);
        return traditionalReflection.isSourceOfUnsoundness(methodSignature);
    }

    public static boolean isModernNewInstanceReflection(MethodSignature methodSignature, View view){
        ModernReflectionMethodSignature modernReflection = new ModernReflectionMethodSignature(view);
        return modernReflection.isNewInstanceReflection(methodSignature);
    }

    public static boolean isModernMethodReflection(MethodSignature methodSignature, View view){
        ModernReflectionMethodSignature modernReflection = new ModernReflectionMethodSignature(view);
        return modernReflection.isMethodReflection(methodSignature);
    }

    public static boolean isModernFieldReflection(MethodSignature methodSignature, View view){
        ModernReflectionMethodSignature modernReflection = new ModernReflectionMethodSignature(view);
        return modernReflection.isFieldReflection(methodSignature);
    }
}
