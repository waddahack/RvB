package rvb;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import managers.PopupManager;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB.State;
import static rvb.RvB.mouseDown;
import static rvb.RvB.stateChanged;
import static rvb.RvB.unite;
import static rvb.RvB.windHeight;
import ui.Button;
import ui.Overlay;
import static rvb.RvB.ref;

public class Creation extends AppCore{
    
    private static Random rand = new Random();
    private ArrayList<Tile> roads;
    
    public Creation(){
        init(RvB.Difficulty.MEDIUM);
        initOverlays();   
        initMap("created");
        
        roads = new ArrayList<>();
        if(!path.isEmpty()){
            for(Tile t : path)
                roads.add(t);
        }
        path.clear();
    }
    
    @Override
    protected void render(){
        for(ArrayList<Tile> row : map)
            for(Tile t : row)
                RvB.drawFilledRectangle(t.getRealX(), t.getRealY(), unite, unite, t.getTexture(), t.getAngle());
        for(Tile road : roads)
            road.renderDirection();
    }
    
    @Override
    protected void initOverlays(){
        overlays = new ArrayList<>();
        Overlay o;
        Button b;
        
        o = new Overlay(0, 0, RvB.windWidth, (int) (60*ref));
        o.setBG(RvB.textures.get("board"), 0.6f);
        b = new Button(RvB.windWidth/2, (int) (30*ref), (int) (180*ref), (int) (38*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        o.addButton(b);
        b = new Button((int) (60*ref), (int) (30*ref), (int) (32*ref), (int) (32*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setBG(RvB.textures.get("arrowBack"));
        o.addButton(b);
        overlays.add(o);
        
        o = new Overlay(0, (int) (windHeight-60*ref), RvB.windWidth, (int) (60*ref));
        o.setBG(RvB.textures.get("board"), 0.6f);
        overlays.add(o);
    }
    
    @Override
    public void renderOverlays(){ 
        Overlay o;
        
        //// Overlay du haut
        o = overlays.get(0);
        o.render();
        o.getButtons().get(0).drawText(0, 0, "Save & Play", RvB.fonts.get("normalL"));
        //
        //// Overlay du bas
        o = overlays.get(1);
        o.render();
        //
    }
    
    @Override
    public void checkOverlaysInput(){
        Overlay o;

        // Overlay principal
        o = overlays.get(0);
        if(o.getButtons().get(0).isClicked(0) && Mouse.getEventButtonState() && !mouseDown){ // Play button clicked
            String[] error = calculatePath();
            if(error == null){
                saveLevel();
                PopupManager.Instance.chooseDifficulty("created");
            }
            else{
                PopupManager.Instance.popup(error);
            }
        }
        else if(o.getButtons().get(1).isClicked(0) && Mouse.getEventButtonState() && !mouseDown){ // Back button clicked
            RvB.state = State.MENU;
        }
        //
    }
    
    @Override
    protected void checkInput(){
        // Click check
        if(stateChanged)
            return;
        while(Mouse.next()){
            int indexX = getMouseIndexX();
            int indexY = getMouseIndexY();
            
            if(Mouse.isButtonDown(0) && !overlays.get(0).buttonClicked(0) && !map.get(indexY).get(indexX).type.equals("road") && !stateChanged && indexY >= 1 && indexY <= RvB.nbTileY-1){ // Puts road
                Tile road = new Tile(RvB.textures.get("roadStraight"), "road");
                road.setRotateIndex(0);
                road.setX(indexX*unite);
                road.setY(indexY*unite);
                map.get(indexY).set(indexX, road);
                searchAndConnect(road);
                roads.add(road);
            }
            else if(Mouse.isButtonDown(1) && !map.get(indexY).get(indexX).type.equals("grass") && !stateChanged){ // Puts grass
                Texture t;
                int n = rand.nextInt(100)+1;
                if(n > 93)
                    t = RvB.textures.get("bigPlant1");
                else if(n > 86)
                    t = RvB.textures.get("bigPlant2");
                else
                    t = RvB.textures.get("grass");
                n = 0;
                if(t != RvB.textures.get("grass"))
                    n = (int)Math.round(rand.nextInt(361)/90)*90;  
                Tile grass = new Tile(t, "grass");
                grass.setAngle(n);
                grass.setRotateIndex(0);
                grass.setX(indexX*unite);
                grass.setY(indexY*unite);
                Tile oldRoad = map.get(indexY).get(indexX);
                if(oldRoad == spawn)
                    spawn = null;
                map.get(indexY).set(indexX, grass);
                // Remove old road & look for potential connect for its neighbors
                if(oldRoad.nextRoad != null){
                    oldRoad.nextRoad.setPreviousRoad(null);
                    searchAndConnect(oldRoad.nextRoad);
                }
                if(oldRoad.previousRoad != null){
                    oldRoad.previousRoad.setNextRoad(null);
                    searchAndConnect(oldRoad.previousRoad);
                }
                roads.remove(oldRoad);
            }
            
            // Overlays inputs
            checkOverlaysInput();
        }
    }
    
    private String[] calculatePath(){
        if(roads.size() == 0)
            return new String[]{"???"};
        path.clear();
        spawn = null;
        base = null;
        for(Tile road : roads){
            if(road.arrowAngle == -1)
                return new String[]{"Path not valid.", "It has to begin, and end, from the left or right side.", "It cannot have extra roads."};
            if(road.previousRoad != null && road.previousRoad.type == "nothing"){
                if(spawn == null)
                    spawn = road;
                else
                    return new String[]{"There can't be more than one path !"};
            } 

            if(road.nextRoad != null && road.nextRoad.type == "nothing")
                base = road;
        }
        if(spawn == null || base == null)
            return new String[]{"Can't be a loop !"};
        Tile road = spawn;
        int n = 0;
        while(road != null){
            road = road.nextRoad;
            n++;
        }
        if(n < roads.size())
            return new String[]{"It cannot have extra roads."};
        road = spawn;
        while(road.type != "nothing"){
            path.add(road);
            road = road.nextRoad;
        }
        return null;
    }
    
    private void saveLevel(){
        try{
            // Level
            String level = "";
            for(ArrayList<Tile> row : map){
                for(Tile tile : row){
                    if(tile == spawn)
                        level += "S";
                    else if(tile == base)
                        level += "B";
                    else if(tile.type == "road")
                        level += "0";
                    else
                        level += ".";
                    level += " ";
                }
                level += "\n";
            }
            // Path
            String pathCoords = "";
            for(Tile road : path){
                pathCoords += road.getIndexX()+"/";
                pathCoords += road.getIndexY()+" ";
            }
            // Write in file
            File file = new File("assets/levels/level_created.txt");
            PrintWriter writer = new PrintWriter(file);
            writer.print(level+"\nPATH: "+pathCoords);
            writer.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    
    private void searchAndConnect(Tile tile){
        ArrayList<Tile> neighbors = searchNeighbors(tile);
        connectTileWithNeighbors(tile, neighbors);
        fixRoadSprite(tile);
        if(tile.previousRoad != null) fixRoadSprite(tile.previousRoad);
        if(tile.nextRoad != null) fixRoadSprite(tile.nextRoad);
    }
    
    private ArrayList<Tile> searchNeighbors(Tile tile){
        ArrayList<Tile> neighbors = new ArrayList<Tile>();
        if(tile.type == "nothing")
            return neighbors;
        
        int i = Math.floorDiv((int)tile.getY(), unite);
        int j = Math.floorDiv((int)tile.getX(), unite);
        if(i <= 0)
            neighbors.add(new Tile(tile.getX(), tile.getY()-unite));
        else if(map.get(i-1).get(j).type == "road") // Check above
            neighbors.add(map.get(i-1).get(j));
        if(i >= map.size()-1)
            neighbors.add(new Tile(tile.getX(), tile.getY()+unite));
        else if(map.get(i+1).get(j).type == "road") // Check under
            neighbors.add(map.get(i+1).get(j));
        if(j <= 0)
            neighbors.add(new Tile(tile.getX()-unite, tile.getY()));
        else if(map.get(i).get(j-1).type == "road") // Check left
            neighbors.add(map.get(i).get(j-1));
        if(j >= map.get(i).size()-1 )
            neighbors.add(new Tile(tile.getX()+unite, tile.getY()));
        else if(map.get(i).get(j+1).type == "road") // Check right
            neighbors.add(map.get(i).get(j+1));
        
        return neighbors;
    }
    
    private void connectTileWithNeighbors(Tile tile, ArrayList<Tile> neighbors){
        Tile side = null;
        for(Tile neighbor : neighbors){ // Sets la previous et next road de la tile
            
            if(neighbor.type == "nothing" && side == null){
                side = neighbor;
                continue;
            }
            if(tile == neighbor.nextRoad || tile == neighbor.previousRoad) // Si les deux tiles sont déjà connectées
                continue;
            
            if((neighbor.nextRoad == null || neighbor.nextRoad.type == "nothing") && (tile.previousRoad == null || tile.previousRoad.type == "nothing")){
                neighbor.setNextRoad(tile);
                tile.setPreviousRoad(neighbor);
            }
            else if((neighbor.previousRoad == null || neighbor.previousRoad.type == "nothing") && (tile.nextRoad == null || tile.nextRoad.type == "nothing")){
                neighbor.setPreviousRoad(tile);
                tile.setNextRoad(neighbor);
            }
        }
        if(side != null){
            if(tile.previousRoad == null)
                tile.setPreviousRoad(side);
            else if(tile.nextRoad == null)
                tile.setNextRoad(side);
        }
    }
    
    protected void fixRoadSprite(Tile road){   
        // Fix the new road's and its neighbors' sprites connections
        if(road.previousRoad == null && road.nextRoad == null)
            return;
        
        Tile previousRoad = road.previousRoad;
        Tile nextRoad = road.nextRoad;
        if(previousRoad == null)
            previousRoad = new Tile(road.getX()+(road.getX()-nextRoad.getX()), road.getY()+(road.getY()-nextRoad.getY()));
        if(nextRoad == null) // Can't be both null anyway
            nextRoad = new Tile(road.getX()+(road.getX()-previousRoad.getX()), road.getY()+(road.getY()-previousRoad.getY()));

        // Si previousRoad est à GAUCHE et nextRoad est en BAS ou l'inverse
        if((previousRoad.getX() < road.getX() && nextRoad.getY() > road.getY()) || (nextRoad.getX() < road.getX() && previousRoad.getY() > road.getY())){
            road.setTexture(RvB.textures.get("roadTurn"));
            road.setAngle(180);
        }
        // Si previousRoad est à GAUCHE et nextRoad est en HAUT ou l'inverse
        else if((previousRoad.getX() < road.getX() && nextRoad.getY() < road.getY()) || (nextRoad.getX() < road.getX() && previousRoad.getY() < road.getY())){
            road.setTexture(RvB.textures.get("roadTurn"));
            road.setAngle(270);
        }
        // Si previousRoad est à DROITE et nextRoad est en BAS ou l'inverse
        else if((previousRoad.getX() > road.getX() && nextRoad.getY() > road.getY()) || (nextRoad.getX() > road.getX() && previousRoad.getY() > road.getY())){
            road.setTexture(RvB.textures.get("roadTurn"));
            road.setAngle(90);
        }
        // Si previousRoad est à DROITE et nextRoad est en HAUT ou l'inverse
        else if((previousRoad.getX() > road.getX() && nextRoad.getY() < road.getY()) || (nextRoad.getX() > road.getX() && previousRoad.getY() < road.getY())){
            road.setTexture(RvB.textures.get("roadTurn"));
            road.setAngle(0);
        }
        // Si previousRoad est à DROITE et nextRoad est à GAUCHE ou l'inverse
        else if((previousRoad.getX() > road.getX() && nextRoad.getX() < road.getX()) || (nextRoad.getX() > road.getX() && previousRoad.getX() < road.getX())){
            road.setTexture(RvB.textures.get("roadStraight"));
            road.setAngle(90);
        }
        // Si previousRoad est en HAUT et nextRoad est en BAS ou l'inverse
        else if((previousRoad.getY() < road.getY() && nextRoad.getY() > road.getY()) || (nextRoad.getY() < road.getY() && previousRoad.getY() > road.getY())){
            road.setTexture(RvB.textures.get("roadStraight"));
            road.setAngle(0);
        }
    }
}