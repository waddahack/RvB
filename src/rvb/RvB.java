package rvb;

import de.matthiasmann.twl.utils.PNGDecoder;
import managers.SoundManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import managers.TextManager;
import managers.PopupManager;
import static org.lwjgl.BufferUtils.createByteBuffer;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL12;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import ui.Overlay;

public class RvB{
    
    public static enum State{
        MENU, GAME, CREATION, EXIT
    }
    public static enum Cursor{
        DEFAULT, POINTER, GRAB
    }
    public static enum Difficulty{
        EASY(1), MEDIUM(2), HARD(3);
        public int value;
        
        Difficulty(int value){
            this.value = value;
        }
        
        public int getNbRoad(){
            if(this == Difficulty.EASY)
                return (int) ((nbTileX*nbTileY) / 6);
            
            if(this == Difficulty.MEDIUM)
                return (int) ((nbTileX*nbTileY) / 7);
            
            return (int) ((nbTileX*nbTileY) / 8);
        }
    }
    
    public static State state = State.MENU;
    public static Cursor cursor = null;
    public static int nbTileX = 32, nbTileY = 18; // Tilemap size. Doit être en accord avec le format des levels campagne à venir (32/18)
    public static int unite;
    public static int windWidth, windHeight;
    public static int syncFPS = 120, averageFPS = syncFPS, FPScounter = 0, counter = 0;
    public static float ref;
    public static boolean mouseDown = false, stateChanged = false;
    public static double lastUpdate, lastUpdateFPS;
    public static double deltaTime;
    public static Menu menu;
    public static Game game = null, adventureGame = null, randomGame = null, createdGame = null;
    public static Creation creation = null;
    public static Map<String, Texture> textures;
    public static Map<String, UnicodeFont> fonts;
    public static Map<String, float[]> colors;
    public static DecimalFormat formatter = new DecimalFormat("#.##");
    public static Texture cursorTexture = null;
    public static int[] cursorPos = new int[2];
    private static Overlay debugTool;
    
    public static void main(String[] args){
        System.setProperty("org.lwjgl.librarypath", new File("lib").getAbsolutePath());
        try{
            Display.setLocation(0, 0);
            Display.setFullscreen(true);
            //Display.setDisplayMode(new DisplayMode(windWidth, windHeight));
            Display.setResizable(false);
            Display.setTitle("RvB");
            Display.setIcon(new ByteBuffer[] {
                loadIcon(16),
                loadIcon(32),
            });

            Display.create();
        }catch (LWJGLException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        } catch (IOException ex) {
            Logger.getLogger(RvB.class.getName()).log(Level.SEVERE, null, ex);
        }
        unite = Math.min(Math.floorDiv(Display.getWidth(), nbTileX), Math.floorDiv(Display.getHeight(), nbTileY));
        if(unite%2 != 0)
            unite -= 1;
        windWidth = unite*nbTileX;
        windHeight = unite*nbTileY;
        ref = unite/60f;
        
        init();
        
        while(!Display.isCloseRequested()){
            lastUpdate = System.currentTimeMillis();
            
            update();
            mouseDown = (Mouse.isButtonDown(0) || Mouse.isButtonDown(1));
            stateChanged = false;
            
            Display.update();
            Display.sync(syncFPS);
            
            deltaTime = System.currentTimeMillis() - lastUpdate;
            calculateFPS();
            
        }
        releaseTextures();
        exit();
    }
    
    private static void calculateFPS(){
        FPScounter += Math.round(1000/deltaTime);
        counter++;
        if(System.currentTimeMillis() - lastUpdateFPS >= 500){
            lastUpdateFPS = System.currentTimeMillis();
            averageFPS = Math.round(FPScounter/counter);
            if(averageFPS > syncFPS)
                averageFPS = syncFPS;
            FPScounter = 0;
            counter = 0;
        }
    }
    
