package de.uniko.sebschlicht.titan.socialnet.model;

import com.tinkerpop.blueprints.Vertex;

import de.uniko.sebschlicht.titan.Walker;
import de.uniko.sebschlicht.titan.socialnet.EdgeType;

public class UserPostIterator implements PostIterator {

    protected UserProxy pUser;

    protected boolean _isInitialized;

    protected StatusUpdateProxy pCrrStatusUpdate;

    protected Vertex _vReplica;

    public UserPostIterator(
            UserProxy pUser) {
        this.pUser = pUser;
        _isInitialized = false;
    }

    public UserProxy getUser() {
        return pUser;
    }

    public void setReplicaVertex(Vertex vReplica) {
        _vReplica = vReplica;
    }

    public Vertex getReplicaVertex() {
        return _vReplica;
    }

    protected static StatusUpdateProxy getLastUserPost(UserProxy pUser) {
        Vertex vLastPost =
                Walker.nextVertex(pUser.getVertex(),
                        EdgeType.PUBLISHED.getLabel());
        if (vLastPost != null) {
            StatusUpdateProxy pStatusUpdate = new StatusUpdateProxy(vLastPost);
            pStatusUpdate.setAuthor(pUser);
            return pStatusUpdate;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        return (pCrrStatusUpdate != null);
    }

    @Override
    public StatusUpdateProxy next() {
        if (!_isInitialized) {
            pCrrStatusUpdate = getLastUserPost(pUser);
            _isInitialized = true;
        }
        StatusUpdateProxy pOldStatusUpdate = pCrrStatusUpdate;
        if (pOldStatusUpdate != null) {
            Vertex vNextStatusUpdate =
                    Walker.nextVertex(pOldStatusUpdate.getVertex(),
                            EdgeType.PUBLISHED.getLabel());
            if (vNextStatusUpdate != null) {
                pCrrStatusUpdate = new StatusUpdateProxy(vNextStatusUpdate);
                pCrrStatusUpdate.setAuthor(pUser);
            } else {
                pCrrStatusUpdate = null;
            }
        }
        return pOldStatusUpdate;
    }

    @Override
    public void remove() {
        if (hasNext()) {
            next();
        }
    }

    @Override
    public long getCrrPublished() {
        if (!_isInitialized) {
            return pUser.getLastPostTimestamp();
        } else if (hasNext()) {
            return pCrrStatusUpdate.getPublished();
        }
        return 0;
    }
}
