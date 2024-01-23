package com.program;

import com.program.util.Queue;
import com.program.util.TileGraph;
import com.program.util.ArrayList;

import com.program.objects.tile.*;

import javafx.geometry.Point3D;

public class World {

    // The values in populations and yields correspond with the order of tile_types
    public final String[] TILE_TYPES = {"grass", "forest", "river", "village"};
    public final double[] POPULATIONS = {0.0, 0.0, 0.0, 0.0};
    public final double[] YIELDS = {0.0, 0.0, 0.0};
    // Ten entries to mimic a percentage probability for each tile type
    public final String[] PROBABILITIES = {"grass", "grass", "grass", "grass", "grass", "forest", "forest", "river", "river", "village"};

    public final Queue<Tile> FREE_TILES = new Queue<>();
    public final ArrayList<Tile> OCCUPIED_TILES = new ArrayList<>();
    public final TileGraph ADJACENCY_GRAPH = new TileGraph();

    public World() {
        // Center the starting grass tile
        Tile startTile = new Tile(new Point3D(App.WINDOW_WIDTH / 2, App.WINDOW_HEIGHT / 2, 0), "grass");
        FREE_TILES.enqueue(startTile);
        ADJACENCY_GRAPH.addVertex(startTile);
    }
}
