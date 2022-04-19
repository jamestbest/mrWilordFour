package com.mygdx.game.Entity;

import com.mygdx.game.Generation.Map;
import com.mygdx.game.Screens.GameScreen;
import com.mygdx.game.floorDrops.Zone;
import io.socket.client.Socket;

import java.util.ArrayList;

public class BarbarianTask extends Task {
    Barbarian b;

    private int x;
    private int y;

    String type;

    public BarbarianTask(Barbarian b, int x, int y, String type){
        this.b = b;
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void completeTask(Map map, Socket socket, boolean isHost){
        switch (type) {
            case "Demolish" -> {
                map.changeThingType(x, y, "", (int) GameScreen.TILE_DIMS, false);
                emitThingChange("", x, y, (int) GameScreen.TILE_DIMS, false, socket);
                map.lightShouldBeUpdated = true;
                map.updateThingNeighbours(x, y);
            }
            case "Steal" -> {
                Zone z = map.findNearestZoneToStealFrom(x, y);
                if (z != null) {
                    ArrayList<String> resources = z.getResources();
                    String resource = resources.get(random.nextInt(resources.size()));
                    int amount = z.getAmountOfResource(resource);
                    z.decrementResource(resource, random.nextInt(amount) + 1);
                }
            }
        }
    }

    public int getMaxPercentage(){
        switch (this.type){
            case "Demolish" -> {
                return 150;
            }
            case "Steal" -> {
                return 100;
            }
            default -> {
                return 0;
            }
        }
    }

    public void incrementPercentage(){
        this.percentageComplete += b.level;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
