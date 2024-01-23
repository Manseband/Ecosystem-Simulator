package com.program.sim;

import com.program.objects.tile.Tile;

import java.util.Arrays;

import static com.program.App.world;

public class PopSim extends Sim {

    public PopSim(double updateRate) {
        super(updateRate);
    }

    public void simulate() {
        Arrays.fill(world.POPULATIONS, 0.0); // Reset the population counters
        for (int i = 0; i < world.OCCUPIED_TILES.size(); i++) {
            Tile thisTile = world.OCCUPIED_TILES.get(i);
            // Sand and rubble tiles do not have a population
            if (!thisTile.type.equals("sand") && !thisTile.type.equals("rubble")) {
                world.POPULATIONS[Arrays.asList(world.TILE_TYPES).indexOf(thisTile.type)] += thisTile.getPopulation();
            }
        }

        Arrays.fill(world.YIELDS, 0.0); // Reset the yield counters
        for (int i = 0; i < world.OCCUPIED_TILES.size(); i++) {
            Tile thisTile = world.OCCUPIED_TILES.get(i);
            // Village tiles do not yield anything
            if (!thisTile.type.equals("village") && !thisTile.type.equals("sand") && !thisTile.type.equals("rubble")) {
                world.YIELDS[Arrays.asList(world.TILE_TYPES).indexOf(thisTile.type)] += thisTile.getYield();
            }
        }
    }
}
