package com.program.sim;

import com.program.App;
import com.program.objects.tile.*;

import javafx.geometry.Point3D;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static com.program.App.world;

public class TerrainSim extends Sim {

    public TerrainSim(double updateRate) {
        super(updateRate);
    }

    public void simulate() {

        Tile thisTile = world.FREE_TILES.dequeue();
        world.OCCUPIED_TILES.add(thisTile);
        world.ADJACENCY_GRAPH.setActive(thisTile); // Vertices are inactive by default

        // Shuffle the order of the adjacencies to make the algorithm more unpredictable
        Collections.shuffle(Arrays.asList(Tile.ADJACENCIES), new Random());

        // Adjacency logic
        label:
        for (int i = 0; i < Tile.ADJACENCIES.length; i++) {
            Point3D adjacent = thisTile.position.add(Tile.ADJACENCIES[i]); // Calculate the next adjacent position
            Point3D roundedAdjacent = roundPoint(adjacent); // Use a rounded position for equalities to avoid decimal precision errors

            // Generate a random type for the adjacent tile
            String adjacentType = world.PROBABILITIES[new Random().nextInt(world.PROBABILITIES.length)];
            if (thisTile.type.equals("river")) {
                adjacentType = "sand"; // Force sand tiles to spawn next to rivers
            }
            Tile roundedAdjacentTile = new Tile(roundedAdjacent, adjacentType);

            // Add the adjacently-positioned tile as an edge in the adjacency graph before any checks are done to see if it can be allocated in the free tiles array
            // This is to allow all six tiles to be added as adjacencies, regardless if they are free or not
            world.ADJACENCY_GRAPH.addVertex(roundedAdjacentTile);
            world.ADJACENCY_GRAPH.addEdge(thisTile, roundedAdjacentTile, 1.0, true); // Weights are unnecessary

            for (int j = 0; j < world.OCCUPIED_TILES.size(); j++) { // Check if this position is already occupied
                Point3D roundedOccupied = world.OCCUPIED_TILES.get(j).position;
                if (roundedOccupied.equals(roundedAdjacent)) {
                    continue label;
                }
            }
            for (int j = 0; j < world.FREE_TILES.size(); j++) { // Check if this position has already been freed
                Point3D roundedFree = world.FREE_TILES.get(j).position;
                if (roundedFree.equals(roundedAdjacent)) {
                    continue label;
                }
            }
            // If the adjacent tile is viable
            world.FREE_TILES.enqueue(roundedAdjacentTile);
        }

        App.renderTile(App.spatial, thisTile);
    }

    /**
     * Rounds each coordinate of a Point3D to two decimal places.
     * @param point The Point3D to be rounded.
     * @return A new Point3D with the rounded coordinates.
     */
    private Point3D roundPoint(Point3D point) {
        double roundedX = Math.round(point.getX() * 100.0) / 100.0;
        double roundedY = Math.round(point.getY() * 100.0) / 100.0;
        double roundedZ = Math.round(point.getZ() * 100.0) / 100.0;
        return new Point3D(roundedX, roundedY, roundedZ);
    }

}
