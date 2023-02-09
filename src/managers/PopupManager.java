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


public class PopupManager {
    public static PopupManager Instance;
    
    private static int width = (int) (800*ref), height = (int) (450*ref);
    private Overlay currentOverlay;
    private Overlay gameOver, enemiesUpgraded, popup, chooseDifficulty, rewardSelection;
    private ArrayList<String> lines;
    private ArrayList<String> buttonsText;
    private ArrayList<UnicodeFont> fonts;
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
        // CHOOSE DIFFICULTY
        chooseDifficulty = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        chooseDifficulty.display(false);
        chooseDifficulty.setBG(RvB.textures.get("board"), 0.8f);
        chooseDifficulty.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(chooseDifficulty.getW()/4, 3*chooseDifficulty.getH()/4, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            if(gameType.equals("random"))
                RvB.newRandomMap(RvB.Difficulty.EASY);
            else
                RvB.newCreatedMap(RvB.Difficulty.EASY);
            RvB.setCursor(RvB.Cursor.DEFAULT);
        });
        chooseDifficulty.addButton(b);
        b = new Button(2*chooseDifficulty.getW()/4, 3*chooseDifficulty.getH()/4, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            if(gameType.equals("random"))
                RvB.newRandomMap(RvB.Difficulty.MEDIUM);
            else
                RvB.newCreatedMap(RvB.Difficulty.MEDIUM);
            RvB.setCursor(RvB.Cursor.DEFAULT);
        });
        chooseDifficulty.addButton(b);
        b = new Button(3*chooseDifficulty.getW()/4, 3*chooseDifficulty.getH()/4, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            if(gameType.equals("random"))
                RvB.newRandomMap(RvB.Difficulty.HARD);
            else
                RvB.newCreatedMap(RvB.Difficulty.HARD);
            RvB.setCursor(RvB.Cursor.DEFAULT);
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
    }
    
    public void gameOver(){
        if(currentOverlay == gameOver)
            return;
        initPopup(gameOver);
        top = height/4;
        addText(Text.GAME_OVER.getLines(), RvB.fonts.get("normalXL"));
        addText("\n", RvB.fonts.get("normalL"));
        addText(Text.WAVE.getText()+" "+game.waveNumber, RvB.fonts.get("normalXLB"));
        
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
        addText(Text.POWER.getText()+" +"+Math.round(game.raztech.getUpgrades().get(1).addOrMultiplicateValue), RvB.fonts.get("normal"));
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
        if(menu != null)
            menu.disableAllButtons();
        if(game != null){
            game.pause();
            game.disableAllButtons();
        }   
        if(creation != null)
            creation.disableAllButtons();
        RvB.setCursor(RvB.Cursor.DEFAULT);
        
    }   
    
    public void closeCurrentPopup(){
        if(currentOverlay == null)
            return;
        currentOverlay.display(false);
        currentOverlay = null;
        if(menu != null)
            menu.enableAllButtons();
        if(game != null){
            game.unpause();
            game.enableAllButtons();
        }
        if(creation != null)
            creation.enableAllButtons();
    }
    
    public boolean onPopup(){
        return currentOverlay != null;
    }
    
    public boolean onChoosingDifficulty(){
        return currentOverlay == chooseDifficulty;
    }
}
