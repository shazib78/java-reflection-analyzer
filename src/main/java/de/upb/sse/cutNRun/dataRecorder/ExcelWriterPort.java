package de.upb.sse.cutNRun.dataRecorder;

import java.io.IOException;
import java.util.Map;

public interface ExcelWriterPort {
    void setHeaders(String... headers);
    void saveData(Map<String, Object[]> data);
    boolean isJarWritten(String jarName) throws IOException;
}
