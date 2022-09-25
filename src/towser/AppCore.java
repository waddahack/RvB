package towser;

import managers.SoundManager;
import ennemies.*;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import towers.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import managers.PopupManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.opengl.Texture;
import towser.Towser.Cursor;
import towser.Towser.Difficulty;
import static towser.Towser.State.MENU;
import static towser.Towser.game;
import static towser.Towser.mouseDown;
import static towser.Towser.unite;
import static towser.Towser.ref;
import static towser.Towser.windHeight;
import static towser.Towser.windWidth;
import ui.*;


public abstract class AppCore {
    
    public enum UEnemy{
        // Balance ends with a 0 or 5 only
        BASIC(0, BasicEnemy.balance, 1, 8),
        FAST(1, FastEnemy.balance, 4, 10),
        TRICKY(2, TrickyEnemy.balance, 6, 10),
        FLYING(3, FlyingEnemy.balance, 13, 5),
        STRONG(4, StrongEnemy.balance, 9, 15);
        
        public final int balance, id, enterAt, nbMax;
        
        UEnemy(int id, int balance, int enterAt, int nbMax){
            this.id = id;
            this.balance = balance;
            this.enterAt = enterAt;
            this.nbMax = nbMax;
        }
        
        public int addToWave(int number, int balance){
            int k = 0;
            int b = balance;
            while(b >= this.balance){
                b -= this.balance;
                k++;
            }
            
            if(k < number)
                number = k;
            for(int i = 0 ; i < number ; i++){
                Enemy e;
                switch(this.id){
                    default:
                        e = new BasicEnemy();
                        break;
                    case 1:
                        e = new FastEnemy();
                        break;
                    case 2:
                        e = new TrickyEnemy();
                        break;
                    case 3:
                        e = new FlyingEnemy();
                        break;
                    case 4:
                        e = new StrongEnemy();
                        break;
                }
                e.decreaseSpawnSpeedBy(0.1*game.waveNumber);
                game.wave.addEnemy(e);
                balance -= this.balance;
            }
            return balance;
        }
    }
    
    public ArrayList<ArrayList<Tile>> map;
    public Tile spawn, base;
    public int money, life, waveNumber, waveReward, nbTower = 2, gameSpeed = 1;
    public ArrayList<Tower> towers, towersDestroyed;
    public ArrayList<Enemy> enemies, enemiesDead, enemiesToAdd;
    public ArrayList<Tile> path;
    protected boolean gameOver;
    protected boolean inWave, dontPlace;
    public Enemy enemySelected = null;
    public boolean ended = false;
    public Tower towerSelected;
    protected Wave wave;
    protected ArrayList<Overlay> overlays;
    protected static int textureID = -10;
    
    public AppCore(){
        
    }
    
    protected void init(Difficulty diff){
        towers = new ArrayList<>();
        towersDestroyed = new ArrayList<>();
        enemies = new ArrayList<>();
        enemiesDead = new ArrayList<>();
        enemiesToAdd = new ArrayList<>();
        gameOver = false;
        inWave = false;
        dontPlace = false;
        towerSelected = null;
        BasicTower.priceP = BasicTower.startPrice;
        CircleTower.priceP = CircleTower.startPrice;
        FlameTower.priceP = FlameTower.startPrice;
        BigTower.priceP = BigTower.startPrice;
        Enemy.bonusLife = 0;
        Enemy.bonusMS = 0;
        
        if(diff == Difficulty.EASY){
            life = 125;
            money = 35000;
            waveNumber = 1;
            waveReward = 275;
        }
        else if(diff == Difficulty.HARD){
            life = 75;
            money = 250;
            waveNumber = 1;
            waveReward = 225;
        }
        else{ //if(diff == Difficulty.MEDIUM)
            life = 100;
            money = 300;
            waveNumber = 1;
            waveReward = 250;
        }
    }
    
