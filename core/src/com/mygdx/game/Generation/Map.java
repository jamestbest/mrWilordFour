package com.mygdx.game.Generation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.AStar.AStar;
import com.mygdx.game.Game.MyGdxGame;
import com.mygdx.game.Generation.Things.Fire;
import com.mygdx.game.Entity.Task;
import com.mygdx.game.Generation.Things.AnimatedThings;
import com.mygdx.game.Generation.Things.Thing;
import com.mygdx.game.Math.CameraTwo;
import com.mygdx.game.Entity.Colonist;
import com.mygdx.game.Math.Math;
import com.mygdx.game.Saving.RLE;
import com.mygdx.game.Screens.GameScreen;
import com.mygdx.game.floorDrops.FloorDrop;
import com.mygdx.game.floorDrops.Zone;
import com.mygdx.game.ui.elements.Notification;
import io.socket.client.Socket;

import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class Map {
    public ArrayList<ArrayList<Tile>> tiles;
    public ArrayList<ArrayList<Thing>> things;
    public ArrayList<Task> tasks = new ArrayList<>();
    public ArrayList<Fire> fire = new ArrayList<>();

    public float totalRaidChanceAffector;

    public int addition;

    private int x;
    private int y;

    public int drawHeight;

    public MapSettings settings;

    public static HashMap<String, TileInformation> tileInformationHashMap;

    static Json json = new Json();

    public boolean lightShouldBeUpdated;

    public ArrayList<Zone> zones = new ArrayList<>();
    public static Color[] zoneColors;
    int zoneColor;
    private int zoneID;

    public ArrayList<FloorDrop> floorDrops = new ArrayList<>();

    private static final Random random = new Random();

    private float chanceToSpawnTree;

    public HashMap<String, Integer> resources;

    public Map(MapSettings settings){
        this.settings = settings;
    }

    public Map(MapSettings settings, int drawHeight, int x, int y){
        this.settings = settings;

        this.x = x;
        this.y = y;
        this.drawHeight = drawHeight;
    }

    public Map(int drawHeight, int x, int y, String seed){
        this.settings = new MapSettings(seed);
        this.drawHeight = drawHeight;

        this.x = x;
        this.y = y;
    }

    public Map(){

    }

    public void generateMap(){
        setup();
        tiles = new ArrayList<>();
        things = new ArrayList<>();
        generateGrass();
        generateStone();

        if (settings.riverToggle){
            findRiverLocs();
            generateRiver(findRiverLocs());
        }

        generateTrees();
        generateBerryBushes();
    }

    public void setup(){
        setTileInfoHashMap();
        zoneColors = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.PINK, Color.ORANGE, Color.LIME, Color.MAGENTA, Color.CYAN};
        zoneColor = 0;
    }


    public void generateStone(){
        for(int i = 0; i < GameScreen.TILES_ON_X; i++) {
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                double temp = Noise2D.noise((((float) i / GameScreen.TILES_ON_X) * settings.perlinFrequency) + addition,
                        (((float) j / GameScreen.TILES_ON_X) * settings.perlinFrequency) + addition, 255);
                if (temp > 0.65f) {
                    Tile tile = new Tile(i, j, "stone");
                    tile.updateWalkAndSpawn(tileInformationHashMap);
                    tiles.get(i).set(j, tile);
                }
            }
        }
    }

    public void generateGrass(){
        for(int i = 0; i < GameScreen.TILES_ON_X; i++) {
            tiles.add(new ArrayList<>());
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                float temp = Noise2D.noise((((float) i / GameScreen.TILES_ON_X) * 40) + addition,
                        (((float) j / GameScreen.TILES_ON_X) * 40) + addition, 255);
                Tile tile;
                if (temp > 0.55f) {
                    tile = new Tile(i, j, "grass");
                }
                else{
                    tile = new Tile(i, j, "dirt");
                }
                tile.updateWalkAndSpawn(tileInformationHashMap);
                tiles.get(i).add(tile);
            }
        }
    }

    public void updateMap(){
        getAdditionFromSeed(settings.seed);
        tiles.clear();
        things.clear();
        generateMap();
    }

    public void generateBlank(){
        setup();
        tiles = new ArrayList<>();
        things = new ArrayList<>();
        for(int i = 0; i < GameScreen.TILES_ON_X; i++){
            tiles.add(new ArrayList<>());
            things.add(new ArrayList<>());
            for(int j = 0; j < GameScreen.TILES_ON_X; j++){
                tiles.get(i).add(new Tile(i, j, "dirt"));
                things.get(i).add(new Thing(i, j, (int) GameScreen.TILE_DIMS, (int) GameScreen.TILE_DIMS, "", (int) GameScreen.TILE_DIMS));
            }
        }
    }

    public void drawMap(SpriteBatch batch, HashMap<String, Texture> tileTextures, CameraTwo camera){
        int startX = (int)(Math.highest (((camera.position.x - (camera.width * camera.zoom)/2f) / GameScreen.TILE_DIMS) - 5, 0));
        int startY = (int)(Math.highest (((camera.position.y - (camera.height * camera.zoom)/2f) / GameScreen.TILE_DIMS) - 5, 0));

        int endX = (int)(Math.lowest((camera.width * camera.zoom / GameScreen.TILE_DIMS) + startX + 10, GameScreen.TILES_ON_X));
        int endY = (int)(Math.lowest((camera.height * camera.zoom / GameScreen.TILE_DIMS) + startY + 10, GameScreen.TILES_ON_X));

        Texture temp;
        String[] types = tileTextures.keySet().toArray(new String[0]);

        for (int k = 0; k < tileTextures.size(); k++) {
            temp = tileTextures.get(types[k]);
            for(int i = startX; i < endX; i++){
                for(int j = startY; j < endY; j++){
                    if (tiles.get(i).get(j).type.equals(types[k])){
                        batch.draw(temp, i * GameScreen.TILE_DIMS, j * GameScreen.TILE_DIMS, GameScreen.TILE_DIMS, GameScreen.TILE_DIMS);
                    }
                }
            }
        }
    }

    public void drawThings(SpriteBatch batch, HashMap<String, TextureAtlas> thingTextures, CameraTwo camera, int startK, int endK){
        int startX = (int)(Math.highest (((camera.position.x - (camera.width * camera.zoom)/2f) / GameScreen.TILE_DIMS) - 5, 0));
        int startY = (int)(Math.highest (((camera.position.y - (camera.height * camera.zoom)/2f) / GameScreen.TILE_DIMS) - 5, 0));

        int endX = (int)(Math.lowest((camera.width * camera.zoom / GameScreen.TILE_DIMS) + startX + 10, GameScreen.TILES_ON_X));
        int endY = (int)(Math.lowest((camera.height * camera.zoom / GameScreen.TILE_DIMS) + startY + 10, GameScreen.TILES_ON_X));
        for (int k = startK; k < endK; k++) {
            for(int i = startX; i < endX; i++) {
                for (int j = endY - 1; j > startY - 1; j--) {
                    if (!things.get(i).get(j).type.equals("") && !things.get(i).get(j).type.equals("edgeBouncer")) {
                        Thing t = things.get(i).get(j);
                        t.draw(batch, thingTextures.get(t.type), k);
                    }
                }
            }
        }
    }

    public void drawMiniMap(SpriteBatch batch, HashMap<String, Texture> textures, HashMap<String, TextureAtlas> thingTextures){
        float miniMapDims = drawHeight / (float) GameScreen.TILES_ON_X;

        for(int k = 0; k < 3; k++) {
            for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
                for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                    batch.draw(textures.get(tiles.get(i).get(j).type), i * miniMapDims + x, j * miniMapDims + y, miniMapDims, miniMapDims);

                    Thing t = things.get(i).get(j);
                    if (!t.type.equals("") && !t.type.equals("edgeBouncer")) {
                        Vector2 mults = GameScreen.getMultiplierFromThings(t.type);
                        t.drawMini(batch, thingTextures.get(t.type), ((i * miniMapDims) + x), ((j * miniMapDims) + y),
                                (miniMapDims * mults.x), (miniMapDims * mults.y), k);
                    }
                }
            }
        }
    }

    public void getAdditionFromSeed(String seed){
        addition = 0;
        for (char c: seed.toCharArray()
        ) {
            addition += c * 10;
        }
    }

    public void generateRiver(ArrayList<Vector2> riverLocs){
        Vector2 start = riverLocs.get(0);
        Vector2 end = riverLocs.get(1);
        ArrayList<Vector2> path;
//        updateBooleanMap();
        path = AStar.pathFindForRivers(start, end, addition, tiles, settings.riverBend, settings.perlinFrequency);
        for (int i = 0; i < path.size(); i++) {
            Vector2 temp = path.get(i);
            if (i + 1 < path.size()) {
                if (path.get(i + 1).y == temp.y) {
                    for (int j = 0; j < 2; j++) {
                        if (temp.y + j < GameScreen.TILES_ON_X){
                            Tile tempTile = tiles.get((int) temp.x).get((int) (temp.y + j));
                            tempTile.type = "water";
                            tempTile.canSpawnOn = false;
                        }
                    }
                }

            }
            for (int j = 0; j < 4; j++) {
                if (temp.x + j < GameScreen.TILES_ON_X) {
                    Tile tempTile = tiles.get((int) (temp.x + j)).get((int) temp.y);
                    tempTile.type = "water";
                    tempTile.canSpawnOn = false;
                }
            }
        }
    }

    public ArrayList<Vector2> findRiverLocs(){
        ArrayList<Vector2> riverLocs = new ArrayList<>();
        Random rand = new Random();
        int sx = rand.nextInt(GameScreen.TILES_ON_X);
        int ex = rand.nextInt(GameScreen.TILES_ON_X);

        while(!tiles.get(sx).get(0).canSpawnOn){
            sx = rand.nextInt(GameScreen.TILES_ON_X);
        }
        while(!tiles.get(ex).get(GameScreen.TILES_ON_X - 1).canSpawnOn){
            ex = rand.nextInt(GameScreen.TILES_ON_X);
        }
        riverLocs.add(new Vector2(sx, 0));
        riverLocs.add(new Vector2(ex, GameScreen.TILES_ON_X - 1));
        return riverLocs;
    }

    public void generateTrees(){
        Random random = new Random();
        for(int i = 0; i < GameScreen.TILES_ON_X; i++){
            things.add(new ArrayList<>());
            for(int j = 0; j < GameScreen.TILES_ON_X; j++){
                if(random.nextInt(100) < settings.treeFreq && tiles.get(i).get(j).canSpawnOn){
                    AnimatedThings temp = new AnimatedThings(i, j, (int) GameScreen.TILE_DIMS, (int) GameScreen.TILE_DIMS * 2, "tree", (int) GameScreen.TILE_DIMS);
                    tiles.get(i).get(j).canSpawnOn = tileInformationHashMap.get("tree").canSpawnOn;
                    tiles.get(i).get(j).canWalkOn = tileInformationHashMap.get("tree").canWalkOn;
                    things.get(i).add(temp);
                }
                else {
                    things.get(i).add(new Thing(i, j, (int) GameScreen.TILE_DIMS, (int) GameScreen.TILE_DIMS, "", (int) GameScreen.TILE_DIMS));
                }
            }
        }
        System.out.println("Trees generated");
    }

    public void generateBerryBushes(){
        Random random = new Random();
        for(int i = 0; i < GameScreen.TILES_ON_X; i++){
            for(int j = 0; j < GameScreen.TILES_ON_X; j++){
                if(random.nextInt(1000) < 1 && tiles.get(i).get(j).canSpawnOn){
                    generateCluster(i,j, 5, "berryBush");
                }
            }
        }
        System.out.println("Berry bushes generated");
    }

    public void generateCluster(int x, int y, int amount, String type){
        Random random = new Random();
        int sx = x - 2;
        int ex = x + 2;
        int sy = y - 2;
        int ey = y + 2;

        for (int i = 0; i < amount; i++) {
            int tempX = random.nextInt(ex - sx) + sx;
            int tempY = random.nextInt(ey - sy) + sy;
            if (isWithinBounds(tempX, tempY)) {
                if (tiles.get(tempX).get(tempY).canSpawnOn) {
                    changeThingType(tempX, tempY, type, (int) (GameScreen.getMultiplierFromThings(type).y * GameScreen.TILE_DIMS), false);
                }
            }
        }
    }

    public void addThing(Thing thing, int x, int y, boolean fromColonist, Socket socket, boolean isHost){
        boolean currentlyEmitsLight = things.get(x).get(y).emitsLight;
        lightShouldBeUpdated = true;
        thing.builtByColonist = fromColonist;
        things.get(x).set(y, thing);
        Thing t = things.get(x).get(y);
        t.update(things);
        t.updateDims();
        t.drawLayer = tileInformationHashMap.get(t.type).drawLayer;
        tiles.get(x).get(y).updateWalkAndSpawn(tileInformationHashMap, thing.type);
        if (!thing.emitsLight && currentlyEmitsLight){
            GameScreen.lightManager.removeLight(x,y);
        }
        updateThingNeighbours(x, y);
//        if (isHost && socket != null){
//            socket.emit("changeThingType", x, y, thing.type, thing.height, thing.emitsLight);
//        }
    }

    public void updateThingNeighbours(int x, int y){
        int[][] neighbours = {{x - 1, y}, {x + 1, y}, {x, y - 1}, {x, y + 1}};
        for (int[] neighbour : neighbours) {
            if (isWithinBounds(neighbour[0], neighbour[1])) {
                things.get(neighbour[0]).get(neighbour[1]).update(things);
            }
        }
    }

    public void changeTileType(int x, int y, String type){
        if (isWithinBounds(x, y)){
            Tile temp = tiles.get(x).get(y);
            temp.type = type;
            temp.updateWalkAndSpawn(tileInformationHashMap);
        }
    }

    public void changeThingType(int x, int y, String type, int height, boolean fromColonist){
        if (isWithinBounds(x, y)){
            Thing temp2 = new Thing(things.get(x).get(y));
            temp2.builtByColonist = fromColonist;
            temp2.type = type;
            temp2.height = height;
            temp2.drawLayer = tileInformationHashMap.get(temp2.type).drawLayer;
            things.get(x).set(y, temp2);
            Tile tempTile = tiles.get(x).get(y);
            tempTile.updateWalkAndSpawn(tileInformationHashMap, temp2.type);
        }
    }

    public boolean isWithinBounds(int newX, int newY){
        return newX >= 0 && newX < GameScreen.TILES_ON_X && newY >= 0 && newY < GameScreen.TILES_ON_X;
    }

    public static void setTileInfoHashMap(){
        tileInformationHashMap = json.fromJson(HashMap.class, TileInformation.class, Gdx.files.internal("info/tileInfo/tileInfo.txt"));
        System.out.println("");
    }

    public String packageTiles(){
        return RLE.encodeTiles(this);
    }

    public String packageThings(){
        return RLE.encodeThings(this);
    }

    public static boolean loadMap(String dirName, String saveName, Map map){
        try{
            ArrayList<String> save = getSaveString(dirName, saveName);

            String mapDims = "250";

            for (String s : save) {
                switch (s.split(" ")[0]) {
                    case "tiles:" -> {
                        String loadedTiles = s.split(" ")[1];
                        map.tiles = RLE.decodeTiles(loadedTiles, Integer.parseInt(mapDims));
                    }
                    case "things:" -> {
                        String loadedThings = s.split(" ")[1];
                        map.things = RLE.decodeThings(loadedThings, Integer.parseInt(mapDims));
                        map.updateAllTilesWithNewThings();
                    }
                    case "mapInfo:" -> {
                        String loadedMapInfo = s.split(": ")[1];
                        String[] splitMapInfo = loadedMapInfo.split(" ");
                        MyGdxGame.initialRes.x = Float.parseFloat(splitMapInfo[0]);
                        MyGdxGame.initialRes.y = Float.parseFloat(splitMapInfo[1]);
                    }
                    case "mapDims:" -> {
                        mapDims = s.split(" ")[1];
                        GameScreen.TILES_ON_X = Integer.parseInt(mapDims);
                    }
                    case "resources:" -> {
                        String loadedResources = s.split(" ")[1];
                        map.resources = json.fromJson(HashMap.class, loadedResources);
                    }
                    case "zones:" -> {
                        String loadedZones = s.split(" ")[1];
                        map.zones = json.fromJson(ArrayList.class, Zone.class, loadedZones);
                    }
                    case "fires:" -> {
                        String loadedFires = s.split(" ")[1];
                        map.fire = json.fromJson(ArrayList.class, Fire.class, loadedFires);
                    }
                    default -> {
                    }
                }
            }
            return true;
        }catch(Exception e){
            System.out.println("Error loading map");
            return false;
        }
    }

    public void updateAllTilesWithNewThings(){
        for (int i = 0; i < GameScreen.TILES_ON_X; i++) {
            for (int j = 0; j < GameScreen.TILES_ON_X; j++) {
                tiles.get(i).get(j).updateWalkAndSpawn(tileInformationHashMap, things.get(i).get(j).type);
            }
        }
    }

    public static ArrayList<Colonist> loadColonists(String dirName, String saveName){
        try {
            ArrayList<String> save = getSaveString(dirName, saveName);

            for (String s : save) {
                if ("colonists:".equals(s.split(" ")[0])) {
                    String loadedColonists = s.split(": ")[1];
                    System.out.println("showing the colonists' json: " + loadedColonists);
                    return json.fromJson(ArrayList.class, Colonist.class, loadedColonists);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> getSaveString(String dirName, String saveName) throws IOException {
        File file = new File("core/assets/Saves/" + dirName + "/" + saveName);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        ArrayList<String> save = new ArrayList<>();
        String temp = br.readLine();
        while (temp != null){
            System.out.println(temp);
            save.add(temp);
            temp = br.readLine();
        }
        br.close();
        System.out.println(save);
        return save;
    }

    public void clearThing(int x, int y){
        things.get(x).get(y).type = "";
        things.get(x).get(y).canConnect = false;
        things.get(x).get(y).emitsLight = false;
        tiles.get(x).get(y).updateWalkAndSpawn(tileInformationHashMap);
    }

    public void drawFires(SpriteBatch batch, HashMap<String, ArrayList<Texture>> textures){
        for (Fire f : fire) {
            f.draw(batch, textures.get(f.getName()));
        }
    }

    public void updateFires(ArrayList<Colonist> colonists){
        ArrayList<Fire> toAdd = new ArrayList<>();
        ArrayList<Fire> toRemove = new ArrayList<>();
        for (Fire f : fire) {
            f.update(this);
            if (f.canSpread){
                Fire s = f.spread(tiles, this);
                if (s != null){
                    toAdd.add(s);
                }
                f.canSpread = false;
            }
            if (f.isDead){
                toRemove.add(f);
            }
        }
        fire.addAll(toAdd);
        ArrayList<Task> tasksToRemove = new ArrayList<>();
        for (Fire f : toRemove) {
            for (Task t : tasks) {
                if (Objects.equals(t.type, "FireFight")) {
                    if (t.getX() == f.getX() && t.getY() == f.getY()) {
                        tasksToRemove.add(t);
                        Colonist c = getColonistWithThisTask(t, colonists);
                        if (c != null) {
                            c.removeCurrentTask();
                        }
                    }
                }
            }
        }
        tasks.removeAll(tasksToRemove);
        fire.removeAll(toRemove);
    }

    public Colonist getColonistWithThisTask(Task t, ArrayList<Colonist> colonists){
        for (Colonist c : colonists) {
            if (c.getCurrentTask() == t) {
                return c;
            }
        }
        return null;
    }

    public void addFire(int x, int y, Socket socket, boolean isHost){
        if (getFireAt(x, y) != null) {
            return;
        }
        fire.add(new Fire(x, y, "default", GameScreen.fireMap.get("default").size()));
        tasks.add(new Task("FireFight", "", x, y));
        if (isHost && socket != null){
            socket.emit("addTask", "FireFight", "", x, y);
            socket.emit("addFire", x, y, "default");
        }

        GameScreen.notifications.add(socket, isHost,new Notification(GameScreen.notifications.getNextId(), "Fire", "fire"));
    }

    public void removeFire(int x, int y, Socket socket, boolean isHost){
        for (Fire f : fire) {
            if (f.getX() == x && f.getY() == y){
                fire.remove(f);
                if (isHost && socket != null){
                    socket.emit("removeFire", x, y);
                }
                break;
            }
        }
    }

    public Fire getFireAt(int x, int y){
        for (Fire f : fire) {
            if (f.getX() == x && f.getY() == y){
                return f;
            }
        }
        return null;
    }

    public Task getTaskAt(int x, int y, String type){
        for (Task t : tasks) {
            if (t.getX() == x && t.getY() == y && t.type.equals(type)){
                return t;
            }
        }
        return null;
    }

    public Task getTaskAt(int x, int y){
        for (Task t : tasks) {
            if (t.getX() == x && t.getY() == y){
                return t;
            }
        }
        return null;
    }

    public Color getNextZoneColor(){
        if (zoneColor == zoneColors.length - 1) {
            zoneColor = 0;
        }
        return zoneColors[zoneColor++];
    }

    public int getNextZoneID(){
        return zoneID++;
    }

    public int getZoneID() {
        return zoneID;
    }

    public void setZoneID(int zoneID) {
        this.zoneID = zoneID;
    }

    public void drawZones(ShapeRenderer shapeRenderer, SpriteBatch batch, HashMap<String, Texture> resourceTextures){
        Gdx.gl.glEnable(GL30.GL_BLEND);
        Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Zone z : zones) {
            z.draw(shapeRenderer, this);
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL30.GL_BLEND);

        batch.begin();
        for (Zone z : zones) {
            z.drawDrops(batch, resourceTextures);
            z.drawText(batch);
        }
        batch.end();
    }

    public Zone findNearestZone(int x, int y, String type, int amount){
        Zone nearest = null;
        float distance = Float.MAX_VALUE;
        for (Zone z : zones) {
            float d = z.distance(new Vector2(x, y));
            boolean hasRoom = z.hasRoom(type, this, amount);
            if (d < distance && hasRoom){
                distance = d;
                nearest = z;
            }
        }
        return nearest;
    }

    public Zone findNearestZoneToStealFrom(int x, int y){
        Zone nearest = null;
        float distance = Float.MAX_VALUE;
        for (Zone z : zones) {
            float d = z.distance(new Vector2(x, y));
            if (d < distance && z.hasAnyThing()){
                distance = d;
                nearest = z;
            }
        }
        return nearest;
    }

    public void addFloorDrop(int x, int y, String type, int amount, Socket socket, boolean isHost){
        boolean alreadyExists = false;
        for (FloorDrop f : floorDrops) {
            if (f.getX() == x && f.getY() == y){
                alreadyExists = true;
                if (f.getType().equals(type)){
                    if (!f.notAtMaxWith(amount)) {
                        f.incrementStackSize(amount);
                    }
                    else{
                        int getMaxAmount = f.getMaxAmountToAdd();
                        f.incrementStackSize(getMaxAmount);
                        amount -= getMaxAmount;
                    }
                    if (isHost && socket != null) {
                        socket.emit("updateFloorDrop", x, y, type, f.getStackSize());
                    }
                    break;
                }
            }
        }
        if (!alreadyExists) {
            floorDrops.add(new FloorDrop(x, y, type, amount));
            tiles.get(x).get(y).hasFloorDropOn = true;
            if (isHost && socket != null) {
                socket.emit("addFloorDrop", x, y, type, amount);
            }
        }
    }

    public void addFloorDrop(int x, int y, String type, Socket socket, boolean isHost){
        addFloorDrop(x, y, type, 1, socket, isHost);
    }

    public void addFloorDrop(FloorDrop f, Socket socket, boolean isHost){
        addFloorDrop(f.getX(), f.getY(), f.getType(), f.getStackSize(), socket, isHost);
    }

    public void updateFloorDrop(int x, int y, String type, int amount){
        FloorDrop fd = getFloorDropAt(x, y);
        if (fd != null){
            if (fd.getType().equals(type)){
                fd.setStackSize(amount);
            }
        }
    }

    public void removeFloorDrop(int x, int y, String type){
        for (FloorDrop f : floorDrops) {
            if (f.getX() == x && f.getY() == y && f.getType().equals(type)){
                floorDrops.remove(f);
                tiles.get(x).get(y).hasFloorDropOn = false;
                break;
            }
        }
    }

    public void removeFloorDrop(FloorDrop f){
        tiles.get(f.getX()).get(f.getY()).hasFloorDropOn = false;
        floorDrops.remove(f);
    }

    public void drawFloorDrops(SpriteBatch batch, HashMap<String, Texture> hm){
        for (FloorDrop f : floorDrops) {
            f.draw(batch, hm);
        }
        checkForRemoval();
    }

    public void checkForRemoval(){
        floorDrops.removeIf(f -> f.shouldBeRemoved);
    }

    public FloorDrop getFloorDropAt(int x, int y){
        for (FloorDrop f : floorDrops) {
            if (f.getX() == x && f.getY() == y){
                return f;
            }
        }
        for (Zone z : zones) {
            if (z.hasDropHere(x, y)){
                return z.getDropAt(x, y);
            }
        }
        return null;
    }

    public ArrayList<Vector2> getRandomPlaceForEntities(Random random){
        int tries = 0;
        do {
            int x = (int) (GameScreen.TILES_ON_X * 0.1);
            int y = (int) (GameScreen.TILES_ON_X * 0.1);
            tries++;
            int choice = random.nextInt(4);
            if (choice == 0){
                x = (int) (random.nextInt(GameScreen.TILES_ON_X) * 0.9);
            }
            else if (choice == 1){
                y = (int) (random.nextInt(GameScreen.TILES_ON_X) * 0.9);
            }
            else if (choice == 2){
                x = (int) (GameScreen.TILES_ON_X * 0.9);
                y = (int) (random.nextInt(GameScreen.TILES_ON_X) * 0.9);
            }
            else {
                x = (int) (random.nextInt(GameScreen.TILES_ON_X) * 0.9);
                y = (int) (GameScreen.TILES_ON_X * 0.9);
            }
            ArrayList<Vector2> possible = canSpawnHere(x, y, 10, 10);
            if (possible.size() > 0){
                return possible;
            }
        }
        while (tries < 100);
        return null;
    }

    public ArrayList<Vector2> canSpawnHere(int x, int y, int width, int height){
        ArrayList<Vector2> places = new ArrayList<>();
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                if (isWithinBounds(i, j)) {
                    if (tiles.get(i).get(j).canSpawnOn){
                        places.add(new Vector2(i, j));
                    }
                }
            }
        }
        return places;
    }

    public void addZone(Zone z){
        zones.add(z);
    }

    public void removeZone(Zone z){
        for (int i = z.getX(); i < z.getX() + z.getWidth(); i++) {
            for (int j = z.getY(); j < z.getY() + z.getHeight(); j++) {
                if (isWithinBounds(i, j) && things.get(i).get(j).type.equals("")) {
                    tiles.get(i).get(j).updateWalkAndSpawn(tileInformationHashMap, things.get(i).get(j).type);
                }
            }
        }
        zones.remove(z);
    }

    public void removeZones(ArrayList<Zone> z){
        for (Zone zone : z) {
            removeZone(zone);
        }
    }

    public void spawnTreeRandomly(int amount){ //it will not attempt to spawn the exact amount
        for (int i = 0; i < amount; i++) {
            int posX = random.nextInt(GameScreen.TILES_ON_X);
            int posY = random.nextInt(GameScreen.TILES_ON_X);

            if (tiles.get(posX).get(posY).canSpawnOn) {
                boolean notInZone = true;
                for (Zone z : zones) {
                    if (z.isInZone(posX, posY, this)) {
                        notInZone = false;
                    }
                }
                if (notInZone) {
                    changeThingType(posX, posY, "tree", (int) (GameScreen.getMultiplierFromThings("tree").y * GameScreen.TILE_DIMS), false);
                }
            }
        }
    }

    public void update(float delta){
        chanceToSpawnTree += delta;
        float MAX_CHANCE_TO_SPAWN_TREE = 100f;
        if (chanceToSpawnTree > MAX_CHANCE_TO_SPAWN_TREE){
            chanceToSpawnTree = 0;
            spawnTreeRandomly(random.nextInt(10) + 1);
        }
    }

    public Float getDistanceMultiplier(int x, int y){
        float maxDistance = (float) java.lang.Math.sqrt(java.lang.Math.pow(GameScreen.TILES_ON_X, 2) + java.lang.Math.pow(GameScreen.TILES_ON_X, 2));
        float distance = (float) java.lang.Math.sqrt(java.lang.Math.pow(x,2) + java.lang.Math.pow(y,2));
        return distance / maxDistance;
    }

    public void decreaseResource(String resource, int amount){
        int toRemove = amount;
        for (Zone z : zones){
            int amountInZone = z.getAmountOfResource(resource);
            while (amountInZone > 0 && toRemove > 0){
                toRemove -= 1;
                amountInZone -= 1;
                z.decrementResource(resource);
            }
        }
    }

    public void setupResourceHashMap(){
        resources = new HashMap<>();
        File dir = new File("core/assets/Textures/Resources");
        String[] files = dir.list();
        for (int i = 0; i < (files != null ? files.length : 0); i++) {
            String file = files[i];
            resources.put(file.split("\\.")[0], 0);
        }
    }

    public void refreshFloorDrops(){
        for (FloorDrop fd : floorDrops){
            tiles.get(fd.getX()).get(fd.getY()).hasFloorDropOn = true;
        }
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
