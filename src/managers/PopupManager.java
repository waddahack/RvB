package managers;

import java.util.ArrayList;
import java.util.Random;
import org.newdawn.slick.UnicodeFont;
import managers.TextManager.Text;
import rvb.RvB;
import static rvb.RvB.creation;
import static rvb.RvB.game;
import static rvb.RvB.menu;
import static rvb.RvB.stateChanged;
import ui.Button;
import ui.Overlay;
import static rvb.RvB.ref;
import Buffs.Buff;
import java.util.Collections;
import java.util.function.Consumer;
import rvb.RvB.State;


public final class PopupManager {
    public static PopupManager Instance;
    
    private static final int width = (int) (800*ref), height = (int) (450*ref);
    private Overlay currentOverlay;
    private Overlay gameOver, gameWin, enemiesUpgraded, popup, chooseDifficulty, rewardSelection, help, chooseMap;
    private final ArrayList<String> lines;
    private final ArrayList<String> buttonsText;
    private final ArrayList<UnicodeFont> fonts;
    private static Random random;
    private String gameType;
    private int top;
    private Buff buff1, buff2, buff3;
    private Consumer<Object> callback = null;

    private PopupManager(){
        random = new Random();
        currentOverlay = null;
        lines = new ArrayList<>();
        buttonsText = new ArrayList<>();
        fonts = new ArrayList<>();
        initOverlays();
    }

    public static void initialize(){
        if(Instance == null)
            Instance = new PopupManager();
    }
    
