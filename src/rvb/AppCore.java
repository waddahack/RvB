package rvb;

import Buffs.Buff;
import ennemies.*;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import towers.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import managers.*;
import managers.TextManager.Text;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.UnicodeFont;
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
        BASIC(0, BasicEnemy.balance, 1, 6),
        FAST(1, FastEnemy.balance, 4, 6),
        TRICKY(2, TrickyEnemy.balance, 7, 10),
        SNIPER(3, SniperEnemy.balance, 10, 8),
        FLYING(4, FlyingEnemy.balance, 13, 6),
        STRONG(5, StrongEnemy.balance, 17, 8);
        
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
                        e = new SniperEnemy();
                        break;
                    case 4:
                        e = new FlyingEnemy();
                        break;
                    case 5:
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
    public int money, life, waveNumber, waveReward, gameSpeed = 1, enemiesBonusLife = 0, enemiesBonusMS = 0;
    public int oldRaztechXpos = -1, oldRaztechYpos = -1;
    private int oldGameSpeed = 0;
    public ArrayList<Shootable> towers, towersDestroyed;
    public ArrayList<Shootable> enemies, enemiesDead, enemiesToAdd;
    public ArrayList<Tile> path;
    //public ArrayList<Rock> rocks;
    public Stack<Buff> buffs;
    public String buffsUsed = "";
    public boolean inWave, gameOver, gameWin, saveBestScore = true, addPP = true;
    public Enemy enemySelected = null;
    public boolean ended = false;
    public Tower towerSelected;
    public Raztech raztech = null;
    public Bazoo bazoo = null;
    protected Wave wave;
    public Overlay OvShop, OvMain, OvEnemyInfo;
    public boolean bossDead = false, bossDefeated = false, gameLoaded = false, endGamePropertiesUpdated = false, attacked = false;
    public static int bossEvery = 6;
    private boolean keyDown = false;
    protected static Random random;
    public Difficulty difficulty;
    protected int textureID = -10;
    protected float waveBalanceMult;
    protected double attackedTimer;
    public int timeInGamePassed;
    public int basicTowerPrice, circleTowerPrice, flameTowerPrice, bigTowerPrice;
    
    public AppCore(){
        random = new Random();
    }
    
    protected void init(Difficulty diff){
        //rocks = new ArrayList<>();
        towers = new ArrayList<>();
        towersDestroyed = new ArrayList<>();
        enemies = new ArrayList<>();
        enemiesDead = new ArrayList<>();
        enemiesToAdd = new ArrayList<>();
        buffs = Buff.initBuffStack();
        gameOver = false;
        gameWin = false;
        inWave = false;
        towerSelected = null;
        timeInGamePassed = 0;
        
        basicTowerPrice = BasicTower.startPrice;
        circleTowerPrice = CircleTower.startPrice;
        flameTowerPrice = FlameTower.startPrice;
        bigTowerPrice = BigTower.startPrice;
        
        waveNumber = 1;
        difficulty = diff;
        life = difficulty.life;
        money = difficulty.money;
        waveReward = difficulty.waveReward;
        waveBalanceMult = difficulty.waveBalanceMult;
    }
    
    protected void initMap(String filePath){
        path = readFile(filePath);
        fillMap(path);
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
        if(path.isEmpty())
            return;
        fillMap(path);
        fixRoadNeighbors();
        fixRoadSprites();
        try {
            createMapTexture();
        } catch (Exception ex) {
            Logger.getLogger(AppCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        //addRocks();
    }
    
    protected void fillMap(ArrayList<Tile> p){
        path = p;
        map = new ArrayList<>();
        // Fill map of null
        for(int i = 0 ; i < RvB.nbTileY ; i++){
            map.add(new ArrayList<>());
            for(int j = 0 ; j < RvB.nbTileX ; j++){
                map.get(i).add(null);
            }
        }
        // Add path tiles to map
        for(int i = 0 ; i < path.size() ; i++)
            map.get(path.get(i).getIndexY()).set(path.get(i).getIndexX(), path.get(i));
            
        // Replace null left by grass
        Tile tile;
        for(int i = 0 ; i < RvB.nbTileY ; i++){
            for(int j = 0 ; j < RvB.nbTileX ; j++){
                if(map.get(i).get(j) == null){
                    tile = new Tile(RvB.textures.get("grass"), "grass");
                    tile.setRotateIndex(0);
                    tile.setX(j*unite);
                    tile.setY(i*unite);
                    map.get(i).set(j, tile);
                }
            }
        }
    }
    
    protected ArrayList<Tile> readFile(String filePath){
        map = new ArrayList<>();
        for(int i = 0 ; i < RvB.nbTileY ; i++){
            map.add(new ArrayList<>());
            for(int j = 0 ; j < RvB.nbTileX ; j++)
                map.get(i).add(null);
        }
        path = new ArrayList<>();
        try{
            File file = new File(filePath);
            Scanner myReader = new Scanner(file);
            String data;
            Tile tile;
            int indexX, indexY;
            while(myReader.hasNext()){
                data = myReader.next();
                String[] indexes = data.split("/");
                tile = new Tile(RvB.textures.get("roadStraight"), "road");
                tile.setRotateIndex(0);
                try{
                    indexX = Integer.parseInt(indexes[0]);
                    indexY = Integer.parseInt(indexes[1]);
                    tile.setX(indexX*unite);
                    tile.setY(indexY*unite);
                    path.add(tile);
                    map.get(indexY).set(indexX, tile);
                }catch(Exception e){
                    PopupManager.Instance.popup(Text.ERROR.getText());
                    myReader.close();
                    path.clear();
                    break;
                }
            }
            myReader.close();
        }
        catch (FileNotFoundException e){
            System.out.println("File : "+filePath+" doesn't exist.");
            e.printStackTrace();
        }
        return path;
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
    
    protected void createMapTexture() throws Exception {
        createMapTexture("flower", false);
    }
    
    protected void createMapTextureEmpty() throws Exception{
        createMapTexture("flower", true);
    }
    
    protected void createMapTexture(String extraName, boolean empty) throws Exception {
        BufferedImage mapImage = new BufferedImage(windWidth, windHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = mapImage.createGraphics();
        
        Image RS = ImageIO.read(new File("assets/images/road_straight.png")).getScaledInstance(unite, unite, 0);
        Image RT = ImageIO.read(new File("assets/images/road_turn.png")).getScaledInstance(unite, unite, 0);
        Image GR = ImageIO.read(new File("assets/images/grass.png")).getScaledInstance(unite, unite, 0);
        
        float extraDensity = 0; // per square
        ArrayList<Image> extraImage = new ArrayList<>();
        int extraSize = 0;
        boolean extraOverlap;
        switch(extraName){
            case "flower":
                extraSize = unite;
                extraImage.add(ImageIO.read(new File("assets/images/big_plant1.png")).getScaledInstance(extraSize, extraSize, 0));
                extraImage.add(ImageIO.read(new File("assets/images/big_plant2.png")).getScaledInstance(extraSize, extraSize, 0));
                extraDensity = 0.2f;
                break;
        }
        extraOverlap = extraDensity*unite*unite >= extraSize*extraSize;
        AffineTransform context = g2d.getTransform();
        
        Tile tile;
        for(int i = 0 ; i < map.size() ; i++){
            for(int j = 0 ; j < map.get(i).size() ; j++){
                tile = map.get(i).get(j);
                
                g2d.rotate(Math.toRadians(tile.getAngle()), j*unite+unite/2, i*unite+unite/2);
 
                if(!empty && map.get(i).get(j).getTexture() == RvB.textures.get("roadStraight"))
                    g2d.drawImage(RS, j*unite, i*unite, null);
                else if(!empty && map.get(i).get(j).getTexture() == RvB.textures.get("roadTurn"))
                    g2d.drawImage(RT, j*unite, i*unite, null);
                else
                    g2d.drawImage(GR, j*unite, i*unite, null);
               
                g2d.setTransform(context);
            }
        }
        if(!extraImage.isEmpty()){
            int posX, posY, nbNexted = 0;
            ArrayList<int[]> posUsed = new ArrayList<>();
            boolean next, stopDecr = false;
            for(int i = 0 ; i < extraDensity*RvB.nbTileX*RvB.nbTileY ; i++){
                if(nbNexted >= extraDensity*RvB.nbTileX*RvB.nbTileY/2)
                    stopDecr = true;
                posX = random.nextInt(RvB.nbTileX*unite);
                posY = random.nextInt(RvB.nbTileY*unite);
                if(!extraOverlap){
                    next = false;
                    for(int[] pos : posUsed){
                        if(pos[0] <= posX+extraSize && pos[0] >= posX-extraSize && pos[1] <= posY+extraSize && pos[1] >= posY-extraSize){
                            next = true;
                            break;
                        }
                    }
                    if(next){
                        if(!stopDecr) i--;
                        nbNexted++;
                        continue;
                    }
                    posUsed.add(new int[]{posX, posY});
                }
                // To not put on roads
                next = false;
                for(Tile road : path){
                    if(road.getRealX() <= posX+unite/2+extraSize/2 && road.getRealX() >= posX-unite/2-extraSize/2 && road.getRealY() <= posY+unite/2+extraSize/2 && road.getRealY() >= posY-unite/2-extraSize/2){
                        next = true;
                        break;
                    }
                }
                if(next){
                    if(!stopDecr) i--;
                    nbNexted++;
                    continue;
                }
                g2d.rotate(Math.toRadians(random.nextFloat()*360), posX, posY);
                g2d.drawImage(extraImage.get(random.nextInt(extraImage.size())), posX-extraSize/2, posY-extraSize/2, null);
                g2d.setTransform(context);
            }
        }

        textureID = RvB.loadTexture(mapImage);
    }
    
    public String getPathString(){
        String pathString = "";
        for(Tile road : path)
            pathString += road.getIndexX()+"/"+road.getIndexY()+"/"+road.nbStepped+(road == path.get(path.size()-1) ? "" : ";");
        return pathString;
    }
    
    public String getHolesString(){
        String holesString = "";
        for(ArrayList<Tile> row : game.map){
            for(Tile tile : row){
                if(tile != null && tile.type.equals("hole"))
                    holesString += tile.getIndexX()+"/"+tile.getIndexY()+";";
            }
        }
        if(!holesString.isEmpty())
            holesString.substring(holesString.length()-1);
        return holesString;
    }
    
    public String getArrayTowers(){
        String arrayTowers = "[";
        for(Shootable t : towers){
            Tower tower = (Tower) t;
            arrayTowers += tower.getJSON()+(t == towers.get(towers.size()-1) ? "" : ", ");
        }
        arrayTowers += "]";
        return arrayTowers;
    }
    
    public String getArrayBuffs(){
        String arrayBuffs = "[";
        for(Buff b : buffs)
            arrayBuffs += b.getJSON()+(b == buffs.get(buffs.size()-1) ? "" : ", ");
        arrayBuffs += "]";
        return arrayBuffs;
    }
    
    public String saveLevel(String name, String dir, boolean overwrite){
        String fileName = "";
        try{
            // Path
            String pathCoords = "";
            for(Tile road : path){
                pathCoords += road.getIndexX()+"/";
                pathCoords += road.getIndexY()+" ";
            }
            // Write in file
            fileName = "level_"+name;
            File file = new File(dir+fileName+".txt");
            if(!file.createNewFile() && !overwrite){
                int i = 0;
                do{
                    fileName = "level_"+name+" ("+(++i)+")";
                    file = new File(dir+fileName+".txt");
                }while(!file.createNewFile());
            }
            PrintWriter writer = new PrintWriter(file);
            writer.print(pathCoords);
            writer.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
        return fileName;
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
                    //rocks.add(r);
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
            if(SoundManager.Instance.isReady()){
                OvMain.getButtons().get(0).enable();
                OvMain.getButtons().get(0).setText(Text.START_WAVE, RvB.fonts.get("normalB"));
            }
            else{
                OvMain.getButtons().get(0).disable();
                OvMain.getButtons().get(0).setText(Text.WAITING, RvB.fonts.get("normal"));
            }
        }
        
        render();
        
        for(Shootable t : towers)
            t.update();
        
        if(inWave){
            wave.update();
            if(bossRound() && bazoo != null)
                bazoo.update();
            for(int i = enemies.size()-1 ; i >= 0 ; i--){
                Enemy e = (Enemy) enemies.get(i);
                if(e != bazoo)
                    e.update();
            }
                
            for(Shootable e : enemies){
                Enemy en = (Enemy) e;
                if(en != bazoo)
                    en.render();
            }
            if(bossRound() && bazoo != null)
                bazoo.render();
        }
        
        if(gameSpeed > 0){
            // Wave check if done
            if(inWave && wave.isDone()){
                OvMain.getButtons().get(0).enable();
                inWave = false;
                wave = null;
                money += waveReward;
                SoundManager.Instance.closeAllClips();
                if(bossDead){
                    if(!gameOver && waveNumber == difficulty.nbWaveMax){
                        gameWin = true;
                    }
                    else if(!gameOver){
                        enemiesBonusLife += 25; // À changer aussi dans RvB.initPropertiesAndGame
                        enemiesBonusMS += 8;
                        PopupManager.Instance.enemiesUpgraded(new String[]{
                            "+25% "+Text.HP.getText(),
                            "+8% "+Text.MS.getText()
                        });
                        bossDead = false;
                        bossDefeated = false;
                    }
                }
                if(!gameOver && !gameWin){
                    waveNumber++;
                    try {
                        RVBDB.Instance.saveGame(waveNumber, money, life, getPathString(), getHolesString(), difficulty.name, getArrayTowers(), getArrayBuffs(), buffsUsed);
                    } catch (SQLException ex) {
                        Logger.getLogger(AppCore.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(waveNumber == 2)
                    TutoManager.Instance.showTutoIfNotDone(TutoManager.TutoStep.FRST_WV);
                else if(waveNumber == 3)
                    TutoManager.Instance.showTutoIfNotDone(TutoManager.TutoStep.SCND_WV);
                else if(waveNumber == 4)
                    TutoManager.Instance.showTutoIfNotDone(TutoManager.TutoStep.THRD_WV);
                else if(waveNumber == 5)
                    TutoManager.Instance.showTutoIfNotDone(TutoManager.TutoStep.FRTH_WV);
            }
        }
        
        renderOverlays();
        
        if(gameOver || gameWin){
            if(gameWin)
                game.waveNumber++;
            gameEnded();
        }
        
        if(gameLoaded)
            gameLoaded = false;
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
                boolean remove = true;
                if(towerSelected == raztech){
                    if(oldRaztechXpos > 0){
                        raztech.x = oldRaztechXpos;
                        raztech.y = oldRaztechYpos;
                        raztech.setIsPlaced(true);
                        remove = false;
                    }
                    else
                        raztech = null;
                }
                // destroy tower
                if(remove)
                    towers.remove(towerSelected);
                selectTower(null);
                RvB.setCursor(Cursor.DEFAULT);
            }
        }
        // Click check
        while(Mouse.next()){
            // Reinitializing if left clicking nowhere
            if(Mouse.isButtonDown(0) && !mouseDown){ //If left or right click
                if(!OvMain.isClicked(0) && !OvShop.isClicked(0)){ //If game overlays aren't clicked
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
            if(Keyboard.isKeyDown(Keyboard.KEY_SPACE) && inWave){
                if(gameSpeed > 0)
                    pause();
                else
                    unpause();
            } 
            // CHANGE SPEED
            else if(Keyboard.isKeyDown(Keyboard.KEY_V)){
                OvMain.getButtons().get(OvMain.getButtons().size()-3).click();
            }
            // DISPLAY LIFEBARS
            else if(Keyboard.isKeyDown(Keyboard.KEY_L)){
                RvB.displayLifebars = !RvB.displayLifebars;
            } 
            // POPUP HELP
            else if(Keyboard.isKeyDown(Keyboard.KEY_H)){
                PopupManager.Instance.help();
            } 
            // START WAVE
            if(Keyboard.isKeyDown(Keyboard.KEY_SPACE) && !inWave){
                OvMain.getButtons().get(0).click();
            } 
            // TOWERS
            if(Keyboard.isKeyDown(Keyboard.KEY_R))
                OvShop.getButtons().get(OvShop.getButtons().size()-1).click();
            else if(Keyboard.isKeyDown(Keyboard.KEY_1) && basicTowerPrice <= money)
                OvShop.getButtons().get(0).click();
            else if(Keyboard.isKeyDown(Keyboard.KEY_2) && circleTowerPrice <= money)
                OvShop.getButtons().get(1).click();
            else if(Keyboard.isKeyDown(Keyboard.KEY_3) && bigTowerPrice <= money)
                OvShop.getButtons().get(2).click();
            else if(Keyboard.isKeyDown(Keyboard.KEY_4) && flameTowerPrice <= money)
                OvShop.getButtons().get(3).click();
            // TOWER STATS
            if(towerSelected != null && Keyboard.isKeyDown(Keyboard.KEY_TAB)){
                towerSelected.switchOverlay();
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
    
    protected void render(){
        RvB.drawTextureID(0, 0, windWidth, windHeight, textureID);

        if(spawn != null)
            spawn.renderDirection();
        
        for(ArrayList<Tile> row : map){
            for(Tile t : row)
                if(t != null) t.update();
        }
        /*for(Rock rock : rocks)
            rock.render();*/
        if(attacked){
            RvB.drawFilledRectangle(base.x+RvB.unite/2, base.y+RvB.unite/2, RvB.unite, RvB.unite-(int)(5*ref), RvB.textures.get("gameHit"), base.x < RvB.unite ? 180 : 0, 1);
            if(System.currentTimeMillis()-attackedTimer >= 400)
                attacked = false;
        }
    }
    
    protected void initOverlays(){
        Button b;
        int nbTower = 4;
        int size = (int) (50*ref);
        int sep = (int) (200*ref);
        int startPos = windWidth/2 - (nbTower-1)*size/2 - (nbTower-1)*sep/2;
        
        // Overlay tours
        OvShop = new Overlay(0, windHeight-(int)(60*ref), windWidth, (int)(60*ref));
        OvShop.setBG(RvB.textures.get("board"), 0.6f);
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
            b = new Button(startPos + (size + sep)*i, (int)(30*ref), size, size, RvB.textures.get(textureName));
            b.setItemFramed(true);
            b.lock();
            int index = i;
            b.setFunction(__ -> {
                if(towerSelected != null)
                    selectTower(null);
                createTower(index);
                if(index == 0)
                    TutoManager.Instance.showTutoIfNotDone(TutoManager.TutoStep.TWR_BGHT);
            });
            OvShop.addButton(b);
        }
        b = new Button(size + sep, (int)(30*ref), size, size, RvB.textures.get("raztech"));
        b.setItemFramed(true);
        b.setFunction(__ -> {
            if(towerSelected != null)
                selectTower(null);
            createTower(4);
        });
        OvShop.addButton(b);
        
        // Overlay top, indexes : WAVEBUT -> 0, DLBUT -> 1, GMSPEEDBUT -> 2, BACKBUT -> 3, HELPBUT -> 4
        OvMain = new Overlay(0, 0, windWidth, (int)(60*ref));
        OvMain.setBG(RvB.textures.get("board"), 0.6f);
        // Wave button
        b = new Button(OvMain.getW()-(int)(150*ref), OvMain.getH()/2, (int)(150*ref), (int)(40*ref));
        b.setText(Text.START_WAVE, RvB.fonts.get("normalB"));
        b.setClickSound(SoundManager.SOUND_WAVE, SoundManager.Volume.VERY_HIGH);
        Button waveBut = b;
        b.setFunction(__ -> {
            if(!inWave){
                waveBut.disable();
                RvB.setCursor(Cursor.DEFAULT);
                waveBut.setText(Text.DEFENDING, RvB.fonts.get("normal"));
                startWave();
            }
        });
        OvMain.addButton(b);
        // Download button
        b = new Button(OvMain.getW()-(int)(30*ref), OvMain.getH()/2, (int)(32*ref), (int)(32*ref));
        b.setBG(RvB.textures.get("download"));
        Button dlBut = b;
        b.setFunction(__ -> {
            File levelsFolder = new File(System.getProperty("user.home")+File.separator+"RvB", "levels");
            if (!levelsFolder.exists()) {
                levelsFolder.mkdir();
            }
            String name = saveLevel("downloaded", levelsFolder.getAbsolutePath()+File.separator, false);
            if(name.isEmpty())
                PopupManager.Instance.popup(Text.ERROR.getText());
            else{
                dlBut.lock();
                PopupManager.Instance.popup(new String[]{Text.MAP_DOWNLOADED.getText(), " ", levelsFolder.getAbsolutePath()+File.separator+name}, new UnicodeFont[]{RvB.fonts.get("normalL"), RvB.fonts.get("normalXL"), RvB.fonts.get("normalXL")}, Text.OK);
            }
        });
        OvMain.addButton(b);
        // game speed button
        b = new Button(OvMain.getW()-(int)(350*ref), OvMain.getH()/2, (int)(60*ref), (int)(30*ref));
        b.setText(Text.X1, RvB.fonts.get("normalB"));
        Button gameSpeedBut = b;
        b.setFunction(__ -> {
            int speed = gameSpeed;
            if(gameSpeed == 0)
                speed = oldGameSpeed;
            switch(speed){
                case 1:
                    if(gameSpeed == 0)
                        oldGameSpeed = 2;
                    else
                        gameSpeed = 2;
                    gameSpeedBut.setText(Text.X2);
                    break;
                case 2:
                    if(gameSpeed == 0)
                        oldGameSpeed = 4;
                    else
                        gameSpeed = 4;
                    gameSpeedBut.setText(Text.X4);
                    break;
                case 4:
                    if(gameSpeed == 0)
                        oldGameSpeed = 1;
                    else
                        gameSpeed = 1;
                    gameSpeedBut.setText(Text.X1);
                    break;
            }
        });
        OvMain.addButton(b);
        // back button
        b = new Button((int)(60*ref), OvMain.getH()/2, (int)(32*ref), (int)(32*ref));
        b.setBG(RvB.textures.get("arrowBack"));
        b.setFunction(__ -> {
            RvB.switchStateTo(MENU);
        });
        OvMain.addButton(b);
        
        // overlay enemy selected
        int width = (int) (700*ref), height = (int) (50*ref);
        OvEnemyInfo = new Overlay(windWidth/2-width/2, (int)(5*ref), width, height);
        OvEnemyInfo.setBG(RvB.textures.get("darkBoard"), 0.4f);
        OvEnemyInfo.setBorder(RvB.colors.get("green_dark"), 2, 0.8f);
    }
    
    public void renderOverlays(){ 
        String t;
        Overlay o;
        Button b;
        
        //// Overlay selection tours
        if(towerSelected == null){
            o = OvShop;
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
                RvB.drawRectangle(x, y, width, height, RvB.colors.get("green_dark"), 1f, (int)(4*ref));
            }
        }
        //
        //// Overlay principal
        o = OvMain;
        o.render();
        
        t = money+"";
        o.drawText((int)(200*ref), o.getH()/2, t, RvB.fonts.get("money"));
        o.drawImage((int)((210+8.8*t.length())*ref), o.getH()/2, (int)(32*ref), (int)(32*ref), RvB.textures.get("coins"));
        
        t = life+"";
        o.drawText((int)(320*ref), o.getH()/2, t, RvB.fonts.get("life"));
        o.drawImage((int)((330+8.8*t.length())*ref), o.getH()/2, (int)(32*ref), (int)(32*ref), RvB.textures.get("heart"));
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
        for(i = 0 ; i < enemiesToAdd.size() ; i++)
            enemies.add(0, enemiesToAdd.get(i));
        enemiesToAdd.clear();
        for(i = 0 ; i < enemiesDead.size() ; i++)
            enemies.remove(enemiesDead.get(i));
        enemiesDead.clear();
        for(i = 0 ; i < towersDestroyed.size() ; i++)
            towers.remove(towersDestroyed.get(i));
        towersDestroyed.clear();
    }

    @SuppressWarnings("unchecked")
    protected void startWave(){
        UEnemy[] uEnemies = UEnemy.values();
        int waveBalance = (waveNumber*waveNumber + waveNumber)/2;
        if(waveNumber >= uEnemies[uEnemies.length-1].enterAt + 1)
            waveBalance *= 20;
        else
            waveBalance *= 14;
        if(waveNumber >= 3)
            waveBalance *= waveBalanceMult;
        waveBalance = 1000*(int) (bossRound() ? waveBalance*0.7 : waveBalance);
        wave = new Wave();
        waveBalance = uEnemies[3].addToWave(1, waveBalance);
        int min, max;
        /*while(waveBalance >= uEnemies[0].balance){
            // Du plus fort au moins fort. Ils commencent à apparaitre à la vague n de max = waveNumber+min-n, et commencent à ne plus apparaitre à la vague n de decrease = (waveNumber+min-n+waveNumber-n) (si = 0, ne disparait jamais)
            for(int i = uEnemies.length-1 ; i >= 0 ; i--){
                min = 1+waveNumber-uEnemies[i].enterAt;
                max = min+(waveNumber-uEnemies[i].enterAt)*2;
                if(min > uEnemies[i].nbMax) min = uEnemies[i].nbMax;
                if(max > uEnemies[i].nbMax) max = uEnemies[i].nbMax;
                waveBalance = uEnemies[i].addToWave((int) Math.floor(min+random.nextFloat()*(max-min)), waveBalance);
            }
        }
        wave.shuffleEnemies();*/
        if(bossRound()){
            bazoo = new Bazoo((waveNumber/bossEvery)-1);
            wave.addEnemy(bazoo, wave.getEnnemies().size()/5);
        }
        enemies = (ArrayList<Shootable>)wave.getEnnemies().clone();
        inWave = true;
        //STATS
        for(Shootable t : towers){
            t.damagesDoneThisWave = 0;
            t.enemiesKilledThisWave = 0;
        }
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
            case 103 :
                tower = new ShootRateTower();
                break;
        }
        if(tower != null && (price <= money || id > 100)){
            if(!towers.contains(tower))
                towers.add(0, tower);
            selectTower(tower);
            RvB.setCursor(Cursor.GRAB);
        }
    }
    
    public void showTile(int ix, int iy, boolean show){
        boolean br = false;
        for(ArrayList<Tile> row : map){
            for(Tile t : row){
                if(t != null && t.getIndexX() == ix && t.getIndexY() == iy){
                    t.show = show;
                    br = true;
                    break;
                }
            }
            if(br) break;
        }
    }
    
    public void raisePrice(Tower t){
        if(t.type == Tower.Type.BASIC)
            basicTowerPrice *= 1.1;
        else if(t.type == Tower.Type.CIRCLE)
            circleTowerPrice *= 1.08;
        else if(t.type == Tower.Type.BIG)
            bigTowerPrice *= 1.1;
        else if(t.type == Tower.Type.FLAME)
            flameTowerPrice *= 1.1;
    }
    
    public void decreasePrice(Tower t){
        if(t.type == Tower.Type.BASIC)
            basicTowerPrice /= 1.1;
        else if(t.type == Tower.Type.CIRCLE)
            circleTowerPrice /= 1.08;
        else if(t.type == Tower.Type.BIG)
            bigTowerPrice /= 1.1;
        else if(t.type == Tower.Type.FLAME)
            flameTowerPrice /= 1.1;
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
        attacked = true;
        attackedTimer = System.currentTimeMillis();
    }

   
    public boolean onPause(){
        return gameSpeed == 0;
    }
    
    public void pause(){
        if(inWave && !onPause()){
            oldGameSpeed = gameSpeed;
            gameSpeed = 0;
            SoundManager.Instance.pauseAll();
        }
    }
    
    public void unpause(){
        if(inWave && onPause()){
            gameSpeed = oldGameSpeed;
            SoundManager.Instance.unpauseAll();
        }
    }
    
    public static boolean bossRound(){
        return game.waveNumber%bossEvery == 0;
    }
    
    protected void gameEnded(){
        if(PopupManager.Instance.onPopup() || RvB.stateChanged)
            return;
        if(!endGamePropertiesUpdated){
            StatsManager.Instance.updateModeStats();
            if(addPP)
                StatsManager.Instance.addProgressionPoints();
            if(saveBestScore)
                StatsManager.Instance.updateBestScore();
            endGamePropertiesUpdated = true;
        }
        if(gameWin)
            PopupManager.Instance.gameWin();
        else
            PopupManager.Instance.gameOver();
        TutoManager.Instance.showTutoIfNotDone(TutoManager.TutoStep.GM_NDD);
    }
    
    public void selectTower(Tower t){
        if(t == null && towers.contains(towerSelected) && !towerSelected.isPlaced())
            towers.remove(towerSelected);
        if(t != null)
            t.setSelected(true);
        else if(towerSelected != null)
            towerSelected.setSelected(false);
        towerSelected = t;
        mouseDown = true;
    }
    
    public void disableAllButtons(){
        for(Button b : OvMain.getButtons())
            b.disable();
        for(Button b : OvShop.getButtons())
            b.disable();
        for(Button b : OvEnemyInfo.getButtons())
            b.disable();
        for(Shootable s : towers){
            Tower t = (Tower)s;
            t.disableAllButtons();
        }
    }
    
    public void enableAllButtons(){
        for(Button b : OvMain.getButtons())
            b.enable();
        for(Button b : OvShop.getButtons())
            b.enable();
        for(Button b : OvEnemyInfo.getButtons())
            b.enable();
        for(Shootable s : towers){
            Tower t = (Tower)s;
            t.enableAllButtons();
        }
    }

    public void setEnemySelected(Enemy e) {
        if(e == null){
            OvEnemyInfo.display(false);
            if(enemySelected != null)
                enemySelected.setSelected(false);
        }  
        else{
            OvEnemyInfo.display(true);
            e.setSelected(true);
        } 
        enemySelected = e;
    }
    
    public void addEnemy(Enemy e){
        enemiesToAdd.add(0, e);
        wave.addEnemy(e);
    }
    
    public int getLife(){
        return life;
    }
}