package de.upb.sse.cutNRun.dataRecorder;

import java.util.Map;

public interface ExcelWriterPort {
    void setHeaders(String... headers);
    void saveData(Map<String, Object[]> data);
}
