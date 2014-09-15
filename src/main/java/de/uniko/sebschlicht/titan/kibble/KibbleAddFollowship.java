package de.uniko.sebschlicht.titan.kibble;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.extension.ExtensionDefinition;
import com.tinkerpop.rexster.extension.ExtensionDescriptor;
import com.tinkerpop.rexster.extension.ExtensionNaming;
import com.tinkerpop.rexster.extension.ExtensionPoint;
import com.tinkerpop.rexster.extension.ExtensionRequestParameter;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.extension.RexsterContext;

import de.uniko.sebschlicht.graphity.exception.IllegalUserIdException;
import de.uniko.sebschlicht.titan.graphity.TitanGraphity;

@ExtensionNaming(
        namespace = "graphity",
        name = "follow")
public class KibbleAddFollowship extends GraphityKibble {

    @ExtensionDefinition(
            extensionPoint = ExtensionPoint.GRAPH)
    @ExtensionDescriptor(
            description = "Adds a followship between two users.")
    public
        ExtensionResponse
        follow(
                @RexsterContext RexsterResourceContext context,
                @RexsterContext TitanGraph graph,
                @ExtensionRequestParameter(
                        name = "following",
                        description = "identifier of the user following") String idFollowing,
                @ExtensionRequestParameter(
                        name = "followed",
                        description = "identifier of the user followed") String idFollowed) {
        TitanGraphity graphity = getGraphityInstance(graph);
        Map<String, String> map = new HashMap<String, String>();
        try {
            boolean followshipAdded =
                    graphity.addFollowship(idFollowing, idFollowed);
            map.put(KEY_RESPONSE_VALUE, String.valueOf(followshipAdded));
            return ExtensionResponse.ok(new JSONObject(map));
        } catch (IllegalUserIdException e) {
            return ExtensionResponse.error(e);
        }
    }
}