    private static ByteBuffer loadIcon(int bytes) throws IOException {
        File initialFile = new File("assets/images/game_icon"+bytes+".png");
        InputStream is = new FileInputStream(initialFile);
        try {
            PNGDecoder decoder = new PNGDecoder(is);
            ByteBuffer bb = ByteBuffer.allocateDirect(decoder.getWidth()*decoder.getHeight()*4);
            decoder.decode(bb, decoder.getWidth()*4, PNGDecoder.Format.RGBA);
            bb.flip();
            return bb;
        } finally {
            is.close();
        }
    }
    
    public static void init(){    
        initTextures();
        initColors();
        initFonts();
        initDebugTool();
        
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
        TextManager.initialize();
        menu = new Menu();
        lastUpdate = System.currentTimeMillis();
        lastUpdateFPS = System.currentTimeMillis();
    }

    public static void update() {
        glClear(GL_COLOR_BUFFER_BIT);
        switch(state){
            case MENU:
                menu.update();
                break;
            case GAME:
                game.update();
                break;
            case CREATION:
                creation.update();
                break;
            case EXIT:
                releaseTextures();
                //releaseAudio();
                exit();
                break;
        }
        
        checkInput();
        PopupManager.Instance.update();
        SoundManager.Instance.update();
        
        // DEBUG
        renderDebugTool();     
        
        renderMouse();
    }  
    
    private static void initDebugTool(){
        debugTool = new Overlay(windWidth-260, windHeight/6, 250, 4*windHeight/6);
        debugTool.setRGBA(new float[]{70/255f, 70/255f, 70/255f}, 0.4f);
        debugTool.display(false);
    }
    
    private static void renderDebugTool(){
        if(!debugTool.isDisplayed())
            return;
        debugTool.render();
        debugTool.setAnchor("topLeft");
        // FPS
        debugTool.drawText(10, 10, "FPS : ", fonts.get("normalS"));
        debugTool.drawText(60, 10, averageFPS+"", fonts.get("normalS"));
    }
    
    public static void renderMouse(){
        if(cursorTexture != null)
            drawFilledRectangle(Mouse.getX()-cursorPos[0], windHeight-Mouse.getY()-cursorPos[1], 28, 28, null, 1, cursorTexture);
    }
    
    public static void checkInput() {
        State s = state;
        if(Keyboard.next()){
            
            // ESCAPE MENU
            if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && !(game != null && game.gameSpeed == 0)){
                switchStateTo(State.MENU);
                if(PopupManager.Instance.onPopup())
                    PopupManager.Instance.closeCurrentPopup();
            }
            
            // DEBUG & CHEATS KEY BINDS
            if(Keyboard.isKeyDown(Keyboard.KEY_F1)){
                if(Keyboard.isKeyDown(Keyboard.KEY_M)){
                    if(game != null)
                        game.money += 10000;
                }
                else if(Keyboard.isKeyDown(Keyboard.KEY_D))
                    debugTool.display(!debugTool.isDisplayed());
                else if(Keyboard.isKeyDown(Keyboard.KEY_L)){
                    if(game != null && game.gameSpeed > 0 && game.raztech != null)
                        game.raztech.levelUp();
                }
                // Game speed
                else if(Keyboard.isKeyDown(Keyboard.KEY_NUMPAD0)){
                    if(game != null)
                        game.gameSpeed = 0;
                }
                else if(Keyboard.isKeyDown(Keyboard.KEY_NUMPAD1)){
                    if(game != null)
                        game.gameSpeed = 1;
                }
                else if(Keyboard.isKeyDown(Keyboard.KEY_NUMPAD2)){
                    if(game != null)
                        game.gameSpeed = 2;
                }
                else if(Keyboard.isKeyDown(Keyboard.KEY_NUMPAD3)){
                    if(game != null)
                        game.gameSpeed = 4;
                }
            }
            
            //System.out.println(Keyboard.getEventKey());
        }
        
