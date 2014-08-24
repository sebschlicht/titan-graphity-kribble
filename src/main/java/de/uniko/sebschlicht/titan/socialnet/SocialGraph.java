package de.uniko.sebschlicht.titan.socialnet;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import de.uniko.sebschlicht.socialnet.SocialNetwork;
import de.uniko.sebschlicht.titan.socialnet.model.UserProxy;

/**
 * social graph that holds a social network and provides interaction
 * 
 * @author sebschlicht
 */
public abstract class SocialGraph implements SocialNetwork {

    /**
     * graph database holding the social network graph
     */
    protected Graph graphDb;

    /**
     * Creates a new social network graph instance using the database provided.
     * 
     * @param graphDb
     *            graph database holding the social network graph to operate on
     */
    public SocialGraph(
            Graph graphDb) {
        this.graphDb = graphDb;
    }

    /**
     * Initializes the social graph instance in order to access and manipulate
     * the social network graph.
     */
    public void init() {
        // can be overridden to create/load indices and similar startup actions
    }

    /**
     * Creates a user that can act in the social network.
     * 
     * @param userIdentifier
     *            identifier of the new user
     * @return user vertex - if the user was successfully created<br>
     *         <b>null</b> - if the identifier is already in use
     */
    public Vertex createUser(long userIdentifier) {
        Vertex vUser = graphDb.addVertex(null);
        vUser.setProperty(UserProxy.PROP_IDENTIFIER, userIdentifier);
        return vUser;
    }
}
