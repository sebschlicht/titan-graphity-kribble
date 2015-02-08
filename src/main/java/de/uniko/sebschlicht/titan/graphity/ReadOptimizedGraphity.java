package de.uniko.sebschlicht.titan.graphity;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

import de.uniko.sebschlicht.socialnet.StatusUpdate;
import de.uniko.sebschlicht.socialnet.StatusUpdateList;
import de.uniko.sebschlicht.titan.Walker;
import de.uniko.sebschlicht.titan.socialnet.EdgeType;
import de.uniko.sebschlicht.titan.socialnet.model.StatusUpdateProxy;
import de.uniko.sebschlicht.titan.socialnet.model.UserProxy;

public class ReadOptimizedGraphity extends TitanGraphity {

    public ReadOptimizedGraphity(
            TitanGraph graphDb) {
        super(graphDb);
    }

    @Override
    protected boolean addFollowship(Vertex vFollowing, Vertex vFollowed) {
        // try to find the replica node of the user followed
        for (Vertex vFollowedReplica : vFollowing.getVertices(Direction.OUT,
                EdgeType.FOLLOWS.getLabel())) {
            if (Walker
                    .nextVertex(vFollowedReplica, EdgeType.REPLICA.getLabel())
                    .equals(vFollowed)) {
                // user is already following this user
                return false;
            }
        }

        // create replica
        final Vertex newReplica = graphDb.addVertex(null);
        vFollowing.addEdge(EdgeType.FOLLOWS.getLabel(), newReplica);
        newReplica.addEdge(EdgeType.REPLICA.getLabel(), vFollowed);
        // check if followed user is the first in following's ego network
        if (Walker.nextVertex(vFollowing, EdgeType.GRAPHITY.getLabel()) == null) {
            vFollowing.addEdge(EdgeType.GRAPHITY.getLabel(), newReplica);
        } else {
            // search for insertion index within following replica layer
            final long followedTimestamp = getLastUpdateByReplica(newReplica);
            long crrTimestamp;
            Vertex prevReplica = vFollowing;
            Vertex nextReplica = null;
            while (true) {
                // get next user
                nextReplica =
                        Walker.nextVertex(prevReplica,
                                EdgeType.GRAPHITY.getLabel());
                if (nextReplica != null) {
                    crrTimestamp = getLastUpdateByReplica(nextReplica);
                    // step on if current user has newer status updates
                    if (crrTimestamp > followedTimestamp) {
                        prevReplica = nextReplica;
                        continue;
                    }
                }
                // insertion position has been found
                break;
            }
            // insert followed user's replica into following's ego network
            if (nextReplica != null) {
                Walker.removeSingleEdge(prevReplica, Direction.OUT,
                        EdgeType.GRAPHITY.getLabel());
                newReplica.addEdge(EdgeType.GRAPHITY.getLabel(), nextReplica);
            }
            prevReplica.addEdge(EdgeType.GRAPHITY.getLabel(), newReplica);
        }
        return true;
    }

    /**
     * remove a followed user from the replica layer
     * 
     * @param followedReplica
     *            replica of the user that will be removed
     */
    private void removeFromReplicaLayer(final Vertex followedReplica) {
        final Vertex prev =
                Walker.previousVertex(followedReplica,
                        EdgeType.GRAPHITY.getLabel());
        final Vertex next =
                Walker.nextVertex(followedReplica, EdgeType.GRAPHITY.getLabel());
        // bridge the user replica in the replica layer
        Walker.removeSingleEdge(prev, Direction.OUT,
                EdgeType.GRAPHITY.getLabel());
        if (next != null) {
            Walker.removeSingleEdge(next, Direction.IN,
                    EdgeType.GRAPHITY.getLabel());
            prev.addEdge(EdgeType.GRAPHITY.getLabel(), next);
        }
        // remove the followship
        Walker.removeSingleEdge(followedReplica, Direction.IN,
                EdgeType.FOLLOWS.getLabel());
        // remove the replica node itself
        Walker.removeSingleEdge(followedReplica, Direction.OUT,
                EdgeType.REPLICA.getLabel());
        followedReplica.remove();
    }

