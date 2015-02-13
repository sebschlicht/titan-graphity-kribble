package de.uniko.sebschlicht.titan.extensions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.rexster.RexsterResourceContext;
import com.tinkerpop.rexster.extension.ExtensionDefinition;
import com.tinkerpop.rexster.extension.ExtensionDescriptor;
import com.tinkerpop.rexster.extension.ExtensionNaming;
import com.tinkerpop.rexster.extension.ExtensionPoint;
import com.tinkerpop.rexster.extension.ExtensionRequestParameter;
import com.tinkerpop.rexster.extension.ExtensionResponse;
import com.tinkerpop.rexster.extension.RexsterContext;

import de.uniko.sebschlicht.titan.graphity.TitanGraphity;
import de.uniko.sebschlicht.titan.socialnet.model.StatusUpdateProxy;
import de.uniko.sebschlicht.titan.socialnet.model.UserProxy;

@ExtensionNaming(
        namespace = GraphityExtension.EXT_NAMESPACE,
        name = BootstrapExtension.EXT_NAME)
public class BootstrapExtension extends GraphityExtension {

    protected static final String EXT_NAME = "bootstrap";

    protected static final int BOOTSTRAP_BLOCK_SIZE = 10000;

    protected static final Random RANDOM = new Random();

    protected static final char[] POST_SYMBOLS;
    static {
        StringBuilder postSymbols = new StringBuilder();
        // numbers
        for (char number = '0'; number <= '9'; ++number) {
            postSymbols.append(number);
        }
        // lower case letters
        for (char letter = 'a'; letter <= 'z'; ++letter) {
            postSymbols.append(letter);
        }
        // upper case letters
        for (char letter = 'a'; letter <= 'z'; ++letter) {
            postSymbols.append(Character.toUpperCase(letter));
        }
        POST_SYMBOLS = postSymbols.toString().toCharArray();
    }

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
                        name = "users",
                        description = "Identifiers of registered users.") JSONArray aRegistrations,
                @ExtensionRequestParameter(
                        name = "posts",
                        description = "Number of status updates accessible by its author's user identifier.") JSONObject oPosts) {
        try {
            TitanGraphity graphity =
                    getGraphityInstance(context, (TitanGraph) graph);
            TitanGraph graphDb = graphity.getGraph();

            int postLength = 140;

            // add vertices to graph
            Map<Long, Object> userVertices = new HashMap<Long, Object>();
            Map<Long, long[]> postsUsers = new HashMap<Long, long[]>();
            int numRegistrations = aRegistrations.length();
            long idUser;
            Vertex vUser;
            // add users
            for (int i = 0; i < numRegistrations; ++i) {
                idUser = aRegistrations.getLong(i);
                vUser = graphDb.addVertex(null);
                userVertices.put(idUser, vUser.getId());
            }
            Map<Long, Object> verticesPosts = new HashMap<Long, Object>();
            JSONArray aUserIds = oPosts.names();
            int numPoster = aUserIds.length();
            String sIdUser;
            Vertex vPost;
            StatusUpdateProxy pStatusUpdate;
            // add posts
            for (int i = 0; i < numPoster; ++i) {
                sIdUser = aUserIds.getString(i);
                idUser = Long.valueOf(sIdUser);
                int numUserPosts = oPosts.getInt(sIdUser);
                long[] postsUser = new long[numUserPosts];
                for (int iPost = 0; iPost < numUserPosts; ++iPost) {
                    vPost = graphDb.addVertex(null);
                    pStatusUpdate = new StatusUpdateProxy(vPost);
                    pStatusUpdate.initVertex(System.currentTimeMillis(),
                            generatePostMessage(postLength));
                    verticesPosts.put(pStatusUpdate.getIdentifier(),
                            vPost.getId());
                    postsUser[iPost] = pStatusUpdate.getIdentifier();
                }
                postsUsers.put(idUser, postsUser);
            }

            // add edges to graph
            UserProxy pAuthor;
            Object idVertex;
            for (int i = 0; i < numPoster; ++i) {
                idUser = aUserIds.getLong(i);
                idVertex = userVertices.get(idUser);
                vUser = graphDb.getVertex(idVertex);
                pAuthor = new UserProxy(vUser);
                long[] postsUser = postsUsers.get(idUser);
                for (long idPost : postsUser) {
                    idVertex = verticesPosts.get(idPost);
                    vPost = graphDb.getVertex(idVertex);
                    pStatusUpdate = new StatusUpdateProxy(vPost);
                    pAuthor.linkStatusUpdate(pStatusUpdate);
                }
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

    protected static String generatePostMessage(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            builder.append(getRandomPostChar());
        }
        return builder.toString();
    }

    protected static char getRandomPostChar() {
        return POST_SYMBOLS[RANDOM.nextInt(POST_SYMBOLS.length)];
    }
}
