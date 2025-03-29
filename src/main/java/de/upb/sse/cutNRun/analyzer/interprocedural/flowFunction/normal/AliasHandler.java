package de.upb.sse.cutNRun.analyzer.interprocedural.flowFunction.normal;


import de.upb.sse.cutNRun.analyzer.interprocedural.DFF;

import java.util.Set;

public interface AliasHandler {

    default void handleAliases(Set<DFF> res){

    }
}