        if(s != state)
            setCursor(Cursor.DEFAULT);
    }
    
    public static void newRandomMap(Difficulty difficulty){
        randomGame = new Game(difficulty);
        
        if(randomGame.path.size() == 0){
            randomGame = null;
            return;
        }
        game = randomGame;
        switchStateTo(State.GAME);
    }
    
    public static void newCreatedMap(Difficulty difficulty){
        createdGame = new Game("created", difficulty);
        game = createdGame;
        switchStateTo(State.GAME);
    }
    
    public static boolean createEmptyMap(){
        try{
            File file = new File("assets/levels/level_created.txt");
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
    
    public static void switchStateTo(State s){
        state = s;
        stateChanged = true;
        setCursor(Cursor.DEFAULT);
        if(s == State.MENU){
            menu.enableAllButtons();
            if(game != null)
                SoundManager.Instance.pauseAll();
        }  
        else if(s == State.GAME){
            game.enableAllButtons();
            SoundManager.Instance.unpauseAll();
        }
        else if(s == State.CREATION)
            creation.enableAllButtons();
    }
    
    public static void drawString(int x, int y, String text, UnicodeFont font){
        font.drawString(x - font.getWidth(text)/2, y - font.getHeight(text)/2, text);
    }
    
    public static void drawRectangle(int x, int y, int width, int height, float[] rgb, float a, int thickness){
        glDisable(GL_TEXTURE_2D);
        
        glColor4f(rgb[0], rgb[1], rgb[2], a);
        
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
    
    public static void drawTextureID(float x, float y, int width, int height, int textureID){
        glPushMatrix(); //Save the current matrix.
        
        glTranslated(x, y, 0);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);
        
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
        
        glPopMatrix(); // Reset the current matrix to the one that was saved.
    }
     
    public static void drawFilledRectangle(float x, float y, int width, int height, Texture texture, double angle, float a){
        glPushMatrix(); //Save the current matrix.
        
        glTranslated(x, y, 0);
        if(angle != 0)
            glRotated(angle, 0, 0, 1);

        drawFilledRectangle(-width/2, -height/2, width, height, null, a, texture);
        
        glPopMatrix(); // Reset the current matrix to the one that was saved.
    }
    
    public static void drawFilledRectangle(float x, float y, int width, int height, float[] rgb, float a, Texture texture){
        if (texture != null) {
            texture.bind();
            glEnable(GL_TEXTURE_2D);
            glColor4f(1, 1, 1, a);
        }
        else{
            glDisable(GL_TEXTURE_2D);
            glColor4f(rgb[0], rgb[1], rgb[2], a);
        }
        
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
    
    public static void exit(){
        Display.destroy();
        System.exit(0);
    }
    
    public static void initTextures() {
        try {     
            textures = new HashMap<>();
            // Cursors
            textures.put("cursorDefault", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/cursor_default.png"))));
            textures.put("cursorPointer", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/cursor_pointer.png"))));
            textures.put("cursorGrab", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/cursor_grab.png"))));
            // Backgrounds
            textures.put("disabled", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/disabled.png"))));
            textures.put("board", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/board.png"))));
            textures.put("darkBoard", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/dark_board.png"))));
            textures.put("title", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/title.png"))));
            // Map
            textures.put("roadStraight", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/road_straight.png"))));
            textures.put("steps0", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/steps0.png"))));
            textures.put("steps1", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/steps1.png"))));
            textures.put("steps2", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/steps2.png"))));
            textures.put("steps3", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/steps3.png"))));
            textures.put("steps4", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/steps4.png"))));
            textures.put("roadTurn", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/road_turn.png"))));
            textures.put("steps0Turn", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/steps0_turn.png"))));
            textures.put("steps1Turn", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/steps1_turn.png"))));
            textures.put("steps2Turn", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/steps2_turn.png"))));
            textures.put("steps3Turn", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/steps3_turn.png"))));
            textures.put("steps4Turn", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/steps4_turn.png"))));
            textures.put("grass", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/grass.png"))));
            textures.put("bigPlant1", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/big_plant1.png"))));
            textures.put("bigPlant2", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/big_plant2.png"))));
            textures.put("rock1", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/rock1.png"))));
            textures.put("rock2", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/rock2.png"))));
            // Icons
            textures.put("FR", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/drapeau_francais.png"))));
            textures.put("ENG", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/drapeau_RU.png"))));
            textures.put("arrow", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/arrow.png"))));
            textures.put("arrowBack", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/arrow_back.png"))));
            textures.put("lock", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/lock.png"))));
            textures.put("place", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/place.png"))));
            textures.put("placeRaztech", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/place_raztech.png"))));
            textures.put("plus", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/plus.png"))));
            textures.put("optionIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/option_icon.png"))));
            textures.put("exitIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/exit_icon.png"))));
            textures.put("rangeIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/range_icon.png"))));
            textures.put("powerIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/power_icon.png"))));
            textures.put("attackSpeedIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/attack_speed_icon.png"))));   
            textures.put("bulletSpeedIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/bullet_speed_icon.png"))));   
            textures.put("explodeRadiusIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/explode_radius_icon.png")))); 
            textures.put("coins", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/coins.png"))));
            textures.put("coinsCantBuy", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/coins_cantBuy.png"))));
            textures.put("heart", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/heart.png"))));
            // Towers
            textures.put("raztech", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/raztech.png"))));
            
            textures.put("basicTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/basic_tower.png"))));
            textures.put("basicTowerBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/basic_tower_base.png"))));
            textures.put("basicTowerTurret", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/basic_tower_turret.png"))));
            
            textures.put("circleTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/circle_tower.png"))));
            
            textures.put("bigTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/big_tower.png"))));
            textures.put("bigTowerBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/big_tower_base.png"))));
            textures.put("bigTowerTurret", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/big_tower_turret.png"))));
            
            textures.put("flameTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/flame_tower.png"))));
            textures.put("flameTowerBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/flame_tower_base.png"))));
            textures.put("flameTowerTurret", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/flame_tower_turret.png"))));
            // Buffs
            textures.put("buff_slow", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/buffs/buff_slow.png"))));
            textures.put("buff_upgrade", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/buffs/buff_upgrade.png"))));
            // Bullets
            textures.put("bulletBlue", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/bullet_blue.png"))));
            textures.put("bullet", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/bullet.png"))));
            textures.put("gun_bullet", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/gun_bullet.png"))));
            textures.put("shell", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/shell.png"))));
            textures.put("flame", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/flame.png"))));
            // Enemies
            textures.put("basicEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/basic_enemy.png"))));
            textures.put("basicEnemyBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/basic_enemy_bright.png"))));
            
            textures.put("fastEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/fast_enemy.png"))));
            textures.put("fastEnemyBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/fast_enemy_bright.png"))));
            
            textures.put("strongEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/strong_enemy.png"))));
            textures.put("strongEnemyBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/strong_enemy_bright.png"))));
            
            textures.put("trickyEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/tricky_enemy.png"))));
            textures.put("trickyEnemyBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/tricky_enemy_bright.png"))));
            
            textures.put("flyingEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/flying_enemy.png"))));
            textures.put("flyingEnemyBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/flying_enemy_base.png"))));
            textures.put("flyingEnemyBaseBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/flying_enemy_base_bright.png"))));
            textures.put("flyingEnemyProp", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/flying_enemy_prop.png"))));
            textures.put("flyingEnemyPropBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/flying_enemy_prop_bright.png"))));
            // Bazoo
            textures.put("bazoo", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo.png"))));
            textures.put("bazooBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_bright.png"))));
            textures.put("bazooZoomed", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_zoomed.png"))));
            textures.put("bazooEvo1", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo1.png"))));
            textures.put("bazooEvo1Bright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo1_bright.png"))));
            textures.put("bazooEvo2", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo2.png"))));
            textures.put("bazooEvo2Bright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo2_bright.png"))));
            textures.put("bazooEvo3", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo3.png"))));
            textures.put("bazooEvo3Bright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo3_bright.png"))));
            textures.put("bazooEvo4", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo4.png"))));
            textures.put("bazooEvo4Bright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo4_bright.png"))));
            textures.put("bazooEvo5", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo5.png"))));
            textures.put("bazooEvo5Bright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo5_bright.png"))));
            textures.put("bazooEvo6", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo6.png"))));
            textures.put("bazooEvo6Bright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo6_bright.png"))));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RvB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RvB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void releaseTextures(){
        for(Map.Entry<String, Texture> entry : textures.entrySet())
            entry.getValue().release();
    }
    
    private static final int BYTES_PER_PIXEL = 4;
    public static int loadTexture(BufferedImage image){
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL); //4 for RGBA, 3 for RGB

        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS

        // You now have a ByteBuffer filled with the color data of each pixel.
        // Now just create a texture ID and bind it. Then you can load it using 
        // whatever OpenGL method you want, for example:

        int textureID = glGenTextures(); //Generate texture ID
        glBindTexture(GL_TEXTURE_2D, textureID); //Bind texture ID

        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        //Return the texture ID so we can bind it later again
        return textureID;
    }
    
    public static void initColors(){
        colors = new HashMap<>();
        float[] life = {255f/255f, 80f/255f, 80f/255f};
        float[] life1 = {255f/255f, 120f/255f, 75f/255f};
        float[] life2 = {255f/255f, 155f/255f, 75f/255f};
        float[] life3 = {255f/255f, 195f/255f, 80f/255f};
        float[] life4 = {255f/255f, 225f/255f, 80f/255f};
        float[] life5 = {255f/255f, 255f/255f, 25f/255f};
        float[] life6 = {210f/255f, 255f/255f, 25f/255f};
        float[] money = {240f/255f, 220f/255f, 0};
        float[] lightGreen = {225f/255f, 240f/255f, 200f/255f};
        float[] lightBlue = {116f/255f, 136f/255f, 204f/255f};
        float[] blue = {58f/255f, 68f/255f, 102f/255f};
        float[] blueDark = {38f/255f, 43f/255f, 68f/255f};
        float[] grey = {90f/255f, 105f/255f, 136f/255f};
        float[] greyLight = {139f/255f, 155f/255f, 180f/255f};
        float[] black = {20f/255f, 20f/255f, 20f/255f};
        float[] green = {92f/255f, 126f/255f, 41f/255f};
        float[] greenSemidark = {72f/255f, 98f/255f, 34f/255f};
        float[] greenDark = {38f/255f, 52f/255f, 18f/255f};
        float[] bonus = {150f/255f, 195f/255f, 255f/255f};
        float[] lightRed = {245f/255f, 225f/255f, 220f/255f};
        
        colors.put("life", life);
        colors.put("life1", life1);
        colors.put("life2", life2);
        colors.put("life3", life3);
        colors.put("life4", life4);
        colors.put("life5", life5);
        colors.put("life6", life6);
        colors.put("money", money);
        colors.put("lightGreen", lightGreen);
        colors.put("lightRed", lightRed);
        colors.put("lightBlue", lightBlue);
        colors.put("blue", blue);
        colors.put("blue_dark", blueDark);
        colors.put("grey", grey);
        colors.put("grey_light", greyLight);
        colors.put("black", black);
        colors.put("green", green);
        colors.put("green_semidark", greenSemidark);
        colors.put("green_dark", greenDark);
        colors.put("bonus", bonus);
    }
    
    @SuppressWarnings("unchecked")
    public static void initFonts() {
        fonts = new HashMap<>();
        float[] color;
        String police = "Bahnschrift";
        
        color = RvB.colors.get("lightGreen");
        Font awtFont = new Font(police, Font.BOLD, (int)(16*ref));
        UnicodeFont normalSB = new UnicodeFont(awtFont);
        normalSB.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalSB.addAsciiGlyphs();
        
        color = RvB.colors.get("lightGreen");
        awtFont = new Font(police, Font.PLAIN, (int)(16*ref));
        UnicodeFont normalS = new UnicodeFont(awtFont);
        normalS.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalS.addAsciiGlyphs();
        
        color = RvB.colors.get("lightGreen");
        awtFont = new Font(police, Font.PLAIN, (int)(20*ref));
        UnicodeFont normal = new UnicodeFont(awtFont);
        normal.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normal.addAsciiGlyphs();
        
        color = RvB.colors.get("black");
        awtFont = new Font(police, Font.PLAIN, (int)(20*ref));
        UnicodeFont normalBlack = new UnicodeFont(awtFont);
        normalBlack.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalBlack.addAsciiGlyphs();
        
        color = RvB.colors.get("lightGreen");
        awtFont = new Font(police, Font.PLAIN, (int)(25*ref));
        UnicodeFont normalL = new UnicodeFont(awtFont);
        normalL.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalL.addAsciiGlyphs();
        
        color = RvB.colors.get("lightGreen");
        awtFont = new Font(police, Font.PLAIN, (int)(34*ref));
        UnicodeFont normalXL = new UnicodeFont(awtFont);
        normalXL.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalXL.addAsciiGlyphs();
        
        color = RvB.colors.get("lightGreen");
        awtFont = new Font(police, Font.BOLD, (int)(20*ref));
        UnicodeFont normalB = new UnicodeFont(awtFont);
        normalB.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalB.addAsciiGlyphs();
        
        color = RvB.colors.get("lightGreen");
        awtFont = new Font(police, Font.BOLD, (int)(24*ref));
        UnicodeFont normalLB = new UnicodeFont(awtFont);
        normalLB.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalLB.addAsciiGlyphs();
        
        color = RvB.colors.get("lightGreen");
        awtFont = new Font(police, Font.BOLD, (int)(34*ref));
        UnicodeFont normalXLB = new UnicodeFont(awtFont);
        normalXLB.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalXLB.addAsciiGlyphs();
        
        color = RvB.colors.get("money");
        awtFont = new Font(police, Font.BOLD, (int)(24*ref));
        UnicodeFont money = new UnicodeFont(awtFont);
        money.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        money.addAsciiGlyphs();
        
        color = RvB.colors.get("life");
        awtFont = new Font(police, Font.BOLD, (int)(24*ref));
        UnicodeFont life = new UnicodeFont(awtFont);
        life.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        life.addAsciiGlyphs();
        
        color = RvB.colors.get("money");
        awtFont = new Font(police, Font.BOLD, (int)(18*ref));
        UnicodeFont canBuy = new UnicodeFont(awtFont);
        canBuy.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        canBuy.addAsciiGlyphs();
        
        color = RvB.colors.get("lightRed");
        awtFont = new Font(police, Font.PLAIN, (int)(18*ref));
        UnicodeFont cantBuy = new UnicodeFont(awtFont);
        cantBuy.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        cantBuy.addAsciiGlyphs();
        
        color = RvB.colors.get("bonus");
        awtFont = new Font(police, Font.PLAIN, (int)(20*ref));
        UnicodeFont bonus = new UnicodeFont(awtFont);
        bonus.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        bonus.addAsciiGlyphs();
        
        // DISPLAY ALL FONTS AVAILABLE
        /*String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        for (int i = 0; i < fonts.length; i++) {
          System.out.println(fonts[i]);
        }*/
        
        try {
            normalS.loadGlyphs();
            fonts.put("normalS", normalS);
            normalSB.loadGlyphs();
            fonts.put("normalSB", normalSB);
            normal.loadGlyphs();
            fonts.put("normal", normal);
            normalBlack.loadGlyphs();
            fonts.put("normalBlack", normalBlack);
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
            money.loadGlyphs();
            fonts.put("money", money);
            life.loadGlyphs();
            fonts.put("life", life);
            canBuy.loadGlyphs();
            fonts.put("canBuy", canBuy);
            cantBuy.loadGlyphs();
            fonts.put("cantBuy", cantBuy);
            bonus.loadGlyphs();
            fonts.put("bonus", bonus);
        } catch (SlickException ex) {
            Logger.getLogger(RvB.class.getName()).log(Level.SEVERE, null, ex);
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
