package rvb;

import Buffs.Buff;
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
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import managers.PopupManager;
import managers.TextManager.Text;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB.Cursor;
import rvb.RvB.Difficulty;
import static rvb.RvB.State.MENU;
import static rvb.RvB.game;
import static rvb.RvB.mouseDown;
import static rvb.RvB.unite;
import static rvb.RvB.ref;
import static rvb.RvB.windHeight;
import static rvb.RvB.windWidth;
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
                game.wave.addEnemy(e);
                balance -= this.balance;
            }
            return balance;
        }
    }
    
    public ArrayList<ArrayList<Tile>> map;
    public Tile spawn, base;
    public int money, life, waveNumber, waveReward, nbTower = 2, gameSpeed = 1, enemiesBonusLife = 0, enemiesBonusMS = 0, lvlBazoo = 0;
    public int oldRaztechXpos = -1, oldRaztechYpos = -1;
    private int oldGameSpeed = 0;
    public ArrayList<Shootable> towers, towersDestroyed;
    public ArrayList<Shootable> enemies, enemiesDead, enemiesToAdd;
    public ArrayList<Tile> path;
    public ArrayList<Rock> rocks;
    public Stack<Buff> buffs;
    protected boolean gameOver;
    public boolean inWave;
    public Enemy enemySelected = null;
    public boolean ended = false;
    public Tower towerSelected;
    public Raztech raztech = null;
    protected Wave wave;
    protected ArrayList<Overlay> overlays;
    public boolean bossDead = false, bossDefeated = false;
    private boolean keyDown = false;
    private static Random random;
    protected int textureID = -10;
    private float waveBalanceMult;
    public int timeInGamePassed;
    public int basicTowerPrice, circleTowerPrice, flameTowerPrice, bigTowerPrice;
    
    public AppCore(){
        random = new Random();
    }
    
    protected void init(Difficulty diff){
        rocks = new ArrayList<>();
        towers = new ArrayList<>();
        towersDestroyed = new ArrayList<>();
        enemies = new ArrayList<>();
        enemiesDead = new ArrayList<>();
        enemiesToAdd = new ArrayList<>();
        buffs = Buff.initBuffStack();
        gameOver = false;
        inWave = false;
        towerSelected = null;
        timeInGamePassed = 0;
        
        basicTowerPrice = BasicTower.startPrice;
        circleTowerPrice = CircleTower.startPrice;
        flameTowerPrice = FlameTower.startPrice;
        bigTowerPrice = BigTower.startPrice;
        
        waveNumber = 1;
        if(diff == Difficulty.EASY){
            life = 125;
            money = 250;
            waveReward = 80;
            waveBalanceMult = 0.9f;
        }
        else if(diff == Difficulty.HARD){
            life = 75;
            money = 150;
            waveReward = 20;
            waveBalanceMult = 1.1f;
        }
        else{ //if(diff == Difficulty.MEDIUM)
            life = 100;
            money = 200;
            waveReward = 50;
            waveBalanceMult = 1f;
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
        //addRocks();
    }
    
    protected void initMap(ArrayList<Tile> path){
        map = new ArrayList<>();
        this.path = new ArrayList<>();
        if(path.isEmpty()){
            this.path = path;
            return;
        }
        ArrayList<Tile> row;
        Texture t;
        Tile tile;
        int n;
        for(int i = 0 ; i < RvB.nbTileY ; i++){
            row = new ArrayList<>();
            for(int j = 0 ; j < RvB.nbTileX ; j++){
                n = random.nextInt(100)+1;
                if(n > 93)
                    t = RvB.textures.get("bigPlant1");
                else if(n > 86)
                    t = RvB.textures.get("bigPlant2");
                else
                    t = RvB.textures.get("grass");
                n = 0;
                if(t != RvB.textures.get("grass"))
                    n = (int)Math.round(random.nextInt(361)/90)*90;  
                tile = new Tile(t, "grass");
                if(i == 0 || i == RvB.nbTileY-1)
                    tile.type = "null";
                tile.setAngle(n);
                tile.setRotateIndex(0);
                tile.setX(j*unite);
                tile.setY(i*unite);

                row.add(tile);
            }
            map.add(row);
        }
        for(Tile road : path){
            tile = new Tile(RvB.textures.get("roadStraight"), "road");
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
        //addRocks();
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
                            n = random.nextInt(100)+1;
                            if(n > 93)
                                t = RvB.textures.get("bigPlant1");
                            else if(n > 86)
                                t = RvB.textures.get("bigPlant2");
                            else
                                t = RvB.textures.get("grass");
                            n = 0;
                            if(t != RvB.textures.get("grass"))
                                n = Math.round(random.nextInt(361)/90)*90;  
                            tile = new Tile(t, "grass");
                            tile.setAngle(n);
                            tile.setRotateIndex(0);
                            break;
                        case "0":
                            tile = new Tile(RvB.textures.get("roadStraight"), "road");
                            tile.setRotateIndex(0);
                            break;
                        case "S":
                            tile = new Tile(RvB.textures.get("roadStraight"), "road");
                            tile.setRotateIndex(0);
                            break;
                        case "B":
                            tile = new Tile(RvB.textures.get("roadStraight"), "road");
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

                    if(row.size() == RvB.nbTileX){
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
            System.out.println("File : "+filePath+" doesn't exist.");
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
                road.setTexture(RvB.textures.get("roadTurn"));
                road.setAngle(180);
            }
            // Si previousRoad est à GAUCHE et nextRoad est en HAUT ou l'inverse
            else if((previousRoad.getX()+unite/2 < road.getX() && nextRoad.getY()+unite/2 < road.getY()) || (nextRoad.getX()+unite/2 < road.getX() && previousRoad.getY()+unite/2 < road.getY())){
                road.setTexture(RvB.textures.get("roadTurn"));
                road.setAngle(270);
            }
            // Si previousRoad est à DROITE et nextRoad est en BAS ou l'inverse
            else if((previousRoad.getX()+unite/2 > road.getX()+unite && nextRoad.getY()+unite/2 > road.getY()+unite) || (nextRoad.getX()+unite/2 > road.getX()+unite && previousRoad.getY()+unite/2 > road.getY()+unite)){
                road.setTexture(RvB.textures.get("roadTurn"));
                road.setAngle(90);
            }
            // Si previousRoad est à DROITE et nextRoad est en HAUT ou l'inverse
            else if((previousRoad.getX()+unite/2 > road.getX()+unite && nextRoad.getY()+unite/2 < road.getY()) || (nextRoad.getX()+unite/2 > road.getX()+unite && previousRoad.getY()+unite/2 < road.getY())){
                road.setTexture(RvB.textures.get("roadTurn"));
                road.setAngle(0);
            }
            // Si previousRoad est à DROITE et nextRoad est à GAUCHE ou l'inverse
            else if((previousRoad.getX()+unite/2 > road.getX()+unite && nextRoad.getX()+unite/2 < road.getX()) || (nextRoad.getX()+unite/2 > road.getX()+unite && previousRoad.getX()+unite/2 < road.getX())){
                road.setTexture(RvB.textures.get("roadStraight"));
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
        
        Tile tile;
        for(int i = 0 ; i < map.size() ; i++){
            for(int j = 0 ; j < map.get(i).size() ; j++){
                tile = map.get(i).get(j);
                
                AffineTransform context = g2d.getTransform();
                
                g2d.rotate(Math.toRadians(tile.getAngle()), j*unite+unite/2, i*unite+unite/2);
 
                if(map.get(i).get(j).getTexture() == RvB.textures.get("roadStraight"))
                    g2d.drawImage(RS, j*unite, i*unite, null);
                else if(map.get(i).get(j).getTexture() == RvB.textures.get("roadTurn"))
                    g2d.drawImage(RT, j*unite, i*unite, null);
                else if(map.get(i).get(j).getTexture() == RvB.textures.get("bigPlant1"))
                    g2d.drawImage(P1, j*unite, i*unite, null);
                else if(map.get(i).get(j).getTexture() == RvB.textures.get("bigPlant2"))
                    g2d.drawImage(P2, j*unite, i*unite, null);
                else
                    g2d.drawImage(GR, j*unite, i*unite, null);
                
                g2d.setTransform(context);
            }
        }

        textureID = RvB.loadTexture(mapImage);
    }
    
    public void addRocks(){
        float p;
        for(int i = 0 ; i < map.size() ; i++)
            for(int j = 0 ; j < map.get(i).size() ; j++){
                if(map.get(i).get(j).type != "grass")
                    continue;
                p = 0.01f;
                // S'il y a une road à côté
                if((i > 0 && map.get(i-1).get(j).type == "road") || (i < RvB.nbTileY-1 && map.get(i+1).get(j).type == "road") || (j > 0 && map.get(i).get(j-1).type == "road") || (j < RvB.nbTileX-1 && map.get(i).get(j+1).type == "road"))
                    p = 0.08f;
                // Ou s'il y a une road sur les diago
                else if((i > 0 && j > 0 && map.get(i-1).get(j-1).type == "road") || (i < RvB.nbTileY-1 && j < RvB.nbTileX-1 && map.get(i+1).get(j+1).type == "road") || (i > 0 && j < RvB.nbTileX-1 && map.get(i-1).get(j+1).type == "road") || (i < RvB.nbTileY-1 && j > 0 && map.get(i+1).get(j-1).type == "road"))
                    p = 0.08f;
                if(random.nextFloat() <= p){
                    Rock r = new Rock(j*unite, i*unite, random.nextInt(3));
                    rocks.add(r);
                    map.get(i).set(j, r);
                }  
            }
    }
    
    public void update(){
        if(gameSpeed > 0 && RvB.state == RvB.State.GAME){
            timeInGamePassed += RvB.deltaTime*gameSpeed;
        }
        
        if(!PopupManager.Instance.onPopup())
            checkInput();
        
        clearArrays();
        
        if(this == RvB.game && !inWave && !PopupManager.Instance.onPopup()){
            if(SoundManager.Instance.isReady())
                overlays.get(1).getButtons().get(0).enable();
            else
                overlays.get(1).getButtons().get(0).disable();
        }
        
        render();
        
        for(Shootable t : towers)
            t.update();
        
        if(inWave){
            wave.update();
            for(int i = enemies.size()-1 ; i >= 0 ; i--)
                enemies.get(i).update();
            for(Shootable e : enemies)
                e.render();
            if(enemySelected != null)
                renderEnemySelected();
        }
        if(gameSpeed > 0){
            // Wave check if done
            if(inWave && wave.isDone()){
                overlays.get(1).getButtons().get(0).enable();
                inWave = false;
                wave = null;
                money += waveReward;
                if(!gameOver)
                    waveNumber++;
                SoundManager.Instance.closeAllClips();
                if(bossDead){
                    enemiesBonusLife += 10;
                    enemiesBonusMS += 5;
                    PopupManager.Instance.enemiesUpgraded(new String[]{
                        "+10% "+Text.HP.getText(),
                        "+5% "+Text.MS.getText()
                    });
                    bossDead = false;
                    bossDefeated = false;
                }
            }
        }
        
        renderOverlays();
        
        if(gameOver)
            gameOver();
    }
    
    protected void checkInput(){
        // Towers placement
        if(towerSelected != null && !towerSelected.isPlaced()){
            if(Mouse.isButtonDown(0) && towerSelected.canBePlaced() && !mouseDown){
                // place tower
                towerSelected.place(map);
                mouseDown = true;
                RvB.setCursor(Cursor.DEFAULT);
            }
            else if(Mouse.isButtonDown(1) && !towerSelected.isForBuff()){
                if(towerSelected == raztech){
                    if(oldRaztechXpos > 0){
                        raztech.x = oldRaztechXpos;
                        raztech.y = oldRaztechYpos;
                        raztech.setIsPlaced(true);
                    }
                    else
                        raztech = null;
                }
                // destroy tower
                selectTower(null);
                RvB.setCursor(Cursor.DEFAULT);
            }
        }
        // Click check
        while(Mouse.next()){
            // Reinitializing if left clicking nowhere
            if(Mouse.isButtonDown(0) && !mouseDown){ //If left or right click
                if(!overlays.get(1).isClicked(0) && !overlays.get(0).isClicked(0)){ //If game overlays aren't clicked
                    if(towerSelected != null && !towerSelected.isClicked(0) && !anyEnemyClicked(0)){
                        selectTower(null);
                    }
                    if(enemySelected != null && !enemySelected.isClicked(0) && !anyTowerClicked(0)){
                        setEnemySelected(null);
                    }
                }
            }
        }
        // RACCOURCIS CLAVIER
        keyDown = Keyboard.getEventKeyState();
        if(!PopupManager.Instance.onPopup() && !keyDown && !Keyboard.isKeyDown(Keyboard.KEY_F1)){
            // PAUSE
            if(Keyboard.isKeyDown(Keyboard.KEY_P) && inWave){
                if(gameSpeed > 0)
                    pause();
                else
                    unpause();
            } 
            // POPUP HELP
            else if(Keyboard.isKeyDown(Keyboard.KEY_H)){
                RvB.debug("Popup help");
            } 
            if(gameSpeed > 0){
                // START WAVE
                if(Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !inWave){
                    overlays.get(1).getButtons().get(0).click();
                } 
                // TOWERS
                if(Keyboard.isKeyDown(Keyboard.KEY_R))
                    overlays.get(0).getButtons().get(overlays.get(0).getButtons().size()-1).click();
                else if(Keyboard.isKeyDown(Keyboard.KEY_1) && basicTowerPrice <= money)
                    overlays.get(0).getButtons().get(0).click();
                else if(Keyboard.isKeyDown(Keyboard.KEY_2) && circleTowerPrice <= money)
                    overlays.get(0).getButtons().get(1).click();
                else if(Keyboard.isKeyDown(Keyboard.KEY_3) && bigTowerPrice <= money)
                    overlays.get(0).getButtons().get(2).click();
                else if(Keyboard.isKeyDown(Keyboard.KEY_4) && flameTowerPrice <= money)
                    overlays.get(0).getButtons().get(3).click();
            }
        }
    }
    
    public boolean anyTowerClicked(int but){
        for(Shootable t : towers)
            if(t.isClicked(but))
                return true;
        return false;
    }
    
    public boolean anyEnemyClicked(int but){
        for(Shootable e : enemies)
            if(e.isClicked(but))
                return true;
        return false;
    }
    
    protected void renderEnemySelected(){
        RvB.drawCircle(enemySelected.getX(), enemySelected.getY(), enemySelected.getHitboxWidth()/2, RvB.colors.get("green_dark"));
        RvB.drawCircle(enemySelected.getX(), enemySelected.getY(), enemySelected.getHitboxWidth()/2+0.5f, RvB.colors.get("green_dark"));
        RvB.drawCircle(enemySelected.getX(), enemySelected.getY(), enemySelected.getHitboxWidth()/2+1, RvB.colors.get("green_dark"));
        RvB.drawCircle(enemySelected.getX(), enemySelected.getY(), enemySelected.getHitboxWidth()/2+1.5f, RvB.colors.get("green_dark"));
    }
    
    protected void render(){
        RvB.drawTextureID(0, 0, windWidth, windHeight, textureID);

        if(spawn != null)
            spawn.renderDirection();
        
        for(Tile road : path)
            road.renderSteps();
        for(Rock rock : rocks)
            rock.render();
    }
    
    protected void initOverlays(){
        overlays = new ArrayList<>();
        Overlay o;
        Button b;
        int nbTower = 4;
        int size = (int) (50*ref);
        int sep = (int) (200*ref);
        int startPos = windWidth/2 - (nbTower-1)*size/2 - (nbTower-1)*sep/2;
        
        // Overlay tours
        o = new Overlay(0, windHeight-(int)(60*ref), windWidth, (int)(60*ref));
        o.setBG(RvB.textures.get("board"), 0.6f);
        String textureName = "";
        for(int i = 0 ; i < nbTower ; i++){
            switch(i){
                case 0:
                    textureName = "basicTower";
                    break;
                case 1:
                    textureName = "circleTower";
                    break;
                case 2:
                    textureName = "bigTower";
                    break;
                case 3:
                    textureName = "flameTower";
                    break;
            }
            b = new Button(startPos + (size + sep)*i, (int)(30*ref), size, size, RvB.textures.get(textureName), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
            b.setItemFramed(true);
            if(i > 0)
                b.lock();
            int index = i;
            b.setFunction(__ -> {
                if(towerSelected != null)
                    selectTower(null);
                createTower(index);
            });
            o.addButton(b);
        }
        b = new Button(size + sep, (int)(30*ref), size, size, RvB.textures.get("raztech"), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setItemFramed(true);
        b.setFunction(__ -> {
            if(towerSelected != null)
                selectTower(null);
            createTower(4);
        });
        o.addButton(b);
        overlays.add(o);
        
        // Overlay top
        o = new Overlay(0, 0, windWidth, (int)(60*ref));
        o.setBG(RvB.textures.get("board"), 0.6f);
        // Wave button
        b = new Button(o.getW()-(int)(150*ref), o.getH()/2, (int)(150*ref), (int)(40*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setClickSound(SoundManager.SOUND_WAVE, SoundManager.Volume.VERY_HIGH);
        Button thisBut = b;
        b.setFunction(__ -> {
            if(!inWave){
                thisBut.disable();
                RvB.setCursor(Cursor.DEFAULT);
                startWave();
            }
        });
        o.addButton(b);
        // game speed button
        b = new Button(o.getW()-(int)(350*ref), o.getH()/2, (int)(60*ref), (int)(30*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
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
        });
        o.addButton(b);
        // back button
        b = new Button((int)(60*ref), o.getH()/2, (int)(32*ref), (int)(32*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setBG(RvB.textures.get("arrowBack"));
        b.setFunction(__ -> {
            RvB.switchStateTo(MENU);
        });
        o.addButton(b);
        overlays.add(o);
        
        // overlay enemy selected
        int width = (int) (700*ref), height = (int) (50*ref);
        o = new Overlay(windWidth/2-width/2, (int)(5*ref), width, height);
        o.setBG(RvB.textures.get("darkBoard"), 0.4f);
        o.setBorder(RvB.colors.get("green_dark"), 2, 0.8f);
        overlays.add(o);
    }
    
    public void renderOverlays(){ 
        String t;
        Overlay o;
        Button b;
        
        //// Overlay selection tours
        if(towerSelected == null){
            o = overlays.get(0);
            o.render();     
            
            b = o.getButtons().get(0);
            if(!b.isLocked())
                drawPrice(basicTowerPrice, b, o);
            
            b = o.getButtons().get(1);
            if(!b.isLocked())
                drawPrice(circleTowerPrice, b, o);
            
            b = o.getButtons().get(2);
            if(!b.isLocked())
                drawPrice(bigTowerPrice, b, o);
            
            b = o.getButtons().get(3);
            if(!b.isLocked())
                drawPrice(flameTowerPrice, b, o);
            
            if(raztech != null){
                int width = (int) (150*ref), height = (int) (14*ref), x = (int)(30*ref), y = o.getY()+2*o.getH()/3-height/2;
                int xpWidth = (int) ((float)(raztech.xp)/(float)(raztech.maxXP) * width);
                if(xpWidth < 0) xpWidth = 0;
                
                o.drawText(x+width/2, o.getH()/3, Text.LVL.getText()+raztech.lvl, RvB.fonts.get("normalS"));
                RvB.drawFilledRectangle(x, y, width, height, RvB.colors.get("lightGreen"), 1f, null);
                RvB.drawFilledRectangle(x, y, xpWidth, height, RvB.colors.get("lightBlue"), 1f, null);
                RvB.drawRectangle(x, y, width, height, RvB.colors.get("green_dark"), 1f, 4);
            }
        }
        //
        //// Overlay principal
        o = overlays.get(1);
        o.render();
        
        t = money+"";
        o.drawText((int)(200*ref), o.getH()/2, t, RvB.fonts.get("money"));
        o.drawImage((int)((210+8.8*t.length())*ref), o.getH()/2, (int)(32*ref), (int)(32*ref), RvB.textures.get("coins"));
        
        t = life+"";
        o.drawText((int)(320*ref), o.getH()/2, t, RvB.fonts.get("life"));
        o.drawImage((int)((330+8.8*t.length())*ref), o.getH()/2, (int)(32*ref), (int)(32*ref), RvB.textures.get("heart"));
        
        t = inWave ? Text.DEFENDING.getText() : Text.START_WAVE.getText();
        if(inWave)
            t = Text.DEFENDING.getText();
        else{
            if(!SoundManager.Instance.isReady())
                t = Text.WAITING.getText();
            else
                t = Text.START_WAVE.getText();
        }
        o.getButtons().get(0).drawText(0, 0, t, RvB.fonts.get(inWave ? "normal" : "normalB"));
            
        t = "x"+gameSpeed;
        o.getButtons().get(1).drawText(0, 0, t, RvB.fonts.get("normalB"));
        //
        if(enemySelected != null && !enemySelected.isDead())
            enemySelected.renderInfo();
    }
    
    private void drawPrice(int priceP, Button b, Overlay o){
        if(money >= priceP){
            b.drawText(-(int)(12*ref), -b.getH()/2-(int)(10*ref), priceP+"", RvB.fonts.get("canBuy"));
            o.drawImage(b.getX()+(int)((6+14)*ref), b.getY()-o.getY()-b.getH()/2-(int)(10*ref), (int)(28*ref), (int)(28*ref), RvB.textures.get("coins"));
        } 
        else{
            b.drawText(-(int)(12*ref), -b.getH()/2-(int)(10*ref), priceP+"", RvB.fonts.get("cantBuy"));
            o.drawImage(b.getX()+(int)((6+14)*ref), b.getY()-o.getY()-b.getH()/2-(int)(10*ref), (int)(28*ref), (int)(28*ref), RvB.textures.get("coinsCantBuy"));
        } 
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
            waveBalance *= 15;
        else
            waveBalance *= 10;
        if(waveNumber >= uEnemies[1].enterAt)
            waveBalance *= waveBalanceMult;
        waveBalance = (int) (bossRound() ? waveBalance*0.6 : waveBalance);
        wave = new Wave();
        int min, max;
        while(waveBalance >= uEnemies[0].balance){
            // Du plus fort au moins fort. Ils commencent à apparaitre à la vague n de max = waveNumber+min-n, et commencent à ne plus apparaitre à la vague n de decrease = (waveNumber+min-n+waveNumber-n) (si = 0, ne disparait jamais)
            for(int i = uEnemies.length-1 ; i >= 0 ; i--){
                min = 1+waveNumber-uEnemies[i].enterAt;
                max = min+(waveNumber-uEnemies[i].enterAt)*2;
                if(min > uEnemies[i].nbMax) min = uEnemies[i].nbMax;
                if(max > uEnemies[i].nbMax) max = uEnemies[i].nbMax;
                waveBalance = uEnemies[i].addToWave((int) Math.floor(min+random.nextFloat()*(max-min)), waveBalance);
            }
        }
        wave.shuffleEnemies();
        if(bossRound())
            wave.addEnemy(new Bazoo(lvlBazoo++));
        enemies = (ArrayList<Shootable>)wave.getEnnemies().clone();
        inWave = true;
    }
    
    public void createTower(int id){
        if(towerSelected != null){
            selectTower(null);
        }
        Tower tower = null;
        int price = 0;
        switch(id){
            case 0 :
                tower = new BasicTower();
                price = basicTowerPrice;
                break;
            case 1 :
                tower = new CircleTower();
                price = circleTowerPrice;
                break;
            case 2 :
                tower = new BigTower();
                price = bigTowerPrice;
                break;
            case 3 :
                tower = new FlameTower();
                price = flameTowerPrice;
                break;
            case 4 :
                if(raztech == null){
                   tower = new Raztech(); 
                }
                else{
                    raztech.setIsPlaced(false);
                    map.get((oldRaztechYpos-unite/2)/unite).set((oldRaztechXpos-unite/2)/unite, new Tile("grass"));
                    tower = raztech;
                } 
                break;
            case 101 :
                tower = new PowerTower();
                break;
            case 102 :
                tower = new RangeTower();
                break;
            /*case 103 :
                tower = new ShootRateTower();
                break;*/
        }
        if(tower != null && (price <= money || id > 100)){
            if(!towers.contains(tower))
                towers.add(0, tower);
            towerSelected = tower;
            RvB.setCursor(Cursor.GRAB);
        }
    }
    
    public void raisePrice(Tower t){
        switch(t.type){
            case "basicTower":
                basicTowerPrice *= 1.2;
                break;
            case "circleTower":
                circleTowerPrice *= 1.08;
                break;
            case "bigTower":
                bigTowerPrice *= 1.1;
                break;
            case "flameTower":
                flameTowerPrice *= 1.1;
                break;
        }
    }
    
    public int getMouseIndexX(){
        int indexX = Mouse.getX()/unite;
        if(indexX < 0) indexX = 0;
        else if(indexX > RvB.nbTileX-1) indexX = RvB.nbTileX-1;
        return indexX;
    }
    
    public int getMouseIndexY(){
        int indexY = (windHeight-Mouse.getY())/unite;
        if(indexY < 0) indexY = 0;
        else if(indexY > RvB.nbTileY-1) indexY = RvB.nbTileY-1;
        return indexY;
    }
    
    public void getAttackedBy(float p){
        life -= p;
        if(life <= 0){
            gameOver = true;
            life = 0;
        } 
    }

    public void pause(){
        if(inWave){
            oldGameSpeed = gameSpeed;
            gameSpeed = 0;
            SoundManager.Instance.pauseAll();
        }
    }
    
    public void unpause(){
        if(inWave){
            gameSpeed = oldGameSpeed;
            SoundManager.Instance.unpauseAll();
        }
    }
    
    public static boolean bossRound(){
        return game.waveNumber%6 == 0;
    }
    
    protected static void gameOver(){
        PopupManager.Instance.gameOver();
    }
    
    public void selectTower(Tower t){
        towerSelected = t;
        mouseDown = true;
    }
    
    public void disableAllButtons(){
        for(Overlay o : overlays)
            for(Button b : o.getButtons())
                b.disable();
    }
    
    public void enableAllButtons(){
        for(Overlay o : overlays)
            for(Button b : o.getButtons())
                b.enable();
    }

    public void setEnemySelected(Enemy e) {
        if(e == null)
            overlays.get(2).display(false);
        else
            overlays.get(2).display(true);
        enemySelected = e;
    }
    
    public ArrayList<Overlay> getOverlays() {
        return overlays;
    }
    
    public void addEnemy(Enemy e){
        enemiesToAdd.add(0, e);
        wave.addEnemy(e);
    }
    
    public int getLife(){
        return life;
    }
}