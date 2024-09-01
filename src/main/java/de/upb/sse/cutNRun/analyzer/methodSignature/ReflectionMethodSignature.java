package de.upb.sse.cutNRun.analyzer.methodSignature;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReflectionMethodSignature implements UnsoundMethodSignatureCategory {
    private View view;
    private List<MethodSignature> methodSignatures;

    public ReflectionMethodSignature(View view) {
        this.view = view;
        MethodSignature invokeMethodSignature =  buildMethodSignature("java.lang.reflect.Method", "invoke",
                                                                      "java.lang.Object", Arrays.asList("java.lang.Object", "java.lang.Object[]"));
        MethodSignature newInstanceMethodSignature =  buildMethodSignature("java.lang.Class", "newInstance",
                                                                           "java.lang.Object", Collections.emptyList());
        MethodSignature newInstanceWithParameters =  buildMethodSignature("java.lang.reflect.Constructor", "newInstance",
                                                                           "java.lang.Object", Arrays.asList("java.lang.Object[]"));
        MethodSignature getDeclaredFieldSignature =  buildMethodSignature("java.lang.Class", "getDeclaredField",
                                                                          "java.lang.reflect.Field", Arrays.asList("java.lang.String"));
        MethodSignature getFieldSignature =  buildMethodSignature("java.lang.Class", "getField",
                                                                          "java.lang.reflect.Field", Arrays.asList("java.lang.String"));

        this.methodSignatures = List.of(invokeMethodSignature, newInstanceMethodSignature, newInstanceWithParameters,
                                        getDeclaredFieldSignature, getFieldSignature);
    }

    private MethodSignature buildMethodSignature(String className, String methodName, String returnType, List<String> parameterType) {
        ClassType classType = view.getIdentifierFactory().getClassType(className);
        return view.getIdentifierFactory()
                   .getMethodSignature(classType, methodName, returnType, parameterType);

    }

    @Override
    public List<MethodSignature> getSignatures(){
        /*ClassType classType = view.getIdentifierFactory().getClassType("java.lang.reflect.Method");
        MethodSignature invokeMethodSignature =  view.getIdentifierFactory()
                                                                            .getMethodSignature(classType, "invoke",
                                                                         "java.lang.Object",
                                                                         Arrays.asList("java.lang.Object", "java.lang.Object[]"));*/
        return methodSignatures;
    }

    @Override
    public boolean isSourceOfUnsoundness(MethodSignature methodSignature) {
        return methodSignatures.contains(methodSignature);
    }
}
