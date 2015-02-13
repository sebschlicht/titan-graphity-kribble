package de.uniko.sebschlicht.titan.extensions.bootstrap;

public class User {

    private Object _vertexId;

    private long[] _subscriptions;

    private long _tsLastPost;

    private Object[] _postVertexIds;

    public User() {
        _tsLastPost = 0;
    }

    public void setSubscriptions(long[] subscriptions) {
        _subscriptions = subscriptions;
    }

    public long[] getSubscriptions() {
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
