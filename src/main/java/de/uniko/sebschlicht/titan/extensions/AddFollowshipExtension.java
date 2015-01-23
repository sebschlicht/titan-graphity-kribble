package de.uniko.sebschlicht.titan.extensions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.extension.ExtensionDefinition;
import com.tinkerpop.rexster.extension.ExtensionDescriptor;
import com.tinkerpop.rexster.extension.ExtensionNaming;
import com.tinkerpop.rexster.extension.ExtensionPoint;
import com.tinkerpop.rexster.extension.ExtensionRequestParameter;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.extension.RexsterContext;

import de.uniko.sebschlicht.graphity.Graphity;
import de.uniko.sebschlicht.graphity.exception.IllegalUserIdException;

@ExtensionNaming(
        namespace = GraphityExtension.EXT_NAMESPACE,
        name = "follow")
public class AddFollowshipExtension extends GraphityExtension {

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
                        name = "following",
                        description = "identifier of the user following") String idFollowing,
                @ExtensionRequestParameter(
                        name = "followed",
                        description = "identifier of the user followed") String idFollowed) {
        Graphity graphity = getGraphityInstance((TitanGraph) graph);
        Map<String, String> map = new HashMap<String, String>();

        int numRetries = 0;
        try {
            //TODO: continue to retry until time x has passed
            do {
                try {
                    boolean followshipAdded =
                            graphity.addFollowship(idFollowing, idFollowed);
                    map.put(KEY_RESPONSE_VALUE, String.valueOf(followshipAdded));
                    return ExtensionResponse.ok(new JSONObject(map));
                } catch (IllegalUserIdException e) {
                    return ExtensionResponse.error(e);
                } catch (Exception e) {
                    if (numRetries++ >= NUM_MAX_RETRIES) {
                        throw e;
                    }
                }
            } while (true);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return ExtensionResponse.error(sw.toString());
        }
    }
}
