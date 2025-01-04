package de.upb.sse.cutNRun.analyzer.intraprocedural;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sootup.core.jimple.basic.Local;
import sootup.java.core.jimple.basic.JavaLocal;

import java.util.Objects;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class Result {
    private int statementLineNumber;
    private ArgumentSource argumentSource;
    private Local trackVariable;

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
        Result that = (Result) o;
        return statementLineNumber == that.statementLineNumber && argumentSource == that.argumentSource;
    }

    @Override
    public int hashCode() {
        return Objects.hash(statementLineNumber, argumentSource);
    }
}
