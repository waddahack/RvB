package rvb;

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
import managers.TextManager.Text;
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
    public int money, life, waveNumber, waveReward, nbTower = 2, gameSpeed = 1;
    public ArrayList<Tower> towers, towersDestroyed;
    public ArrayList<Enemy> enemies, enemiesDead, enemiesToAdd;
    public ArrayList<Tile> path;
    public ArrayList<Rock> rocks;
    protected boolean gameOver;
    protected boolean inWave, dontPlace, towerHovered = false;
    public Enemy enemySelected = null;
    public boolean ended = false;
    public Tower towerSelected;
    protected Wave wave;
    protected ArrayList<Overlay> overlays;
    public boolean bossDead = false, bossDefeated = false;
    private static Random random;
    protected int textureID = -10;
    private float waveBalanceMult;
    
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
        gameOver = false;
        inWave = false;
        dontPlace = false;
        towerSelected = null;
        
        BasicTower.priceP = BasicTower.startPrice;
        CircleTower.priceP = CircleTower.startPrice;
        FlameTower.priceP = FlameTower.startPrice;
        BigTower.priceP = BigTower.startPrice;
        Bazoo.bossLevel = 0;
        Enemy.bonusLife = 0;
        Enemy.bonusMS = 0;
        
        waveNumber = 6;
        if(diff == Difficulty.EASY){
            life = 125;
            money = 325;
            waveReward = 275;
            waveBalanceMult = 0.9f;
        }
        else if(diff == Difficulty.HARD){
            life = 75;
            money = 275;
            waveReward = 225;
            waveBalanceMult = 1.1f;
        }
        else{ //if(diff == Difficulty.MEDIUM)
            life = 100;
            money = 300;
            waveReward = 250;
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
        clearArrays();
        
        if(this == RvB.game && !inWave){
            if(SoundManager.Instance.isReady())
                overlays.get(1).getButtons().get(0).enable();
            else
                overlays.get(1).getButtons().get(0).disable();
        }
        
        if(!PopupManager.Instance.onPopup())
            checkInput();
        
        render();
        
        for(int i = 0 ; i < towers.size() ; i++){
            towers.get(i).update();
            if(towers.get(i).toRemove()){
                towers.remove(i);
                i--;
            }  
        }
        
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
                    Enemy.bonusLife += 10;
                    Enemy.bonusMS += 5;
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
        for(Tower t : towers){
            if(overlays.get(0).getButtons().get(0).isClicked(0))
                dontPlace = true;
            if(dontPlace && !Mouse.isButtonDown(0))
                dontPlace = false;
            if(!t.isPlaced() && Mouse.isButtonDown(0) && t.canBePlaced() && !dontPlace && !mouseDown){
                t.place(map);
                mouseDown = true;
                RvB.setCursor(Cursor.DEFAULT);
            }
            else if(!t.isPlaced() && Mouse.isButtonDown(1)){
                selectTower(null);
                t.destroy();
                RvB.setCursor(Cursor.DEFAULT);
            }
            if(!mouseDown && t.isPlaced()){
                if(t.isClicked(0) && towerSelected == null)
                    selectTower(t);
                else if(t.isClicked(0) && towerSelected != null && t == towerSelected && !overlays.get(0).isClicked(0))
                    selectTower(null);
                else if(t.isClicked(0) && towerSelected != null && towerSelected.isPlaced() && !overlays.get(0).isClicked(0))
                    selectTower(t);
            }
            
            
        }
        // Click check
        while(Mouse.next()){
            // Reinitializing if clicking nowhere
            if((Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) && !mouseDown){
                if(towerSelected != null && !towerSelected.isClicked(0) && !overlays.get(0).isClicked(0) && !overlays.get(1).isClicked(0)){
                    selectTower(null);
                }
                if(enemySelected != null && !overlays.get(1).isClicked(0)){
                    if(towerSelected == null && !overlays.get(0).isClicked(0))
                        setEnemySelected(null);
                    else if(towerSelected != null && !towerSelected.isClicked(0) && !overlays.get(0).isClicked(0))
                        setEnemySelected(null);
                }
            }
        }
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
                case 1:
                    textureName = "circleTower";
                    break;
                case 2:
                    textureName = "bigTower";
                    break;
                case 3:
                    textureName = "flameTower";
                    break;
                default: // 0 or default
                    textureName = "basicTower";
                    break;
            }
            b = new Button(startPos + (size + sep)*i, (int)(30*ref), size, size, RvB.textures.get(textureName), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
            b.setItemFramed(true);
            int index = i;
            b.setFunction(__ -> {
                if(towerSelected == null)
                    createTower(index);
            });
            o.addButton(b);
        }
        overlays.add(o);
        
        // Overlay top
        o = new Overlay(0, 0, windWidth, (int)(60*ref));
        o.setBG(RvB.textures.get("board"), 0.6f);
        // Wave button
        b = new Button(o.getW()-(int)(150*ref), o.getH()/2, (int)(150*ref), (int)(40*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setClickSound(SoundManager.SOUND_WAVE, SoundManager.Volume.VERY_HIGH);
        Button thisBut = b;
        b.setFunction(__ -> {
            if(!inWave && Mouse.getEventButtonState()){
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
        if(towerSelected == null || !towerSelected.isPlaced()){
            o = overlays.get(0);
            o.render();     
            
            b = o.getButtons().get(0);
            drawPrice(BasicTower.priceP, b, o);
            
            b = o.getButtons().get(1);
            drawPrice(CircleTower.priceP, b, o);
            
            b = o.getButtons().get(2);
            drawPrice(BigTower.priceP, b, o);
            
            b = o.getButtons().get(3);
            drawPrice(FlameTower.priceP, b, o);
        }
        //
        //// Overlay principal
        o = overlays.get(1);
        o.render();
        
        t = money+"";
        o.drawText((int)(200*ref), o.getH()/2, t, RvB.fonts.get("money"));
        o.drawImage((int)((210+8.8*t.length())*ref)-(int)(16*ref), o.getH()/2-(int)(16*ref), (int)(32*ref), (int)(32*ref), RvB.textures.get("coins"));
        
        t = life+"";
        o.drawText((int)(320*ref), o.getH()/2, t, RvB.fonts.get("life"));
        o.drawImage((int)((330+8.8*t.length())*ref)-(int)(16*ref), o.getH()/2-(int)(16*ref), (int)(32*ref), (int)(32*ref), RvB.textures.get("heart"));
        
        t = inWave ? Text.DEFENDING.getText() : Text.START_WAVE.getText();
        o.getButtons().get(0).drawText(0, 0, t, RvB.fonts.get(inWave ? "normal" : "normalB"));
            
        t = "x"+gameSpeed;
        o.getButtons().get(1).drawText(0, 0, t, RvB.fonts.get("normalB"));
        //
        if(enemySelected != null)
            enemySelected.renderInfo();
    }
    
    private void drawPrice(int priceP, Button b, Overlay o){
        if(money >= priceP){
            b.drawText(-(int)(12*ref), -b.getH()/2-(int)(10*ref), priceP+"", RvB.fonts.get("canBuy"));
            o.drawImage(b.getX()+(int)(6*ref), b.getY()-o.getY()-b.getH()/2-(int)((10+14)*ref), (int)(28*ref), (int)(28*ref), RvB.textures.get("coins"));
        } 
        else{
            b.drawText(-(int)(12*ref), -b.getH()/2-(int)(10*ref), priceP+"", RvB.fonts.get("cantBuy"));
            o.drawImage(b.getX()+(int)(6*ref), b.getY()-o.getY()-b.getH()/2-(int)((10+14)*ref), (int)(28*ref), (int)(28*ref), RvB.textures.get("coinsCantBuy"));
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
        waveBalance = (int) (bossRound() ? waveBalance*0.5 : waveBalance);
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
            wave.addEnemy(new Bazoo(Bazoo.bossLevel));
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
            RvB.setCursor(Cursor.GRAB);
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
    
    public void getAttackedBy(int p){
        life -= p;
        if(life <= 0){
            gameOver = true;
            life = 0;
        } 
    }

    public static boolean bossRound(){
        return game.waveNumber%6 == 0;
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
    
    public ArrayList<Enemy> getEnnemiesDead(){
        return enemiesDead;
    }
    
    public int getLife(){
        return life;
    }
}