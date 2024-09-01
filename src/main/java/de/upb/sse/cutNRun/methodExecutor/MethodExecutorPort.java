package de.upb.sse.cutNRun.methodExecutor;

import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;

import java.io.IOException;

public interface MethodExecutorPort {
    void executeMethod(String methodToBeExecuted, String programPath) throws CancelException, IOException, ClassHierarchyException;
}