    protected void initMap(String lvlName){
        readFile("assets/levels/level_"+lvlName+".txt");
        fixRoadNeighbors();
        fixRoadSprites();
        try {
            createMapTexture();
        } catch (Exception ex) {
            Logger.getLogger(AppCore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void initMap(ArrayList<Tile> path){
        map = new ArrayList<>();
        this.path = new ArrayList<>();
        if(path.size() == 0){
            this.path = path;
            return;
        }
        ArrayList<Tile> row;
        Random rand = new Random();
        Texture t;
        Tile tile;
        int n;
        for(int i = 0 ; i < Towser.nbTileY ; i++){
            row = new ArrayList<>();
            for(int j = 0 ; j < Towser.nbTileX ; j++){
                n = rand.nextInt(100)+1;
                if(n > 93)
                    t = Towser.textures.get("bigPlant1");
                else if(n > 86)
                    t = Towser.textures.get("bigPlant2");
                else
                    t = Towser.textures.get("grass");
                n = 0;
                if(t != Towser.textures.get("grass"))
                    n = (int)Math.round(rand.nextInt(361)/90)*90;  
                tile = new Tile(t, "grass");
                tile.setAngle(n);
                tile.setRotateIndex(0);
                tile.setX(j*unite);
                tile.setY(i*unite);
                row.add(tile);
            }
            map.add(row);
        }
        for(Tile road : path){
            tile = new Tile(Towser.textures.get("roadStraight"), "road");
            tile.setRotateIndex(0);
            tile.setX(road.getX()*unite);
            tile.setY(road.getY()*unite);
            this.path.add(tile);
            map.get((int) road.getY()).set((int) road.getX(), tile);
        }
        fixRoadNeighbors();
        fixRoadSprites();
        try {
            createMapTexture();
        } catch (Exception ex) {
            Logger.getLogger(AppCore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void readFile(String filePath){
        map = new ArrayList<>();
        path = new ArrayList<>();
        try{
            File file = new File(filePath);
            Scanner myReader = new Scanner(file);
            ArrayList<Tile> row = new ArrayList<>();
            int i = 0, j = 0, n;
            Texture t;
            Random rand = new Random();
            String data;
            Tile tile = null;
            boolean readPath = false;
            while(myReader.hasNext()){
                data = myReader.next();
                if(readPath){
                    String[] indexes = data.split("/");
                    int indexX = Integer.parseInt(indexes[0]);
                    int indexY = Integer.parseInt(indexes[1]);
                    path.add(map.get(indexY).get(indexX));
                }
                else{
                    switch(data){
                        case ".":
                            n = rand.nextInt(100)+1;
                            if(n > 93)
                                t = Towser.textures.get("bigPlant1");
                            else if(n > 86)
                                t = Towser.textures.get("bigPlant2");
                            else
                                t = Towser.textures.get("grass");
                            n = 0;
                            if(t != Towser.textures.get("grass"))
                                n = Math.round(rand.nextInt(361)/90)*90;  
                            tile = new Tile(t, "grass");
                            tile.setAngle(n);
                            tile.setRotateIndex(0);
                            break;
                        case "0":
                            tile = new Tile(Towser.textures.get("roadStraight"), "road");
                            tile.setRotateIndex(0);
                            break;
                        case "S":
                            tile = new Tile(Towser.textures.get("roadStraight"), "road");
                            tile.setRotateIndex(0);
                            break;
                        case "B":
                            tile = new Tile(Towser.textures.get("roadStraight"), "road");
                            tile.setRotateIndex(0);
                            break;
                        case "PATH:":
                            readPath = true;
                            break;
                        default:
                            tile = new Tile(new float[]{0f, 0f, 0f}, "void");
                            break;
                    }
                    if(readPath)
                        continue;
                    tile.setX(j*unite);
                    tile.setY(i*unite);
                    row.add(tile);

                    if(row.size() == Towser.nbTileX){
                        map.add(row);
                        row = new ArrayList<>();
                        j = 0;
                        i++;
                    }
                    else
                        j++;
                }
            }
            myReader.close();
        }
        catch (FileNotFoundException e){
            System.out.println("File : "+path+" doesn't exist.");
            e.printStackTrace();
        }
    }
    
    protected void fixRoadNeighbors(){
        Tile road;
        for(int i = 0 ; i < path.size() ; i++){
            road = path.get(i);
            if(i == 0){
                if(road.getIndexY() == 0) // sur le bord haut
                    road.previousRoad = new Tile(road.getX(), road.getY()-unite);
                else if(road.getIndexY() == map.size()-1)// sur le bord bas
                    road.previousRoad = new Tile(road.getX(), road.getY()+unite);
                else if(road.getIndexX() == 0) // sur le bord gauche
                    road.previousRoad = new Tile(road.getX()-unite, road.getY());
                else // sur le bord droit
                    road.previousRoad = new Tile(road.getX()+unite, road.getY());
                road.nextRoad = path.get(i+1);
                spawn = road;
            }
            else if(i == path.size()-1){
                road.previousRoad = path.get(i-1);
                if(road.getIndexY() == 0) // sur le bord haut
                    road.nextRoad = new Tile(road.getX(), road.getY()-unite);
                else if(road.getIndexY() == map.size()-1)// sur le bord bas
                    road.nextRoad = new Tile(road.getX(), road.getY()+unite);
                else if(road.getIndexX() == 0) // sur le bord gauche
                    road.nextRoad = new Tile(road.getX()-unite, road.getY());
                else // sur le bord droit
                    road.nextRoad = new Tile(road.getX()+unite, road.getY());
                base = road;
            }
            else{
                road.previousRoad = path.get(i-1);
                road.nextRoad = path.get(i+1);
            }
            road.setDirection();
        }
    }
    
    protected void fixRoadSprites(){
        // Fix road sprites connections
        Tile road, nextRoad = null, previousRoad = null;
        for(int i = 0 ; i < path.size() ; i++){
            road = path.get(i);
            previousRoad = road.previousRoad;
            nextRoad = road.nextRoad;

            // Si previousRoad est à GAUCHE et nextRoad est en BAS ou l'inverse
            if((previousRoad.getX()+unite/2 < road.getX() && nextRoad.getY()+unite/2 > road.getY()+unite) || (nextRoad.getX()+unite/2 < road.getX() && previousRoad.getY()+unite/2 > road.getY()+unite)){
                road.setTexture(Towser.textures.get("roadTurn"));
                road.setAngle(180);
            }
            // Si previousRoad est à GAUCHE et nextRoad est en HAUT ou l'inverse
            else if((previousRoad.getX()+unite/2 < road.getX() && nextRoad.getY()+unite/2 < road.getY()) || (nextRoad.getX()+unite/2 < road.getX() && previousRoad.getY()+unite/2 < road.getY())){
                road.setTexture(Towser.textures.get("roadTurn"));
                road.setAngle(270);
            }
            // Si previousRoad est à DROITE et nextRoad est en BAS ou l'inverse
            else if((previousRoad.getX()+unite/2 > road.getX()+unite && nextRoad.getY()+unite/2 > road.getY()+unite) || (nextRoad.getX()+unite/2 > road.getX()+unite && previousRoad.getY()+unite/2 > road.getY()+unite)){
                road.setTexture(Towser.textures.get("roadTurn"));
                road.setAngle(90);
            }
            // Si previousRoad est à DROITE et nextRoad est en HAUT ou l'inverse
            else if((previousRoad.getX()+unite/2 > road.getX()+unite && nextRoad.getY()+unite/2 < road.getY()) || (nextRoad.getX()+unite/2 > road.getX()+unite && previousRoad.getY()+unite/2 < road.getY())){
                road.setTexture(Towser.textures.get("roadTurn"));
                road.setAngle(0);
            }
            // Si previousRoad est à DROITE et nextRoad est à GAUCHE ou l'inverse
            else if((previousRoad.getX()+unite/2 > road.getX()+unite && nextRoad.getX()+unite/2 < road.getX()) || (nextRoad.getX()+unite/2 > road.getX()+unite && previousRoad.getX()+unite/2 < road.getX())){
                road.setTexture(Towser.textures.get("roadStraight"));
                road.setAngle(90);
            }
        }
    }
    
    private void createMapTexture() throws Exception {
        BufferedImage mapImage = new BufferedImage(windWidth, windHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = mapImage.createGraphics();
        
        Image RS = ImageIO.read(new File("assets/images/road_straight.png")).getScaledInstance(unite, unite, 0);
        Image RT = ImageIO.read(new File("assets/images/road_turn.png")).getScaledInstance(unite, unite, 0);
        Image GR = ImageIO.read(new File("assets/images/grass.png")).getScaledInstance(unite, unite, 0);
        Image P1 = ImageIO.read(new File("assets/images/big_plant1.png")).getScaledInstance(unite, unite, 0);
        Image P2 = ImageIO.read(new File("assets/images/big_plant2.png")).getScaledInstance(unite, unite, 0);
        
        for(int i = 0 ; i < map.size() ; i++){
            for(int j = 0 ; j < map.get(i).size() ; j++){
                AffineTransform context = g2d.getTransform();
                
                g2d.rotate(Math.toRadians(map.get(i).get(j).getAngle()), j*unite+unite/2, i*unite+unite/2);
 
                if(map.get(i).get(j).textures.get(0) == Towser.textures.get("roadStraight"))
                    g2d.drawImage(RS, j*unite, i*unite, null);
                else if(map.get(i).get(j).textures.get(0) == Towser.textures.get("roadTurn"))
                    g2d.drawImage(RT, j*unite, i*unite, null);
                else if(map.get(i).get(j).textures.get(0) == Towser.textures.get("bigPlant1"))
                    g2d.drawImage(P1, j*unite, i*unite, null);
                else if(map.get(i).get(j).textures.get(0) == Towser.textures.get("bigPlant2"))
                    g2d.drawImage(P2, j*unite, i*unite, null);
                else
                    g2d.drawImage(GR, j*unite, i*unite, null);
                
                g2d.setTransform(context);
            }
        }

        textureID = Towser.loadTexture(mapImage);
    }
    
    public void update(){
        clearArrays();
        
        if(this == Towser.game && !inWave){
            if(SoundManager.Instance.isReady())
                overlays.get(1).getButtons().get(0).setDisabled(false);
            else
                overlays.get(1).getButtons().get(0).setDisabled(true);
        }
        
        if(!PopupManager.Instance.onPopup())
            checkInput();
        
        render();
        if(gameSpeed > 0){
            if(inWave){
                wave.update();
                for(int i = enemies.size()-1 ; i >= 0 ; i--)
                    enemies.get(i).update();
                for(Enemy e : enemies)
                    e.render();
                if(enemySelected != null)
                    renderEnemySelected();
            }
            // Wave check
            if(inWave && wave.isDone()){
                overlays.get(1).getButtons().get(0).setHidden(false);
                overlays.get(1).getButtons().get(1).setHidden(true);
                inWave = false;
                wave = null;
                money += waveReward;
                if(!gameOver)
                    waveNumber++;
                gameSpeed = 1;
                SoundManager.Instance.closeAllClips();
                if(waveNumber%5 == 0){
                    int up = 0;
                    if(Math.random() > 0.5){
                        up = 4+(waveNumber/5)*(waveNumber/5);
                        Enemy.bonusLife += up;
                        PopupManager.Instance.enemiesUpgraded("+"+up+"% life point");
                    }
                    else{
                        up = 9+(waveNumber/5)*(waveNumber/5);
                        Enemy.bonusMS += up;
                        PopupManager.Instance.enemiesUpgraded("+"+up+"% move speed");
                    }
                }
            }
        }
        
        for(int i = 0 ; i < towers.size() ; i++){
            towers.get(i).update();
            if(towers.get(i).toRemove()){
                towers.remove(i);
                i--;
            }
        }
            
        
        renderOverlays();
        
        if(gameOver)
            gameOver();
        
        PopupManager.Instance.update();
    }
    
    protected void checkInput(){
        // Towers placement
        for(Tower t : towers){
            if(overlays.get(0).getButtons().get(0).isClicked(0))
                dontPlace = true;
            if(dontPlace && !Mouse.isButtonDown(0))
                dontPlace = false;
            if(!t.isPlaced() && Mouse.isButtonDown(0) && t.canBePlaced() && !dontPlace && !mouseDown){
                t.place(map);
                mouseDown = true;
                Towser.setCursor(Cursor.DEFAULT);
            }
            else if(!t.isPlaced() && Mouse.isButtonDown(1)){
                selectTower(null);
                t.destroy();
                Towser.setCursor(Cursor.DEFAULT);
            }
            if(t.isClicked(0) && towerSelected == null)
                selectTower(t);
            else if(t.isClicked(0) && towerSelected != null && towerSelected.isPlaced() && !overlays.get(0).isClicked(0))
                selectTower(t);
        }
        // Click check
        while(Mouse.next() || Keyboard.next()){
            // Reinitializing if clicking nowhere
            if((Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) && !mouseDown){
                if(towerSelected != null && !towerSelected.isClicked(0) && !overlays.get(0).isClicked(0) && !overlays.get(1).isClicked(0)){
                    selectTower(null);
                }
                if(enemySelected != null && !overlays.get(1).isClicked(0)){
                    if(towerSelected == null && !overlays.get(0).isClicked(0))
                        enemySelected = null;
                    else if(towerSelected != null && !towerSelected.isClicked(0) && !overlays.get(0).isClicked(0))
                        enemySelected = null;
                }
            }
            
            // Overlays inputs
            checkOverlaysInput();
        }
    }
    
    protected void renderEnemySelected(){
        Towser.drawCircle(enemySelected.getX(), enemySelected.getY(), enemySelected.getWidth()/2, Towser.colors.get("green_dark"));
        Towser.drawCircle(enemySelected.getX(), enemySelected.getY(), enemySelected.getWidth()/2+0.5f, Towser.colors.get("green_dark"));
        Towser.drawCircle(enemySelected.getX(), enemySelected.getY(), enemySelected.getWidth()/2+1, Towser.colors.get("green_dark"));
        Towser.drawCircle(enemySelected.getX(), enemySelected.getY(), enemySelected.getWidth()/2+1.5f, Towser.colors.get("green_dark"));
    }
    
    protected void render(){
        Towser.drawTextureID(0, 0, windWidth, windHeight, textureID);

        if(spawn != null)
            spawn.renderDirection();
    }
    
    protected void initOverlays(){
        overlays = new ArrayList<>();
        Overlay o;
        Button b;
        int size = (int) (58*ref);
        int sep = (int) (200*ref);
        
        o = new Overlay(0, windHeight-(int)(86*ref), windWidth, (int)(86*ref));
        o.setBG(Towser.textures.get("board"), 0.6f);
        o.setA(0.6f);
        b = new Button(windWidth/2 - 3*size/2 - 3*sep/2, (int)(55*ref), size, size, Towser.textures.get("basicTower"), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        b.setItemFramed(true);
        o.addButton(b);
        b = new Button(windWidth/2 - size/2 - sep/2, (int)(55*ref), size, size, Towser.textures.get("circleTower"), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        b.setItemFramed(true);
        o.addButton(b);
        b = new Button(windWidth/2 + size/2 + sep/2, (int)(55*ref), size, size, Towser.textures.get("bigTower"), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        b.setItemFramed(true);
        o.addButton(b);
        b = new Button(windWidth/2 + 3*size/2 + 3*sep/2, (int)(55*ref), size, size, Towser.textures.get("flameTower"), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        b.setItemFramed(true);
        o.addButton(b);
        overlays.add(o);
        
        o = new Overlay(0, 0, windWidth, (int)(60*ref));
        o.setBG(Towser.textures.get("board"), 0.6f);
        b = new Button(o.getW()-(int)(150*ref), o.getH()/2, (int)(150*ref), (int)(40*ref), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        o.addButton(b);
        b = new Button(o.getW()-(int)(150*ref), o.getH()/2, (int)(150*ref), (int)(40*ref), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        b.setHidden(true);
        o.addButton(b);
        
        b = new Button(o.getW()-(int)(400*ref), o.getH()/2, (int)(100*ref), (int)(30*ref), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        o.addButton(b);
        overlays.add(o);
        
        int width = (int) (700*ref), height = (int) (50*ref);
        o = new Overlay(windWidth/2-width/2, (int)(5*ref), width, height);
        o.setBG(Towser.textures.get("darkBoard"), 0.4f);
        o.setBorder(Towser.colors.get("green_dark"), 2, 0.8f);
        overlays.add(o);
    }
    
    public void renderOverlays(){ 
        String t;
        Overlay o;
        Button b;
        
        //// Overlay selection tours
        if(towerSelected == null || !towerSelected.isPlaced()){
            o = overlays.get(0);
            o.render();     
            
            t = BasicTower.priceP+"";
            b = o.getButtons().get(0);
            if(money >= BasicTower.priceP)
                b.drawText(0, -b.getH()/2-(int)(12*ref), t, Towser.fonts.get("canBuy"));
            else
                b.drawText(0, -b.getH()/2-(int)(12*ref), t, Towser.fonts.get("cantBuy"));
            
            t = CircleTower.priceP+"";
            b = o.getButtons().get(1);
            if(money >= CircleTower.priceP)
                b.drawText(0, -b.getH()/2-(int)(12*ref), t, Towser.fonts.get("canBuy"));
            else
                b.drawText(0, -b.getH()/2-(int)(12*ref), t, Towser.fonts.get("cantBuy"));
            
            t = BigTower.priceP+"";
            b = o.getButtons().get(2);
            if(money >= BigTower.priceP)
                b.drawText(0, -b.getH()/2-(int)(12*ref), t, Towser.fonts.get("canBuy"));
            else
                b.drawText(0, -b.getH()/2-(int)(12*ref), t, Towser.fonts.get("cantBuy"));
            
            t = FlameTower.priceP+"";
            b = o.getButtons().get(3);
            if(money >= FlameTower.priceP)
                b.drawText(0, -b.getH()/2-(int)(12*ref), t, Towser.fonts.get("canBuy"));
            else
                b.drawText(0, -b.getH()/2-(int)(12*ref), t, Towser.fonts.get("cantBuy"));
        }
        //
        //// Overlay principal
        o = overlays.get(1);
        o.render();
        
        t = money+"";
        o.drawText((int)(80*ref), o.getH()/2, t, Towser.fonts.get("money"));
        Towser.drawFilledRectangle((int)((90+8.8*t.length())*ref)-(int)(16*ref), o.getH()/2-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, Towser.textures.get("coins"));
        
        t = life+"";
        o.drawText((int)(200*ref), o.getH()/2, t, Towser.fonts.get("life"));
        Towser.drawFilledRectangle((int)((210+8.8*t.length())*ref)-(int)(16*ref), o.getH()/2-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, Towser.textures.get("heart"));

        if(!o.getButtons().get(0).isHidden()){
            t = "Wave " + waveNumber;
            o.getButtons().get(0).drawText(0, 0, t, Towser.fonts.get("normalB"));
        }
        if(!o.getButtons().get(1).isHidden()){
            t = "x"+gameSpeed;
            o.getButtons().get(1).drawText(0, 0, t, Towser.fonts.get("normalB"));
        }
        t = "Menu";
        o.getButtons().get(2).drawText(0, 0, t, Towser.fonts.get("normal"));
        //
        //// Overlay enemy selected
        o = overlays.get(2);
        o.render();
        if(enemySelected != null)
            enemySelected.renderInfo(o);
        else
            o.drawText(o.getW()/2, o.getH()/2, "Select an enemy", Towser.fonts.get("normal"));
        //
    }
    
    public void checkOverlaysInput(){
        Overlay o;
        // Overlay selections tours
        o = overlays.get(0);
        for(Button b : o.getButtons()) // Check tower clicked
            if(b.isClicked(0) && towerSelected == null)
                createTower(o.getButtons().indexOf(b));
        //
        // Overlay principal
        o = overlays.get(1);
        if(o.getButtons().get(0).isClicked(0) && !inWave && Mouse.getEventButtonState()){
            o.getButtons().get(0).setHidden(true);
            o.getButtons().get(1).setHidden(false);
            startWave();
        }
        else if(o.getButtons().get(1).isClicked(0)){
            switch(gameSpeed){
                case 1:
                    gameSpeed = 2;
                    break;
                case 2:
                    gameSpeed = 4;
                    break;
                case 4:
                    gameSpeed = 1;
                    break;
            }
        }
        if(o.getButtons().get(2).isClicked(0)){
            Towser.switchStateTo(MENU);
        }
        //
    }
    
    public void clearArrays(){
        int i;
        for(i = 0 ; i < enemiesDead.size() ; i++)
            enemies.remove(enemiesDead.get(i));
        enemiesDead.clear();
        for(i = 0 ; i < enemiesToAdd.size() ; i++)
            enemies.add(0, enemiesToAdd.get(i));
        enemiesToAdd.clear();
        for(i = 0 ; i < towersDestroyed.size() ; i++)
            towers.remove(towersDestroyed.get(i));
        towersDestroyed.clear();
    }

    @SuppressWarnings("unchecked")
    protected void startWave(){
        UEnemy[] uEnemies = UEnemy.values();
        int waveBalance = (waveNumber*waveNumber + waveNumber)/2;
        if(waveNumber >= uEnemies[uEnemies.length-1].enterAt + 1)
            waveBalance *= 13;// old : 10+(uEnemies[uEnemies.length-1].enterAt+ 1) - Math.min(waveNumber, 18);
        else
            waveBalance *= 10;
        
        wave = new Wave();
        int min, max;
        while(waveBalance >= uEnemies[0].balance){
            // Du plus fort au moins fort. Ils commencent à apparaitre à la vague n de max = waveNumber+min-n, et commencent à ne plus apparaitre à la vague n de decrease = (waveNumber+min-n+waveNumber-n) (si = 0, ne disparait jamais)
            for(int i = uEnemies.length-1 ; i >= 0 ; i--){
                min = 1+waveNumber-uEnemies[i].enterAt;
                max = min+(waveNumber-uEnemies[i].enterAt)*2;
                if(min > uEnemies[i].nbMax) min = uEnemies[i].nbMax;
                if(max > uEnemies[i].nbMax) max = uEnemies[i].nbMax;
                waveBalance = uEnemies[i].addToWave((int) Math.floor(min+Math.random()*(max-min)), waveBalance);
            }
        }
        wave.shuffleEnemies();
        enemies = (ArrayList<Enemy>)wave.getEnnemies().clone();

        inWave = true;
    }
    
    public void createTower(int id){
        if(towerSelected != null){
            if(!towerSelected.isPlaced())
                towerSelected.destroy();
            towerSelected.setSelected(false);
            towerSelected = null;
        }
        Tower tower = null;
        switch(id){
            case 0 :
                tower = new BasicTower();
                break;
            case 1 :
                tower = new CircleTower();
                break;
            case 2 :
                tower = new BigTower();
                break;
            case 3 :
                tower = new FlameTower();
                break;
        }
        if(tower != null && tower.getPrice() <= money){
            towers.add(tower);
            towerSelected = tower;
            Towser.setCursor(Cursor.GRAB);
        }
    }
    
    public int getMouseIndexX(){
        int indexX = Mouse.getX()/unite;
        if(indexX < 0) indexX = 0;
        else if(indexX > Towser.nbTileX-1) indexX = Towser.nbTileX-1;
        return indexX;
    }
    
    public int getMouseIndexY(){
        int indexY = (windHeight-Mouse.getY())/unite;
        if(indexY < 0) indexY = 0;
        else if(indexY > Towser.nbTileY-1) indexY = Towser.nbTileY-1;
        return indexY;
    }
    
    public void getAttackedBy(int p){
        life -= p;
        if(life <= 0){
            gameOver = true;
            life = 0;
        } 
    }

    protected static void gameOver(){
        PopupManager.Instance.gameOver();
    }
    
    public void selectTower(Tower t){
        if(towerSelected != null)
            towerSelected.setSelected(false);
        towerSelected = t;
        if(t != null)
            t.setSelected(true);
        mouseDown = true;
    }
    
    public void disableAllButtons(){
        for(Overlay o : overlays)
            for(Button b : o.getButtons())
                b.setDisabled(true);
    }
    
    public void enableAllButtons(){
        for(Overlay o : overlays)
            for(Button b : o.getButtons())
                b.setDisabled(false);
    }
    
    public void addEnemy(Enemy e){
        enemiesToAdd.add(0, e);
        wave.addEnemy(e);
    }
    
    public ArrayList<Enemy> getEnnemiesDead(){
        return enemiesDead;
    }
    
    public int getLife(){
        return life;
    }
}