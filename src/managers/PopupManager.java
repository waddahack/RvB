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
    private Overlay gameOver, gameWin, enemiesUpgraded, popup, popupTuto, chooseDifficulty, rewardSelection, help, chooseMap;
    private final ArrayList<String> lines, anchors;
    private final ArrayList<UnicodeFont> fonts;
    private static Random random;
    private String gameType;
    private int top;
    private Buff buff1, buff2, buff3;
    private Consumer<Object> callback = null;
    private boolean addBackground = true;

    private PopupManager(){
        random = new Random();
        currentOverlay = null;
        lines = new ArrayList<>();
        fonts = new ArrayList<>();
        anchors = new ArrayList<>();
        initOverlays();
    }

    public static void initialize(){
        if(Instance == null)
            Instance = new PopupManager();
    }
    
    public void initOverlays(){
        Button b;
        int butWith = (int) (180*ref);
        int butHeight = (int)(ref*36);
        // POPUP AFFICHAGE
        popup = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        popup.display(false);
        popup.setBG(RvB.textures.get("board"), 0.8f);
        popup.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(popup.getW()/2, 3*popup.getH()/4, butWith, butHeight);
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
        });
        popup.addButton(b);
        //
        // POPUP TUTO
        popupTuto = new Overlay(0, 0, 0, 0);
        popupTuto.display(false);
        popupTuto.setBG(RvB.textures.get("board"), 0.8f);
        popupTuto.setBorder(RvB.colors.get("green_dark"), 4, 1);
        //
        // CHOOSE MAP
        chooseMap = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        chooseMap.display(false);
        chooseMap.setBG(RvB.textures.get("board"), 0.8f);
        chooseMap.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(chooseMap.getW()/2, 6*chooseMap.getH()/10, (int)(butWith*1.2), (int)(butHeight*1.2));
        b.setText(Text.RANDOM_MAP, RvB.fonts.get("normalLB"));
        b.setFunction(__ -> {
            chooseDifficulty("random");
            // Then it does newRandomMap()
        });
        chooseMap.addButton(b);
        b = new Button(chooseMap.getW()/2, 8*chooseMap.getH()/10, (int)(butWith*1.2), (int)(butHeight*1.2));
        b.setText(Text.LOAD_MAP, RvB.fonts.get("normalLB"));
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
        b = new Button(chooseDifficulty.getW()/2, 4*chooseDifficulty.getH()/8, butWith, butHeight);
        b.setText(Text.EASY, RvB.fonts.get("normalLB"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            if(gameType.equals("random"))
                RvB.newRandomMap(RvB.Difficulty.EASY);
            else if(gameType.equals("created"))
                RvB.newCreatedMap(RvB.Difficulty.EASY);
            else
                RvB.loadMap(gameType, RvB.Difficulty.EASY);
        });
        b.setOnHoverFunction(__ -> {
            renderDiffDesc(RvB.Difficulty.EASY);
            renderBestScore(RvB.Difficulty.EASY);
        });
        chooseDifficulty.addButton(b);
        b = new Button(chooseDifficulty.getW()/2, 5*chooseDifficulty.getH()/8, butWith, butHeight);
        b.setText(Text.MEDIUM, RvB.fonts.get("normalLB"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            if(gameType.equals("random"))
                RvB.newRandomMap(RvB.Difficulty.MEDIUM);
            else if(gameType.equals("created"))
                RvB.newCreatedMap(RvB.Difficulty.MEDIUM);
            else
                RvB.loadMap(gameType, RvB.Difficulty.MEDIUM);
        });
        b.setOnHoverFunction(__ -> {
            renderDiffDesc(RvB.Difficulty.MEDIUM);
            renderBestScore(RvB.Difficulty.MEDIUM);
        });
        chooseDifficulty.addButton(b);
        b = new Button(chooseDifficulty.getW()/2, 6*chooseDifficulty.getH()/8, butWith, butHeight);
        b.setText(Text.HARD, RvB.fonts.get("normalLB"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            if(gameType.equals("random"))
                RvB.newRandomMap(RvB.Difficulty.HARD);
            else if(gameType.equals("created"))
                RvB.newCreatedMap(RvB.Difficulty.HARD);
            else
                RvB.loadMap(gameType, RvB.Difficulty.HARD);
        });
        b.setOnHoverFunction(__ -> {
            renderDiffDesc(RvB.Difficulty.HARD);
            renderBestScore(RvB.Difficulty.HARD);
        });
        chooseDifficulty.addButton(b);
        b = new Button(chooseDifficulty.getW()/2, 7*chooseDifficulty.getH()/8, butWith, butHeight);
        b.setText(Text.HARDCORE, RvB.fonts.get("normalLB"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            if(gameType.equals("random"))
                RvB.newRandomMap(RvB.Difficulty.HARDCORE);
            else if(gameType.equals("created"))
                RvB.newCreatedMap(RvB.Difficulty.HARDCORE);
            else
                RvB.loadMap(gameType, RvB.Difficulty.HARDCORE);
        });
        b.setOnHoverFunction(__ -> {
            renderDiffDesc(RvB.Difficulty.HARDCORE);
            renderBestScore(RvB.Difficulty.HARDCORE);
        });
        chooseDifficulty.addButton(b);
        // GAME OVER
        gameOver = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        gameOver.display(false);
        gameOver.setBG(RvB.textures.get("board"), 0.8f);
        gameOver.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(gameOver.getW()/2, 3*gameOver.getH()/4, (int) (250*ref), (int)(50*ref));
        b.setText(Text.MENU, RvB.fonts.get("normalLB"));
        b.setFunction(__ -> {
            stateChanged = true;
            game.ended = true;
            for(int i = 0 ; i < game.enemies.size() ; i++)
                game.enemies.get(i).die();
            game.clearArrays();
            SoundManager.Instance.closeAllClips();
            RvB.switchStateTo(RvB.State.MENU);
            closeCurrentPopup();
        });
        gameOver.addButton(b);
        // GAME WIN
        gameWin = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        gameWin.display(false);
        gameWin.setBG(RvB.textures.get("board"), 0.8f);
        gameWin.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(gameWin.getW()/2, 3*gameWin.getH()/4, (int) (250*ref), (int)(50*ref));
        b.setText(Text.MENU, RvB.fonts.get("normalLB"));
        b.setFunction(__ -> {
            stateChanged = true;
            game.ended = true;
            game.clearArrays();
            SoundManager.Instance.closeAllClips();
            RvB.switchStateTo(RvB.State.MENU);
            closeCurrentPopup();
        });
        gameWin.addButton(b);
        // ENEMIES UPGRADED
        enemiesUpgraded = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        enemiesUpgraded.display(false);
        enemiesUpgraded.setBG(RvB.textures.get("board"), 0.8f);
        enemiesUpgraded.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(enemiesUpgraded.getW()/2, 3*enemiesUpgraded.getH()/4, (int) (butWith*1.2), butHeight);
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            game.enableAllButtons();
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
        b = new Button(11*help.getW()/12, 19*help.getH()/20, (int) (butWith*1.2), butHeight);
        b.setText(Text.CLOSE, RvB.fonts.get("normalLB"));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            game.enableAllButtons();
        });
        help.addButton(b);
    }
    
    public void update(){
        if(currentOverlay == null && !popupTuto.isDisplayed())
            return;
        if(addBackground)
            RvB.drawFilledRectangle(0, 0, RvB.windWidth, RvB.windHeight, null, 0.4f, RvB.textures.get("board"));
        if(currentOverlay != null)
            renderCurrentOverlay();
        popupTuto.render();
    }
    
    private void renderCurrentOverlay(){
        currentOverlay.render();
        
        if(currentOverlay == null)
            return;
        int top = this.top;
        for(int i = 0 ; i < lines.size() ; i++){
            if(i > 0)
                top += fonts.get(i).getHeight(lines.get(i))+8*ref;
            currentOverlay.drawText(currentOverlay.getW()/2, top, lines.get(i), fonts.get(i), anchors.get(i));
        }
        
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
    
    public void popup(String text, Text butText){
        popup(new String[]{text}, null, butText);
    }
    
    public void popup(String text){
        popup(new String[]{text}, null, Text.OK);
    }
    
    public void popup(String[] infoLines){
        popup(infoLines, null, Text.OK);
    }
    
    public void popup(String[] infoLines, Text butText){
        popup(infoLines, null, butText);
    }
    
    public void popup(String[] infoLines, UnicodeFont[] fonts, Text butText){
        initPopup(popup);
        top = height/4;
        popup.getButtons().get(0).setText(butText, RvB.fonts.get("normalLB"));
        for(int i = 0 ; i < infoLines.length ; i++)
            addText(infoLines[i], (fonts != null ? (i < fonts.length ? fonts[i] : fonts[fonts.length-1]) : RvB.fonts.get("normalXL")));
    }
    
    public void popupTuto(int x, int y, int w, int h, Text text, int[] pointPos, Consumer<Object> callback){
        if(RvB.state != RvB.State.GAME)
            return;
        initPopup(popupTuto);
        popupTuto.setW(w);
        popupTuto.setH(h);
        popupTuto.updateCoords(x-w/2, y-h/2);
        Button b = new Button(w/2, h, (int)(60*ref), (int)(24*ref));
        b.setFunction(__ -> {
            stateChanged = true;
            closeCurrentPopup();
            if(currentOverlay != null){
                for(Button but : currentOverlay.getButtons())
                    but.enable();
            }
            addBackground = true;
            callback.accept(null);
        });
        b.setText(Text.NEXT, RvB.fonts.get("normalLB"));
        popupTuto.getButtons().clear();
        popupTuto.addButton(b);
        if(currentOverlay == null)
            addBackground = false;
        popupTuto.clearTexts();
        popupTuto.addText(text, RvB.fonts.get("normal"), new int[]{w/2, Math.floorDiv(8-text.getLines().length, 2)*(RvB.fonts.get("normal").getFont().getSize()+(int)(8*ref))+(int)(8*ref)}, "topMid");
        popupTuto.clearImages();
        if(pointPos != null){
            int px = pointPos[0], py = pointPos[1], angle = 45;
            if(px+popupTuto.getX() > RvB.windWidth/2 && py+popupTuto.getY() > RvB.windHeight/2)
                angle = 135;
            else if(px+popupTuto.getX() < RvB.windWidth/2 && py+popupTuto.getY() > RvB.windHeight/2)
                angle = 225;
            else if(px+popupTuto.getX() < RvB.windWidth/2 && py+popupTuto.getY() < RvB.windHeight/2)
                angle = -135;
            popupTuto.addImage(px, py, RvB.unite*2, RvB.unite*2, RvB.textures.get("arrowPoint"), angle);
        }
        if(RvB.state == State.GAME && (game.gameOver || game.gameWin)){
            game.unpause();
        }
    }
    
    public void chooseDifficulty(String gameType){
        initPopup(chooseDifficulty);
        top = height/4;
        this.gameType = gameType;
        
        addText(Text.SELECT_DIFF.getText(), RvB.fonts.get("normalXL"));
        addText("("+Text.CANCEL.getText()+")", RvB.fonts.get("normalL"));
        
        if(!TutoManager.Instance.hasDone(TutoManager.TutoStep.GM_NDD)){
            chooseDifficulty.getButtons().get(1).lock();
            chooseDifficulty.getButtons().get(2).lock();
            chooseDifficulty.getButtons().get(3).lock();
        }
        else{
            chooseDifficulty.getButtons().get(1).unlock();
            chooseDifficulty.getButtons().get(2).unlock();
            chooseDifficulty.getButtons().get(3).unlock();
        }
    }
    
    public void chooseMap(){
        initPopup(chooseMap);
        top = height/4;
        
        addText(Text.SELECT_MODE.getText(), RvB.fonts.get("normalXL"));
        addText("("+Text.CANCEL.getText()+")", RvB.fonts.get("normalL"));
        
        if(!TutoManager.Instance.hasDone(TutoManager.TutoStep.GM_NDD))
            chooseMap.getButtons().get(1).lock();
        else
            chooseMap.getButtons().get(1).unlock();
    }
    
    public void gameOver(){
        if(currentOverlay == gameOver)
            return;
        if(!game.ended && !SoundManager.SOUND_BAZOO_LAUGH.isRunning())
            SoundManager.Instance.playOnce(SoundManager.SOUND_BAZOO_LAUGH);
        initPopup(gameOver);
        Button b = RvB.game.getOverlays().get(1).getButtons().get(1);
        b.setX(gameOver.getW()-b.getW());
        b.setY(b.getH());
        b.enable();
        gameOver.addButton(b);
        if(RvB.state == State.GAME){
            game.unpause();
        } 
        top = height/4;
        addText(Text.GAME_OVER.getLines(), RvB.fonts.get("normalXL"));
        addText("\n", RvB.fonts.get("normalL"));
        addText(Text.WAVE.getText()+" "+game.waveNumber, RvB.fonts.get("normalXLB"));
    }
    
    public void gameWin(){
        if(currentOverlay == gameWin)
            return;
        if(!game.ended)
            SoundManager.Instance.playOnce(SoundManager.SOUND_GAME_WIN);
        initPopup(gameWin);
        Button b = RvB.game.getOverlays().get(1).getButtons().get(1);
        b.setX(gameWin.getW()-b.getW());
        b.setY(b.getH());
        b.enable();
        gameWin.addButton(b);
        if(RvB.state == State.GAME){
            game.unpause();
        } 
        top = height/4;
        addText(Text.GAME_WIN.getLines()[0], RvB.fonts.get("normalXLB"));
        addText("\n", RvB.fonts.get("normalL"));
        addText(Text.GAME_WIN.getLines()[1], RvB.fonts.get("normalL"));
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
        Button b = enemiesUpgraded.getButtons().get(0);
        b.setText(game.bossDefeated ? Text.BOSS_DEFEATED_ANSWER : Text.BOSS_NOT_DEFEATED_ANSWER, RvB.fonts.get("normalLB"));
        b.indexSwitch = r;
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
                addText(Text.TOWER_UNLOCKED.getText()+" : --circleTower-- ", RvB.fonts.get("normal"));
                break;
            case 4:
                addText(Text.TOWER_UNLOCKED.getText()+" : --bigTower-- ", RvB.fonts.get("normal"));
                break;
            case 6:
                addText(Text.TOWER_UNLOCKED.getText()+" : --flameTower-- ", RvB.fonts.get("normal"));
                break;
        }
        
        buff1 = game.buffs.empty() ? null : game.buffs.pop();
        buff2 = game.buffs.empty() ? null : game.buffs.pop();
        buff3 = game.buffs.empty() ? null : game.buffs.pop();
        
        Button b;
        if(buff1 != null){
            b = new Button(rewardSelection.getW()/6, 3*rewardSelection.getH()/4, (int) (160*ref), (int) (160*ref));
            b.setFunction(__ -> {
                stateChanged = true;
                closeCurrentPopup();
                game.enableAllButtons();
                
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
            b = new Button(rewardSelection.getW()/2, 3*rewardSelection.getH()/4, (int) (160*ref), (int) (160*ref));
            b.setFunction(__ -> {
                stateChanged = true;
                closeCurrentPopup();
                game.enableAllButtons();
                
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
            b = new Button(5*rewardSelection.getW()/6, 3*rewardSelection.getH()/4, (int) (160*ref), (int) (160*ref));
            b.setFunction(__ -> {
                stateChanged = true;
                closeCurrentPopup();
                game.enableAllButtons();
                
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
    }

    private void addText(String text, UnicodeFont font){
        addText(text, font, "center");
    }
    
    private void addText(String[] lines, UnicodeFont font){
        addText(lines, font, "center");
    }
    
    private void addText(String text, UnicodeFont font, String anchor){
        lines.add(text);
        fonts.add(font);
        anchors.add(anchor);
    }
    
    private void addText(String[] lines, UnicodeFont font, String anchor){
        for(String t : lines){
            this.lines.add(t);
            fonts.add(font);
            anchors.add(anchor);
        }
    }
    
    private void initPopup(Overlay overlay){
        if(overlay != popupTuto){
            lines.clear();
            fonts.clear();
            currentOverlay = overlay;
            currentOverlay.display(true);
        }
        else{
            popupTuto.display(true);
            if(currentOverlay != null){
                for(Button b : currentOverlay.getButtons())
                    b.disable();
            }
        }
        
        if(RvB.state == State.GAME){
            game.pause();
            game.disableAllButtons();
        }   
        if(RvB.state == State.MENU){
            menu.disableAllButtons();
        }   
        if(RvB.state == State.CREATION)
            creation.disableAllButtons();
        RvB.setCursor(RvB.Cursor.DEFAULT);
    }   
    
    public void closeCurrentPopup(){
        boolean reenable = false;
        if(popupTuto.isDisplayed()){
            popupTuto.display(false);
            if(currentOverlay != null)
                return;
            reenable = true;
        }
        if(currentOverlay == null && !reenable)
            return;
        if(currentOverlay != null){
            currentOverlay.display(false);
            currentOverlay = null;
        }
        
        if(RvB.state == State.GAME){
            game.unpause();
            game.enableAllButtons();
        }
        if(RvB.state == State.CREATION)
            creation.enableAllButtons();
        if(RvB.state == State.MENU){
            menu.enableAllButtons();
        }
        if(callback != null){
            callback.accept(null);
            if(!onPopup())
                callback = null;
        }
        RvB.setCursor(RvB.Cursor.DEFAULT);
    }
    
    private void renderDiffDesc(RvB.Difficulty diff){
        int posX = chooseDifficulty.getX()+chooseDifficulty.getW()/4;
        int posY = chooseDifficulty.getY()+chooseDifficulty.getH()/2;
        RvB.drawString(posX, posY, "--heart-- "+diff.life, RvB.fonts.get("normalL"));
        RvB.drawString(posX, posY+(int)(42*ref), "--coins-- "+diff.money, RvB.fonts.get("normalL"));
        RvB.drawString(posX, posY+2*(int)(42*ref), "--coinsAdd-- "+diff.waveReward, RvB.fonts.get("normalL"));
        RvB.drawString(posX, posY+3*(int)(42*ref), "--enemyRate-- "+(int)(diff.waveBalanceMult*100)+"%", RvB.fonts.get("normalL"));
        if(!gameType.equals("random"))
            return;
        Text t = null;
        switch(diff.name){
            case "EASY" :
                t = Text.LONG;
                break;
            case "MEDIUM" :
                t = Text.MEDIUM;
                break;
            case "HARD" :
                t = Text.SHORT;
                break;
            case "HARDCORE" :
                t = Text.SHORT;
                break;
        }
        RvB.drawString(posX, posY+4*(int)(42*ref), "--roadStraight-- "+t.getText(), RvB.fonts.get("normalL"));
    }
    
    private void renderBestScore(RvB.Difficulty diff){
        if(gameType.equals("adventure"))
            return;
        String bestScore = null;
        switch(diff.name){
            case "EASY" :
                bestScore = StatsManager.Instance.modeEasyBestScore;
                break;
            case "MEDIUM" :
                bestScore = StatsManager.Instance.modeMediumBestScore;
                break;
            case "HARD" :
                bestScore = StatsManager.Instance.modeHardBestScore;
                break;
            case "HARDCORE" :
                bestScore = StatsManager.Instance.modeHardcoreBestScore;
                break;
        }
        if(bestScore == null || bestScore.isEmpty())
            return;
        int posX = chooseDifficulty.getW()-chooseDifficulty.getW()/6;
        int posY = chooseDifficulty.getH()/2;
        String nbWave = bestScore.split(";")[0], lifePercent = bestScore.split(";")[1];
        
        chooseDifficulty.drawText(posX, posY, Text.PB.getText(), RvB.fonts.get("titleS"));
        
        if(Integer.parseInt(nbWave) < diff.nbWaveMax){
            chooseDifficulty.drawText(posX, posY+(int)(32*ref), Text.WAVE.getText()+" : "+nbWave, RvB.fonts.get("normal"));
        }
        else{
            chooseDifficulty.drawText(posX, posY+(int)(32*ref), Text.WON.getText(), RvB.fonts.get("normal"));
            chooseDifficulty.drawText(posX, posY+(int)(64*ref), Text.LIFEPOINT_LEFT.getText()+" : "+lifePercent+"%", RvB.fonts.get("normal"));
        }
    }
    
    public boolean onPopup(){
        return (currentOverlay != null || popupTuto.isDisplayed());
    }
    
    public boolean onPopupTuto(){
        return popupTuto.isDisplayed();
    }
    
    public boolean onRewardSelection(){
        return currentOverlay == rewardSelection;
    }
}
