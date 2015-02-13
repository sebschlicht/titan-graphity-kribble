package de.uniko.sebschlicht.titan.extensions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.NotImplementedException;
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

import de.uniko.sebschlicht.titan.graphity.ReadOptimizedGraphity;
import de.uniko.sebschlicht.titan.graphity.TitanGraphity;
import de.uniko.sebschlicht.titan.graphity.WriteOptimizedGraphity;
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
                        name = "subscriptions",
                        description = "Identifiers of users subscribed to accessible by the subscriber's user identifier.") JSONObject oSubscriptions,
                @ExtensionRequestParameter(
                        name = "posts",
                        description = "Number of status updates accessible by its author's user identifier.") JSONObject oPosts) {
        try {
            TitanGraphity graphity =
                    getGraphityInstance(context, (TitanGraph) graph);
            TitanGraph graphDb = graphity.getGraph();

            int postLength = 140;

            // load user set
            Set<Long> userIds = new TreeSet<Long>();
            JSONArray aUserIds = oSubscriptions.names();
            int numSubscribers = aUserIds.length();
            String sIdUser;
            long idUser;
            int numUserSubscriptions;
            long idFollowed;
            for (int i = 0; i < numSubscribers; ++i) {
                // add subscriber
                sIdUser = aUserIds.getString(i);
                idUser = Long.valueOf(sIdUser);
                userIds.add(idUser);
                // add users followed
                JSONArray aUsersFollowed = oSubscriptions.getJSONArray(sIdUser);
                numUserSubscriptions = aUsersFollowed.length();
                long[] userSubscriptions = new long[numUserSubscriptions];
                for (int iFollowed = 0; i < numUserSubscriptions; ++iFollowed) {
                    idFollowed = aUsersFollowed.getLong(iFollowed);
                    userIds.add(idFollowed);
                    userSubscriptions[iFollowed] = idFollowed;
                }
            }

            // add vertices to graph
            // add users
            Map<Long, Object> userVertices = new HashMap<Long, Object>();
            Vertex vUser;
            for (long userId : userIds) {
                vUser = graphDb.addVertex(null);
                userVertices.put(userId, vUser.getId());
            }
            // add posts
            Map<Long, Object[]> postsVertices = new HashMap<Long, Object[]>();
            aUserIds = oPosts.names();
            int numPoster = aUserIds.length();
            Vertex vPost;
            StatusUpdateProxy pStatusUpdate;
            for (int i = 0; i < numPoster; ++i) {
                sIdUser = aUserIds.getString(i);
                idUser = Long.valueOf(sIdUser);
                int numUserPosts = oPosts.getInt(sIdUser);
                Object[] postVertices = new Object[numUserPosts];
                for (int iPost = 0; iPost < numUserPosts; ++iPost) {
                    vPost = graphDb.addVertex(null);
                    pStatusUpdate = new StatusUpdateProxy(vPost);
                    pStatusUpdate.initVertex(System.currentTimeMillis(),
                            generatePostMessage(postLength));
                    postVertices[iPost] = vPost.getId();
                }
                postsVertices.put(idUser, postVertices);
            }

            // add edges to graph
            UserProxy pAuthor;
            Object idVertex;
            // add post edges
            for (int i = 0; i < numPoster; ++i) {
                idUser = aUserIds.getLong(i);
                idVertex = userVertices.get(idUser);
                vUser = graphDb.getVertex(idVertex);
                pAuthor = new UserProxy(vUser);
                Object[] postVertices = postsVertices.get(idUser);
                for (Object idPostVertex : postVertices) {
                    vPost = graphDb.getVertex(idPostVertex);
                    pStatusUpdate = new StatusUpdateProxy(vPost);
                    pAuthor.linkStatusUpdate(pStatusUpdate);
                }
            }
            // add subscription edges
            aUserIds = oSubscriptions.names();
            Vertex vFollowed;
            for (int i = 0; i < numSubscribers; ++i) {
                sIdUser = aUserIds.getString(i);
                idUser = Long.valueOf(sIdUser);
                vUser = graphDb.getVertex(userVertices.get(idUser));
                pAuthor = new UserProxy(vUser);
                JSONArray aUsersFollowed = oSubscriptions.getJSONArray(sIdUser);
                numUserSubscriptions = aUsersFollowed.length();
                for (int iFollowed = 0; i < numUserSubscriptions; ++iFollowed) {
                    idUser = aUsersFollowed.getLong(iFollowed);
                    vFollowed = graphDb.getVertex(userVertices.get(idUser));
                    if (graphity instanceof WriteOptimizedGraphity) {
                        ((WriteOptimizedGraphity) graphity).doAddFollowship(
                                vUser, vFollowed);
                    } else if (graphity instanceof ReadOptimizedGraphity) {
                        throw new NotImplementedException(
                                "bootstrap not implemented for ReadOptimizedGraphity");
                    }
                }
            }

            // TODO build Graphity index if using ReadOptimizedGraphity

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
