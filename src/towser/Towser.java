package towser;

import managers.SoundManager;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import managers.PopupManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class Towser{
    
    public static enum State{
        MENU, GAME, CREATION, EXIT
    }
    public static enum Cursor{
        DEFAULT, POINTER, GRAB
    }
    public static enum Difficulty{
        EASY(1.5f), MEDIUM(2f), HARD(2.5f);
        public float value;
        private Difficulty(float value){
            this.value = value;
        }
    }
    
    public static State state = State.MENU;
    public static Cursor cursor = null;
    public static int nbTileX = 22, nbTileY = 18; // Tilemap size. Doit être en accord avec le format des levels campagne à venir (22/18)
    public static int unite;
    public static int fps = 120, windWidth, windHeight;
    public static float ref, uniteRef;
    public static boolean mouseDown = false, stateChanged = false;
    private static double lastUpdate;
    public static double deltaTime;
    public static Menu menu;
    public static Game game = null, adventureGame = null, randomGame = null, createdGame = null;
    public static Creation creation = null;
    public static Map<String, Texture> textures;
    public static Map<String, UnicodeFont> fonts;
    public static Map<String, float[]> colors;
    public static DecimalFormat formatter = new DecimalFormat("#.##");
    private static Texture cursorTexture = null;
    private static int[] cursorPos = new int[2];
    
    public static void main(String[] args){
        System.setProperty("org.lwjgl.librarypath", new File("lib").getAbsolutePath());
        try{
            Display.setLocation(0, 0);
            Display.setFullscreen(true);
            //Display.setDisplayMode(new DisplayMode(windWidth, windHeight));
            Display.setResizable(false);
            Display.setTitle("Towser");
            Display.create();
        }catch (LWJGLException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
        unite = Math.min(Math.floorDiv(Display.getWidth(), nbTileX), Math.floorDiv(Display.getHeight(), nbTileY));
        if(unite%2 != 0)
            unite -= 1;
        windWidth = unite*nbTileX;
        windHeight = unite*nbTileY;
        ref = ((float)Math.max(windWidth, windHeight)/(float)Math.min(windWidth, windHeight));
        uniteRef = unite/50f;
        
        initTextures();
        initColors();
        setUpFont();
        
        init();
        
        while(!Display.isCloseRequested()){
            lastUpdate = System.currentTimeMillis();
            
            checkInput();
            render();
            mouseDown = (Mouse.isButtonDown(0) || Mouse.isButtonDown(1));
            stateChanged = false;
            
            Display.update();
            Display.sync(fps);
            deltaTime = System.currentTimeMillis() - lastUpdate;
        }
        releaseTextures();
        exit();
    }
    
    private static void init(){        
        glEnable(GL11.GL_BLEND);
        glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        glViewport(Display.getWidth()/2-windWidth/2, Display.getHeight()/2-windHeight/2, windWidth, windHeight);
        glMatrixMode(GL_PROJECTION);
        glOrtho(0, windWidth, windHeight, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        Mouse.setGrabbed(true);
        Mouse.setCursorPosition(windWidth/2, windHeight/2);
        setCursor(Cursor.DEFAULT);
        SoundManager.initialize();
        SoundManager.Instance.playAllAmbiance();
        PopupManager.initialize();
        menu = new Menu();
        lastUpdate = System.currentTimeMillis();
    }

    private static void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        switch(state){
            case MENU:
                menu.render();
                break;
            case GAME:
                game.update();
                break;
            case CREATION:
                creation.update();
                break;
            case EXIT:
                releaseTextures();
                exit();
                break;
        }
        renderMouse();
    }  
    
    private static void renderMouse(){
        if(cursorTexture != null)
            drawFilledRectangle(Mouse.getX()-cursorPos[0], windHeight-Mouse.getY()-cursorPos[1], 28, 28, null, 1, cursorTexture);
    }
    
    private static void checkInput() {
        State s = state;
        // ESCAPE MENU
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
            if(state == State.GAME)
                SoundManager.Instance.pauseAll();
            menu.enableAllButtons();
            switchStateTo(State.MENU);
        }
        // MENU BUTTONS
        if(state == State.MENU){
            if(menu.getStart().isClicked(0)){
                if(adventureGame == null || adventureGame.ended || adventureGame.waveNumber == 1)
                    adventureGame = new Game("1");
                else
                    SoundManager.Instance.unpauseAll();
                
                game = adventureGame;
                switchStateTo(State.GAME);
            }  
            if(menu.getRandom().isClicked(0)){
                if(randomGame == null || randomGame.ended || randomGame.waveNumber == 1)
                    randomGame = new Game(generateRandomPath(Difficulty.EASY)); 
                else
                    SoundManager.Instance.unpauseAll();
                
                game = randomGame;
                switchStateTo(State.GAME);
            }
            if(menu.getCreate().isClicked(0)){
                if(generateEmptyMap())
                    creation = new Creation();
                
                switchStateTo(State.CREATION);
            }  
            if(menu.getExit().isClicked(0))
                switchStateTo(State.EXIT);
        }
        if(s != state)
            setCursor(Cursor.DEFAULT);
    }
    
    private static boolean generateEmptyMap(){
        try{
            File file = new File("levels/level_created.txt");
            if(file.createNewFile()){
                FileWriter myWriter = new FileWriter(file, false);
                String emptyMap = "";
                for(int i = 0 ; i < nbTileY ; i++){
                    for(int j = 0 ; j < nbTileX ; j++)
                        emptyMap += ". ";
                    emptyMap += "\n";
                }
                myWriter.write(emptyMap);
                myWriter.close();
            }
            return true;
        }
        catch(Exception e){
            System.out.println(e);
            return false;
        }
    }
    
    private static ArrayList<Tile> generateRandomPath(Difficulty diff){
        Random rand = new Random();
        ArrayList<ArrayList<Tile>> map = new ArrayList<>();
        ArrayList<Tile> row, path = new ArrayList<>(), neighbors = new ArrayList<>();
        int x, y;
        Tile road, previous;
        String dir, dirToCount;
        // nombre de road de la map restante
        int nbRoadLeft = (int) ((nbTileX*nbTileY) / (3*diff.value));
        System.out.println(nbRoadLeft);
        // remplissage de la map par du vide
        for(int i = 0 ; i < nbTileY ; i++){
            row = new ArrayList<>();
            for(int j = 0 ; j < nbTileX ; j++)
                row.add(null);
            map.add(row);
        }
        // spawn random sur un bord
        x = rand.nextInt(2);
        if(x == 0){
            x = rand.nextInt(nbTileX);
            y = rand.nextInt(2)*(nbTileY-1);
        }
        else{
            x = rand.nextInt(2)*(nbTileX-1);
            y = rand.nextInt(nbTileY);
        }
        road = new Tile(x, y);
        if(x == 0)
            road.previousRoad = new Tile(x-1, y);
        else if(x == nbTileX-1)
            road.previousRoad = new Tile(x+1, y);
        else if(y == 0)
            road.previousRoad = new Tile(x, y-1);
        else
            road.previousRoad = new Tile(x, y+1);
        map.get(y).set(x, road);
        path.add(road);
        nbRoadLeft--;
        // Construction de la route
        int i = 0, r;
        int[][] check;
        Tile up, down, left, right, accross, temp;
        while(nbRoadLeft > 0){
            //TODO après, on fonce vers une sortie et pour ça :
            //     - on regarde de tous les côtés et on compte le nombre de côté qui n'ont pas de chemin sur le passage
            // (faire une fonction qui retourne une liste avec les directions left/right/up/down qui contient les coords d'un chemin qui coupe le chemin)
            //         - si n > 0, prendre la direction la plus courte
            //         - sinon, faut faire demi tour :
            //             - regarder la direction du chemin bloquant droite et gauche, et tourner en direction du chemin qui à la même direction
            neighbors.clear();
            up = new Tile(x, y-1);
            down = new Tile(x, y+1);
            left = new Tile(x-1, y);
            right = new Tile(x+1, y);
            accross = null;
            // ajout des positions voisines si c'est ni un bord ni une road
            if(y-1 >= 0 && map.get(y-1).get(x) == null)
                neighbors.add(up);
            if(y+1 <= nbTileY-1 && map.get(y+1).get(x) == null)
                neighbors.add(down);
            if(x-1 >= 0 && map.get(y).get(x-1) == null)
                neighbors.add(left);
            if(x+1 <= nbTileX-1 && map.get(y).get(x+1) == null)
                neighbors.add(right);
            // remove les voisins qui nous fait entrer dans une boucle
            // (on remove ceux qui vont dans le même sens quela tuile sur laquelle on s'est cogné, en checkant les 3 tuiles en face)
            if(neighbors.size() > 1 && i > 0){
                previous = path.get(i-1);
                previous.setDirectionWithPos();
                switch (previous.getDirection()) {
                    case "up":
                        accross = up;
                        check = new int[][]{{x-1, y-1}, {x, y-1}, {x+1, y-1}};
                        break;
                    case "down":
                        accross = down;
                        check = new int[][]{{x-1, y+1}, {x, y+1}, {x+1, y+1}};
                        break;
                    case "left":
                        accross = left;
                        check = new int[][]{{x-1, y-1}, {x-1, y}, {x-1, y+1}};
                        break;
                    default:
                        accross = right;
                        check = new int[][]{{x+1, y-1}, {x+1, y}, {x+1, y+1}};
                        break;
                }
                for(int[] toCheck : check){
                    if(toCheck[0] < 0 || toCheck[0] > nbTileX-1 || toCheck[1] < 0 || toCheck[1] > nbTileY-1){ //Si c'est un bord
                        if(toCheck[0] != x && toCheck[1] != y) // Si ce n'est pas la tuile d'en face
                            continue;
                        if(toCheck[0] < 0)
                            dir = "left";
                        else if(toCheck[0] > nbTileX-1)
                            dir = "right";
                        else if(toCheck[1] < 0)
                            dir = "up";
                        else
                            dir = "down";
                        dirToCount = directionToCount(path.get(0), new int[]{x, y}, dir);
                        // si pas première fois qu'on fat ça, la comparaison du comptage à suivre marche pas. Trouver une solution ou refaire le lvl si failure
                        // compter les tuiles qui sont dans la dirToCount pour chaque tuile de path si tile.getDirection() == dir
                        // (vérifier qu'il n'y a pas de tuile qu'a pas la même dir mais qui doit compter aussi)
                    }    
                    temp = map.get(toCheck[1]).get(toCheck[0]);
                    if(temp != null){
                        temp.setDirectionWithPos();
                        dir = temp.getDirection();
                        if(dir.equals(previous.getDirection()))
                            dir = temp.previousRoad.getDirection();
                        switch (dir) {
                            case "up":
                                neighbors.remove(up);
                                break;
                            case "down":
                                neighbors.remove(down);
                                break;
                            case "left":
                                neighbors.remove(left);
                                break;
                            default:
                                neighbors.remove(right);
                                break;
                        }
                        if(toCheck[0] != x && toCheck[1] != y && neighbors.contains(accross))// Si la tuile d'en face est libre, faut la remove
                            neighbors.remove(accross);
                        break;
                    }
                }
            }
            if(neighbors.isEmpty()){
                System.out.println("failure");
                PopupManager.Instance.popup("Failure. "+path.size()+" roads.");
                break;
            }
            
            r = rand.nextInt(3);
            if(r != 0 && neighbors.contains(accross)){
                x = (int) accross.getX();
                y = (int) accross.getY();
            }
            else{
                if(neighbors.contains(accross))
                    neighbors.remove(accross);
                r = rand.nextInt(neighbors.size());
                x = (int) neighbors.get(r).getX();
                y = (int) neighbors.get(r).getY();
            }
            
            road = new Tile(x, y);
            road.setPreviousRoad(path.get(i));
            path.get(i).setNextRoad(road);
            
            map.get(y).set(x, road);
            path.add(road);
            nbRoadLeft--;
            i++;
        }
        return path;
    }
    
    private static String directionToCount(Tile firstTile, int[] lastTilePos, String dir){
        if(dir.equals("left") || dir.equals("right")){
            if(firstTile.getY() > lastTilePos[1])
                return "up";
            else
                return "down";
        }
        else{
            if(firstTile.getX() > lastTilePos[0])
                return "left";
            else
                return "right";
        }
    } 
    
    public static void switchStateTo(State s){
        state = s;
        stateChanged = true;
        setCursor(Cursor.DEFAULT);
    }
    
    public static void drawString(int x, int y, String text, UnicodeFont font){
        font.drawString(x - font.getWidth(text)/2, y - font.getHeight(text)/2, text);
    }
    
    public static void drawRectangle(int x, int y, int width, int height, float[] rgb, float a, int thickness){
        glColor3f(rgb[0], rgb[1], rgb[2]);
        
        glBegin(GL_LINES);
            for(int i = 0 ; i < thickness ; i++){
                glVertex2i(x-i, y-i/*-1*/);
                glVertex2i(x+i+1 + width, y-i/*-1*/);
                glVertex2i(x+i+1 + width, y-i/*-1*/);
                glVertex2i(x+i+1 + width, y+i + height);
                glVertex2i(x+i+1 + width, y+i + height);
                glVertex2i(x-i-1, y+i + height);
                glVertex2i(x-i-1, y+i + height);
                glVertex2i(x-i, y-i/*-1*/);
            }
        glEnd();
    }
    
    public static void drawFilledRectangle(double x, double y, int width, int height, Texture texture, double angle){
        glPushMatrix(); //Save the current matrix.
        
        glTranslated(x, y, 0);
        if(angle != 0)
            glRotated(angle, 0, 0, 1);

        drawFilledRectangle(-width/2, -height/2, width, height, null, 1, texture);
        
        glPopMatrix(); // Reset the current matrix to the one that was saved.
    }
    
    public static void drawFilledRectangle(double x, double y, int width, int height, float[] rgb, float a, Texture texture){
        if (texture != null) {
            texture.bind();
            glEnable(GL_TEXTURE_2D);
            glColor4f(1, 1, 1, a);
        }
        else
            glColor4f(rgb[0], rgb[1], rgb[2], a);

        glBegin(GL_QUADS);
            glTexCoord2f(0, 0);
            glVertex2d(x, y);
            glTexCoord2f(1, 0);
            glVertex2d(x + width, y);
            glTexCoord2f(1, 1);
            glVertex2d(x + width, y + height);
            glTexCoord2f(0, 1);
            glVertex2d(x, y + height);
        glEnd();
        
        if(texture != null)
            glDisable(GL_TEXTURE_2D);
    }
    
    public static void drawCircle(double x, double y, float radius, float[] rgb){
        float DEG2RAD = (float) (3.15149/180), degInRad;
        glBegin(GL_LINE_LOOP);
        glColor3f(rgb[0], rgb[1], rgb[2]);
        for(int i = 0 ; i < 360; i++){
            degInRad = i*DEG2RAD;
            glVertex2d(x+cos(degInRad)*radius, y+sin(degInRad)*radius);
        }
        glEnd();
    }
    
    public static void drawFilledCircle(double x, double y, float radius, float[] rgb, float a){
        float DEG2RAD = (float) (3.14159/180), degInRad, degInRad2;
        glBegin(GL_TRIANGLES);
        glColor4f(rgb[0], rgb[1], rgb[2], a);
        for(int i = 0 ; i < 360; i++){
            degInRad = i*DEG2RAD;
            degInRad2 = (i+1)*DEG2RAD;
            glVertex2d(x, y);
            glVertex2d(x+cos(degInRad)*radius, y+sin(degInRad)*radius);
            glVertex2d(x+cos(degInRad2)*radius, y+sin(degInRad2)*radius);
       }
       glEnd();
    }
    
    private static void exit(){
        Display.destroy();
        System.exit(0);
    }
    
    private static void initColors(){
        colors = new HashMap<String, float[]>();
        float[] life = {255f/255f, 80f/255f, 80f/255f};
        float[] white = {225f/255f, 240f/255f, 200f/255f};
        float[] blue = {58f/255f, 68f/255f, 102f/255f};
        float[] blueDark = {38f/255f, 43f/255f, 68f/255f};
        float[] grey = {90f/255f, 105f/255f, 136f/255f};
        float[] greyLight = {139f/255f, 155f/255f, 180f/255f};
        float[] green = {92f/255f, 126f/255f, 41f/255f};
        float[] greenSemidark = {72f/255f, 98f/255f, 34f/255f};
        float[] greenDark = {38f/255f, 52f/255f, 18f/255f};
        
        colors.put("life", life);
        colors.put("white", white);
        colors.put("blue", blue);
        colors.put("blue_dark", blueDark);
        colors.put("grey", grey);
        colors.put("grey_light", greyLight);
        colors.put("green", green);
        colors.put("green_semidark", greenSemidark);
        colors.put("green_dark", greenDark);
    }
    
    private static void initTextures() {
        try {     
            textures = new HashMap<>();
            // Other
            textures.put("arrow", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/arrow.png"))));
            // Cursors
            textures.put("cursorDefault", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/cursor_default.png"))));
            textures.put("cursorPointer", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/cursor_pointer.png"))));
            textures.put("cursorGrab", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/cursor_grab.png"))));
            // Backgrounds
            textures.put("disabled", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/disabled.png"))));
            textures.put("board", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/board.png"))));
            textures.put("enemyBoard", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/enemy_board.png"))));
            textures.put("red", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/red.png"))));
            textures.put("white", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/white.png"))));
            // Map
            textures.put("roadStraight", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/road_straight.png"))));
            textures.put("roadTurn", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/road_turn.png"))));
            textures.put("grass", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/grass.png"))));
            textures.put("bigPlant1", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/big_plant1.png"))));
            textures.put("bigPlant2", TextureLoader.getTexture("PNG", new FileInputStream(new File("images/big_plant2.png"))));
            // Towers
            textures.put("basicTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("towers/basic_tower.png"))));
            textures.put("basicTowerBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("towers/basic_tower_base.png"))));
            textures.put("basicTowerTurret", TextureLoader.getTexture("PNG", new FileInputStream(new File("towers/basic_tower_turret.png"))));
            textures.put("circleTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("towers/circle_tower.png"))));
            // Bullets
            textures.put("bulletBlue", TextureLoader.getTexture("PNG", new FileInputStream(new File("towers/bullet_blue.png"))));
            textures.put("bulletGrey", TextureLoader.getTexture("PNG", new FileInputStream(new File("towers/bullet_grey.png"))));
            // Enemies
            textures.put("basicEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("enemies/basic_enemy.png"))));
            textures.put("basicEnemyBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("enemies/basic_enemy_bright.png"))));
            textures.put("fastEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("enemies/fast_enemy.png"))));
            textures.put("fastEnemyBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("enemies/fast_enemy_bright.png"))));
            textures.put("strongEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("enemies/strong_enemy.png"))));
            textures.put("strongEnemyBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("enemies/strong_enemy_bright.png"))));
            textures.put("trickyEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("enemies/tricky_enemy.png"))));
            textures.put("trickyEnemyBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("enemies/tricky_enemy_bright.png"))));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Towser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Towser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void releaseTextures(){
        for(Map.Entry<String, Texture> entry : textures.entrySet())
            entry.getValue().release();
    }
    
    @SuppressWarnings("unchecked")
    private static void setUpFont() {
        fonts = new HashMap<>();
        float[] color;
        String police = "Bahnschrift";
        
        color = Towser.colors.get("white");
        Font awtFont = new Font(police, Font.PLAIN, (int) (12*ref));
        UnicodeFont normalS = new UnicodeFont(awtFont);
        normalS.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalS.addAsciiGlyphs();
        
        color = Towser.colors.get("white");
        awtFont = new Font(police, Font.PLAIN, (int) (16*ref));
        UnicodeFont normal = new UnicodeFont(awtFont);
        normal.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normal.addAsciiGlyphs();
        
        color = Towser.colors.get("white");
        awtFont = new Font(police, Font.PLAIN, (int) (20*ref));
        UnicodeFont normalL = new UnicodeFont(awtFont);
        normalL.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalL.addAsciiGlyphs();
        
        color = Towser.colors.get("white");
        awtFont = new Font(police, Font.PLAIN, (int) (30*ref));
        UnicodeFont normalXL = new UnicodeFont(awtFont);
        normalXL.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalXL.addAsciiGlyphs();
        
        color = Towser.colors.get("white");
        awtFont = new Font(police, Font.BOLD, (int) (16*ref));
        UnicodeFont normalB = new UnicodeFont(awtFont);
        normalB.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalB.addAsciiGlyphs();
        
        color = Towser.colors.get("white");
        awtFont = new Font(police, Font.BOLD, (int) (20*ref));
        UnicodeFont normalLB = new UnicodeFont(awtFont);
        normalLB.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalLB.addAsciiGlyphs();
        
        color = Towser.colors.get("white");
        awtFont = new Font(police, Font.BOLD, (int) (30*ref));
        UnicodeFont normalXLB = new UnicodeFont(awtFont);
        normalXLB.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalXLB.addAsciiGlyphs();
        
        //color = Towser.colors.get("white");
        awtFont = new Font(police, Font.BOLD, (int) (20*ref));
        UnicodeFont astres = new UnicodeFont(awtFont);
        astres.getEffects().add(new ColorEffect(new Color(240, 220, 0)));
        astres.addAsciiGlyphs();
        
        color = Towser.colors.get("life");
        awtFont = new Font(police, Font.BOLD, (int) (20*ref));
        UnicodeFont life = new UnicodeFont(awtFont);
        life.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        life.addAsciiGlyphs();
        
        //color = Towser.colors.get("white");
        awtFont = new Font(police, Font.BOLD, (int) (14*ref));
        UnicodeFont canBuy = new UnicodeFont(awtFont);
        canBuy.getEffects().add(new ColorEffect(new Color(240, 220, 0)));
        canBuy.addAsciiGlyphs();
        
        //color = Towser.colors.get("white");
        awtFont = new Font(police, Font.BOLD, (int) (14*ref));
        UnicodeFont cantBuy = new UnicodeFont(awtFont);
        cantBuy.getEffects().add(new ColorEffect(new Color(210, 30, 30)));
        cantBuy.addAsciiGlyphs();
        
        // DISPLAY ALL FONTS AVAILABLE
        /*String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        for (int i = 0; i < fonts.length; i++) {
          System.out.println(fonts[i]);
        }*/
        
        try {
            normalS.loadGlyphs();
            fonts.put("normalS", normalS);
            normal.loadGlyphs();
            fonts.put("normal", normal);
            normalL.loadGlyphs();
            fonts.put("normalL", normalL);
            normalXL.loadGlyphs();
            fonts.put("normalXL", normalXL);
            normalB.loadGlyphs();
            fonts.put("normalB", normalB);
            normalLB.loadGlyphs();
            fonts.put("normalLB", normalLB);
            normalXLB.loadGlyphs();
            fonts.put("normalXLB", normalXLB);
            astres.loadGlyphs();
            fonts.put("astres", astres);
            life.loadGlyphs();
            fonts.put("life", life);
            canBuy.loadGlyphs();
            fonts.put("canBuy", canBuy);
            cantBuy.loadGlyphs();
            fonts.put("cantBuy", cantBuy);
        } catch (SlickException ex) {
            Logger.getLogger(Towser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void setCursor(Cursor cursorType){
        if(cursorType == cursor)
            return;
        
        cursor = cursorType;
        if(cursor == Cursor.DEFAULT){
            cursorTexture = textures.get("cursorDefault");
            cursorPos[0] = 7;
            cursorPos[1] = 4;
        }
        else if(cursor == Cursor.POINTER){
            cursorTexture = textures.get("cursorPointer");
            cursorPos[0] = 10;
            cursorPos[1] = 3;
        }
        else if(cursor == Cursor.GRAB){
            cursorTexture = textures.get("cursorGrab");
            cursorPos[0] = 13;
            cursorPos[1] = 16;
        }
    }
}
