package de.uniko.sebschlicht.titan.socialnet.model;

import com.tinkerpop.blueprints.Vertex;

public class UserProxy extends SocialItemProxy {

    /**
     * timestamp of last stream update
     */
    public static final String PROP_LAST_STREAM_UDPATE = "stream_update";

    /**
     * last recent status update posted by this user
     */
    protected StatusUpdateProxy lastPost;

    public UserProxy(
            Vertex vUser) {
        super(vUser);
    }
}
