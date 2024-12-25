package de.upb.sse.cutNRun.analyzer.methodSignature;

import sootup.core.signatures.MethodSignature;

import java.util.List;

public interface UnsoundMethodSignatureCategory {
    List<MethodSignature> getAllSignatures();

    boolean isSourceOfUnsoundnessAndIncreaseCount(MethodSignature methodSignature);

    int getTotalReflectionCount();
}
