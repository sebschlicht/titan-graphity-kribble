package de.uniko.sebschlicht.titan.extensions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;

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

import de.uniko.sebschlicht.socialnet.requests.RequestType;
import de.uniko.sebschlicht.titan.graphity.TitanGraphity;

@ExtensionNaming(
        namespace = GraphityExtension.EXT_NAMESPACE,
        name = BootstrapExtension.EXT_NAME)
public class BootstrapExtension extends GraphityExtension {

    protected static final String EXT_NAME = "bootstrap";

    protected static final int BOOTSTRAP_BLOCK_SIZE = 10000;

    @ExtensionDefinition(
            extensionPoint = ExtensionPoint.GRAPH)
    @ExtensionDescriptor(
            description = "Bootstrap a large number of Graphity's write requests.")
    public
        ExtensionResponse
        bootstrap(
                @RexsterContext RexsterResourceContext context,
                @RexsterContext Graph graph,
                @ExtensionRequestParameter(
                        name = "entries",
                        description = "Concatenation of the String array representations of the requests that will be bootstrapped.") String sEntries) {
        try {
            // parse String to JSON array and convert to String array
            JSONArray aEntries = new JSONArray(sEntries);
            String[] entries = new String[aEntries.length()];
            for (int i = 0; i < entries.length; ++i) {
                entries[i] = aEntries.getString(i);
            }

            TitanGraphity graphity =
                    getGraphityInstance(context, (TitanGraph) graph);
            TitanGraph graphDb = graphity.getGraph();
            int numPendingRequests = 0;
            String sIdFollow = String.valueOf(RequestType.FOLLOW.getId());
            String sIdPost = String.valueOf(RequestType.POST.getId());
            String sIdUnfollow = String.valueOf(RequestType.UNFOLLOW.getId());
            String sId;
            for (int i = 0; i < entries.length;) {
                sId = entries[i++];
                if (sIdFollow.equals(sId)) {
                    graphity.addFollowship(entries[i++], entries[i++], false);
                } else if (sIdPost.equals(sId)) {
                    graphity.addStatusUpdate(entries[i++], entries[i++], false);
                } else if (sIdUnfollow.equals(sId)) {
                    graphity.removeFollowship(entries[i++], entries[i++], false);
                } else {
                    throw new IllegalStateException();
                }
                numPendingRequests += 1;
                if (numPendingRequests > BOOTSTRAP_BLOCK_SIZE) {
                    graphDb.commit();
                    numPendingRequests = 0;
                }
            }
            if (numPendingRequests > 0) {
                graphDb.commit();
            }

            Map<String, String> map = new HashMap<String, String>();
            map.put(KEY_RESPONSE_VALUE, "true");
            return ExtensionResponse.ok(map);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return ExtensionResponse.error(sw.toString());
        }
    }
}
