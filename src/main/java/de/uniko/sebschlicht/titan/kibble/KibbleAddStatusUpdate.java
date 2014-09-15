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
import de.uniko.sebschlicht.graphity.exception.IllegalUserIdException;

public class KibbleAddStatusUpdate extends GraphityKibble {

    @ExtensionDefinition(
            extensionPoint = ExtensionPoint.GRAPH)
    @ExtensionDescriptor(
            description = "Adds a status update for an user.")
    public
        ExtensionResponse
        post(
                @RexsterContext RexsterResourceContext context,
                @RexsterContext TitanGraph graph,
                @ExtensionRequestParameter(
                        name = "author",
                        description = "identifier of the status update author") String idAuthor,
                @ExtensionRequestParameter(
                        name = "message",
                        description = "status update content") String message) {
        Graphity graphity = getGraphityInstance(graph);
        Map<String, String> map = new HashMap<String, String>();
        try {
            long idStatusUpdate = graphity.addStatusUpdate(idAuthor, message);
            map.put(KEY_RESPONSE_VALUE, String.valueOf(idStatusUpdate));
            return ExtensionResponse.ok(new JSONObject(map));
        } catch (IllegalUserIdException e) {
            return ExtensionResponse.error(e);
        }
    }
}
