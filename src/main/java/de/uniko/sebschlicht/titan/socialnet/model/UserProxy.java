package de.uniko.sebschlicht.titan.socialnet.model;

import com.tinkerpop.blueprints.Vertex;

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
