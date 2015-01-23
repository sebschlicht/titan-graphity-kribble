package de.uniko.sebschlicht.titan.extensions;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.rexster.extension.AbstractRexsterExtension;

import de.uniko.sebschlicht.titan.graphity.TitanGraphity;
import de.uniko.sebschlicht.titan.graphity.WriteOptimizedGraphity;

public abstract class GraphityExtension extends AbstractRexsterExtension {

    protected static final String KEY_RESPONSE_VALUE = "responseValue";

    protected static final String EXT_NAMESPACE = "graphity";

    protected static final int NUM_MAX_RETRIES = 10;

    protected TitanGraphity graphity;

    protected TitanGraphity getGraphityInstance(TitanGraph graph) {
        if (graphity != null) {
            return graphity;
        }
        graphity = new WriteOptimizedGraphity(graph);
        graphity.init();
        return graphity;
    }
}
