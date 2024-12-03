package de.upb.sse.cutNRun.analyzer.methodSignature;

import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModernReflectionMethodSignature implements UnsoundMethodSignatureCategory {
    private View view;
    private List<MethodSignature> allMethodSignatures;
    private List<MethodSignature> methodReflectionSignatures;
    private List<MethodSignature> fieldReflectionSignatures;
    private List<MethodSignature> newInstanceReflectionSignatures;
    private int totalModernReflectionCount;
    private int methodReflectionCount;
    private int fieldReflectionCount;
    private int newInstanceReflectionCount;

    public ModernReflectionMethodSignature(View view) {
        this.view = view;
        this.totalModernReflectionCount = 0;
        MethodSignature findStaticSignature = buildMethodSignature("java.lang.invoke.MethodHandles$Lookup", "findStatic",
                                                                   "java.lang.invoke.MethodHandle", Arrays.asList("java.lang.Class", "java.lang.String", "java.lang.invoke.MethodType"));
        MethodSignature findVirtualSignature = buildMethodSignature("java.lang.invoke.MethodHandles$Lookup", "findVirtual",
                                                                    "java.lang.invoke.MethodHandle", Arrays.asList("java.lang.Class", "java.lang.String", "java.lang.invoke.MethodType"));
        MethodSignature findConstructorSignature = buildMethodSignature("java.lang.invoke.MethodHandles$Lookup", "findConstructor",
                                                                        "java.lang.invoke.MethodHandle", Arrays.asList("java.lang.Class", "java.lang.invoke.MethodType"));
        MethodSignature findStaticGetterSignature = buildMethodSignature("java.lang.invoke.MethodHandles$Lookup", "findStaticGetter",
                                                                         "java.lang.invoke.MethodHandle", Arrays.asList("java.lang.Class", "java.lang.String", "java.lang.Class"));
        MethodSignature findGetterSignature = buildMethodSignature("java.lang.invoke.MethodHandles$Lookup", "findGetter",
                                                                   "java.lang.invoke.MethodHandle", Arrays.asList("java.lang.Class", "java.lang.String", "java.lang.Class"));
        MethodSignature findSpecialSignature = buildMethodSignature("java.lang.invoke.MethodHandles$Lookup", "findSpecial",
                                                                    "java.lang.invoke.MethodHandle", Arrays.asList("java.lang.Class", "java.lang.String", "java.lang.invoke.MethodType", "java.lang.Class"));
        this.methodReflectionSignatures = List.of(findStaticSignature, findVirtualSignature, findSpecialSignature);
        this.newInstanceReflectionSignatures = List.of(findConstructorSignature);
        this.fieldReflectionSignatures = List.of(findStaticGetterSignature, findGetterSignature);

        /*this.allMethodSignatures = List.of(findStaticSignature, findVirtualSignature, findConstructorSignature,
                                           findStaticGetterSignature, findGetterSignature, findSpecialSignature);*/
        this.allMethodSignatures = new ArrayList<>();
        allMethodSignatures.addAll(methodReflectionSignatures);
        allMethodSignatures.addAll(newInstanceReflectionSignatures);
        allMethodSignatures.addAll(fieldReflectionSignatures);
    }

    private MethodSignature buildMethodSignature(String className, String methodName, String returnType, List<String> parameterType) {
        ClassType classType = view.getIdentifierFactory().getClassType(className);
        return view.getIdentifierFactory()
                   .getMethodSignature(classType, methodName, returnType, parameterType);

    }

    @Override
    public List<MethodSignature> getSignatures() {
        return allMethodSignatures;
    }

    @Override
    public boolean isSourceOfUnsoundness(MethodSignature methodSignature) {
        boolean isUnsound = allMethodSignatures.contains(methodSignature);
        if(isUnsound){
            totalModernReflectionCount++;
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
        return totalModernReflectionCount;
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
