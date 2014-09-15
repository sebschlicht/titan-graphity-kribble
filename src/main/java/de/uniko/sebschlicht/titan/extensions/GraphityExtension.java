package de.uniko.sebschlicht.titan.extensions;

import java.util.HashMap;
import java.util.Map;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.rexster.extension.AbstractRexsterExtension;

import de.uniko.sebschlicht.titan.graphity.TitanGraphity;
import de.uniko.sebschlicht.titan.graphity.WriteOptimizedGraphity;

public abstract class GraphityExtension extends AbstractRexsterExtension {

    protected static final String KEY_RESPONSE_VALUE = "responseValue";

    protected static final String EXT_NAMESPACE = "graphity";

    private static Map<TitanGraph, TitanGraphity> GRAPHITY_INSTANCES;
    static {
        GRAPHITY_INSTANCES = new HashMap<TitanGraph, TitanGraphity>();
    }

    protected TitanGraphity graphity;

    protected TitanGraphity getGraphityInstance(TitanGraph graph) {
        if (graphity != null) {
            return graphity;
        }
        graphity = GRAPHITY_INSTANCES.get(graph);
        if (graphity == null) {
            graphity = new WriteOptimizedGraphity(graph);
            GRAPHITY_INSTANCES.put(graph, graphity);
        }
        return graphity;
    }
}
