package com.program.objects.tile;

import java.util.*;

import com.program.App;
import javafx.geometry.Point3D;
import javafx.scene.control.Tooltip;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

import static com.program.util.ObjLoader.loadObj;

public class Tile {

    public final Point3D position; // Center of the tile
    public String type;
    public PhongMaterial material;
    public Shape3D mesh;

    public final static double RADIUS = 50; // Distance from the center to a vertex
    public final static double APOTHEM = Math.sqrt(3)/2 * RADIUS; // Distance from the center to the midpoint of an edge
    // All six possible vectors that can be added from an existing tile's center to create an adjacent tile
    public final static Point3D[] ADJACENCIES = {new Point3D(APOTHEM, -1.5*RADIUS, 0), new Point3D(2*APOTHEM, 0, 0),
    new Point3D(APOTHEM, 1.5*RADIUS, 0), new Point3D(-APOTHEM, 1.5*RADIUS, 0),
    new Point3D(-2*APOTHEM, 0, 0), new Point3D(-APOTHEM, -1.5*RADIUS, 0)};

    private final Map<String, Color> MAT_COLORS = new HashMap<>();
    private final int[] ROTATIONS = {30, 90, 150, 210, 270, 330};
    private final Rotate ROTATE = new Rotate();

    private boolean fullyMatured = false;
    private int stage = 1;
    private final int MAX_STAGE = 3;

    // Record the adjacent tile's first type so that it can be reverted to, in the case of a village tile becoming abandoned.
    public final String firstType;
    // If a village tile is not next to enough resources, it will become abandoned.
    // The tile can still support growth afterward, but cannot be taken over again.
    private boolean abandoned = false;

    // Encapsulated so that they are only changed when a tile grows through an internal method
    private double population = 1.0;
    private double yield = 2.0;

    public Tile(Point3D pos, String type) {

        MAT_COLORS.put("grass", Color.LIGHTGREEN);
        MAT_COLORS.put("forest", Color.DARKGREEN);
        MAT_COLORS.put("river", Color.DEEPSKYBLUE);
        MAT_COLORS.put("sand", Color.CORNSILK);
        MAT_COLORS.put("village", Color.SADDLEBROWN);
        MAT_COLORS.put("rubble", Color.DARKGREY);

        position = pos;
        this.type = type;
        firstType = type;

        // Set a random rotate for each tile for variety
        ROTATE.setAngle(ROTATIONS[new Random().nextInt(ROTATIONS.length)]);
        ROTATE.setAxis(Rotate.Z_AXIS);

        initMesh();

        mesh.setOnMouseClicked(e -> {
            PickResult pr = e.getPickResult();
            System.out.println("Tile clicked: " + this);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return Objects.equals(position, tile.position);
    }

    @Override
    public String toString() {
        return "Tile [" + position.getX() + ", " + position.getY() + ", " + position.getZ() + "] -> " + type;
    }

    /**
     * Initializes the mesh and all of its properties.
     * This is done automatically in the constructor but can be called again to rerender.
     */
    public void initMesh() {
        mesh = loadObj("demo/src/main/java/com/program/assets/objects/" + type + stage + ".obj");
        material = new PhongMaterial(MAT_COLORS.get(type));
        mesh.setMaterial(material);
        mesh.setTranslateX(position.getX()); mesh.setTranslateY(position.getY()); mesh.setTranslateZ(position.getZ());
        if (type.equals("forest") && stage == 3) {
            // To accommodate for the tree geometry that is not covered by the tile underneath
            ROTATE.setPivotX(-0.437223);
        }
        else { ROTATE.setPivotX(0); }
        mesh.getTransforms().add(ROTATE);
    }

    /**
     * Matures the tile by one stage.
     */
    public void mature() {
        setStageAndRerender(stage + 1);
    }

    /**
     * Forces the tile to be set to a certain stage without needing to mature it.
     * @param stage The stage the tile should be set to.
     */
    public void setStageAndRerender(int stage) {
        if (stage >= 1 && stage <= MAX_STAGE) {
            this.stage = stage;
            fullyMatured = false;
            // Make the tile produce resources relative to its growth stage
            population = 1.0 * stage;
            yield = 2.0 * stage;
            rerender();
            if (stage == MAX_STAGE) {
                fullyMatured = true;
            }
        }
    }

    /**
     * Rerenders the tile's mesh so that any changes to it can be reflected on-screen.
     */
    private void rerender() {
        App.unrenderTile(App.spatial, this);
        initMesh();
        App.renderTile(App.spatial, this);
    }

    public boolean isFullyMatured() { return fullyMatured; }

    public int getStage() {return stage; }

    /**
     * Sets the field abandoned to true.
     */
    public void markAbandoned() { abandoned = true; }

    public boolean isAbandoned() { return abandoned; }

    public double getPopulation() { return population; }

    public double getYield() { return yield; }

    public double getRotation() { return ROTATE.getAngle(); }

    public void setRotation(double angle) { ROTATE.setAngle(angle); }
}
