package de.uniko.sebschlicht.titan.kibble;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.extension.ExtensionDefinition;
import com.tinkerpop.rexster.extension.ExtensionDescriptor;
import com.tinkerpop.rexster.extension.ExtensionPoint;
import com.tinkerpop.rexster.extension.ExtensionRequestParameter;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.extension.RexsterContext;

import de.uniko.sebschlicht.graphity.Graphity;
import de.uniko.sebschlicht.graphity.exception.UnknownFollowedIdException;
import de.uniko.sebschlicht.graphity.exception.UnknownFollowingIdException;

public class KibbleRemoveFollowship extends GraphityKibble {

    @ExtensionDefinition(
            extensionPoint = ExtensionPoint.GRAPH)
    @ExtensionDescriptor(
            description = "Removes a followship between two users.")
    public
        ExtensionResponse
        unfollow(
                @RexsterContext RexsterResourceContext content,
                @RexsterContext TitanGraph graph,
                @ExtensionRequestParameter(
                        name = "following",
                        description = "identifier of the user following") String idFollowing,
                @ExtensionRequestParameter(
                        name = "followed",
                        description = "identifier of the user followed") String idFollowed) {
        Graphity graphity = getGraphityInstance(graph);
        Map<String, String> map = new HashMap<String, String>();
        try {
            map.put(KEY_RESPONSE_VALUE, String.valueOf(graphity
                    .removeFollowship(idFollowing, idFollowed)));
            return ExtensionResponse.ok(new JSONObject(map));
        } catch (UnknownFollowingIdException | UnknownFollowedIdException e) {
            return ExtensionResponse.error(e);
        }
    }
}
