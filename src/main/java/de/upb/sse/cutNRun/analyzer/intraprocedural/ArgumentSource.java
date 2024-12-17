package de.upb.sse.cutNRun.analyzer.intraprocedural;

public enum ArgumentSource {
    LOCAL,
    METHOD_PARAMETER,
    RETURN_FROM_METHOD,
    FIELD,
    CONTRADICTING,
    UNKOWN,
    ERROR_BRANCHING_AND_STRINGCONCAT
}
