package de.uniko.sebschlicht.titan.extensions;

import java.io.PrintWriter;
import java.io.StringWriter;
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
import de.uniko.sebschlicht.socialnet.StatusUpdate;
import de.uniko.sebschlicht.socialnet.StatusUpdateList;

@ExtensionNaming(
        namespace = GraphityExtension.EXT_NAMESPACE,
        name = ReadStatusUpdatesExtension.EXT_NAME)
public class ReadStatusUpdatesExtension extends GraphityExtension {

    protected static final String EXT_NAME = "feeds";

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
        try {
            Graphity graphity =
                    getGraphityInstance(context, (TitanGraph) graph);

            try {
                StatusUpdateList statusUpdates =
                        graphity.readStatusUpdates(idReader, 15);
                JSONArray jaStatusUpdates = new JSONArray();
                for (StatusUpdate statusUpdate : statusUpdates.getList()) {
                    jaStatusUpdates.put(statusUpdate.getMap());
                }
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(KEY_RESPONSE_VALUE, jaStatusUpdates);
                return ExtensionResponse.ok(new JSONObject(map));
            } catch (UnknownReaderIdException e) {
                //TODO: implement client to catch this because it is no error in benchmark context
                return ExtensionResponse.error(e.toString());
            }
        } catch (Exception e) {
            LOG.error(e);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return ExtensionResponse.error(sw.toString());
        }
    }
}
