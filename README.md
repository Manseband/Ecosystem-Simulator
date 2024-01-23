# Ecosystem-Simulator

## Documentation

### Controls
The toolbar at the top of the window is where you will find all of the relevant controls.
The drop down menu allows you to switch between individual simulations. Once you make your choice, use the neighboring four icons to control the speed of the respective simulation.
Click the Reset World button to reset the world, including the graphs on the side panel, if you were dissatisfied with the results of a simulation’s run and want to start anew.
Click the Free Cam button to enter the free camera. 
In this mode, you can move around using **Left Click + Drag** and rotate on the Z-axis using **Right Click + Drag**. However, once you start to move, your rotation will be reset.
Scaling the 3D elements is also possible. **Scroll Wheel Up** will enlarge the elements, while **Scroll Wheel Down** will shrink them.
If you stray too far from land, you can always press **Space** to return to the starting position, rotation and scale.
Click the Orthographic Cam to return to the orthographic view.
Click the Collapse Graphs button to hide the graphs panel and expand the subscene view.
### Mechanics
- #### PopSim
	Observes changes in populations and yields by scanning through each occupied tile individually every tick, and adding their two attributes to the appropriate species arrays.
- #### Grapher
	Monitors the changes in populations and displays them onto graphs in the side panel. Updates at the same tickrate as the PopSim. Graphs will delete old data from their series to keep a fixed aspect ratio when displaying them.
- #### Clock
	Controls the background color of the subscene to simulate a day/night cycle. The length of one 12-hour period can be changed by editing the DAY_LENGTH field in App.java.
- #### TerrainSim
	Adds a new tile to the screen every time it ticks. The type of the tile is randomly chosen based on a weighted probability distribution.
- #### GrowthSim
	Checks if a certain set of conditions are present for a tile to grow to the next stage, or mutate into a completely different type.
### Tile
| Adjacency Conditions | *Grass* | *Forest* | *River* | *Village* | *Sand* |
| ---- | ---- | ---- | ---- | ---- | ---- |
| **Spawn** | Random | Random | Random | Random | River |
| **Growth** | River | River | River | River & (Grass or Forest) | None |
| **Expansion** | None | River | River | River & (Grass or Forest) | None |
| **Expands Into** | None | Grass | Sand* | Grass or Forest | None |
| **Death** | Village | Village | Sand** | River or Grass or Forest | River* |
| **Replacement** | Village<br>Sand** | Village | Sand** | Rubble | River |
###### \*A river cannot expand on its own. It must join forces with an adjacent river.
###### \*\*Only if the evaporation and desertification feature is toggled on.

### Notes
All **Spawn** probabilities are determined by random number generation, that chooses an element in the weighted PROBABILITIES array in World.java.
For a tile to achieve **Expansion**, it must be fully matured.
For **Precipitation** to occur on a *Grass* tile, it must be completely surrounded by *Grass* or *Forest* tiles, of which at least 3 are immature. This is to balance out the barren fields of *Grass* and *Forest* that are left dehydrated because of their relatively high spawn probabilities.
For **Precipitation** to occur on a *Forest* tile, it must be completely surrounded by immature *Grass* or *Forest* tiles.
*Grass* will experience **Desertification** if the *River* that initially provided growth to the tile evaporated at some point, and the *Grass* no longer has an adjacency with a *River*. The *Grass* tile will turn into a *Sand* tile.
After the **Death** of a *Village* tile, it is marked as “Abandoned”. This means another *Village* can never expand in the place of the other’s abandonment, and the tile is reverted to the previous type that inhabited it (either *Grass* or *Forest*). Before this happens, it first undergoes a period of ruin in which a *Rubble* tile sediments in its place.
The only purpose of *Sand* tiles is to provide *River* tiles room to flow. They do not promote growth or contribute to population/yield statistics.
