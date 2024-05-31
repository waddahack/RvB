package rvb;

import Windows.StatsWindow;
import Windows.MenuWindow;
import Windows.OptionsWindow;
import Buffs.*;
import de.matthiasmann.twl.utils.PNGDecoder;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import managers.*;
import managers.TextManager.Text;
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
import towers.Tower;
import ui.Overlay;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.management.ManagementFactory;
import org.lwjgl.opengl.DisplayMode;
import towers.Raztech;

public class RvB{
    
    public static enum State{
        MENU, STATS, OPTIONS, GAME, CREATION, EXIT
    }
    public static enum Cursor{
        DEFAULT, POINTER, GRAB
    }
    public static enum Difficulty{
        EASY(24, 125, 350, 300, 0.8f, 0.8f, "EASY", 1, 0.5f),
        MEDIUM(30, 100, 300, 250, 1f, 1f, "MEDIUM", 2, 1),
        HARD(36, 75, 250, 200, 1.1f, 1.1f, "HARD", 3, 3f),
        HARDCORE(36, 1, 250, 200, 1.1f, 1.1f, "HARDCORE", 3, 10); 
        
        public int probabilityRange; // for turns probability maps random
        public float riskValue;
        public String name;
        public int nbWaveMax, life, money, waveReward;
        public float waveBalanceMult, enemiesLife;
        
        Difficulty(int nbWaveMax, int life, int money, int waveReward, float waveBalanceMult, float enemiesLife, String name, int probabilityRange, float riskValue){
            this.nbWaveMax = nbWaveMax;
            this.life = life;
            this.money = money;
            this.waveReward = waveReward;
            this.waveBalanceMult = waveBalanceMult;
            this.enemiesLife = enemiesLife;
            this.name = name;
            this.probabilityRange = probabilityRange;
            this.riskValue = riskValue;
        }
        
        public int getNbRoad(){
            if(this == Difficulty.EASY)
                return (int) ((nbTileX*nbTileY) / 5);
            
            if(this == Difficulty.MEDIUM)
                return (int) ((nbTileX*nbTileY) / 6);
            
            return (int) ((nbTileX*nbTileY) / 7);
        }
    }
    
    public static State state = State.MENU;
    public static Cursor cursor = null;
    public static int nbTileX = 32, nbTileY = 18; // Tilemap size. Doit être en accord avec le format des levels campagne à venir (32/18)
    public static int unite;
    public static int windWidth, windHeight;
    public static int syncFPS = 120, averageFPS = syncFPS, FPScounter = 0, counter = 0;
    public static float ref;
    public static boolean mouseDown = false, stateChanged = false, displayLifebars = true;
    public static double lastUpdate, lastUpdateFPS;
    public static double deltaTime;
    public static MenuWindow menu;
    public static StatsWindow menuStats;
    public static OptionsWindow options;
    public static Game game = null, adventureGame = null;
    public static Creation creation = null;
    public static Map<String, Texture> textures;
    public static Map<String, UnicodeFont> fonts;
    public static Map<String, float[]> colors;
    public static DecimalFormat formatter = new DecimalFormat("#.##");
    public static Texture cursorTexture = null;
    public static int[] cursorPos = new int[2];
    private static Overlay debugTool;
    private static ArrayList<String> consoleLines = new ArrayList<>();
    private static String commandPrompt = "";
    private static int nbConsoleLines = 0, nbConsoleLinesMax = 7;
    private static boolean listeningKeyboard = false;
    private static boolean exit = false, debugging = false;
    // PROPERTIES
    public static boolean cheatsActivated;
    
