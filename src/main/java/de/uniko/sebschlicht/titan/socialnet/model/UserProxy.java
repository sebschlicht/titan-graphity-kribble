package de.uniko.sebschlicht.titan.socialnet.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

import de.uniko.sebschlicht.titan.Walker;
import de.uniko.sebschlicht.titan.socialnet.EdgeType;

public class UserProxy extends SocialItemProxy {

    /**
     * timestamp of last user post
     */
    public static final String PROP_LAST_POST_TIMESTAMP = "last_post";

    /**
     * (optional) last recent status update posted by this user
     */
    protected StatusUpdateProxy lastPost;

    /**
     * (optional) timestamp of the last recent status update posted by this user
     */
    protected long _lastPostTimestamp;

    public UserProxy(
            Vertex vUser) {
        super(vUser);
        _lastPostTimestamp = -1;
    }

    /**
     * Adds a status update to the user.<br>
     * Links the status update vertex to the user vertex and to previous updates
     * if any.
     * Updates the author vertex's last post timestamp.
     * 
     * @param pStatusUpdate
     *            proxy of the new status update
     */
    public void addStatusUpdate(StatusUpdateProxy pStatusUpdate) {
        linkStatusUpdate(pStatusUpdate);
        // update last post timestamp
        setLastPostTimestamp(pStatusUpdate.getPublished());
    }

    /**
     * Links a status update vertex to the user vertex and to previous updates
     * if
     * any.
     * 
     * @param pStatusUpdate
     *            proxy of the status update
     */
    public void linkStatusUpdate(StatusUpdateProxy pStatusUpdate) {
        // get last recent status update
        Vertex lastUpdate =
                Walker.nextVertex(vertex, EdgeType.PUBLISHED.getLabel());
        // update references to previous status update (if existing)
        if (lastUpdate != null) {
            Walker.removeSingleEdge(vertex, Direction.OUT,
                    EdgeType.PUBLISHED.getLabel());
            pStatusUpdate.getVertex().addEdge(EdgeType.PUBLISHED.getLabel(),
                    lastUpdate);
        }
        // add reference from user to current update node
        vertex.addEdge(EdgeType.PUBLISHED.getLabel(), pStatusUpdate.getVertex());
    }

    public void setLastPostTimestamp(long lastPostTimestamp) {
        vertex.setProperty(PROP_LAST_POST_TIMESTAMP, lastPostTimestamp);
        _lastPostTimestamp = lastPostTimestamp;
    }

    public long getLastPostTimestamp() {
        if (_lastPostTimestamp == -1) {
            Long value = vertex.getProperty(PROP_LAST_POST_TIMESTAMP);
            _lastPostTimestamp = (value == null) ? 0L : value;
        }
        return _lastPostTimestamp;
    }
}
