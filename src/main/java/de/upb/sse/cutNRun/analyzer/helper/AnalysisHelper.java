package de.upb.sse.cutNRun.analyzer.helper;

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;

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
}
