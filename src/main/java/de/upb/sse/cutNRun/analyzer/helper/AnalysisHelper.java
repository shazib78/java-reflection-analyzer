package de.upb.sse.cutNRun.analyzer.helper;

import de.upb.sse.cutNRun.analyzer.methodSignature.ModernReflectionMethodSignature;
import de.upb.sse.cutNRun.analyzer.methodSignature.TraditionalReflectionMethodSignature;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    public static AbstractInvokeExpr getAbstractInvokeExpr(Stmt stmt) {
        if (stmt instanceof JInvokeStmt) {
            JInvokeStmt jInvokeStmt = (JInvokeStmt) stmt;
            return jInvokeStmt.getInvokeExpr().orElse(null);
            /*if (abstractInvokeExpr instanceof JVirtualInvokeExpr) {
                return (JVirtualInvokeExpr) abstractInvokeExpr;
            }*/
        } else if (stmt instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) stmt;
            Value rightOp = jAssignStmt.getRightOp();
            if (rightOp instanceof AbstractInvokeExpr) {
                return (AbstractInvokeExpr) rightOp;
            }
        }
        return null;
    }

    public static JInterfaceInvokeExpr getInterfaceInvokeExpr(Stmt stmt) {
        if (stmt instanceof JInvokeStmt) {
            JInvokeStmt jInvokeStmt = (JInvokeStmt) stmt;
            AbstractInvokeExpr abstractInvokeExpr = jInvokeStmt.getInvokeExpr().orElse(null);
            if (abstractInvokeExpr instanceof JInterfaceInvokeExpr) {
                return (JInterfaceInvokeExpr) abstractInvokeExpr;
            }
        } else if (stmt instanceof JAssignStmt) {
            JAssignStmt jAssignStmt = (JAssignStmt) stmt;
            Value rightOp = jAssignStmt.getRightOp();
            if (rightOp instanceof JInterfaceInvokeExpr) {
                return (JInterfaceInvokeExpr) rightOp;
            }
        }
        return null;
    }

    public static boolean isMethodHasBody(JVirtualInvokeExpr jVirtualInvokeExpr, View view){
        if(jVirtualInvokeExpr != null) {
            Optional<? extends SootMethod> sootMethod = view.getMethod(jVirtualInvokeExpr.getMethodSignature());
            if(sootMethod.isPresent()){
                return sootMethod.get().hasBody();
            }
        }
        return false;
    }

    public static boolean isAbstractMethod(JVirtualInvokeExpr jVirtualInvokeExpr, View view){
        if(jVirtualInvokeExpr != null) {
            Optional<? extends SootMethod> sootMethod = view.getMethod(jVirtualInvokeExpr.getMethodSignature());
            if(sootMethod.isPresent()){
                return sootMethod.get().isAbstract();
            }
        }
        return false;
    }

    public static boolean isSubClass(SootClass subClass, ClassType superClassType, View view) {
        List<ClassType> parentClasses = new ArrayList();
        while (subClass.hasSuperclass() && !subClass.getSuperclass().get().getFullyQualifiedName().equals("java.lang.Object")) {
            ClassType classType = subClass.getSuperclass().get();
            Optional<? extends SootClass> sootClass = view.getClass(classType);
            if(sootClass.isPresent()){
                parentClasses.add(classType);
                subClass = sootClass.get();
            } else {
                break;
            }
        }
        return parentClasses.contains(superClassType);
    }

    public static boolean isNewStringObjectCreationSignature(MethodSignature specialInvokeMethodSignature, View view) {
        MethodSignature StringConstructor = buildMethodSignature("java.lang.String", "<init>", "void",
                                                                 Arrays.asList("java.lang.String"), view);
        MethodSignature charArrayConstructor = buildMethodSignature("java.lang.String", "<init>", "void",
                                                                    Arrays.asList("char[]"), view);
        MethodSignature charArrayWithSubRangeConstructor = buildMethodSignature("java.lang.String", "<init>", "void",
                                                                                Arrays.asList("char[]", "int", "int"), view);
        MethodSignature byteArrayConstructor = buildMethodSignature("java.lang.String", "<init>", "void",
                                                                    Arrays.asList("byte[]"), view);
        MethodSignature byteArrayWithSubRangeConstructor = buildMethodSignature("java.lang.String", "<init>", "void",
                                                                                Arrays.asList("byte[]", "int", "int"), view);
        List<MethodSignature> stringConstructorSignatures = List.of(StringConstructor, charArrayConstructor, charArrayWithSubRangeConstructor,
                                                                    byteArrayConstructor, byteArrayWithSubRangeConstructor);
        return stringConstructorSignatures.contains(specialInvokeMethodSignature);
    }

    public static MethodSignature buildMethodSignature(String className, String methodName, String returnType, List<String> parameterType, View view) {
        ClassType classType = view.getIdentifierFactory().getClassType(className);
        return view.getIdentifierFactory()
                   .getMethodSignature(classType, methodName, returnType, parameterType);

    }

    public static boolean isTraditionalNewInstanceReflection(MethodSignature methodSignature, View view) {
        TraditionalReflectionMethodSignature traditionalReflection = new TraditionalReflectionMethodSignature(view);
        return traditionalReflection.isNewInstanceReflection(methodSignature);
    }

    public static boolean isTraditionalMethodReflection(MethodSignature methodSignature, View view) {
        TraditionalReflectionMethodSignature traditionalReflection = new TraditionalReflectionMethodSignature(view);
        return traditionalReflection.isMethodReflection(methodSignature);
    }

    public static boolean isTraditionalFieldReflection(MethodSignature methodSignature, View view) {
        TraditionalReflectionMethodSignature traditionalReflection = new TraditionalReflectionMethodSignature(view);
        return traditionalReflection.isFieldReflection(methodSignature);
    }

    public static boolean isTraditionalReflection(MethodSignature methodSignature, View view) {
        TraditionalReflectionMethodSignature traditionalReflection = new TraditionalReflectionMethodSignature(view);
        return traditionalReflection.isSourceOfUnsoundness(methodSignature);
    }

    public static boolean isModernNewInstanceReflection(MethodSignature methodSignature, View view) {
        ModernReflectionMethodSignature modernReflection = new ModernReflectionMethodSignature(view);
        return modernReflection.isNewInstanceReflection(methodSignature);
    }

    public static boolean isModernMethodReflection(MethodSignature methodSignature, View view) {
        ModernReflectionMethodSignature modernReflection = new ModernReflectionMethodSignature(view);
        return modernReflection.isMethodReflection(methodSignature);
    }

    public static boolean isModernFieldReflection(MethodSignature methodSignature, View view) {
        ModernReflectionMethodSignature modernReflection = new ModernReflectionMethodSignature(view);
        return modernReflection.isFieldReflection(methodSignature);
    }

    public static boolean isModernReflection(MethodSignature methodSignature, View view) {
        ModernReflectionMethodSignature modernReflection = new ModernReflectionMethodSignature(view);
        return modernReflection.isSourceOfUnsoundness(methodSignature);
    }
}
