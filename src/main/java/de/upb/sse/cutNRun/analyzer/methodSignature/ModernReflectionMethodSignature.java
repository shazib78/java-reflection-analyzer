package de.upb.sse.cutNRun.analyzer.methodSignature;

import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModernReflectionMethodSignature implements UnsoundMethodSignatureCategory {
    private View view;
    private List<MethodSignature> methodSignatures;

    public ModernReflectionMethodSignature(View view) {
        this.view = view;
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

        this.methodSignatures = List.of(findStaticSignature, findVirtualSignature, findConstructorSignature,
                                        findStaticGetterSignature, findGetterSignature, findSpecialSignature);
    }

    private MethodSignature buildMethodSignature(String className, String methodName, String returnType, List<String> parameterType) {
        ClassType classType = view.getIdentifierFactory().getClassType(className);
        return view.getIdentifierFactory()
                   .getMethodSignature(classType, methodName, returnType, parameterType);

    }

    @Override
    public List<MethodSignature> getSignatures() {
        return methodSignatures;
    }

    @Override
    public boolean isSourceOfUnsoundness(MethodSignature methodSignature) {
        return methodSignatures.contains(methodSignature);
    }
}