    public void initOverlays(){
        Button b;
        Overlay o;
        int butWith = (int) (180*ref);
        int butHeight = (int)(ref*36);
        int tempX, tempY;
        // POPUP AFFICHAGE
        popup = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        popup.display(false);
        popup.setBG(RvB.textures.get("board"), 0.8f);
        popup.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(popup.getW()/2, 3*popup.getH()/4, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            
            if(callback != null)
                callback.accept(null);
        });
        popup.addButton(b);
        //
        // CHOOSE MAP
        chooseMap = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        chooseMap.display(false);
        chooseMap.setBG(RvB.textures.get("board"), 0.8f);
        chooseMap.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(chooseMap.getW()/2, 6*chooseMap.getH()/10, (int)(butWith*1.2), (int)(butHeight*1.2), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            chooseDifficulty("random");
            // Then it does newRandomMap()
        });
        chooseMap.addButton(b);
        b = new Button(chooseMap.getW()/2, 8*chooseMap.getH()/10, (int)(butWith*1.2), (int)(butHeight*1.2), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            String filePath = RvB.selectMap();
            if(!filePath.isEmpty())
                chooseDifficulty(filePath);
                // Then it does newLoadedMap()
        });
        chooseMap.addButton(b);
        //
        // CHOOSE DIFFICULTY
        chooseDifficulty = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        chooseDifficulty.display(false);
        chooseDifficulty.setBG(RvB.textures.get("board"), 0.8f);
        chooseDifficulty.setBorder(RvB.colors.get("green_dark"), 4, 1);
        tempX = chooseDifficulty.getX()+chooseDifficulty.getW()/6;
        tempY = chooseDifficulty.getY()+chooseDifficulty.getH()/2;
        b = new Button(chooseDifficulty.getW()/2, 4*chooseDifficulty.getH()/8, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            if(gameType.equals("random"))
                RvB.newRandomMap(RvB.Difficulty.EASY);
            else if(gameType.equals("created"))
                RvB.newCreatedMap(RvB.Difficulty.EASY);
            else
                RvB.newLoadedMap(gameType, RvB.Difficulty.EASY);
            RvB.setCursor(RvB.Cursor.DEFAULT);
        });
        b.setOnHoverFunction(__ -> {
            RvB.drawFilledRectangle(tempX, tempY-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("heart"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("125")/2, tempY, "125", RvB.fonts.get("normal"));
            RvB.drawFilledRectangle(tempX, tempY+(int)(32*ref)-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("coins"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("225")/2, tempY+(int)(32*ref), "225", RvB.fonts.get("normal"));
            RvB.drawFilledRectangle(tempX, tempY+2*(int)(32*ref)-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("coinsAdd"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("220")/2, tempY+2*(int)(32*ref), "220", RvB.fonts.get("normal"));
            RvB.drawFilledRectangle(tempX, tempY+3*(int)(32*ref)-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("enemyRate"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("90%")/2, tempY+3*(int)(32*ref), "90%", RvB.fonts.get("normal"));
            if(!gameType.equals("random"))
                return;
            RvB.drawFilledRectangle(tempX+(int)(2*ref), tempY+4*(int)(32*ref)-(int)(14*ref), (int)(28*ref), (int)(28*ref), null, 1, RvB.textures.get("roadStraight"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth(Text.LONG.getText())/2, tempY+4*(int)(32*ref), Text.LONG.getText(), RvB.fonts.get("normal"));
        });
        chooseDifficulty.addButton(b);
        b = new Button(chooseDifficulty.getW()/2, 5*chooseDifficulty.getH()/8, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            if(gameType.equals("random"))
                RvB.newRandomMap(RvB.Difficulty.MEDIUM);
            else
                RvB.newCreatedMap(RvB.Difficulty.MEDIUM);
            RvB.setCursor(RvB.Cursor.DEFAULT);
        });
        b.setOnHoverFunction(__ -> {
            RvB.drawFilledRectangle(tempX, tempY-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("heart"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("100")/2, tempY, "100", RvB.fonts.get("normal"));
            RvB.drawFilledRectangle(tempX, tempY+(int)(32*ref)-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("coins"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("200")/2, tempY+(int)(32*ref), "200", RvB.fonts.get("normal"));
            RvB.drawFilledRectangle(tempX, tempY+2*(int)(32*ref)-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("coinsAdd"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("200")/2, tempY+2*(int)(32*ref), "200", RvB.fonts.get("normal"));
            RvB.drawFilledRectangle(tempX, tempY+3*(int)(32*ref)-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("enemyRate"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("100%")/2, tempY+3*(int)(32*ref), "100%", RvB.fonts.get("normal"));
            if(!gameType.equals("random"))
                return;
            RvB.drawFilledRectangle(tempX+(int)(2*ref), tempY+4*(int)(32*ref)-(int)(14*ref), (int)(28*ref), (int)(28*ref), null, 1, RvB.textures.get("roadStraight"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth(Text.NORMAL.getText())/2, tempY+4*(int)(32*ref), Text.NORMAL.getText(), RvB.fonts.get("normal"));
        });
        chooseDifficulty.addButton(b);
        b = new Button(chooseDifficulty.getW()/2, 6*chooseDifficulty.getH()/8, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            if(gameType.equals("random"))
                RvB.newRandomMap(RvB.Difficulty.HARD);
            else
                RvB.newCreatedMap(RvB.Difficulty.HARD);
            RvB.setCursor(RvB.Cursor.DEFAULT);
        });
        b.setOnHoverFunction(__ -> {
            RvB.drawFilledRectangle(tempX, tempY-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("heart"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("75")/2, tempY, "75", RvB.fonts.get("normal"));
            RvB.drawFilledRectangle(tempX, tempY+(int)(32*ref)-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("coins"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("175")/2, tempY+(int)(32*ref), "175", RvB.fonts.get("normal"));
            RvB.drawFilledRectangle(tempX, tempY+2*(int)(32*ref)-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("coinsAdd"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("180")/2, tempY+2*(int)(32*ref), "180", RvB.fonts.get("normal"));
            RvB.drawFilledRectangle(tempX, tempY+3*(int)(32*ref)-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("enemyRate"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("110%")/2, tempY+3*(int)(32*ref), "110%", RvB.fonts.get("normal"));
            if(!gameType.equals("random"))
                return;
            RvB.drawFilledRectangle(tempX+(int)(2*ref), tempY+4*(int)(32*ref)-(int)(14*ref), (int)(28*ref), (int)(28*ref), null, 1, RvB.textures.get("roadStraight"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth(Text.SHORT.getText())/2, tempY+4*(int)(32*ref), Text.SHORT.getText(), RvB.fonts.get("normal"));
        });
        chooseDifficulty.addButton(b);
        b = new Button(chooseDifficulty.getW()/2, 7*chooseDifficulty.getH()/8, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            if(gameType.equals("random"))
                RvB.newRandomMap(RvB.Difficulty.HARDCORE);
            else
                RvB.newCreatedMap(RvB.Difficulty.HARDCORE);
            RvB.setCursor(RvB.Cursor.DEFAULT);
        });
        b.setOnHoverFunction(__ -> {
            RvB.drawFilledRectangle(tempX, tempY-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("heart"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("1")/2, tempY, "1", RvB.fonts.get("normal"));
            RvB.drawFilledRectangle(tempX, tempY+(int)(32*ref)-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("coins"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("175")/2, tempY+(int)(32*ref), "175", RvB.fonts.get("normal"));
            RvB.drawFilledRectangle(tempX, tempY+2*(int)(32*ref)-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("coinsAdd"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("180")/2, tempY+2*(int)(32*ref), "180", RvB.fonts.get("normal"));
            RvB.drawFilledRectangle(tempX, tempY+3*(int)(32*ref)-(int)(16*ref), (int)(32*ref), (int)(32*ref), null, 1, RvB.textures.get("enemyRate"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth("110%")/2, tempY+3*(int)(32*ref), "110%", RvB.fonts.get("normal"));
            if(!gameType.equals("random"))
                return;
            RvB.drawFilledRectangle(tempX+(int)(2*ref), tempY+4*(int)(32*ref)-(int)(14*ref), (int)(28*ref), (int)(28*ref), null, 1, RvB.textures.get("roadStraight"));
            RvB.drawString(tempX+(int)(48*ref)+RvB.fonts.get("normal").getWidth(Text.SHORT.getText())/2, tempY+4*(int)(32*ref), Text.SHORT.getText(), RvB.fonts.get("normal"));
        });
        chooseDifficulty.addButton(b);
        // GAME OVER
        gameOver = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        gameOver.display(false);
        gameOver.setBG(RvB.textures.get("board"), 0.8f);
        gameOver.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(gameOver.getW()/2, 3*gameOver.getH()/4, (int) (250*ref), (int)(50*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            stateChanged = true;
            game.ended = true;
            for(int i = 0 ; i < game.enemies.size() ; i++)
                game.enemies.get(i).die();
            game.clearArrays();
            SoundManager.Instance.closeAllClips();
            RvB.switchStateTo(RvB.State.MENU);
            closeCurrentPopup();
            RvB.setCursor(RvB.Cursor.DEFAULT);
        });
        gameOver.addButton(b);
        // GAME WIN
        gameWin = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        gameWin.display(false);
        gameWin.setBG(RvB.textures.get("board"), 0.8f);
        gameWin.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(gameWin.getW()/2, 3*gameWin.getH()/4, (int) (250*ref), (int)(50*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            stateChanged = true;
            game.ended = true;
            game.clearArrays();
            SoundManager.Instance.closeAllClips();
            RvB.switchStateTo(RvB.State.MENU);
            closeCurrentPopup();
            RvB.setCursor(RvB.Cursor.DEFAULT);
        });
        gameWin.addButton(b);
        // ENEMIES UPGRADED
        enemiesUpgraded = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        enemiesUpgraded.display(false);
        enemiesUpgraded.setBG(RvB.textures.get("board"), 0.8f);
        enemiesUpgraded.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(enemiesUpgraded.getW()/2, 3*enemiesUpgraded.getH()/4, (int) (butWith*1.2), butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            game.enableAllButtons();
            RvB.setCursor(RvB.Cursor.DEFAULT);
        });
        enemiesUpgraded.addButton(b);
        // REWARD SELECTION
        rewardSelection = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        rewardSelection.display(false);
        rewardSelection.setBG(RvB.textures.get("board"), 0.8f);
        rewardSelection.setBorder(RvB.colors.get("green_dark"), 4, 1);
        // HELP
        help = new Overlay(RvB.windWidth/2-width, RvB.windHeight/2-height, width*2, height*2);
        help.display(false);
        help.setBG(RvB.textures.get("board"), 0.8f);
        help.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(11*help.getW()/12, 19*help.getH()/20, (int) (butWith*1.2), butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            game.enableAllButtons();
            RvB.setCursor(RvB.Cursor.DEFAULT);
        });
        help.addButton(b);
    }
    
    public void update(){
        if(currentOverlay == null)
            return;
        RvB.drawFilledRectangle(0, 0, RvB.windWidth, RvB.windHeight, null, 0.4f, RvB.textures.get("board"));
        renderCurrentOverlay();
        if(currentOverlay == null)
            return;
        int top = this.top;
        for(int i = 0 ; i < lines.size() ; i++){
            if(i > 0)
                top += fonts.get(i).getHeight(lines.get(i))+8*ref;
            currentOverlay.drawText(currentOverlay.getW()/2, top, lines.get(i), fonts.get(i));
        }
        for(int i = 0 ; i < buttonsText.size() ; i++)
            currentOverlay.getButtons().get(i).drawText(0, 0, buttonsText.get(i), RvB.fonts.get("normalLB"));
    }
    
    private void renderCurrentOverlay(){
        currentOverlay.render();
        if(currentOverlay == rewardSelection){
            Button b;
            Buff buff;
            for(int i = 0 ; i < 3 ; i++){
                switch(i){
                    default:
                        buff = buff1;
                        break;
                    case 1:
                        buff = buff2;
                        break;
                    case 2:
                        buff = buff3;
                        break;
                }
                if(buff == null)
                    break;
                b = rewardSelection.getButtons().get(i);
                if(b.isHovered()){
                    int nbLines = buff.getDescription().length;
                    int totalHeight = (RvB.fonts.get("normalS").getFont().getSize()+1)*nbLines; // betweenLines * nbLines
                    int betweenLines = 0;
                    for(int j = 0 ; j < nbLines ; j++){
                        rewardSelection.drawText(b.getX()-rewardSelection.getX(), (int) (b.getY()-rewardSelection.getY()-totalHeight/2 + betweenLines), buff.getDescription()[j], RvB.fonts.get("normalS"));
                        betweenLines += RvB.fonts.get("normalS").getFont().getSize()+1;
                    }
                }
                else{
                    rewardSelection.drawText(b.getX()-rewardSelection.getX(), (int) (b.getY()-rewardSelection.getY()-b.getH()/2+20*ref), buff.name.getText(), RvB.fonts.get("normalB"));
                    rewardSelection.drawImage(b.getX()-rewardSelection.getX(), (int) (b.getY()-rewardSelection.getY()+b.getH()/2-48*ref-20*ref), (int)(96*ref), (int)(96*ref), buff.logo);
                } 
            }
        }
    }
    
    public void setCallback(Consumer<Object> callback){
        this.callback = callback;
    }
    
    public void popup(String text, String butText){
        popup(new String[]{text}, null, butText);
    }
    
    public void popup(String text){
        popup(new String[]{text}, null, "Ok");
    }
    
    public void popup(String[] infoLines){
        popup(infoLines, null, "Ok");
    }
    
    public void popup(String[] infoLines, String butText){
        popup(infoLines, null, butText);
    }
    
    public void popup(String[] infoLines, UnicodeFont[] fonts, String butText){
        initPopup(popup);
        top = height/4;
        for(int i = 0 ; i < infoLines.length ; i++)
            addText(infoLines[i], (fonts != null ? (i < fonts.length ? fonts[i] : fonts[fonts.length-1]) : RvB.fonts.get("normalXL")));
        
        buttonsText.add(butText);
    }
    
    public void chooseDifficulty(String gameType){
        initPopup(chooseDifficulty);
        top = height/4;
        this.gameType = gameType;
        
        addText(Text.SELECT_DIFF.getText(), RvB.fonts.get("normalXL"));
        addText("("+Text.CANCEL.getText()+")", RvB.fonts.get("normalL"));
        
        buttonsText.add(Text.EASY.getText());
        buttonsText.add(Text.NORMAL.getText());
        buttonsText.add(Text.HARD.getText());
        buttonsText.add(Text.HARDCORE.getText());
    }
    
    public void chooseMap(){
        initPopup(chooseMap);
        top = height/4;
        
        addText(Text.SELECT_MODE.getText(), RvB.fonts.get("normalXL"));
        addText("("+Text.CANCEL.getText()+")", RvB.fonts.get("normalL"));
        
        buttonsText.add(Text.RANDOM_MAP.getText());
        buttonsText.add(Text.LOAD_MAP.getText());
    }
    
    public void gameOver(){
        if(currentOverlay == gameOver)
            return;
        if(!game.ended)
            SoundManager.Instance.playOnce(SoundManager.SOUND_GAME_OVER);
        initPopup(gameOver);
        if(RvB.state == State.GAME){
            game.unpause();
        } 
        top = height/4;
        addText(Text.GAME_OVER.getLines(), RvB.fonts.get("normalXL"));
        addText("\n", RvB.fonts.get("normalL"));
        addText(Text.WAVE.getText()+" "+game.waveNumber, RvB.fonts.get("normalXLB"));
        
        buttonsText.add(Text.MENU.getText());
    }
    
    public void gameWin(){
        if(currentOverlay == gameWin)
            return;
        if(!game.ended)
            SoundManager.Instance.playOnce(SoundManager.SOUND_GAME_WIN);
        initPopup(gameWin);
        if(RvB.state == State.GAME){
            game.unpause();
        } 
        top = height/4;
        addText(Text.GAME_WIN.getLines()[0], RvB.fonts.get("normalXLB"));
        addText("\n", RvB.fonts.get("normalL"));
        addText(Text.GAME_WIN.getLines()[1], RvB.fonts.get("normalL"));
        
        buttonsText.add(Text.MENU.getText());
    }
    
    public void enemiesUpgraded(String[] infoLines){
        initPopup(enemiesUpgraded);
        top = height/4;

        int r = random.nextInt(5);      
        addText(game.bossDefeated ? Text.BOSS_DEFEATED.getLines()[r] : Text.BOSS_NOT_DEFEATED.getLines()[r], RvB.fonts.get("normalXL"));
        addText("\n", RvB.fonts.get("normalXL"));
        addText(Text.ALL_ENEMIES.getText(), RvB.fonts.get("normalL"));
        for(int i = 0 ; i < infoLines.length ; i++)
            addText(infoLines[i], RvB.fonts.get("normalXLB"));
        
        r = random.nextInt(5); 
        buttonsText.add(game.bossDefeated ? Text.BOSS_DEFEATED_ANSWER.getLines()[r] : Text.BOSS_NOT_DEFEATED_ANSWER.getLines()[r]);
    }
    
    public void rewardSelection(){
        initPopup(rewardSelection);
        rewardSelection.clearButtons();
        rewardSelection.clearImages();
        top = height/8;
        
        addText(Text.LEVEL.getText() + game.raztech.lvl + " !", RvB.fonts.get("normalXLB"));
        addText("\n", RvB.fonts.get("normal"));
        addText(Text.RANGE.getText()+" +"+Math.round(game.raztech.getUpgrades().get(0).addOrMultiplicateValue), RvB.fonts.get("normal"));
        addText(Text.POWER.getText()+" +"+game.raztech.getUpgrades().get(1).addOrMultiplicateValue, RvB.fonts.get("normal"));
        addText(Text.SHOOTRATE.getText()+" +"+game.raztech.getUpgrades().get(2).addOrMultiplicateValue, RvB.fonts.get("normal"));
        switch(game.raztech.lvl){
            case 2:
                addText(Text.TOWER_UNLOCKED.getText(), RvB.fonts.get("normal"));
                break;
            case 4:
                addText(Text.TOWER_UNLOCKED.getText(), RvB.fonts.get("normal"));
                break;
            case 6:
                addText(Text.TOWER_UNLOCKED.getText(), RvB.fonts.get("normal"));
                break;
        }
        
        buff1 = game.buffs.empty() ? null : game.buffs.pop();
        buff2 = game.buffs.empty() ? null : game.buffs.pop();
        buff3 = game.buffs.empty() ? null : game.buffs.pop();
        
        Button b;
        if(buff1 != null){
            b = new Button(rewardSelection.getW()/6, 3*rewardSelection.getH()/4, (int) (160*ref), (int) (160*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
            b.setFunction(__ -> {
                stateChanged = true;
                closeCurrentPopup();
                game.enableAllButtons();
                RvB.setCursor(RvB.Cursor.DEFAULT);
                
                buff1.pick();
                
                if(buff1.isAnyLeft())
                    game.buffs.push(buff1);
                if(buff2 != null) game.buffs.push(buff2);
                if(buff3 != null) game.buffs.push(buff3);
                Collections.shuffle(game.buffs);
            });
            rewardSelection.addButton(b);
        }
        if(buff2 != null){
            b = new Button(rewardSelection.getW()/2, 3*rewardSelection.getH()/4, (int) (160*ref), (int) (160*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
            b.setFunction(__ -> {
                stateChanged = true;
                closeCurrentPopup();
                game.enableAllButtons();
                RvB.setCursor(RvB.Cursor.DEFAULT);
                
                buff2.pick();
                
                if(buff2.isAnyLeft())
                    game.buffs.push(buff2);
                if(buff1 != null) game.buffs.push(buff1);
                if(buff3 != null) game.buffs.push(buff3);
                Collections.shuffle(game.buffs);
            });     
            rewardSelection.addButton(b);
        }
        if(buff3 != null){
            b = new Button(5*rewardSelection.getW()/6, 3*rewardSelection.getH()/4, (int) (160*ref), (int) (160*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
            b.setFunction(__ -> {
                stateChanged = true;
                closeCurrentPopup();
                game.enableAllButtons();
                RvB.setCursor(RvB.Cursor.DEFAULT);
                
                buff3.pick();
                
                if(buff3.isAnyLeft())
                    game.buffs.push(buff3);
                if(buff1 != null) game.buffs.push(buff1);
                if(buff2 != null) game.buffs.push(buff2);
                Collections.shuffle(game.buffs);
            });
            rewardSelection.addButton(b);
        }
    }
    
    public void help(){
        if(currentOverlay == help)
            return;
        initPopup(help);
        top = height/8;
        
        addText(Text.HOW_TO_PLAY.getText(), RvB.fonts.get("normalXLB"));
        addText("\n", RvB.fonts.get("normalS"));
        addText(Text.GUIDE.getLines(), RvB.fonts.get("normalL"));
        addText("\n", RvB.fonts.get("normalS"));
        addText(Text.INFO.getText(), RvB.fonts.get("normalXLB"));
        addText("\n", RvB.fonts.get("normalS"));
        addText(Text.INFO_GUIDE.getLines(), RvB.fonts.get("normalL"));
        addText("\n", RvB.fonts.get("normalS"));
        addText(Text.SHORTCUTS.getText(), RvB.fonts.get("normalXLB"));
        addText("\n", RvB.fonts.get("normalS"));
        addText(Text.SHORTCUTS_GUIDE.getLines(), RvB.fonts.get("normalL"));
        
        buttonsText.add(Text.CLOSE.getText());
    }

    private void addText(String text, UnicodeFont font){
        lines.add(text);
        fonts.add(font);
    }
    
    private void addText(String[] lines, UnicodeFont font){
        for(String t : lines){
            this.lines.add(t);
            fonts.add(font);
        }
    }
    
    private void initPopup(Overlay overlay){
        lines.clear();
        buttonsText.clear();
        fonts.clear();
        currentOverlay = overlay;
        currentOverlay.display(true);
        if(RvB.state == State.MENU)
            menu.disableAllButtons();
        if(RvB.state == State.GAME){
            game.pause();
            game.disableAllButtons();
        }   
        if(RvB.state == State.CREATION)
            creation.disableAllButtons();
        RvB.setCursor(RvB.Cursor.DEFAULT);
        
    }   
    
    public void closeCurrentPopup(){
        if(currentOverlay == null)
            return;
        currentOverlay.display(false);
        currentOverlay = null;
        if(RvB.state == State.MENU)
            menu.enableAllButtons();
        if(RvB.state == State.GAME){
            game.unpause();
            game.enableAllButtons();
        }
        if(RvB.state == State.CREATION)
            creation.enableAllButtons();
    }
    
    public boolean onPopup(){
        return currentOverlay != null;
    }
    
    public boolean onChoosingDifficulty(){
        return currentOverlay == chooseDifficulty;
    }
}