    @Override
    protected boolean removeFollowship(Vertex vFollowing, Vertex vFollowed) {
        // find the replica node of the user followed
        Vertex vReplica = null;
        for (Vertex vFollowedReplica : vFollowing.getVertices(Direction.OUT,
                EdgeType.FOLLOWS.getLabel())) {
            if (Walker
                    .nextVertex(vFollowedReplica, EdgeType.REPLICA.getLabel())
                    .equals(vFollowed)) {
                vReplica = vFollowedReplica;
                break;
            }
        }
        // there is no such followship existing
        if (vReplica == null) {
            return false;
        }
        removeFromReplicaLayer(vReplica);
        return true;
    }

    /**
     * update the ego networks of a user's followers
     * 
     * @param user
     *            user where changes have occurred
     */
    private void updateEgoNetwork(final Vertex user) {
        Vertex followingUser, lastPosterReplica;
        Vertex prevReplica, nextReplica;
        // loop through followers
        for (Vertex followedReplica : user.getVertices(Direction.IN,
                EdgeType.REPLICA.getLabel())) {
            // load each replica and the user corresponding
            followingUser =
                    Walker.previousVertex(followedReplica,
                            EdgeType.FOLLOWS.getLabel());
            // bridge user node
            prevReplica =
                    Walker.previousVertex(followedReplica,
                            EdgeType.GRAPHITY.getLabel());
            if (!prevReplica.equals(followingUser)) {
                Walker.removeSingleEdge(followedReplica, Direction.IN,
                        EdgeType.GRAPHITY.getLabel());
                nextReplica =
                        Walker.nextVertex(followedReplica,
                                EdgeType.GRAPHITY.getLabel());
                if (nextReplica != null) {
                    Walker.removeSingleEdge(followedReplica, Direction.OUT,
                            EdgeType.GRAPHITY.getLabel());
                    prevReplica.addEdge(EdgeType.GRAPHITY.getLabel(),
                            nextReplica);
                }
            }
            // insert user's replica at its new position
            lastPosterReplica =
                    Walker.nextVertex(followingUser,
                            EdgeType.GRAPHITY.getLabel());
            if (!lastPosterReplica.equals(followedReplica)) {
                Walker.removeSingleEdge(followingUser, Direction.OUT,
                        EdgeType.GRAPHITY.getLabel());
                followingUser.addEdge(EdgeType.GRAPHITY.getLabel(),
                        followedReplica);
                followedReplica.addEdge(EdgeType.GRAPHITY.getLabel(),
                        lastPosterReplica);
            }
        }
    }

    @Override
    protected long addStatusUpdate(Vertex vAuthor, StatusUpdate statusUpdate) {
        // get last recent status update
        final Vertex lastUpdate =
                Walker.nextVertex(vAuthor, EdgeType.PUBLISHED.getLabel());

        // create new status update node and fill via proxy
        Vertex crrUpdate = graphDb.addVertex(null);
        StatusUpdateProxy pStatusUpdate = new StatusUpdateProxy(crrUpdate);
        //TODO handle service overload
        pStatusUpdate.init();
        UserProxy pAuthor = new UserProxy(vAuthor);
        pStatusUpdate.setAuthor(pAuthor);
        pStatusUpdate.setMessage(statusUpdate.getMessage());
        pStatusUpdate.setPublished(statusUpdate.getPublished());

        // update references to previous status update (if existing)
        if (lastUpdate != null) {
            Walker.removeSingleEdge(vAuthor, Direction.OUT,
                    EdgeType.PUBLISHED.getLabel());
            crrUpdate.addEdge(EdgeType.PUBLISHED.getLabel(), lastUpdate);
        }
        // add reference from user to current update node
        vAuthor.addEdge(EdgeType.PUBLISHED.getLabel(), crrUpdate);
        pAuthor.setLastPostTimestamp(statusUpdate.getPublished());

        // update ego networks of status update author followers
        updateEgoNetwork(vAuthor);

        return pStatusUpdate.getIdentifier();
    }

    @Override
    protected StatusUpdateList readStatusUpdates(
            Vertex vReader,
            int numStatusUpdates) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Retrieves the timestamp of the last recent status update of the user
     * specified.
     * 
     * @param userReplica
     *            replica of the user
     * @return timestamp of the user's last recent status update
     */
    private static long getLastUpdateByReplica(final Vertex userReplica) {
        final Vertex user =
                Walker.nextVertex(userReplica, EdgeType.REPLICA.getLabel());
        UserProxy pUser = new UserProxy(user);
        return pUser.getLastPostTimestamp();
    }
}
