package de.uniko.sebschlicht.titan.graphity;

import java.util.TreeSet;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import de.uniko.sebschlicht.socialnet.StatusUpdate;
import de.uniko.sebschlicht.socialnet.StatusUpdateList;
import de.uniko.sebschlicht.titan.socialnet.EdgeType;
import de.uniko.sebschlicht.titan.socialnet.model.PostIteratorComparator;
import de.uniko.sebschlicht.titan.socialnet.model.StatusUpdateProxy;
import de.uniko.sebschlicht.titan.socialnet.model.UserPostIterator;
import de.uniko.sebschlicht.titan.socialnet.model.UserProxy;

public class WriteOptimizedGraphity extends TitanGraphity {

    public WriteOptimizedGraphity(
            TitanGraph graphDb) {
        super(graphDb);
    }

    @Override
    public boolean addFollowship(Vertex vFollowing, Vertex vFollowed) {
        // try to find the vertex of the user followed
        for (Vertex vIsFollowed : vFollowing.getVertices(Direction.OUT,
                EdgeType.FOLLOWS.getLabel())) {
            if (vIsFollowed.equals(vFollowed)) {
                return false;
            }
        }

        doAddFollowship(vFollowing, vFollowed);
        return true;
    }

    public void doAddFollowship(Vertex vFollowing, Vertex vFollowed) {
        // create star topology
        vFollowing.addEdge(EdgeType.FOLLOWS.getLabel(), vFollowed);
    }

    @Override
    public boolean removeFollowship(Vertex vFollowing, Vertex vFollowed) {
        // delete the followship if existing
        Edge followship = null;
        for (Edge follows : vFollowing.getEdges(Direction.OUT,
                EdgeType.FOLLOWS.getLabel())) {
            if (follows.getVertex(Direction.IN).equals(vFollowed)) {
                followship = follows;
                break;
            }
        }

        // there is no such followship existing
        if (followship == null) {
            return false;
        }

        followship.remove();
        return true;
    }

    @Override
    protected long addStatusUpdate(Vertex vAuthor, StatusUpdate statusUpdate) {
        // create new status update vertex and fill via proxy
        Vertex crrUpdate = graphDb.addVertex(null);
        StatusUpdateProxy pStatusUpdate = new StatusUpdateProxy(crrUpdate);
        //TODO handle service overload
        pStatusUpdate.initVertex(statusUpdate.getPublished(),
                statusUpdate.getMessage());

        // add status update to user (link vertex, update user)
        UserProxy pAuthor = new UserProxy(vAuthor);
        pAuthor.addStatusUpdate(pStatusUpdate);

        return pStatusUpdate.getIdentifier();
    }

    @Override
    protected StatusUpdateList readStatusUpdates(
            Vertex vReader,
            int numStatusUpdates) {
        StatusUpdateList statusUpdates = new StatusUpdateList();
        if (vReader == null) {
            return statusUpdates;
        }
        final TreeSet<UserPostIterator> postIterators =
                new TreeSet<UserPostIterator>(new PostIteratorComparator());

        // loop through users followed
        UserProxy pCrrUser;
        UserPostIterator postIterator;
        for (Vertex vFollowed : vReader.getVertices(Direction.OUT,
                EdgeType.FOLLOWS.getLabel())) {
            // add post iterator
            pCrrUser = new UserProxy(vFollowed);
            postIterator = new UserPostIterator(pCrrUser);

            if (postIterator.hasNext()) {
                postIterators.add(postIterator);
            }
        }

        // handle queue
        while ((statusUpdates.size() < numStatusUpdates)
                && !postIterators.isEmpty()) {
            // add last recent status update
            postIterator = postIterators.pollLast();
            statusUpdates.add(postIterator.next().getStatusUpdate());

            // re-add iterator if not empty
            if (postIterator.hasNext()) {
                postIterators.add(postIterator);
            }
        }
        return statusUpdates;
    }
}
