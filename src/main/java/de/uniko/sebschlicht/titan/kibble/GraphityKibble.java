package de.uniko.sebschlicht.titan.kibble;

import java.util.HashMap;
import java.util.Map;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.rexster.extension.AbstractRexsterExtension;

import de.uniko.sebschlicht.titan.graphity.WriteOptimizedGraphity;
import de.uniko.sebschlicht.titan.socialnet.SocialGraph;

public abstract class GraphityKibble extends AbstractRexsterExtension {

    protected static final String KEY_RESPONSE_VALUE = "responseValue";

    private static Map<Graph, SocialGraph> SOCIAL_GRAPHS;
    static {
        SOCIAL_GRAPHS = new HashMap<Graph, SocialGraph>();
    }

    protected SocialGraph socialGraph;

    protected SocialGraph getSocialGraph(Graph graph) {
        if (socialGraph != null) {
            return socialGraph;
        }
        SocialGraph socialGraph = SOCIAL_GRAPHS.get(graph);
        if (socialGraph == null) {
            socialGraph = new WriteOptimizedGraphity(graph);
            SOCIAL_GRAPHS.put(graph, socialGraph);
        }
        return socialGraph;
    }
}
