package com.program.sim;

import com.program.objects.tile.Tile;
import com.program.util.ArrayList;

import static com.program.App.world;

public class GrowthSim extends Sim {

    // The following variables can be tweaked to the user's content. ALl changes will be reflected in the simulation
    // the next time the program is run.
    final double grassGrowthPercentChance = 50;
    final double forestGrowthPercentChance = 50;
    final double riverGrowthPercentChance = 40;
    final double villageGrowthPercentChance = 40;

    final double grassPrecipitationPercentChance = 20;
    final double grassDesertificationPercentChange = 10;
    final double forestPrecipitationPercentChance = 20;
    final double forestExpansionPercentChance = 30;
    final double riverInundationPercentChance = 20;
    final double riverEvaporationPercentChance = 10;
    final double villageExpansionPercentChance = 20;
    final double villageAbandonmentPercentChance = 30;
    final double rubbleSedimentationPercentChance = 10;

    // Toggles for the experimental features. Be aware, this may lead to impractical results.
    public static boolean enableEvaporationAndDesertification = false;
    public static boolean enablePrecipitation = false;

    // Toggle for debug messages in console (e.g. status updates for mutations)
    final boolean enableDebug = false;

    public GrowthSim(double updateRate) {
        super(updateRate);
    }

    public void simulate() {
        nextTile:
        for (int i = 0; i < world.OCCUPIED_TILES.size(); i++) {
            Tile thisTile = world.OCCUPIED_TILES.get(i);
            // These are declared here as they are used by most of the tile types to check what resources are near.
            boolean nextToGrass = false;
            boolean nextToForest = false;
            boolean nextToRiver = false;
            boolean nextToSand = false;

            // Calculate all the adjacent tiles to this tile
            ArrayList<Tile> adjacentTiles = world.ADJACENCY_GRAPH.returnAdjacentActiveTiles(thisTile);

            if (thisTile.type.equals("grass")) {
                // Get the necessary adjacency information
                int adjacentImmatureTiles = 0;
                for (int j = 0; j < adjacentTiles.size(); j++) {
                    Tile thisAdjacentTile = adjacentTiles.get(j);
                    if (thisAdjacentTile.type.equals("river")) {
                        nextToRiver = true;
                    }
                    else if (!thisAdjacentTile.isFullyMatured()) {
                        adjacentImmatureTiles++;
                    }
                }
                // If the grass tile is completely surrounded by immature tiles, simulate a precipitation effect.
                // This is to stimulate growth in barren fields that would never grow otherwise as they are far from rivers
                if (adjacentImmatureTiles == 6 && enablePrecipitation) {
                    if (tryMutateTile(thisTile, "river", 1, grassPrecipitationPercentChance)) {
                        break; // Break so the new river cannot do any more actions this tick (such as grow)
                    }
                }
                else if (nextToRiver) {
                    tryGrowTile(thisTile, grassGrowthPercentChance);
                }
                else { // If the grass was initially around water to the point of maturing, but that water evaporated, the grass will dry up as well turning into a desert
                    if (thisTile.getStage() > 1 && enableEvaporationAndDesertification) {
                        tryMutateTile(thisTile, "sand", 1, grassDesertificationPercentChange);
                        // Don't need to break out of this block since it is the last thing executed
                    }
                }
            }

            else if (thisTile.type.equals("forest")) {
                // Get the necessary adjacency information
                int adjacentImmatureTiles = 0;
                for (int j = 0; j < adjacentTiles.size(); j++) {
                    Tile thisAdjacentTile = adjacentTiles.get(j);
                    if (thisAdjacentTile.type.equals("river")) {
                        nextToRiver = true;
                    }
                    else if (!thisAdjacentTile.isFullyMatured()) {
                        adjacentImmatureTiles++;
                    }
                }
                // Expand forests over grass if they are fully matured
                if (thisTile.isFullyMatured()) { // Not worth checking if it's next to grass for expansion since grass is abundant
                    for (int j = 0; j < adjacentTiles.size(); j++) {
                        Tile thisAdjacentTile = adjacentTiles.get(j);
                        // Don't expand over fully matured grass tiles, because then forest would be too invasive
                        if (thisAdjacentTile.type.equals("grass") && !thisAdjacentTile.isFullyMatured()) {
                            if (tryMutateTile(thisAdjacentTile, "forest", 2, forestExpansionPercentChance)) {
                                break nextTile; // Break so the forest cannot do any more actions this tick (such as grow)
                            }
                        }
                    }
                }
                // If the grass tile is completely surrounded by immature tiles, simulate a precipitation effect.
                // This is to stimulate growth in barren fields that would never grow otherwise as they are far from rivers
                if (adjacentImmatureTiles == 6 && enablePrecipitation) {
                    if (tryMutateTile(thisTile, "river", 1, forestPrecipitationPercentChance)) {
                        break; // Break so the new river cannot do any more actions this tick (such as grow)
                    }
                }
                else if (nextToRiver) {
                    tryGrowTile(thisTile, forestGrowthPercentChance);
                }
            }

            else if (thisTile.type.equals("river")) {
                // Get the necessary adjacency information
                for (int j = 0; j < adjacentTiles.size(); j++) {
                    Tile thisAdjacentTile = adjacentTiles.get(j);
                    if (thisAdjacentTile.type.equals("sand")) {
                        nextToSand = true;
                    }
                    else if (thisAdjacentTile.type.equals("river")) {
                        nextToRiver = true;
                        if (thisAdjacentTile.getStage() >= 2) {
                            // Set all river tiles to be facing the same way for their waves to line up
                            thisTile.setRotation(thisAdjacentTile.getRotation());
                        }
                    }
                }
                // Expand rivers over sand tiles if they are next to another river and fully matured
                if (thisTile.isFullyMatured() && nextToRiver && nextToSand) {
                    for (int k = 0; k < adjacentTiles.size(); k++) {
                        Tile thisAdjacentTile = adjacentTiles.get(k);
                        if (thisAdjacentTile.type.equals("sand")) {
                            if (tryMutateTile(thisAdjacentTile, "river", 1, riverInundationPercentChance)) {
                                break nextTile; // Break so the new river cannot do any more actions this tick (such as grow)
                            }
                        }
                    }
                }
                else if (nextToRiver) {
                    tryGrowTile(thisTile, riverGrowthPercentChance);
                }
                else { // If the river is on its own, it's treated as a small pond and dries up
                    // Check if the river is completely surrounded by tiles. This is so we don't evaporate rivers that have just spawned in
                    // Also check if the river is next to a sand tile. This is so we don't evaporate rivers that are the result of precipitation.
                    if (adjacentTiles.size() == 6 && nextToSand && enableEvaporationAndDesertification) {
                        tryMutateTile(thisTile, "sand", 1, riverEvaporationPercentChance);
                    }
                }
            }

            else if (thisTile.type.equals("village")) {
                // Get the necessary adjacency information
                int adjacentGrassTiles = 0;
                int adjacentForestTiles = 0;
                for (int j = 0; j < adjacentTiles.size(); j++) {
                    Tile thisAdjacentTile = adjacentTiles.get(j);
                    if (thisAdjacentTile.type.equals("grass")) {
                        nextToGrass = true;
                        adjacentGrassTiles++;
                    }
                    else if (thisAdjacentTile.type.equals("forest")) {
                        nextToForest = true;
                        adjacentForestTiles++;
                    }
                    else if (thisAdjacentTile.type.equals("river")) {
                        nextToRiver = true;
                    }
                }
                // Expand villages over grass or forest tiles if they are fully matured
                if (thisTile.isFullyMatured() && (nextToGrass || nextToForest) && nextToRiver) {
                    for (int j = 0; j < adjacentTiles.size(); j++) {
                        Tile thisAdjacentTile = adjacentTiles.get(j);
                        // Take over grass and forest tiles only, and don't take over tiles that were previously village tiles and became abandoned

                        // Before expanding over grass, we check if the tile is also next to a forest. This will ensure that the village
                        // can still survive after the expansion as it does not become deprived of resources. The village can also
                        // be next to more than one grass tile, in which case we can also go ahead with the expansion.
                        if (thisAdjacentTile.type.equals("grass") && !thisAdjacentTile.isAbandoned() && (nextToForest || adjacentGrassTiles > 1)) {
                            if (tryMutateTile(thisAdjacentTile, "village", 2, villageExpansionPercentChance)) {
                                break nextTile; // Break so the village cannot do any more actions this tick (such as grow)
                            }
                        }
                        // Before expanding over forest, we check if the tile is also next to a grass. This will ensure that the village
                        // can still survive after the expansion as it does not become deprived of resources. The village can also
                        // be next to more than one forest tile, in which case we can also go ahead with the expansion.
                        else if (thisAdjacentTile.type.equals("forest") && !thisAdjacentTile.isAbandoned() && (nextToGrass || adjacentForestTiles > 1)) {
                            if (tryMutateTile(thisAdjacentTile, "village", 2, villageExpansionPercentChance)) {
                                break nextTile; // Break so the village cannot do any more actions this tick (such as grow)
                            }
                        }
                        // These checks will not prevent a village expanding too far to a tile that will not have the available resources
                        // to survive. It will also not prevent a village tile sabotaging another village tile by expanding over its
                        // single needed resource.
                    }
                }
                else if ((nextToGrass || nextToForest) && nextToRiver) { // Grow villages if they are next to a river and a grass or forest tile
                    tryGrowTile(thisTile, villageGrowthPercentChance);
                }
                else { // If the village is not near enough resources, it will die off (can't expand or grow)
                    // Check if the village is completely surrounded by tiles. This is so we don't abandon villages that have just spawned in
                    // If the TerrainSim is paused you may notice villages existing on the edges that are not growing nor abandoning, this is because of the above mechanic
                    if (adjacentTiles.size() == 6) {
                        if (tryMutateTile(thisTile, "rubble", 1, villageAbandonmentPercentChance)) {
                            thisTile.markAbandoned();
                        }
                    }
                }
            }

            else if (thisTile.type.equals("rubble")) {
                tryGrowTile(thisTile, rubbleSedimentationPercentChance); // "Sediment" the rubble tile
                // When the sediment fully deposits, revert the tile back to its first type (either grass or forest)
                if (thisTile.isFullyMatured()) {
                    if (thisTile.firstType.equals("village")) { // If it was always a village, revert it to a grass tile
                        tryMutateTile(thisTile, "grass", 1, grassGrowthPercentChance);
                    }
                    else {
                        tryMutateTile(thisTile, thisTile.firstType, 1, rubbleSedimentationPercentChance);
                    }
                }
            }
        }
    }

    /**
     * @param tile The tile that is attempting to grow.
     * @return Returns true if the growth occurred, otherwise returns false.
     */
    private boolean tryGrowTile(Tile tile, double chance) {
        if (!tile.isFullyMatured() && rollPercentChance(chance)) {
            tile.mature();
            return true;
        }
        return false;
    }

    /**
     * @param tile The tile that is attempting to mutate.
     * @param type The type of the new tile.
     * @param stage The stage the new tile will start at.
     * @param chance The percent chance the mutation will occur.
     * @return Returns true if the mutation occurred, otherwise returns false.
     */
    private boolean tryMutateTile(Tile tile, String type, int stage, double chance) {
        if (rollPercentChance(chance)) {
            if (enableDebug) {
                System.out.println(chance + "% chance rolled for " + tile);
                System.out.println(tile + " is now " + type + ".");
            }
            tile.type = type;
            tile.setStageAndRerender(stage); // Set the new grass tile to start in the first stage
            return true;
        }
        return false;
    }

    private boolean rollPercentChance(double chance) {
        if (chance >= 0 && chance <= 100) {
            return Math.random() < (chance / 100.0);
        }
        return false;
    }
}
