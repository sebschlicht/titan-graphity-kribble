package de.uniko.sebschlicht.titan.graphity;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import de.uniko.sebschlicht.socialnet.StatusUpdate;
import de.uniko.sebschlicht.socialnet.StatusUpdateList;
import de.uniko.sebschlicht.titan.socialnet.SocialGraph;
import de.uniko.sebschlicht.titan.socialnet.model.UserProxy;

/**
 * social graph for Graphity implementations
 * 
 * @author sebschlicht
 * 
 */
public abstract class Graphity extends SocialGraph {

    /**
     * Creates a new Graphity instance using the database provided.
     * 
     * @param graphDb
     *            graph database holding any Graphity social network graph to
     *            operate on
     */
    public Graphity(
            Graph graphDb) {
        super(graphDb);
    }

    @Override
    public void init() {
        //TODO create user identifier index if not existing
    }

    /**
     * Searches the social network graph for an user.
     * 
     * @param userIdentifier
     *            identifier of the user searched
     * @return user node - if the user is existing in social network graph<br>
     *         <b>null</b> - if there is no vertex representing the user
     *         specified
     */
    protected Vertex findUser(long userIdentifier) {
        for (Vertex vUser : graphDb.getVertices(UserProxy.PROP_IDENTIFIER,
                userIdentifier)) {
            return vUser;
        }
        return null;
    }

    /**
     * Loads a user from social network or lazily creates a new one.
     * 
     * @param userIdentifier
     *            identifier of the user to interact with
     * @return user vertex - existing or created vertex representing the user
     */
    protected Vertex loadUser(long userIdentifier) {
        Vertex vUser = findUser(userIdentifier);
        if (vUser != null) {
            // user is already existing
            return vUser;
        }
        return createUser(userIdentifier);
    }

    @Override
    public boolean addUser(String sUserIdentifier) {
        Long userIdentifier = Long.valueOf(sUserIdentifier);

        Vertex vUser = findUser(userIdentifier);
        if (vUser == null) {
            // user identifier not in use yet
            createUser(userIdentifier);
            return true;
        }
        return false;
    }

    @Override
    public boolean addFollowship(String sIdFollowing, String sIdFollowed) {
        //TODO transaction handling and locking, then call addFollowship with vertices resolved
        try {
            long idFollowing = Long.valueOf(sIdFollowing);
            long idFollowed = Long.valueOf(sIdFollowed);
            if (idFollowing <= 0 || idFollowed <= 0) {
                throw new IllegalArgumentException(
                        "node ids must be greater than zero");
            }
            Vertex vFollowing = loadUser(idFollowing);
            Vertex vFollowed = loadUser(idFollowed);
            return addFollowship(vFollowing, vFollowed);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Adds a followship between two user vertices to the social network graph.
     * 
     * @param vFollowing
     *            vertex of the user that wants to follow another user
     * @param vFollowed
     *            vertex of the user that will be followed
     * @return true - if the followship was successfully created<br>
     *         false - if this followship is already existing
     */
    abstract protected boolean
        addFollowship(Vertex vFollowing, Vertex vFollowed);

    @Override
    public boolean removeFollowship(String idFollowing, String idFollowed) {
        //TODO transaction handling and locking, then call removeFollowship with vertices resolved
        //TODO throw UnknownFollowxxxIdException if resolve fails
        return false;
    }

    /**
     * Removes a followship between two user vertices from the social network
     * graph.
     * 
     * @param vFollowing
     *            vertex of the user that wants to unfollow a user
     * @param vFollowed
     *            vertex of the user that will be unfollowed
     * @return true - if the followship was successfully removed<br>
     *         false - if this followship is not existing
     */
    abstract protected boolean removeFollowship(
            Vertex vFollowing,
            Vertex vFollowed);

    @Override
    public String addStatusUpdate(String idAuthor, String message) {
        //TODO transaction handling and locking, then call addStatusUpdate with vertex resolved
        return null;
    }

    /**
     * Adds a status update vertex to the social network.
     * 
     * @param vAuthor
     *            user vertex of the status update author
     * @param statusUpdate
     *            status update data
     * @return identifier of the status update vertex
     */
    abstract protected long addStatusUpdate(
            Vertex vAuthor,
            StatusUpdate statusUpdate);

    @Override
    public StatusUpdateList readStatusUpdates(
            String idReader,
            int numStatusUpdates) {
        //TODO transaction handling, then call readStatusUpdates with vertex resolved
        return null;
    }

    abstract protected StatusUpdateList readStatusUpdates(
            Vertex vReader,
            int numStatusUpdates);
}
