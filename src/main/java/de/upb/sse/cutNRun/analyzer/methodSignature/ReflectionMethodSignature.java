package de.upb.sse.cutNRun.analyzer.methodSignature;

import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReflectionMethodSignature implements UnsoundMethodSignatureCategory {
    private View view;
    private List<MethodSignature> allReflectionSignatures;
    private List<MethodSignature> methodReflectionSignatures;
    private List<MethodSignature> fieldReflectionSignatures;
    private List<MethodSignature> newInstanceReflectionSignatures;
    private int totalReflectionCount;
    private int methodReflectionCount;
    private int fieldReflectionCount;
    private int newInstanceReflectionCount;

    public ReflectionMethodSignature(View view) {
        this.view = view;
        this.totalReflectionCount = 0;
        this.methodReflectionCount = 0;
        this.fieldReflectionCount = 0;
        this.newInstanceReflectionCount = 0;

        /*MethodSignature invokeMethodSignature =  buildMethodSignature("java.lang.reflect.Method", "invoke",
                                                                      "java.lang.Object", Arrays.asList("java.lang.Object", "java.lang.Object[]"));*/
        MethodSignature getMethodSignature = buildMethodSignature("java.lang.Class", "getMethod",
                                                                  "java.lang.reflect.Method", Arrays.asList("java.lang.String", "java.lang.Class[]"));
        MethodSignature getDeclaredMethodMethodSignature = buildMethodSignature("java.lang.Class", "getDeclaredMethod",
                                                                                "java.lang.reflect.Method", Arrays.asList("java.lang.String", "java.lang.Class[]"));
        MethodSignature newInstanceMethodSignature = buildMethodSignature("java.lang.Class", "newInstance",
                                                                          "java.lang.Object", Collections.emptyList());
        MethodSignature newInstanceWithParameters = buildMethodSignature("java.lang.reflect.Constructor", "newInstance",
                                                                         "java.lang.Object", Arrays.asList("java.lang.Object[]"));
        MethodSignature getDeclaredFieldSignature = buildMethodSignature("java.lang.Class", "getDeclaredField",
                                                                         "java.lang.reflect.Field", Arrays.asList("java.lang.String"));
        MethodSignature getFieldSignature = buildMethodSignature("java.lang.Class", "getField",
                                                                 "java.lang.reflect.Field", Arrays.asList("java.lang.String"));
        /*MethodSignature findStaticSignature = buildMethodSignature("java.lang.invoke.MethodHandles$Lookup", "findStatic",
                                                                 "java.lang.invoke.MethodHandle", Arrays.asList("java.lang.Class","java.lang.String", "java.lang.invoke.MethodType"));*/

        this.methodReflectionSignatures = List.of(getMethodSignature, getDeclaredMethodMethodSignature);
        this.newInstanceReflectionSignatures = List.of(newInstanceMethodSignature, newInstanceWithParameters);
        this.fieldReflectionSignatures = List.of(getDeclaredFieldSignature, getFieldSignature);
        this.allReflectionSignatures = new ArrayList<>();
        allReflectionSignatures.addAll(methodReflectionSignatures);
        allReflectionSignatures.addAll(newInstanceReflectionSignatures);
        allReflectionSignatures.addAll(fieldReflectionSignatures);
    }

    private MethodSignature buildMethodSignature(String className, String methodName, String returnType, List<String> parameterType) {
        ClassType classType = view.getIdentifierFactory().getClassType(className);
        return view.getIdentifierFactory()
                   .getMethodSignature(classType, methodName, returnType, parameterType);

    }

    @Override
    public List<MethodSignature> getSignatures() {
        /*ClassType classType = view.getIdentifierFactory().getClassType("java.lang.reflect.Method");
        MethodSignature invokeMethodSignature =  view.getIdentifierFactory()
                                                                            .getMethodSignature(classType, "invoke",
                                                                         "java.lang.Object",
                                                                         Arrays.asList("java.lang.Object", "java.lang.Object[]"));*/
        return allReflectionSignatures;
    }

    @Override
    public boolean isSourceOfUnsoundness(MethodSignature methodSignature) {
        boolean isUnsound = allReflectionSignatures.contains(methodSignature);
        if (isUnsound) {
            totalReflectionCount++;
            if (methodReflectionSignatures.contains(methodSignature)) {
                methodReflectionCount++;
            } else if (newInstanceReflectionSignatures.contains(methodSignature)) {
                newInstanceReflectionCount++;
            } else if (fieldReflectionSignatures.contains(methodSignature)) {
                fieldReflectionCount++;
            }
        }
        return isUnsound;
    }

    @Override
    public int getTotalReflectionCount() {
        return totalReflectionCount;
    }

    public int getMethodReflectionCount() {
        return methodReflectionCount;
    }

    public int getFieldReflectionCount() {
        return fieldReflectionCount;
    }

    public int getNewInstanceReflectionCount() {
        return newInstanceReflectionCount;
    }
}
