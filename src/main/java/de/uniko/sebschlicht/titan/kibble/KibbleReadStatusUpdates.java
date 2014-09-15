package de.uniko.sebschlicht.titan.kibble;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
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
import de.uniko.sebschlicht.graphity.exception.UnknownReaderIdException;

public class KibbleReadStatusUpdates extends GraphityKibble {

    @ExtensionDefinition(
            extensionPoint = ExtensionPoint.GRAPH)
    @ExtensionDescriptor(
            description = "Adds a status update for an user.")
    public ExtensionResponse feeds(
            @RexsterContext RexsterResourceContext context,
            @RexsterContext TitanGraph graph,
            @ExtensionRequestParameter(
                    name = "reader",
                    description = "identifier of the reader") String idReader) {
        Graphity graphity = getGraphityInstance(graph);

        try {
            JSONObject jsonObject =
                    new JSONObject(graphity.readStatusUpdates(idReader, 15)
                            .toString());
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(KEY_RESPONSE_VALUE, jsonObject);
            return ExtensionResponse.ok(new JSONObject(map));
        } catch (UnknownReaderIdException | JSONException e) {
            return ExtensionResponse.error(e);
        }
    }
}
