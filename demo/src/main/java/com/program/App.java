package com.program;

import com.program.objects.tile.Tile;
import com.program.sim.PopSim;

import com.program.sim.GrowthSim;
import com.program.sim.TerrainSim;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.chart.*;
import javafx.scene.paint.Color;
import javafx.geometry.Orientation;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class App extends Application {

    public static final double WINDOW_WIDTH = 1920;
    public static final double WINDOW_HEIGHT = 1080;
    private static final double SCROLLPANE_WIDTH = 200;
    private static final double TOOLBAR_HEIGHT = 50;
    private static final double SCROLL_SPEED = 0.01;

    public static Group spatial = new Group(); // Contains all the 3D elements
    private static SubScene subscene; // Contains the spatial group and a skybox

    public static World world = new World();
    private int graphTick = 0;
    private final int MAX_GRAPH_TICK = 10;
    private final XYChart.Series<Number, Number>[] SERIES // Contains the collective data points of all the graphs
            = new XYChart.Series[world.POPULATIONS.length + world.YIELDS.length];

    private final double DAY_LENGTH = 5.0 * 60000; // Multiplying by 60000 to convert from minutes to millis
    private String period = "day";

    private double lastX, lastY;
    private double zoom = 1.0;
    private final double ZOOM_SPEED = 0.2;
    private final double MIN_ZOOM = 0.5;
    private final double MAX_ZOOM = 3;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {

        boolean is3DSupported = Platform.isSupported(ConditionalFeature.SCENE3D);
        if(!is3DSupported) {
           System.out.println("Sorry, 3D is not supported in JavaFX on this platform.");
           return;
        }

        // Simulator Setup
        final TerrainSim tsim = new TerrainSim(2.0);
        final PopSim psim = new PopSim(2.0);
        final GrowthSim gsim = new GrowthSim(4.0);

        final Timeline grapher = new Timeline(new KeyFrame(Duration.seconds(psim.getUpdateRate()), (t) -> plotPoints()));
        grapher.setCycleCount(Timeline.INDEFINITE);

        // 3D Setup
        ParallelCamera pcam = new ParallelCamera(); // Orthographic projection
        pcam.setTranslateZ(-500);
        pcam.setRotationAxis(Rotate.X_AXIS); pcam.setRotate(55); // Higher values bring it closer to the ground
        pcam.setFarClip(6000);
        pcam.setNearClip(0.01);

        boolean isPOV = true;
        PerspectiveCamera fcam = new PerspectiveCamera(isPOV); // Perspective projection
        Translate pos = new Translate(WINDOW_WIDTH / 2 - Tile.RADIUS, WINDOW_HEIGHT + Tile.RADIUS, -500);
        final Rotate tilt = new Rotate(60, Rotate.X_AXIS); // Rotate controlling the x-axis tilt
        Rotate spin = new Rotate(0, Rotate.Z_AXIS); // Rotate controlling the z-axis rotation, affected by movement
        fcam.getTransforms().addAll(pos, spin, tilt);
        fcam.setFieldOfView(50); // Any higher than this is nauseating
        fcam.setFarClip(6000);
        fcam.setNearClip(0.01);

        subscene = new SubScene(spatial, (WINDOW_WIDTH - SCROLLPANE_WIDTH), (WINDOW_HEIGHT - TOOLBAR_HEIGHT), true, SceneAntialiasing.BALANCED);
        subscene.setCamera(pcam);
        final Color dayColor = Color.LIGHTSKYBLUE;
        final Color nightColor = Color.MIDNIGHTBLUE.darker();
        final Color[] backgroundColor = {Color.LIGHTSKYBLUE};
        final double clockTickRate = 100.0; // In millis
        final double[] dt = {0.0};
        subscene.setFill(backgroundColor[0]);

        final Timeline clock = new Timeline(new KeyFrame(Duration.millis(clockTickRate), (t) -> {
            if (period.equals("day")) {
                backgroundColor[0] = dayColor.interpolate(nightColor, dt[0]);
            } if (period.equals("night")) {
                backgroundColor[0] = nightColor.interpolate(dayColor, dt[0]);
            }
            // Add a fraction of the DAY_LENGTH according to how fast the clock is ticking
            dt[0] += clockTickRate / DAY_LENGTH;
            subscene.setFill(backgroundColor[0]);
            if (dt[0] >= 1) { // Reset dt when the interpolation is finished a full cycle
                dt[0] = 0;
                period = period.equals("day") ? "night" : "day"; // Ternary operator swaps the values "day" and "night"
            }
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        // Camera movement
        subscene.setOnMousePressed(event -> {
            lastX = event.getSceneX();
            lastY = event.getSceneY();
        });
        subscene.setOnMouseDragged(event -> {
            if (subscene.getCamera().equals(fcam)) { // If the free cam is the active camera
                if (event.isPrimaryButtonDown()) { // On left-click
                    double currentX = event.getSceneX();
                    double currentY = event.getSceneY();

                    double deltaX = lastX - currentX;
                    double deltaY = lastY - currentY;
                    deltaX = Math.min(deltaX, 5); // Cap the panning speed to 5
                    deltaY = Math.min(deltaY, 5);
                    pos.setX(pos.getX() + deltaX);
                    pos.setY(pos.getY() + deltaY);

                    spin.setAngle(0); // Reset the spin angle once the camera begins to move again

                    fcam.getTransforms().clear();
                    fcam.getTransforms().addAll(pos, spin, tilt);

                    lastX = event.getSceneX();
                    lastY = event.getSceneY();
                }
                if (event.isSecondaryButtonDown()) { // On right-click
                    double currentX = event.getSceneX();

                    double deltaX = (lastX - currentX) / 10;
                    deltaX = Math.min(deltaX, 5); // Cap the rotation speed to 5
                    spin.setAngle(spin.getAngle() + deltaX);
                    if (spin.getAngle() < 0) // Prevent unnecessary angles
                        spin.setAngle(360.0 + spin.getAngle());
                    if (spin.getAngle() > 360)
                        spin.setAngle(spin.getAngle() - 360.0);

                    fcam.getTransforms().clear();
                    fcam.getTransforms().addAll(pos, spin, tilt);

                    lastX = event.getSceneX();
                }
            }
        });
        subscene.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            // Only allow zooming when in free cam
            if (subscene.getCamera().equals(fcam)) {
                if (deltaY < 0 && zoom > MIN_ZOOM) { // Scroll down
                    zoom -= ZOOM_SPEED;
                }
                if (deltaY > 0 && zoom < MAX_ZOOM) { // Scroll up
                    zoom += ZOOM_SPEED;
                }
                spatial.setScaleX(zoom); spatial.setScaleY(zoom); spatial.setScaleZ(zoom); // Scale the 3D elements accordingly
            }
        });
        subscene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) { // When spacebar is pressed
                // Teleport the camera back to the starting position
                pos.setX(WINDOW_WIDTH / 2 - Tile.RADIUS);
                pos.setY(WINDOW_HEIGHT + Tile.RADIUS);
                pos.setZ(-500);
                // Reset the rotation
                spin.setAngle(0.0);
                fcam.getTransforms().clear();
                fcam.getTransforms().addAll(pos, spin, tilt);
                // Reset the scaling of the 3D elements
                zoom = 1.0;
                spatial.setScaleX(zoom); spatial.setScaleY(zoom); spatial.setScaleZ(zoom);
            }
        });

        // 2D Setup
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        borderPane.setCenter(subscene);

        // Graph Panel Setup
        Label chickenLabel = new Label("Chicken Population");
        Label foxLabel = new Label("Fox Population");
        Label fishLabel = new Label("Fish Population");
        Label humanLabel = new Label("Human Population");
        Label pastureLabel = new Label("Pasture Yield");
        Label timberLabel = new Label("Timber Yield");
        Label waterLabel = new Label("Water Yield");
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(10, 0, 10, 0)); // Top, right, bottom, left
        vbox.setSpacing(5);
        vbox.getChildren().addAll(chickenLabel, foxLabel, fishLabel, humanLabel);
        vbox.getChildren().addAll(pastureLabel, timberLabel, waterLabel);

        for (int i = 0; i < vbox.getChildren().size(); i++) { // Shrink the fonts of all the labels
            ((Label) vbox.getChildren().get(i)).setFont(new Font("Amble CN", 16));
        }

        ScrollPane graphPane = new ScrollPane();
        graphPane.setPrefWidth(SCROLLPANE_WIDTH);
        graphPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        graphPane.setContent(vbox);
        graphPane.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SCROLL_SPEED;
            graphPane.setVvalue(graphPane.getVvalue() - deltaY);
        });
        borderPane.setRight(graphPane);

        // Initialize the graphs
        initGraphs(vbox);

        // Controls Panel Setup
        ComboBox<String> simSelector = new ComboBox<>();
        simSelector.getItems().addAll("All Sims", "TerrainSim", "GrowthSim");
        simSelector.setValue("All Sims");
        Button playButton, pauseButton, slowDownButton, speedUpButton;
        playButton = new Button();
        pauseButton = new Button();
        speedUpButton = new Button();
        slowDownButton = new Button();

        ImageView playImg = new ImageView(new Image("file:demo/src/main/java/com/program/assets/icons/play.png"));
        playImg.setFitHeight(20); playImg.setFitWidth(20);
        playButton.setGraphic(playImg);
        playButton.setOnAction(e -> {
            switch (simSelector.getValue()) {
                case "All Sims":
                    tsim.play(); gsim.play(); psim.play(); grapher.play();
                    System.out.println("All Sims played.");
                    break;
                case "TerrainSim":
                    tsim.play();
                    psim.play(); grapher.play();
                    System.out.println("TerrainSim played.");
                    break;
                case "GrowthSim":
                    gsim.play();
                    psim.play(); grapher.play();
                    System.out.println("GrowthSim played.");
            }
            focusSubscene();
        });
        ImageView pauseImg = new ImageView(new Image("file:demo/src/main/java/com/program/assets/icons/pause.png"));
        pauseImg.setFitHeight(20); pauseImg.setFitWidth(20);
        pauseButton.setGraphic(pauseImg);
        pauseButton.setOnAction(e -> {
            switch (simSelector.getValue()) {
                case "All Sims":
                    tsim.pause(); gsim.pause(); psim.pause(); grapher.pause();
                    System.out.println("All Sims paused.");
                    break;
                case "TerrainSim":
                    tsim.pause();
                    // If gsim was also paused, pause the others since no changes can be made to the populations/yields
                    if (gsim.isPaused()) {
                        psim.pause(); grapher.pause();
                    }
                    System.out.println("TerrainSim paused.");
                    break;
                case "GrowthSim":
                    gsim.pause();
                    if (tsim.isPaused()) {
                        psim.pause(); grapher.pause();
                    }
                    System.out.println("GrowthSim paused.");
            }
            focusSubscene();
        });
        ImageView slowDownImg = new ImageView(new Image("file:demo/src/main/java/com/program/assets/icons/left_arrows.png"));
        slowDownImg.setFitHeight(20); slowDownImg.setFitWidth(20);
        slowDownButton.setGraphic(slowDownImg);
        slowDownButton.setOnAction(e -> {
            switch (simSelector.getValue()) {
                case "All Sims":
                    tsim.halveSpeed(); gsim.halveSpeed();
                    System.out.println("All Sims slowed down.");
                    break;
                case "TerrainSim":
                    tsim.halveSpeed();
                    System.out.println("TerrainSim slowed down to " + tsim.getRate() + " seconds/tick.");
                    break;
                case "GrowthSim":
                    gsim.halveSpeed();
                    System.out.println("GrowthSim slowed down to " + gsim.getRate() + " seconds/tick.");
            }
            focusSubscene();
        });
        ImageView speedUpImg = new ImageView(new Image("file:demo/src/main/java/com/program/assets/icons/right_arrows.png"));
        speedUpImg.setFitHeight(20); speedUpImg.setFitWidth(20);
        speedUpButton.setGraphic(speedUpImg);
        speedUpButton.setOnAction(e -> {
            switch (simSelector.getValue()) {
                case "All Sims":
                    tsim.doubleSpeed(); gsim.doubleSpeed();
                    System.out.println("All Sims sped up.");
                    break;
                case "TerrainSim":
                    tsim.doubleSpeed();
                    System.out.println("TerrainSim sped up to " + tsim.getRate() + " seconds/tick.");
                    break;
                case "GrowthSim":
                    gsim.doubleSpeed();
                    System.out.println("GrowthSim sped up to " + gsim.getRate() + " seconds/tick.");
            }
            focusSubscene();
        });
        Button fcamButton = new Button("Free Cam");
        fcamButton.setOnAction(e -> {
            subscene.setCamera(fcam);
            spatial.setScaleX(zoom); spatial.setScaleY(zoom); spatial.setScaleZ(zoom);
            focusSubscene();
        });
        Button pcamButton = new Button("Orthographic Cam");
        pcamButton.setOnAction(e -> {
            subscene.setCamera(pcam);
            spatial.setScaleX(1); spatial.setScaleY(1); spatial.setScaleZ(1); // Reset the zoom
            focusSubscene();
        });
        Button resetButton = new Button("Reset World");
        resetButton.setOnAction(e -> {
            tsim.pause(); gsim.pause(); psim.pause(); grapher.pause();
            world = new World();
            spatial.getChildren().clear();
            initGraphs(vbox);
            tsim.simulate(); // Add the starting tile
            focusSubscene();
        });
        Button collapseButton = new Button("Collapse Graphs");
        collapseButton.setPrefWidth(105); // So the width remains constant when the text changes to "Show Graphs"
        collapseButton.setOnAction(e -> {
            graphPane.setVisible(!graphPane.isVisible());
            subscene.setWidth(graphPane.isVisible() ? WINDOW_WIDTH - SCROLLPANE_WIDTH : WINDOW_WIDTH);
            collapseButton.setText(graphPane.isVisible() ? "Collapse Graphs": "Show Graphs");
            focusSubscene();
        });
        MenuButton featureSelector = new MenuButton("Enable Features");
        featureSelector.setOnHiding(e -> focusSubscene()); // This will not work if the MenuButton is closed by clicking on its text (JavaFX bug)

        CheckBox feature1Box = new CheckBox("Evaporation & Desertification");
        feature1Box.setOnAction(e -> {
            // This switch trick can be used here because the state begins in false which corresponds to the unchecked checkbox
            GrowthSim.enableEvaporationAndDesertification = !GrowthSim.enableEvaporationAndDesertification;
        });
        CustomMenuItem feature1Item = new CustomMenuItem(feature1Box);
        feature1Item.setHideOnClick(false); // Don't close the feature selector when this item is clicked

        CheckBox feature2Box = new CheckBox("Precipitation");
        feature2Box.setOnAction(e -> {
            GrowthSim.enablePrecipitation = !GrowthSim.enablePrecipitation;
        });
        CustomMenuItem feature2Item = new CustomMenuItem(feature2Box);
        feature2Item.setHideOnClick(false);

        featureSelector.getItems().addAll(feature1Item, feature2Item);

        HBox separator = new HBox();
        separator.setPrefWidth(420); // Add some space between the speed control area and the rest of the buttons
        HBox separator2 = new HBox();
        separator2.setPrefWidth(675); // Add some more space until the feature selector
        ToolBar toolbar = new ToolBar(simSelector, playButton, pauseButton, slowDownButton, speedUpButton,
                resetButton, separator, fcamButton, pcamButton, featureSelector, separator2, collapseButton);
        toolbar.setOrientation(Orientation.HORIZONTAL);
        toolbar.setMinHeight(TOOLBAR_HEIGHT);
        borderPane.setTop(toolbar);

        Scene scene = new Scene(borderPane, WINDOW_WIDTH, WINDOW_HEIGHT, true);
        stage.setScene(scene);
        stage.setTitle("Ecosystem Simulator");
        stage.setResizable(false);
        stage.show();

        tsim.simulate(); // Add the starting tile
    }

    public static void renderTile(Group spatial, Tile tile) {
        spatial.getChildren().add(tile.mesh);
    }

    public static void unrenderTile(Group spatial, Tile tile) {
        spatial.getChildren().remove(tile.mesh);
    }

    private void initGraphs(VBox vbox) {

        // In the case that we are resetting the simulation, clear the existing SERIES
        for (XYChart.Series<Number, Number> series : SERIES) {
            if (series != null) {
                series.getData().clear();
            }
        }
        graphTick = 0;
        // And remove all the plotted charts
        for (int i = 0; i < vbox.getChildren().size(); i++) {
            if (vbox.getChildren().get(i) instanceof AreaChart) {
                vbox.getChildren().remove(i);
            }
        }

        for (int i = 1; i <= world.POPULATIONS.length + world.YIELDS.length; i++) {
            NumberAxis xAxis = new NumberAxis();
            xAxis.setForceZeroInRange(false); // Don't force 0 to always display since we will start removing old entries
            xAxis.setMinorTickCount(0); // Don't display small ticks
            xAxis.setTickLabelsVisible(false); // Don't display labels on the x-axis since they represent the number of ticks over an arbitrary period of time
            NumberAxis yAxis = new NumberAxis();
            yAxis.setForceZeroInRange(false); // Don't force 0 so the graph doesn't become incredibly stretched
            yAxis.setMinorTickCount(0); // Don't display small ticks
            AreaChart<Number, Number> chart = new AreaChart<>(xAxis, yAxis);
            chart.setLegendVisible(false);

            XYChart.Series<Number, Number> thisSeries = new XYChart.Series<>();
            thisSeries.getData().add(new XYChart.Data<>(0, 0));
            chart.getData().add(thisSeries);
            SERIES[i - 1] = thisSeries;

            chart.setPrefSize(vbox.getWidth(), 100);
            vbox.getChildren().add(i * 2 - 1, chart); // Insert after the corresponding label
        }
    }

    private void plotPoints() {
        graphTick++;
        for (int i = 0; i < world.POPULATIONS.length; i++) { // Plot the population array
            SERIES[i].getData().add(new XYChart.Data<>(graphTick, (int) world.POPULATIONS[i]));
            if (graphTick > MAX_GRAPH_TICK) { // Start removing old data past the max tick
                SERIES[i].getData().remove(0);
            }
        }
        for (int i = world.POPULATIONS.length; i < world.POPULATIONS.length + world.YIELDS.length; i++) { // Plot the yields array
            SERIES[i].getData().add(new XYChart.Data<>(graphTick, (int) world.YIELDS[i - world.POPULATIONS.length]));
            if (graphTick > MAX_GRAPH_TICK) { // Start removing old data past the max tick
                SERIES[i].getData().remove(0);
            }
        }
    }

    private void focusSubscene() { subscene.requestFocus(); }
}
