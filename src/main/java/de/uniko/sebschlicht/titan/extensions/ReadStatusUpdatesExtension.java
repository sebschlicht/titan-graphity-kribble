package de.uniko.sebschlicht.titan.extensions;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
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
import de.uniko.sebschlicht.graphity.exception.UnknownReaderIdException;
import de.uniko.sebschlicht.socialnet.StatusUpdateList;

@ExtensionNaming(
        namespace = GraphityExtension.EXT_NAMESPACE,
        name = "feeds")
public class ReadStatusUpdatesExtension extends GraphityExtension {

    @ExtensionDefinition(
            extensionPoint = ExtensionPoint.GRAPH)
    @ExtensionDescriptor(
            description = "Adds a status update for an user.")
    public ExtensionResponse feeds(
            @RexsterContext RexsterResourceContext context,
            @RexsterContext Graph graph,
            @ExtensionRequestParameter(
                    name = "reader",
                    description = "identifier of the reader") String idReader) {
        Graphity graphity = getGraphityInstance((TitanGraph) graph);

        try {
            StatusUpdateList statusUpdates =
                    graphity.readStatusUpdates(idReader, 15);
            JSONArray jaStatusUpdates = new JSONArray(statusUpdates.getList());
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(KEY_RESPONSE_VALUE, jaStatusUpdates);
            return ExtensionResponse.ok(new JSONObject(map));
        } catch (UnknownReaderIdException e) {
            return ExtensionResponse.error(e);
        }
    }
}
