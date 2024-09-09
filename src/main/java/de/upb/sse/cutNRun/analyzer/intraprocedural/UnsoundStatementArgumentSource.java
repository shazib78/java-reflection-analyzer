package de.upb.sse.cutNRun.analyzer.intraprocedural;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Objects;

@Builder
@AllArgsConstructor
public class UnsoundStatementArgumentSource {
    private int statementLineNumber;
    private ArgumentSource argumentSource;

    @Override
    public String toString() {
        return "UnsoundStatementArgumentSource{" +
                "statementLineNumber=" + statementLineNumber +
                ", argumentSource=" + argumentSource +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UnsoundStatementArgumentSource that = (UnsoundStatementArgumentSource) o;
        return statementLineNumber == that.statementLineNumber && argumentSource == that.argumentSource;
    }

    @Override
    public int hashCode() {
        return Objects.hash(statementLineNumber, argumentSource);
    }
}
