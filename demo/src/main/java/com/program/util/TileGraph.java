/* Name: Andrew Manga
 ** Date: June 14, 2023
 ** Class: ICS4U1 - J. Radulovic
 ** Assignment: Culminating
 ** Purpose: Create a JavaFX program to simulate and illustrate the changes in population in an ecosystem.
 */

package com.program.util;

import com.program.objects.tile.Tile;

import java.util.Objects;

public class TileGraph {

    public class Edge {
        Vertex destination;
        double weight;

        public Edge(Vertex destination, double weight) {
            this.destination = destination;
            this.weight = weight;
        }
    }

    public class Vertex {
        Tile label;
        boolean inactive; // Used for adding inactive tiles to the graph when a tile's adjacencies are calculated, but before they become occupied
        ArrayList<Edge> edges;

        public Vertex(Tile label) {
            this.label = label;
            inactive = true;
            edges = new ArrayList<Edge>();
        }

        @Override
        public boolean equals(Object o) { // Return true only if labels are equal
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vertex vertex = (Vertex) o;
            return Objects.equals(label, vertex.label);
        }
    }
    
    private int order = 0;
    private int size = 0;
    private ArrayList<Vertex> vertices = new ArrayList<Vertex>();

    /**
     * @return The number of vertices in the graph.
     */
    public int order() {
        return order;
    }

    /**
     * @return The number of edges in the graph.
     */
    public int size() {
        return size;
    }

    /**
     * @param sourceLabel The label that the vertex contains.
     * @return The number of edges incident with the vertex.
     */
    public int degree(Tile sourceLabel) {
        int indexOfSource = getIndexOfVertex(sourceLabel);
        return (vertices.get(indexOfSource).edges.size());
    }

    /**
     * @return A string representation of each vertex alongside all of its connected vertices.
     */
    public String toString() {
        String result = "";
        for (int i = 0; i < vertices.size(); i++) {
            result += "\"" + vertices.get(i).label + "\", Inactive = " + vertices.get(i).inactive + " -> [";
            for (int j = 0; j < vertices.get(i).edges.size(); j++) {
                Edge e = vertices.get(i).edges.get(j);
                result += e.destination.label + " (" + e.weight + ")";
                if (j != vertices.get(i).edges.size() - 1) // Don't add a comma after the last element
                    result += ", "; 
            }
            result += "]\n";
        }
        return result;
    }

    /**
     * @return A copy of the vertices list.
     */
    public ArrayList<Vertex> getVertices() {
        return (ArrayList<Vertex>) vertices.clone();
    }

    /**
     * Sets a vertex to be inactive. In a TileGraph, this signifies the tile's position exists in space but the tile's mesh and other properties are not activated yet.
     * @param label The label that the vertex contains.
     */
    public void setInactive(Tile label) {
        vertices.get(getIndexOfVertex(label)).inactive = true;
    }

    /**
     * Sets a vertex to be active. In a TileGraph, this signifies the tile exists and its mesh is also activated in the scene.
     * @param label The label that the vertex contains.
     */
    public void setActive(Tile label) {
        vertices.get(getIndexOfVertex(label)).inactive = false;
    }

    /**
     * Adds a new vertex to the graph.
     * @param label The label that the new vertex will contain.
     */
    public void addVertex(Tile label) {
        if (getIndexOfVertex(label) == -1) { // Check if the vertex does not already exist in the graph
            vertices.add(new Vertex(label));
            order++;
        }
    }

    /**
     * Searches through the vertices array for a vertex that contains the same label.
     * @param label The label that the vertex contains.
     * @return The index of the vertex in the vertices array if found, otherwise returns -1.
     */
    public int getIndexOfVertex(Tile label) {
        return vertices.indexOf(new Vertex(label));
    }

    /**
     * Draws an edge from a source to a destination with a given weight.
     * @param sourceLabel The label of the vertex that the edge will begin from.
     * @param destinationLabel The label of the vertex that the edge will point to.
     * @param weight The weight of the edge.
     * @param bidirectional If an edge should also be generated from the destination to the source.
     */
    public void addEdge(Tile sourceLabel, Tile destinationLabel, double weight, boolean bidirectional) {
        int indexOfSource = getIndexOfVertex(sourceLabel);
        int indexOfDestination = getIndexOfVertex(destinationLabel);
    
        if (indexOfSource != -1 && indexOfDestination != -1) { // Check if the vertices exist in the graph
            Vertex sourceVertex = vertices.get(indexOfSource);
            Vertex destinationVertex = vertices.get(indexOfDestination);
            Edge newEdge = new Edge(destinationVertex, weight);
            boolean existing = false;
            for (int i = 0; i < sourceVertex.edges.size(); i++) { // Check if the edge already exists in the source vertex's edge list
                if (sourceVertex.edges.get(i).destination.label.equals(destinationLabel)) {
                    existing = true;
                    break;
                }
            }
            if (!existing) {
                sourceVertex.edges.add(newEdge);
                size++;
            }

            if (bidirectional) { // Add an edge going the other way
                newEdge = new Edge(sourceVertex, weight);
                existing = false;
                for (int i = 0; i < destinationVertex.edges.size(); i++) {
                    if (destinationVertex.edges.get(i).destination.label.equals(sourceLabel)) {
                        existing = true;
                        break;
                    }
                }
                if (!existing) {
                    destinationVertex.edges.add(newEdge);
                }
            }
        }
    }

    /**
     * @param label1 The label of the first vertex.
     * @param label2 The label of the second vertex.
     * @return True if there exists an edge between the first and second vertices, otherwise returns false.
     */
    public boolean areAdjacent(Tile label1, Tile label2) {
        int indexOfSource = getIndexOfVertex(label1); // Search if the source label exists
        if (indexOfSource != -1) { // If the source label does exist
            Vertex sourceVertex = vertices.get(indexOfSource);
            for (int i = 0; i < sourceVertex.edges.size(); i++) {
                if (sourceVertex.edges.get(i).destination.label.equals(label2))
                    return true;
            }
        }
        areAdjacent(label2, label1); // Check the other way
        return false;
    }

    /**
     * @param sourceLabel The label that the vertex contains.
     * @return An ArrayList of Tiles that are adjacent to the source and active.
     */
    public ArrayList<Tile> returnAdjacentActiveTiles(Tile sourceLabel) {
        ArrayList<Tile> adjacentTiles = new ArrayList<>();
        int indexOfVertex = getIndexOfVertex(sourceLabel); // Search if the label exists
        if (indexOfVertex != -1) { // If the label does exist
            Vertex sourceVertex = vertices.get(indexOfVertex);
            for (int i = 0; i < sourceVertex.edges.size(); i++) {
                if (!sourceVertex.edges.get(i).destination.inactive) { // If the tile is not inactive
                    adjacentTiles.add(sourceVertex.edges.get(i).destination.label);
                }
            }
        }
        return adjacentTiles;
    }
}
