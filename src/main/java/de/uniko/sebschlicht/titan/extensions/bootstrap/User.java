package de.uniko.sebschlicht.titan.extensions.bootstrap;

public class User {

    private Object _vertexId;

    private User[] _subscriptions;

    private long _tsLastPost;

    private Object[] _postVertexIds;

    public User() {
        _tsLastPost = 0;
    }

    public void setSubscriptions(User[] subscriptions) {
        _subscriptions = subscriptions;
    }

    public User[] getSubscriptions() {
        return _subscriptions;
    }

    public void setVertexId(Object vertexId) {
        _vertexId = vertexId;
    }

    public Object getVertexId() {
        return _vertexId;
    }

    public void setStatusUpdates(Object[] postVertexIds) {
        _postVertexIds = postVertexIds;
    }

    public Object[] getStatusUpdates() {
        return _postVertexIds;
    }

    public void setLastPostTimestamp(long tsLastPost) {
        _tsLastPost = tsLastPost;
    }

    public long getLastPostTimestamp() {
        return _tsLastPost;
    }
}
