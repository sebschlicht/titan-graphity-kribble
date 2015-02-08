package de.uniko.sebschlicht.titan;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * graph walker for Titan
 * 
 * @author sebschlicht
 * 
 */
public abstract class Walker {

    /**
     * Walks along an edge type to the next vertex.
     * 
     * @param sourceVertex
     *            vertex to start from
     * @param edgeLabel
     *            label of the edge to walk along
     * @return next vertex the edge specified directs to<br>
     *         <b>null</b> - if the start vertex has no such edge directing out
     */
    public static Vertex nextVertex(Vertex sourceVertex, String edgeLabel) {
        for (Vertex destinationNode : sourceVertex.getVertices(Direction.OUT,
                edgeLabel)) {
            return destinationNode;
        }
        return null;
    }

    /**
     * Walks backwards along an edge type to the previous vertex.
     * 
     * @param sourceVertex
     *            vertex to start from
     * @param edgeLabel
     *            label of the edge to walk along
     * @return previous vertex the edge specified directs from<br>
     *         <b>null</b> - if the start vertex has no such edge directing in
     */
    public static Vertex previousVertex(Vertex sourceVertex, String edgeLabel) {
        for (Vertex destinationNode : sourceVertex.getVertices(Direction.IN,
                edgeLabel)) {
            return destinationNode;
        }
        return null;
    }

    /**
     * Removes the first edge matching the given criteria retrieved by
     * <i>getEdges</i>.
     * 
     * @param sourceVertex
     *            vertex to start from
     * @param direction
     *            direction the edge has for source vertex
     * @param edgeLabel
     *            label of the edge to remove
     */
    public static void removeSingleEdge(
            Vertex sourceVertex,
            Direction direction,
            String edgeLabel) {
        for (Edge edge : sourceVertex.getEdges(direction, edgeLabel)) {
            edge.remove();
            break;
        }
    }
}
