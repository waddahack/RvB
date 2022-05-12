package towser;

import managers.SoundManager;
import ennemies.*;
import towers.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import managers.PopupManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.opengl.Texture;
import towser.Towser.Cursor;
import static towser.Towser.game;
import static towser.Towser.mouseDown;
import static towser.Towser.ref;
import static towser.Towser.unite;
import static towser.Towser.windHeight;
import static towser.Towser.windWidth;
import ui.*;


public abstract class AppCore {
    
    public enum UEnemy{
        // Balance ends with a 0 or 5 only
        BASIC(0, BasicEnemy.balance, 1, 8), FAST(1, FastEnemy.balance, 4, 10), TRICKY(2, TrickyEnemy.balance, 6, 10), STRONG(3, StrongEnemy.balance, 9, 15);
        
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
    
    protected ArrayList<ArrayList<Tile>> map;
    protected Tile spawn, base;
    public int money, life, waveNumber, waveReward, nbTower = 2, gameSpeed = 1;
    protected ArrayList<Tower> towers, towersDestroyed;
    public ArrayList<Enemy> enemies, ennemiesDead;
    protected ArrayList<Tile> path = new ArrayList<>();
    protected boolean gameOver;
    protected boolean inWave, dontPlace;
    public Enemy enemySelected = null;
    public boolean ended = false;
    protected Tower towerSelected;
    protected Wave wave;
    protected ArrayList<Overlay> overlays;
    
    public AppCore(int lvl){
        init(lvl);
        
        life = 100;
        money = 30000;
        waveNumber = 17;
        waveReward = 250;
        
        initOverlays();
    }
    
    protected void init(int lvl){
        readFile("levels/level_"+lvl+".txt");
        fixRoadNeighbors();
        fixRoadSprites();
        
        towers = new ArrayList<>();
        towersDestroyed = new ArrayList<>();
        enemies = new ArrayList<>();
        ennemiesDead = new ArrayList<>();
        gameOver = false;
        inWave = false;
        dontPlace = false;
        towerSelected = null;
        BasicTower.priceP = BasicTower.startPrice;
        CircleTower.priceP = CircleTower.startPrice;
        Enemy.bonusLife = 0;
        Enemy.bonusMS = 0;
    }
    
