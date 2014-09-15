package de.uniko.sebschlicht.titan.kibble;

import java.util.HashMap;
import java.util.Map;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.rexster.extension.AbstractRexsterExtension;

import de.uniko.sebschlicht.titan.graphity.TitanGraphity;
import de.uniko.sebschlicht.titan.graphity.WriteOptimizedGraphity;

public abstract class GraphityKibble extends AbstractRexsterExtension {

    protected static final String KEY_RESPONSE_VALUE = "responseValue";

    private static Map<TitanGraph, TitanGraphity> GRAPHITY_INSTANCES;
    static {
        GRAPHITY_INSTANCES = new HashMap<TitanGraph, TitanGraphity>();
    }

    protected TitanGraphity graphity;

    protected TitanGraphity getGraphityInstance(TitanGraph graph) {
        if (graphity != null) {
            return graphity;
        }
        TitanGraphity graphity = GRAPHITY_INSTANCES.get(graph);
        if (graphity == null) {
            graphity = new WriteOptimizedGraphity(graph);
            GRAPHITY_INSTANCES.put(graph, graphity);
        }
        return graphity;
    }
}
