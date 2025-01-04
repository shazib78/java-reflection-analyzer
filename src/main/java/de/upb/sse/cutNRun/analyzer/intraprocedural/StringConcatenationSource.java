package de.upb.sse.cutNRun.analyzer.intraprocedural;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import sootup.core.jimple.basic.Local;
import sootup.java.core.jimple.basic.JavaLocal;

import java.util.List;

import static de.upb.sse.cutNRun.analyzer.intraprocedural.ArgumentSource.CONTRADICTING;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class StringConcatenationSource {
    private List<ArgumentSource> argumentSources;
    private List<Local> nextVariablesToTrack;

    public boolean isEmpty() {
        if ((argumentSources == null || argumentSources.isEmpty())
                && (nextVariablesToTrack == null || nextVariablesToTrack.isEmpty())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isEveryStringFromSameSource() {
        if (CollectionUtils.isEmpty(argumentSources) || !CollectionUtils.isEmpty(nextVariablesToTrack)) {
            return false;
        } else {
            ArgumentSource sourceToCompare = argumentSources.get(0);
            for (ArgumentSource argumentSource : argumentSources) {
                if (argumentSource != sourceToCompare) {
                    return false;
                }
            }
            return true;
        }
    }

    public ArgumentSource getSource() {
        if (isEveryStringFromSameSource()) {
            return argumentSources.get(0);
        } else {
            return CONTRADICTING;
        }
    }
}
