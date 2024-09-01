package de.upb.sse.cutNRun.methodExecutor;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import de.upb.sse.cutNRun.slicer.Slicer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class MethodExecutorAdaptor implements MethodExecutorPort {
    private Slicer slicer;

    @Override
    public void executeMethod(String methodToBeExecuted, String appJarPath) throws CancelException, IOException, ClassHierarchyException {
        slicer.slice(appJarPath, appJarPath, methodToBeExecuted, "main");
    }
}
