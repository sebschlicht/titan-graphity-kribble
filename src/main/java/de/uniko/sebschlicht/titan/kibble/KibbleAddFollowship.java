package de.uniko.sebschlicht.titan.kibble;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.extension.ExtensionDefinition;
import com.tinkerpop.rexster.extension.ExtensionDescriptor;
import com.tinkerpop.rexster.extension.ExtensionNaming;
import com.tinkerpop.rexster.extension.ExtensionPoint;
import com.tinkerpop.rexster.extension.ExtensionRequestParameter;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.extension.RexsterContext;

import de.uniko.sebschlicht.titan.socialnet.SocialGraph;

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
                @RexsterContext Graph graph,
                @ExtensionRequestParameter(
                        name = "idFollowing",
                        description = "identifier of the user following") String idFollowing,
                @ExtensionRequestParameter(
                        name = "idFollowed",
                        description = "identifier of the user followed") String idFollowed) {
        SocialGraph socialGraph = getSocialGraph(graph);
        Map<String, String> map = new HashMap<String, String>();
        map.put(KEY_RESPONSE_VALUE, String.valueOf(socialGraph.addFollowship(
                idFollowing, idFollowed)));
        return ExtensionResponse.ok(new JSONObject(map));
    }
}