    public static void main(String[] args){
        System.setProperty("org.lwjgl.librarypath", new File("lib").getAbsolutePath());
        /*String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            // Ajouter l'option -z noexecstack aux arguments de lancement
            System.setProperty("org.lwjgl.librarypath", "lib/");
            System.setProperty("run.args", "-z noexecstack");
        } else if (os.contains("mac os x") || os.contains("macos") || os.contains("darwin")) {
            // Ajouter l'option -Wl,-no_pie aux arguments de lancement
            System.setProperty("org.lwjgl.librarypath", "lib/");
            System.setProperty("run.args", "-Wl,-no_pie");
        }
        if (System.getProperty("org.lwjgl.librarypath") == null) {
            System.setProperty("org.lwjgl.librarypath", new File("lib").getAbsolutePath());
        }*/
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.contains("-agentlib:jdwp")) {
                debugging = true;
                break;
            }
        }
        try{
            if(Display.getDesktopDisplayMode().isFullscreenCapable() && !debugging)
                Display.setDisplayModeAndFullscreen(Display.getDesktopDisplayMode());
            else{
                java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                Display.setDisplayMode(new DisplayMode(screenSize.width, screenSize.height));
            }
            
            Display.setLocation(0, 0);
            Display.setInitialBackground(72f/255f, 98/255f, 34/255f);
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

        while(!Display.isCloseRequested() && !exit){
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
        saveAndExit();
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
        StatsManager.initialize();
        menu = new MenuWindow();
        menuStats = new StatsWindow();
        options = new OptionsWindow();
        TutoManager.initialize();
        RVBDB.initialize();
        
        lastUpdate = System.currentTimeMillis();
        lastUpdateFPS = System.currentTimeMillis();
    }

    public static void initPropertiesAndGame(boolean inGame, boolean cheatsOn, String language, String stats, String tutoSteps, String pathString, String holesString, String difficulty, int life, int money, int waveNumber, String arrayTowers, String arrayBuffs, String buffsUsed) throws IOException{
        cheatsActivated = cheatsOn;
        switch(language){
            case "FR":
                menu.FR.click(false);
                break;
            case "ENG":
                menu.ENG.click(false);
                break;
        }
        ObjectMapper mapper = new ObjectMapper();
        if(stats != null && !stats.isEmpty()){
            StatsManager.Instance = mapper.readValue(stats, StatsManager.class);
            StatsManager.Instance.updateProgression();
        }
        if(inGame){
            Difficulty diff = Difficulty.MEDIUM;
            switch(difficulty){
                case "EASY":
                    diff = Difficulty.EASY;
                    break;
                case "MEDIUM":
                    diff = Difficulty.MEDIUM;
                    break;
                case "HARD":
                    diff = Difficulty.HARD;
                    break;
                case "HARDCORE":
                    diff = Difficulty.HARDCORE;
                    break;
            }
            String[] arrayPath = pathString.split(";");
            ArrayList<Tile> path = new ArrayList<>();
            for(String pos : arrayPath){
                Tile road = new Tile(RvB.textures.get("roadStraight"), "road");
                String[] indexes = pos.split("/");
                road.setRotateIndex(0);
                road.setX(Integer.parseInt(indexes[0])*unite);
                road.setY(Integer.parseInt(indexes[1])*unite);
                road.nbStepped = Integer.parseInt(indexes[2]);
                path.add(road);
            }
            setMap(path, diff);
            if(game != null){
                game.gameLoaded = true;
                game.life = life;
                game.money = money;
                game.waveNumber = waveNumber;
                game.enemiesBonusLife = 25*((int)((waveNumber+1)/AppCore.bossEvery));
                game.enemiesBonusMS = 8*((int)((waveNumber+1)/AppCore.bossEvery));
                // Steps on roads
                for(Tile road : game.path)
                    road.setSteppedTexture();
                // Towers
                Tower[] towers = mapper.readValue(arrayTowers, Tower[].class);
                for(Tower t : towers){
                    game.towers.add(t);
                    t.updatePos();
                    t.autoPlace(game.map);
                    if(t.getFocusButton() != null)
                        t.getFocusButton().indexSwitch = t.getFocusIndex();
                    for(int i = 0 ; i < t.nbUpgradesUsed.length ; i++)
                        t.getUpgrades().get(i).setNbUsed(t.nbUpgradesUsed[i]);
                    if(t.name == Text.RAZTECH){
                        game.raztech = (Raztech) t;
                        for(int i = 0 ; i < game.raztech.lvl-1 ; i++)
                            game.raztech.levelUp(true);
                    }
                }
                // Holes
                if(!holesString.isEmpty()){
                    String[] arrayHoles = holesString.split(";");
                    for(String pos : arrayHoles){
                        Tile hole = new Tile(RvB.textures.get("grassHole"), "hole");
                        String[] indexes = pos.split("/");
                        hole.setRotateIndex(0);
                        hole.setX(Integer.parseInt(indexes[0])*unite);
                        hole.setY(Integer.parseInt(indexes[1])*unite);
                        game.map.get(hole.getIndexY()).set(hole.getIndexX(), hole);
                    }
                }
                // Buffs
                Buff[] buffs = mapper.readValue(arrayBuffs, Buff[].class);
                game.buffs.clear();
                for(Buff b : buffs)
                    game.buffs.push(b);
                // BuffsUsed
                String[] arrayBuffsUsed = buffsUsed.split(";");
                for(String bu : arrayBuffsUsed){
                    switch(bu){
                        case "OS":
                            new OS().pick();
                            break;
                        case "Slow":
                            new Slow().pick();
                            break;
                        case "UpPowerTower":
                            new UpPowerTower().pick();
                            break;
                        case "UpRangeTower":
                            new UpRangeTower().pick();
                            break;
                        case "UpShootRateTower":
                            new UpShootRateTower().pick();
                            break;
                        case "Upgrade":
                            new Upgrade().pick();
                            break;
                        case "XP":
                            new XP().pick();
                            break;
                    }
                }
                game.buffsUsed = buffsUsed;
            }
        }
        TutoManager.Instance.setupSteps(tutoSteps);
    }
    
    public static void update() {
        glClear(GL_COLOR_BUFFER_BIT);
        switch(state){
            case MENU:
                menu.update();
                break;
            case STATS:
                menuStats.update();
                break;
            case OPTIONS:
                options.update();
                break;
            case GAME:
                game.update();
                break;
            case CREATION:
                creation.update();
                break;
            case EXIT:
                exit = true;
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
        debugTool = new Overlay((int) (windWidth-290*ref), windHeight/6, (int) (280*ref), 4*windHeight/6);
        debugTool.setRGBA(new float[]{70/255f, 70/255f, 70/255f}, 0.5f);
        debugTool.display(false);
    }
    
    private static void renderDebugTool(){
        if(!debugTool.isDisplayed())
            return;
        debugTool.render();
        // Memory
        int memFree = (int) (Runtime.getRuntime().freeMemory()/1000000), memUsed = (int)(Runtime.getRuntime().totalMemory()/1000000), memAlloc = (int) (Runtime.getRuntime().totalMemory()/1000000 - memFree);
        debugTool.drawText((int)(10*ref), (int)(10*ref), "Memory used :", fonts.get("normalS"), "topLeft");
        debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)(10*ref), memUsed+"MB", fonts.get("normalS"), "topRight");
        debugTool.drawText((int)(10*ref), (int)(30*ref), "Allocated memory :", fonts.get("normalS"), "topLeft");
        debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)(30*ref), memAlloc+"MB", fonts.get("normalS"), "topRight");
        debugTool.drawText((int)(10*ref), (int)(50*ref), "Memory free :", fonts.get("normalS"), "topLeft");
        debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)(50*ref), memFree+"MB", fonts.get("normalS"), "topRight");
        // FPS
        debugTool.drawText((int)(10*ref), (int)(70*ref), "FPS :", fonts.get("normalS"), "topLeft");
        debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)(70*ref), averageFPS+"", fonts.get("normalS"), "topRight");
        // Cheats activated
        debugTool.drawText((int)(10*ref), (int)(90*ref), "Cheats :", fonts.get("normalS"), "topLeft");
        debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)(90*ref), (cheatsActivated ? "on" : "off"), fonts.get("normalS"), "topRight");
        // PP
        debugTool.drawText((int)(10*ref), (int)(110*ref), "PP :", fonts.get("normalS"), "topLeft");
        debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)(110*ref), ""+StatsManager.Instance.progression, fonts.get("normalS"), "topRight");
        // Say Hi!
        debugTool.drawText((int)(10*ref), (int)(130*ref), "Say hi :", fonts.get("normalS"), "topLeft");
        debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)(130*ref), "F1+H", fonts.get("normalS"), "topRight");
        if(game != null){
            int s = 170;
            debugTool.drawText(debugTool.getW()/2, (int)(s*ref), "Game", fonts.get("normalS"), "center");
            debugTool.drawText((int)(10*ref), (int)((s+20)*ref), "Wave :", fonts.get("normalS"), "topLeft");
            debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+20)*ref), game.waveNumber+"", fonts.get("normalS"), "topRight");
            debugTool.drawText((int)(10*ref), (int)((s+40)*ref), "Difficulty :", fonts.get("normalS"), "topLeft");
            debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+40)*ref), game.difficulty.name, fonts.get("normalS"), "topRight");
            debugTool.drawText((int)(10*ref), (int)((s+60)*ref), "Nb towers :", fonts.get("normalS"), "topLeft");
            debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+60)*ref), game.towers.size()+"", fonts.get("normalS"), "topRight");
            /*debugTool.drawText((int)(10*ref), (int)((s+60)*ref), "Raztech lvl :", fonts.get("normalS"), "topLeft");
            debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+60)*ref), (game.raztech != null ? game.raztech.lvl+"" : ""), fonts.get("normalS"), "topRight");*/
            debugTool.drawText((int)(10*ref), (int)((s+80)*ref), "Nb enemies :", fonts.get("normalS"), "topLeft");
            debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+80)*ref), ""+game.enemies.size(), fonts.get("normalS"), "topRight");
            debugTool.drawText((int)(10*ref), (int)((s+100)*ref), "Time passed in game :", fonts.get("normalS"), "topLeft");
            debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+100)*ref), (int)(game.timeInGamePassed/1000)+"", fonts.get("normalS"), "topRight");
            s += 140;
            // Selected enemy
            if(game.enemySelected != null){
                debugTool.drawText(debugTool.getW()/2, (int)(s*ref), "Selected enemy", fonts.get("normalS"), "center");
                debugTool.drawText((int)(10*ref), (int)((s+20)*ref), "Life :", fonts.get("normalS"), "topLeft");
                debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+20)*ref), game.enemySelected.getRoundedLife()+"", fonts.get("normalS"), "topRight");
                debugTool.drawText((int)(10*ref), (int)((s+40)*ref), "Total move speed :", fonts.get("normalS"), "topLeft");
                debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+40)*ref), formatter.format(game.enemySelected.getMoveSpeed()), fonts.get("normalS"), "topRight");
                debugTool.drawText((int)(10*ref), (int)((s+60)*ref), "Bonus Life/MS :", fonts.get("normalS"), "topLeft");
                debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+60)*ref), formatter.format(game.enemySelected.bonusLife)+"/"+formatter.format(game.enemySelected.bonusMS), fonts.get("normalS"), "topRight");
                debugTool.drawText((int)(10*ref), (int)((s+80)*ref), "Aim :", fonts.get("normalS"), "topLeft");
                debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+80)*ref), game.enemySelected.enemyAimed == null ? "None" : game.enemySelected.enemyAimed.getName().getText(), fonts.get("normalS"), "topRight");
                s += 120;
            }
            // Selected tower
            if(game.towerSelected != null){
                debugTool.drawText(debugTool.getW()/2, (int)(s*ref), "Selected tower", fonts.get("normalS"), "center");
                debugTool.drawText((int)(10*ref), (int)((s+20)*ref), "Life :", fonts.get("normalS"), "topLeft");
                debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+20)*ref), game.towerSelected.life+"", fonts.get("normalS"), "topRight");
                debugTool.drawText((int)(10*ref), (int)((s+40)*ref), "Range/Power/AS :", fonts.get("normalS"), "topLeft");
                debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+40)*ref), formatter.format(game.towerSelected.range)+" / "+formatter.format(game.towerSelected.power)+" / "+formatter.format(game.towerSelected.shootRate), fonts.get("normalS"), "topRight");
                debugTool.drawText((int)(10*ref), (int)((s+60)*ref), "Bonus R/P/AS :", fonts.get("normalS"), "topLeft");
                debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+60)*ref), formatter.format(game.towerSelected.bonusRange)+" / "+formatter.format(game.towerSelected.bonusPower)+" / "+formatter.format(game.towerSelected.bonusShootRate), fonts.get("normalS"), "topRight");
                debugTool.drawText((int)(10*ref), (int)((s+80)*ref), "Follows :", fonts.get("normalS"), "topLeft");
                debugTool.drawText(debugTool.getW()-(int)(10*ref), (int)((s+80)*ref), game.towerSelected.getFollow()+"", fonts.get("normalS"), "topRight");
            }
        }
        // CONSOLE
        debugTool.drawText(debugTool.getW()/2, (int)(debugTool.getH()-20*ref-(nbConsoleLinesMax+1)*20*ref), "Console :", fonts.get("normalS"), "center");
        if(nbConsoleLines > nbConsoleLinesMax)
            debugTool.drawText((int)(30*ref), (int)(debugTool.getH()-20*ref-nbConsoleLinesMax*20*ref), "+"+(nbConsoleLines-nbConsoleLinesMax), fonts.get("normalS"), "topLeft");
        String[] sep;
        for(int i = 0 ; i < consoleLines.size() ; i++){
            sep = consoleLines.get(consoleLines.size()-1-i).split(" > ");
            if(sep.length > 1)
                debugTool.drawText((int)(30*ref), (int)(debugTool.getH()-20*ref-i*20*ref), sep[0], fonts.get("normalS"), "topRight");
            debugTool.drawText((int)(30*ref), (int)(debugTool.getH()-20*ref-i*20*ref), " > "+sep[sep.length-1], fonts.get("normalS"), "topLeft");
        }
            
    }
    
    public static void debug(boolean v){
        debug(v+"");
    }
    
    public static void debug(double v){
        debug(v+"");
    }
    
    public static void debug(int v){
        debug(v+"");
    }
    
    public static void debug(float v){
        debug(v+"");
    }
    
    public static void debug(String line){
        String l;
        if(nbConsoleLines > 0){
            int i = 1;
            String[] sep = consoleLines.get(consoleLines.size()-i).split(" > ");
            while(sep.length == 1)
                sep = consoleLines.get(consoleLines.size()-(++i)).split(" > ");
            if(line.indexOf(sep[1]) != -1){
                for(int j = 0 ; j < i ; j++){
                    consoleLines.remove(consoleLines.size()-1);
                    nbConsoleLines--;
                }
                int n = Integer.parseInt(sep[0])+1;
                if(n > 999) n = 999;
                l = n+" > "+line;
                addToConsole(l);
                return;
            }
        }
        l = "1 > "+line;
        addToConsole(l);
        
        System.out.println(line);
    }
    
    private static void addToConsole(String l){
        int index;
        while(fonts.get("normalS").getWidth(l) >= debugTool.getW()-(int)(20*ref)){
            index = l.length()-1;
            while(fonts.get("normalS").getWidth(l.substring(0, index)) >= debugTool.getW()-(int)(20*ref))
                index--;
            addLine(l.substring(0, index));
            l = "  "+l.substring(index);
        }
        addLine(l);
    }
    
    private static void addLine(String line){
        consoleLines.add(line);
        if(consoleLines.size() > nbConsoleLinesMax)
            consoleLines.remove(0);
        nbConsoleLines++;
    }
    
    public static void renderMouse(){
        if(cursorTexture != null)
            drawFilledRectangle(Mouse.getX()-cursorPos[0], windHeight-Mouse.getY()-cursorPos[1], (int)(32*ref), (int)(32*ref), null, 1, cursorTexture);
    }
    
    public static void checkInput() {
        State s = state;
        if(Keyboard.next()){
            // LISTENING KEYBOARD FOR COMMAND PROMPT
            if(listeningKeyboard && Keyboard.getEventKeyState()){
                if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE){
                    listeningKeyboard = false;
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_BACK){
                    commandPrompt = commandPrompt.substring(0, commandPrompt.length()-1);
                }
                else if(Keyboard.getEventKey() == Keyboard.KEY_RETURN){
                    PopupManager.Instance.closeCurrentPopup();
                    try{
                        StatsManager.Instance.cheatAddProgressionPoints(Integer.parseInt(commandPrompt));
                    } catch(Exception e){
                        debug(commandPrompt+" is not a number.");
                    }
                    listeningKeyboard = false;
                }
                else
                    commandPrompt += Keyboard.getEventCharacter();
            }
            
            // ESCAPE MENU
            if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && (game == null || game.ended || (!game.gameOver && !game.gameWin)) && !PopupManager.Instance.onRewardSelection()){
                if(PopupManager.Instance.onPopup() && !PopupManager.Instance.onPopupTuto())
                    PopupManager.Instance.closeCurrentPopup();
                else if(!PopupManager.Instance.onPopupTuto())
                    switchStateTo(State.MENU);
            }
            
            // COMMANDES
            if(Keyboard.isKeyDown(Keyboard.KEY_F1)){  
                // Debug tool
                if(Keyboard.isKeyDown(Keyboard.KEY_D))
                    debugTool.display(!debugTool.isDisplayed());
                // Clear console
                else if(Keyboard.isKeyDown(Keyboard.KEY_C)){
                    consoleLines.clear();
                    nbConsoleLines = 0;
                }
                // Say hi
                else if(Keyboard.isKeyDown(Keyboard.KEY_H)){
                    debug("Hi !");
                }
            }
            
            if(cheatsActivated)
                checkCheatsInput();
            
            //System.out.println(Keyboard.getEventKey());
        }
        
        if(s != state)
            setCursor(Cursor.DEFAULT);
    }
    
    private static void checkCheatsInput(){
        if(Keyboard.isKeyDown(Keyboard.KEY_F1)){
            // Prompt command
            if(Keyboard.isKeyDown(Keyboard.KEY_P)){
                PopupManager.Instance.popup("How many PP to add ?", Text.CANCEL);
                PopupManager.Instance.setCallback(__ -> {
                    listeningKeyboard = false;
                });
                listeningKeyboard = true;
                commandPrompt = "";
            }
            // Money
            else if(Keyboard.isKeyDown(Keyboard.KEY_M)){
                if(game != null)
                    game.money += 10000;
            }
            // Level up
            else if(Keyboard.isKeyDown(Keyboard.KEY_L)){
                if(game != null && game.gameSpeed > 0 && game.raztech != null)
                    game.raztech.levelUp();
            }
            // Wave +1
            else if(Keyboard.isKeyDown(Keyboard.KEY_W)){
                if(game != null && !game.inWave)
                    game.waveNumber++;
            }
            // Kill all enemies
            else if(Keyboard.isKeyDown(Keyboard.KEY_K)){
                if(game != null && game.inWave){
                    for(Shootable e : game.enemies){
                        e.life = 0;
                        e.die();
                    }
                }
            }
            
            else if(Keyboard.isKeyDown(Keyboard.KEY_T)){
                // Complete Tuto [C+T]
                if(Keyboard.isKeyDown(Keyboard.KEY_C)){
                    switchStateTo(State.MENU);
                    TutoManager.Instance.completeAllTuto();
                }
                // Reset tuto (therefore resets game in progress)
                else{
                    switchStateTo(State.MENU);
                    PopupManager.Instance.closeCurrentPopup();
                    TutoManager.Instance.clearStepsDone();
                    game = null;
                }
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
    }
    
    public static void newRandomMap(Difficulty difficulty){
        if(TutoManager.Instance.hasDone(TutoManager.TutoStep.GM_NDD))
            game = new Game(difficulty);
        else{
            String pathString = "0/8 1/8 1/7 2/7 2/6 2/5 2/4 2/3 3/3 3/2 4/2 5/2 6/2 6/3 6/4 6/5 7/5 8/5 9/5 9/6 10/6 11/6 11/7 10/7 10/8 9/8 8/8 7/8 7/9 6/9 5/9 5/10 5/11 6/11 6/12 7/12 8/12 8/11 9/11 9/12 9/13 10/13 11/13 11/12 11/11 12/11 12/10 13/10 13/9 13/8 13/7 13/6 13/5 14/5 15/5 16/5 16/6 17/6 17/7 17/8 17/9 18/9 19/9 20/9 20/10 20/11 20/12 20/13 20/14 21/14 21/15 22/15 23/15 24/15 25/15 26/15 26/14 26/13 26/12 25/12 25/11 24/11 24/10 24/9 23/9 23/8 23/7 23/6 23/5 24/5 25/5 26/5 27/5 27/6 28/6 28/7 29/7 29/8 29/9 30/9 31/9 ";
            String[] indexes = pathString.split(" ");
            ArrayList<Tile> path = new ArrayList<>();
            for(String index : indexes){
                int x = Integer.parseInt(index.split("/")[0]);
                int y = Integer.parseInt(index.split("/")[1]);
                Tile t = new Tile(RvB.textures.get("roadStraight"), "road");
                t.setX(x*unite);
                t.setY(y*unite);
                path.add(t);
            }
            game = new Game(path, difficulty);
        }
            
        if(game.path.size() == 0){
            game = null;
            return;
        }
        switchStateTo(State.GAME);
    }
    
    public static void newCreatedMap(Difficulty difficulty){
        game = new Game("assets/temp/level_created.txt", difficulty);
        Text error = game.calculatePath(difficulty);
        if(error != null){
            game.saveBestScore = false;
            game.addPP = false;
            if(error != Text.PATH_TOO_LONG){
                game = null;
                return;
            }
            PopupManager.Instance.popup(error.getLines());
        }
        switchStateTo(State.GAME);
    }
    
    public static void loadMap(String path, Difficulty difficulty){
        game = new Game(path, difficulty);
        Text error = game.calculatePath(difficulty);
        if(error != null){
            game.saveBestScore = false;
            game.addPP = false;
            if(error != Text.PATH_TOO_LONG){
                game = null;
                return;
            }
            PopupManager.Instance.popup(error.getLines());
        }
        switchStateTo(State.GAME);
        debug("Map loaded : "+path);
    }
    
    public static void setMap(ArrayList<Tile> path, Difficulty difficulty){
        game = new Game(path, difficulty);
        Text error = game.calculatePath(difficulty);
        if(error != null){
            game.saveBestScore = false;
            game.addPP = false;
            if(error != Text.PATH_TOO_LONG){
                game = null;
                debug("Error : Map saved not set");
            }
        }
    }
    
    public static boolean createLevelCreatedFile(){
        try{
            File file = new File("assets/temp/level_created.txt");
            file.createNewFile();
            return true;
        }
        catch(Exception e){
            System.out.println(e);
            return false;
        }
    }
    
    public static String selectMap(){
        String filePath = "";
        boolean success = false;
        try {
            Display.setFullscreen(false);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            success = true;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MenuWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(MenuWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MenuWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MenuWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LWJGLException ex) {
            Logger.getLogger(MenuWindow.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            if(!success) PopupManager.Instance.popup(Text.ERROR.getText());
        }
        File levelsFolder = new File(System.getProperty("user.home")+File.separator+"RvB", "levels");
        if(!levelsFolder.exists()) {
            levelsFolder.mkdir();
        }
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(levelsFolder);
        int returnVal = chooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            filePath = chooser.getSelectedFile().getPath();
        }
        success = false;
        try {
            Display.setFullscreen(true);
            success = true;
        } catch (LWJGLException ex) {
            Logger.getLogger(MenuWindow.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            if(!success) PopupManager.Instance.popup(Text.ERROR.getText());
        }

        return filePath;
    }
    
    public static void switchStateTo(State s){
        state = s;
        stateChanged = true;
        setCursor(Cursor.DEFAULT);
        if(s == State.MENU){
            if(game != null)
                game.pause();
        }  
        else if(s == State.GAME){
            game.unpause();
            game.enableAllButtons();
            TutoManager.Instance.showTutoIfNotDone(TutoManager.TutoStep.WLCM_RND);
        }
        else if(s == State.CREATION){
            creation.enableAllButtons();
            TutoManager.Instance.showTutoIfNotDone(TutoManager.TutoStep.WLCM_CRTN);
        }
    }
    
    public static void drawString(int x, int y, String string, UnicodeFont font){
        String[] lines = string.split("--");
        if(lines.length == 1 || lines.length%2 == 0)
            font.drawString(x - font.getWidth(string)/2, y - font.getHeight(string)/2, string);
        else{
            int textWidth = 0, cursorX = 0, fontSize = font.getFont().getSize(), imageSize = (int)(fontSize*1.5);
            for(int i = 0 ; i < lines.length ; i++){
                if(i%2 != 0)
                    textWidth += imageSize;
                else
                    textWidth += font.getWidth(lines[i]);
            }
            for(int i = 0 ; i < lines.length ; i++){
                String s = lines[i];
                if(i%2 != 0){
                    drawFilledRectangle(x+cursorX - textWidth/2, y - imageSize/2, imageSize, imageSize, null, 1, textures.get(lines[i]));
                    cursorX += imageSize;
                }
                else{
                    font.drawString(x+cursorX - textWidth/2, y - fontSize/2, s);
                    cursorX += font.getWidth(s);
                }
            }
        }
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
            glVertex2d(0, 0);
            glTexCoord2f(1, 0);
            glVertex2d(width, 0);
            glTexCoord2f(1, 1);
            glVertex2d(width, height);
            glTexCoord2f(0, 1);
            glVertex2d(0, height);
        glEnd();
        
        glPopMatrix(); // Reset the current matrix to the one that was saved.
    }
    
    public static void drawFilledRectangle(float x, float y, int width, int height, Texture texture, double angle, float a, int anchorX, int anchorY){
        glPushMatrix(); //Save the current matrix.
        
        glTranslated(x+anchorX, y+anchorY, 0);
        if(angle != 0)
            glRotated(angle, 0, 0, 1);

        drawFilledRectangle(-width/2-anchorX, -height/2-anchorY, width, height, null, a, texture);
        
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
        if(texture != null) {
            texture.bind();
            glEnable(GL_TEXTURE_2D);
            glColor4f(1, 1, 1, a);
        }
        else{
            glDisable(GL_TEXTURE_2D);
            glColor4f(rgb[0], rgb[1], rgb[2], a);
        }
        
        glBegin(GL_QUADS);
            glTexCoord2f(0, 0); glVertex2d(x, y);
            glTexCoord2f(1, 0); glVertex2d(x + width, y);
            glTexCoord2f(1, 1); glVertex2d(x + width, y + height);
            glTexCoord2f(0, 1); glVertex2d(x, y + height);
        glEnd();
        
        if(texture != null)
            glDisable(GL_TEXTURE_2D);
    }
    
    public static void drawCircle(double x, double y, float radius, float[] rgb){
        drawCircle(x, y, radius, rgb, 1);
    }
    
    public static void drawCircle(double x, double y, float radius, float[] rgb, int thickness){
        float DEG2RAD = (float) (3.15149/180), degInRad;
        glBegin(GL_LINE_LOOP);
        glColor3f(rgb[0], rgb[1], rgb[2]);
        for(float t = 0 ; t < thickness ; t+=0.5f){
            for(int i = 0 ; i < 360; i++){
                degInRad = i*DEG2RAD;
                glVertex2d(x+cos(degInRad)*(radius+t), y+sin(degInRad)*(radius+t));
            }
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
    
    public static void saveAndExit(){
        releaseTextures();
        try {
            boolean inGame = game != null && !game.ended && !game.gameOver && !game.gameWin;
            if(inGame)
                RVBDB.Instance.saveGame(game.waveNumber, game.money, game.life, game.getPathString(), game.getHolesString(), game.difficulty.name, game.getArrayTowers(), game.getArrayBuffs(), game.buffsUsed);
            RVBDB.Instance.updateStats(StatsManager.Instance.getJSON());
            RVBDB.Instance.updateTutoSteps(TutoManager.Instance.getStepsJSON());
            RVBDB.Instance.updateInGame(inGame);
            RVBDB.Instance.updateLanguage(TextManager.Instance.getLanguage());
        } catch (SQLException ex) {
            Logger.getLogger(RvB.class.getName()).log(Level.SEVERE, null, ex);
        }
        RVBDB.Instance.exitDB();
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
            textures.put("grassHole", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/grass_hole.png"))));
            textures.put("gameHit", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/game_hit.png"))));
            /*textures.put("rock1", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/rock1.png"))));
            textures.put("rock2", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/rock2.png"))));*/
            textures.put("frame", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/tile_frame.png"))));
            // Icons
            textures.put("FR", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/drapeau_francais.png"))));
            textures.put("ENG", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/drapeau_RU.png"))));
            textures.put("arrow", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/arrow.png"))));
            textures.put("arrowBack", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/arrow_back.png"))));
            textures.put("download", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/download.png"))));
            textures.put("lock", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/lock.png"))));
            textures.put("plus", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/plus.png"))));
            textures.put("optionIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/option_icon.png"))));
            textures.put("exitIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/exit_icon.png"))));
            textures.put("rangeIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/range_icon.png"))));
            textures.put("powerIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/power_icon.png"))));
            textures.put("attackSpeedIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/attack_speed_icon.png"))));   
            textures.put("bulletSpeedIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/bullet_speed_icon.png"))));   
            textures.put("explodeRadiusIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/explode_radius_icon.png")))); 
            textures.put("coins", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/coins.png"))));
            textures.put("coinsAdd", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/coins_add.png"))));
            textures.put("coinsCantBuy", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/coins_cantBuy.png"))));
            textures.put("heart", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/heart.png"))));
            textures.put("enemyRate", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/enemy_rate.png"))));
            textures.put("enemyLife", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/enemy_life.png"))));
            textures.put("pathIcon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/path_icon.png"))));
            textures.put("cross", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/cross.png"))));
            textures.put("arrowPoint", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/arrow_point.png"))));
            textures.put("questionMark", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/images/question_mark.png"))));
            // Towers
            textures.put("raztech", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/raztech.png"))));
            textures.put("placeRaztech", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/place_raztech.png"))));
            
            textures.put("basicTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/basic_tower.png"))));
            textures.put("basicTowerBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/basic_tower_base.png"))));
            textures.put("basicTowerTurret", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/basic_tower_turret.png"))));
            textures.put("basicTowerBaseBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/basic_tower_base_bright.png"))));
            textures.put("basicTowerTurretBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/basic_tower_turret_bright.png"))));
            
            textures.put("circleTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/circle_tower.png"))));
            textures.put("circleTowerBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/circle_tower_base.png"))));
            textures.put("circleTowerTurret", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/circle_tower_turret.png"))));
            textures.put("circleTowerBaseBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/circle_tower_base_bright.png"))));
            textures.put("circleTowerTurretBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/circle_tower_turret_bright.png"))));
            
            textures.put("bigTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/big_tower.png"))));
            textures.put("bigTowerBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/big_tower_base.png"))));
            textures.put("bigTowerTurret", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/big_tower_turret.png"))));
            textures.put("bigTowerBaseBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/big_tower_base_bright.png"))));
            textures.put("bigTowerTurretBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/big_tower_turret_bright.png"))));
            
            textures.put("flameTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/flame_tower.png"))));
            textures.put("flameTowerBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/flame_tower_base.png"))));
            textures.put("flameTowerTurret", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/flame_tower_turret.png"))));
            textures.put("flameTowerBaseBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/flame_tower_base_bright.png"))));
            textures.put("flameTowerTurretBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/flame_tower_turret_bright.png"))));
            
            textures.put("powerTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/power_tower.png"))));
            textures.put("powerTowerBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/power_tower_base.png"))));
            textures.put("powerTowerElec", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/power_tower_elec.png"))));
            
            textures.put("rangeTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/range_tower.png"))));
            textures.put("rangeTowerBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/range_tower_base.png"))));
            textures.put("rangeTowerBalls", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/range_tower_balls.png"))));
            
            textures.put("shootrateTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/shootrate_tower.png"))));
            textures.put("shootrateTowerBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/shootrate_tower_base.png"))));
            textures.put("shootrateTowerBullet", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/shootrate_tower_bullet.png"))));
            // Buffs
            textures.put("buff_Slow", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/buffs/buff_slow.png"))));
            textures.put("buff_Upgrade", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/buffs/buff_upgrade.png"))));
            textures.put("buff_OS", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/buffs/buff_os.png"))));
            textures.put("buff_XP", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/buffs/buff_xp.png"))));
            textures.put("buff_UpPowerTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/buffs/buff_upPowerTower.png"))));
            textures.put("buff_UpRangeTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/buffs/buff_upRangeTower.png"))));
            textures.put("buff_UpShootRateTower", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/buffs/buff_upShootRateTower.png"))));
            textures.put("powerUp", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/buffs/power_up.png"))));
            textures.put("rangeUp", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/buffs/range_up.png"))));
            textures.put("shootRateUp", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/buffs/attackSpeed_up.png"))));
            // Bullets
            textures.put("bulletBlue", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/bullet_blue.png"))));
            textures.put("bullet", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/bullet.png"))));
            textures.put("gun_bullet", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/gun_bullet.png"))));
            textures.put("shell", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/shell.png"))));
            textures.put("flame", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/towers/flame.png"))));
            textures.put("roundBullet", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/enemy_bullet.png"))));
            // Enemies
            textures.put("basicEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/basic_enemy.png"))));
            textures.put("basicEnemyBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/basic_enemy_bright.png"))));
            
            textures.put("fastEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/fast_enemy.png"))));
            textures.put("fastEnemyBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/fast_enemy_bright.png"))));
            
            textures.put("strongEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/strong_enemy.png"))));
            textures.put("strongEnemyBase", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/strong_enemy_base.png"))));
            textures.put("strongEnemyCannon", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/strong_enemy_cannon.png"))));
            textures.put("strongEnemyBaseBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/strong_enemy_base_bright.png"))));
            textures.put("strongEnemyCannonBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/strong_enemy_cannon_bright.png"))));
            
            textures.put("trickyEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/tricky_enemy.png"))));
            textures.put("trickyEnemyBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/tricky_enemy_bright.png"))));
            
            textures.put("sniperEnemy", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/sniper_enemy.png"))));
            textures.put("sniperEnemyBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/sniper_enemy_bright.png"))));
            textures.put("sniperEnemyVehicle", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/sniper_enemy_vehicle.png"))));
            textures.put("sniperEnemyVehicleBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/sniper_enemy_vehicle_bright.png"))));
            textures.put("sniperEnemySniping", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/sniper_enemy_sniping.png"))));
            textures.put("sniperEnemySnipingBright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/sniper_enemy_sniping_bright.png"))));
            
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
            textures.put("bazooEvo7", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo7.png"))));
            textures.put("bazooEvo7Bright", TextureLoader.getTexture("PNG", new FileInputStream(new File("assets/enemies/bazoo_evo7_bright.png"))));
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
        float[] life6 = {180f/255f, 255f/255f, 25f/255f};
        float[] life7 = {120f/255f, 255f/255f, 25f/255f};
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
        colors.put("life7", life7);
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
        
        color = colors.get("lightGreen");
        Font awtFont = new Font(police, Font.BOLD, (int)(16*ref));
        UnicodeFont normalSB = new UnicodeFont(awtFont);
        normalSB.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalSB.addAsciiGlyphs();
        
        color = colors.get("lightGreen");
        awtFont = new Font(police, Font.PLAIN, (int)(16*ref));
        UnicodeFont normalS = new UnicodeFont(awtFont);
        normalS.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalS.addAsciiGlyphs();
        
        color = colors.get("lightGreen");
        awtFont = new Font(police, Font.PLAIN, (int)(20*ref));
        UnicodeFont normal = new UnicodeFont(awtFont);
        normal.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normal.addAsciiGlyphs();
        
        color = colors.get("black");
        awtFont = new Font(police, Font.PLAIN, (int)(20*ref));
        UnicodeFont normalBlack = new UnicodeFont(awtFont);
        normalBlack.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalBlack.addAsciiGlyphs();
        
        color = colors.get("lightGreen");
        awtFont = new Font(police, Font.PLAIN, (int)(25*ref));
        UnicodeFont normalL = new UnicodeFont(awtFont);
        normalL.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalL.addAsciiGlyphs();
        
        color = colors.get("lightGreen");
        awtFont = new Font(police, Font.PLAIN, (int)(34*ref));
        UnicodeFont normalXL = new UnicodeFont(awtFont);
        normalXL.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalXL.addAsciiGlyphs();
        
        color = colors.get("lightGreen");
        awtFont = new Font(police, Font.BOLD, (int)(20*ref));
        UnicodeFont normalB = new UnicodeFont(awtFont);
        normalB.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalB.addAsciiGlyphs();
        
        color = colors.get("lightGreen");
        awtFont = new Font(police, Font.BOLD, (int)(24*ref));
        UnicodeFont normalLB = new UnicodeFont(awtFont);
        normalLB.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalLB.addAsciiGlyphs();
        
        color = colors.get("lightGreen");
        awtFont = new Font(police, Font.BOLD, (int)(34*ref));
        UnicodeFont normalXLB = new UnicodeFont(awtFont);
        normalXLB.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        normalXLB.addAsciiGlyphs();
        
        color = colors.get("green_dark");
        awtFont = new Font(police, Font.BOLD, (int)(20*ref));
        UnicodeFont titleS = new UnicodeFont(awtFont);
        titleS.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        titleS.addAsciiGlyphs();
        
        color = colors.get("green_dark");
        awtFont = new Font(police, Font.BOLD, (int)(32*ref));
        UnicodeFont title = new UnicodeFont(awtFont);
        title.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        title.addAsciiGlyphs();
        
        color = colors.get("green_dark");
        awtFont = new Font(police, Font.BOLD, (int)(48*ref));
        UnicodeFont titleL = new UnicodeFont(awtFont);
        titleL.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        titleL.addAsciiGlyphs();
        
        color = colors.get("green_dark");
        awtFont = new Font(police, Font.BOLD, (int)(72*ref));
        UnicodeFont titleXL = new UnicodeFont(awtFont);
        titleXL.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        titleXL.addAsciiGlyphs();
        
        color = colors.get("money");
        awtFont = new Font(police, Font.BOLD, (int)(24*ref));
        UnicodeFont money = new UnicodeFont(awtFont);
        money.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        money.addAsciiGlyphs();
        
        color = colors.get("life");
        awtFont = new Font(police, Font.BOLD, (int)(24*ref));
        UnicodeFont life = new UnicodeFont(awtFont);
        life.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        life.addAsciiGlyphs();
        
        color = colors.get("money");
        awtFont = new Font(police, Font.BOLD, (int)(18*ref));
        UnicodeFont canBuy = new UnicodeFont(awtFont);
        canBuy.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        canBuy.addAsciiGlyphs();
        
        color = colors.get("lightRed");
        awtFont = new Font(police, Font.PLAIN, (int)(18*ref));
        UnicodeFont cantBuy = new UnicodeFont(awtFont);
        cantBuy.getEffects().add(new ColorEffect(new Color(color[0], color[1], color[2])));
        cantBuy.addAsciiGlyphs();
        
        color = colors.get("bonus");
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
            titleS.loadGlyphs();
            fonts.put("titleS", titleS);
            title.loadGlyphs();
            fonts.put("title", title);
            titleL.loadGlyphs();
            fonts.put("titleL", titleL);
            titleXL.loadGlyphs();
            fonts.put("titleXL", titleXL);
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