    protected void readFile(String filePath){
        map = new ArrayList<>();
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
                            spawn = tile;
                            break;
                        case "B":
                            tile = new Tile(Towser.textures.get("roadStraight"), "road");
                            tile.setRotateIndex(0);
                            base = tile;
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
                if(base.getIndexY() == 0) // sur le bord haut
                    road.nextRoad = new Tile(base.getX(), base.getY()-unite);
                else if(base.getIndexY() == map.size()-1)// sur le bord bas
                    road.nextRoad = new Tile(base.getX(), base.getY()+unite);
                else if(base.getIndexX() == 0) // sur le bord gauche
                    road.nextRoad = new Tile(base.getX()-unite, base.getY());
                else // sur le bord droit
                    road.nextRoad = new Tile(base.getX()+unite, base.getY());
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
    
    public void update(){
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
        
        for(Tower t : towers)
            t.update();
        
        renderOverlays();
        
        if(gameOver)
            gameOver();
        
        PopupManager.Instance.update();
    }
    
    protected void checkInput(){
        clearArrays();
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
        ArrayList<Tile> tilesToRender = new ArrayList<>();
        ArrayList<Tile> tilesToRenderNext = new ArrayList<>();
        int layer = 0;
        for(int i = 0 ; i < Towser.nbTileY ; i++){
            for(Tile t : map.get(i)){
                t.renderLayer(layer);
                if(t.textures.size() > layer+1)
                    tilesToRender.add(t);
            }
        }
        while(!tilesToRender.isEmpty()){
            layer++;
            for(Tile t : tilesToRender){
                t.renderLayer(layer);
                if(t.textures.size() > layer+1)
                    tilesToRenderNext.add(t);
            }
            tilesToRender.clear();
            for(Tile t : tilesToRenderNext)
                tilesToRender.add(t);
            tilesToRenderNext.clear();
        }
        if(spawn != null)
            spawn.renderDirection();
    }
    
    protected void initOverlays(){
        overlays = new ArrayList<>();
        Overlay o;
        Button b;
        int size = (int) (50*ref);
        int sep = (int) (100*ref);
        
        o = new Overlay(0, (int) (windHeight-75*ref), windWidth, (int) (75*ref));
        o.setBG(Towser.textures.get("board"));
        o.setA(0.8f);
        b = new Button(windWidth/2 - size/2 - sep/2, (int) (45*ref), size, size, Towser.textures.get("basicTower"), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        o.addButton(b);
        b = new Button(windWidth/2 + size/2 + sep/2, (int) (45*ref), size, size, Towser.textures.get("circleTower"), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        o.addButton(b);
        overlays.add(o);
        
        o = new Overlay(0, 0, windWidth, (int) (50*ref));
        o.setBG(Towser.textures.get("board"));
        o.setA(0.8f);
        b = new Button((int) (o.getW()-100*ref), (int) (25*ref), (int) (130*ref), (int) (30*ref), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        o.addButton(b);
        b = new Button((int) (o.getW()-100*ref), (int) (25*ref), (int) (130*ref), (int) (30*ref), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        b.setHidden(true);
        o.addButton(b);
        overlays.add(o);
        
        int width = (int) (400*ref), height = (int) (40*ref);
        o = new Overlay(windWidth/2-width/2, 5, width, height);
        o.setBG(Towser.textures.get("enemyBoard"));
        o.setA(0.6f);
        o.setBorder(Towser.colors.get("green_dark"), 2);
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
            
            UnicodeFont font = Towser.fonts.get("canBuy");
            if(money < BasicTower.priceP)
                font = Towser.fonts.get("cantBuy");
            t = BasicTower.priceP+"*";
            b = o.getButtons().get(0);
            b.drawText(0, (int) (-b.getH()/2-12*ref), t, font);
            
            font = Towser.fonts.get("canBuy");
            if(money < CircleTower.priceP)
                font = Towser.fonts.get("cantBuy");
            t = CircleTower.priceP+"*";
            b = o.getButtons().get(1);
            b.drawText(0, (int) (-b.getH()/2-12*ref), t, font);
        }
        //
        //// Overlay principal
        o = overlays.get(1);
        o.render();
        
        t = money+"*";
        o.drawText((int) (80*ref), o.getH()/2, t, Towser.fonts.get("astres"));
        
        t = life+"";
        o.drawText((int) (200*ref), o.getH()/2, t, Towser.fonts.get("life"));
        
        if(!o.getButtons().get(0).isHidden()){
            t = "Wave " + waveNumber;
            o.getButtons().get(0).drawText(0, 0, t, Towser.fonts.get("normalB"));
        }
        if(!o.getButtons().get(1).isHidden()){
            t = "x"+gameSpeed;
            o.getButtons().get(1).drawText(0, 0, t, Towser.fonts.get("normalB"));
        }
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
                createTower(o.getButtons().indexOf(b)+2);

        for(int i = 0 ; i < nbTower ; i++){ // Check tower pressed by keyboard
            if(Keyboard.isKeyDown(i+2) || Keyboard.isKeyDown(79+i))
                createTower(Keyboard.getEventKey());  
        }
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
                    gameSpeed = 1;
                    break;
            }
        }
        //
    }
    
    protected void clearArrays(){
        int i;
        for(i = 0 ; i < ennemiesDead.size() ; i++)
            enemies.remove(ennemiesDead.get(i));
        ennemiesDead.clear();
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
        enemies = (ArrayList<Enemy>)wave.getEnnemies().clone();
        Collections.sort(enemies);

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
        if(id >= 79)
            id -= 77;
        id -= 2;
        switch(id){
            case 0 :
                tower = new BasicTower();
                break;
            case 1 :
                tower = new CircleTower();
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
    
    public void addEnemie(Enemy e){
        wave.addEnemy(e);
        enemies.add(0, e);
    }
    
    public ArrayList<Tower> getTowers(){
        return towers;
    }
    
    public ArrayList<Tower> getTowersDestroyed(){
        return towersDestroyed;
    }
    
    public ArrayList<Enemy> getEnnemiesDead(){
        return ennemiesDead;
    }
    
    public ArrayList<ArrayList<Tile>> getMap(){
        return map;
    }
    
    public ArrayList<Tile> getPath(){
        return path;
    }
    
    public Tile getSpawn(){
        return spawn;
    }
    
    public Tile getBase(){
        return base;
    }
    
    public int getLife(){
        return life;
    }
}