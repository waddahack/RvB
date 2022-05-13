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
        EASY(0), MEDIUM(2), HARD(3);
        public int value;
        public static int multiplicaterMax = 4;
        private Difficulty(int value){
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
    public static Game game = null;
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
            switchStateTo(State.MENU);
            setCursor(Cursor.DEFAULT);
        }
        // MENU BUTTONS
        if(state == State.MENU){
            if(menu.getStart().isClicked(0)){
                switchStateTo(State.GAME);
                if(game == null || game.ended || game.waveNumber == 1)
                    game = new Game("1");
                else
                    SoundManager.Instance.unpauseAll();
            }  
            if(menu.getRandom().isClicked(0)){
                switchStateTo(State.GAME);
                if(game != null && !game.ended && game.waveNumber > 1){
                    SoundManager.Instance.unpauseAll();
                    return;
                }
                if(generateRandomMap(Difficulty.EASY));
                    game = new Game("random");
            }
            if(menu.getCreate().isClicked(0)){
                switchStateTo(State.CREATION);
                if(creation != null && !creation.ended)
                    return;
                
                if(generateEmptyMap())
                    creation = new Creation();
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
    
    private static boolean generateRandomMap(Difficulty diff){
        try{
            File file = new File("levels/level_random.txt");
            if(file.createNewFile()){
                FileWriter myWriter = new FileWriter(file, false);
                Random rand = new Random();
                int x, y;
                ArrayList<int[]> path = new ArrayList<>();
                // nombre de road de la map restante
                int nbRoadLeft = Math.max(nbTileX, nbTileY)*2*(Difficulty.multiplicaterMax-diff.value);
                // spawn random sur un bord
                y = rand.nextInt(nbTileY);
                if(y == 0 || y == nbTileY-1)
                    x = rand.nextInt(nbTileX);
                else
                    x = rand.nextInt(2)*(nbTileX-1);
                path.add(new int[]{x, y});
                nbRoadLeft--;
                // tant que la road actuelle est à une distance < au nb de road max accordé
                ArrayList<int[]> coordsAvailable = new ArrayList<>();
                int[] left, right, up, down;
                while(nbRoadLeft > 0){ //TODO à revoir
                    // tant que nbRoadLeft > 0 on trace une route au pif
                    // après, on foce vers une sortie et pour ça :
                    //     - on regarde de tous les côtés et on compte le nombre de côté qui n'ont pas de chemin sur le passage
                    // (faire une fonction qui retourne une liste avec les directions left/right/up/down qui contient les coords d'un chemin qui coupe le chemin)
                    //         - si n > 0, prendre la direction la plus courte
                    //         - sinon, faut faire demi tour :
                    //             - regarder la direction du chemin bloquant droite et gauche, et tourner en direction du chemin qui à la même direction
                    coordsAvailable.clear();
                    left = new int[]{x-1, y};
                    right = new int[]{x+1, y};
                    up = new int[]{x, y-1};
                    down = new int[]{x, y+1};
                    // ajout des voisins si c'est pas un bord
                    if(left[0] >= 0 && left[1] == y)
                        coordsAvailable.add(left);
                    if(right[0] <= nbTileX-1 && right[1] == y)
                        coordsAvailable.add(right);
                    if(up[0] == x && up[1] >= 0)
                        coordsAvailable.add(up);
                    if(down[0] == x && down[1] <= nbTileY-1)
                        coordsAvailable.add(down);
                    // remove les voisins qui sont déjà dans path
                    for(int[] coord : path){
                        if(coord[0] == left[0] && coord[1] == left[1])
                            coordsAvailable.remove(left);
                        else if(coord[0] == right[0] && coord[1] == right[1])
                            coordsAvailable.remove(right);
                        else if(coord[0] == up[0] && coord[1] == up[1])
                            coordsAvailable.remove(up);
                        else if(coord[0] == down[0] && coord[1] == down[1])
                            coordsAvailable.remove(down);
                    }
                    // remove les voisins qui nous fait entrer dans une boucle
                    if(coordsAvailable.size() > 1){
                        //    - si j'ai un voisin en face de moi (par rapport à ma direction)
                        //        - si sa direction est perpendiculaire, aller dans la direction opposé
                        //        - sinon, regarder celle de son prédécesseur et aller dans la direction opposé
                        if(!coordsAvailable.contains(up) && !coordsAvailable.contains(down)){
                            
                        }
                        else if(!coordsAvailable.contains(left) && !coordsAvailable.contains(right)){
                            
                        }
                    }
                    // prendre les coords aléatoirement parmi celles dispo
                    int r = rand.nextInt(coordsAvailable.size());
                    x = coordsAvailable.get(r)[0];
                    y = coordsAvailable.get(r)[1];
                    path.add(new int[]{x, y});
                    nbRoadLeft--;
                }
                String randomMap = "";
                myWriter.write(randomMap);
                myWriter.close();
            }
            return true;
        }
        catch(Exception e){
            System.out.println(e);
            return false;
        }
    }
    
    private static int distanceFromEdge(int x, int y){
        int distanceX, distanceY;
        distanceX = x;
        if(distanceX > (nbTileX-1)/2)
            distanceX = nbTileX-1-x;
        distanceY = y;
        if(distanceY > (nbTileY-1)/2)
            distanceY = nbTileY-1-y;
        return Math.min(distanceX, distanceY);
    }
    
    private static void switchStateTo(State s){
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
