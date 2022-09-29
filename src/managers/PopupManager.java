package managers;

import java.util.ArrayList;
import org.newdawn.slick.UnicodeFont;
import rvb.RvB;
import static rvb.RvB.creation;
import static rvb.RvB.game;
import static rvb.RvB.menu;
import static rvb.RvB.stateChanged;
import ui.Button;
import ui.Overlay;
import static rvb.RvB.ref;


public class PopupManager {
    public static PopupManager Instance;
    
    private static int width = (int) (800*ref), height = (int) (450*ref);
    private static int oldGameSpeed;
    private Overlay currentOverlay;
    private Overlay gameOver, enemiesUpgraded, popup, chooseDifficulty;
    private ArrayList<String> lines;
    private ArrayList<String> buttonsText;
    private ArrayList<UnicodeFont> fonts;

    public PopupManager(){
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
        int butWith = (int) (180*ref);
        int butHeight = (int)(ref*36);
        // POPUP AFFICHAGE
        popup = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        popup.display(false);
        popup.setBG(RvB.textures.get("board"), 0.8f);
        popup.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(popup.getW()/2, 3*popup.getH()/4, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        popup.addButton(b);
        //
        // CHOOSE DIFFICULTY
        chooseDifficulty = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        chooseDifficulty.display(false);
        chooseDifficulty.setBG(RvB.textures.get("board"), 0.8f);
        chooseDifficulty.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(chooseDifficulty.getW()/4, 3*chooseDifficulty.getH()/4, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        chooseDifficulty.addButton(b);
        b = new Button(2*chooseDifficulty.getW()/4, 3*chooseDifficulty.getH()/4, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        chooseDifficulty.addButton(b);
        b = new Button(3*chooseDifficulty.getW()/4, 3*chooseDifficulty.getH()/4, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        chooseDifficulty.addButton(b);
        // GAME OVER
        gameOver = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        gameOver.display(false);
        gameOver.setBG(RvB.textures.get("board"), 0.8f);
        gameOver.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(gameOver.getW()/2, 3*gameOver.getH()/4, (int) (250*ref), (int)(50*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        gameOver.addButton(b);
        // ENEMIES UPGRADED
        enemiesUpgraded = new Overlay(RvB.windWidth/2-width/2, RvB.windHeight/2-height/2, width, height);
        enemiesUpgraded.display(false);
        enemiesUpgraded.setBG(RvB.textures.get("board"), 0.8f);
        enemiesUpgraded.setBorder(RvB.colors.get("green_dark"), 4, 1);
        b = new Button(enemiesUpgraded.getW()/2, 3*enemiesUpgraded.getH()/4, butWith, butHeight, RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"));
        enemiesUpgraded.addButton(b);
    }
    
    public void update(){
        if(currentOverlay == null)
            return;
        RvB.drawFilledRectangle(0, 0, RvB.windWidth, RvB.windHeight, null, 0.4f, RvB.textures.get("board"));
        currentOverlay.render();
        for(int i = 0 ; i < lines.size() ; i++)
            currentOverlay.drawText(currentOverlay.getW()/2, height/4+i*fonts.get(i).getHeight(lines.get(i)), lines.get(i), fonts.get(i));
        for(int i = 0 ; i < buttonsText.size() ; i++)
            currentOverlay.getButtons().get(i).drawText(0, 0, buttonsText.get(i), RvB.fonts.get("normalLB"));
        checkPopupInput();
    }
    
    private void checkPopupInput(){
        if(currentOverlay == null)
            return;
        boolean clicked = false;
        if(currentOverlay == gameOver){
            if(gameOver.getButtons().get(0).isClicked(0)){
                clicked = true;
                stateChanged = true;
                game.ended = true;
                for(int i = 0 ; i < game.enemies.size() ; i++)
                    game.enemies.get(i).die();
                game.clearArrays();
                SoundManager.Instance.closeAllClips();
                RvB.switchStateTo(RvB.State.MENU);
                currentOverlay = null;
                gameOver.display(false);
            }
        }
        else if(currentOverlay == enemiesUpgraded){
            if(enemiesUpgraded.getButtons().get(0).isClicked(0)){
                clicked = true;
                stateChanged = true;
                currentOverlay = null;
                enemiesUpgraded.display(false);
                game.gameSpeed = oldGameSpeed;
                game.enableAllButtons();
            }
        }
        else if(currentOverlay == popup){
            if(popup.getButtons().get(0).isClicked(0)){
                clicked = true;
                stateChanged = true;
                currentOverlay = null;
                popup.display(false);
                if(menu != null)
                    menu.enableAllButtons();
                if(game != null){
                    if(game.gameSpeed == 0)
                        game.gameSpeed = oldGameSpeed;
                    game.enableAllButtons();
                }
                if(creation != null)
                    creation.enableAllButtons();
            }
        }
        else if(currentOverlay == chooseDifficulty){
            if(chooseDifficulty.getButtons().get(0).isClicked(0) || chooseDifficulty.getButtons().get(1).isClicked(0) || chooseDifficulty.getButtons().get(2).isClicked(0)){
                clicked = true;
                stateChanged = true;
                currentOverlay = null;
                chooseDifficulty.display(false);
            }
            if(chooseDifficulty.getButtons().get(0).isClicked(0))
                RvB.newRandomMap(RvB.Difficulty.EASY);
            else if(chooseDifficulty.getButtons().get(1).isClicked(0))
                RvB.newRandomMap(RvB.Difficulty.MEDIUM);
            else if(chooseDifficulty.getButtons().get(2).isClicked(0))
                RvB.newRandomMap(RvB.Difficulty.HARD);
        }
        if(clicked)
            RvB.setCursor(RvB.Cursor.DEFAULT);
    }
    
    public void popup(String texte){
        initPopup(popup);
        lines.add(texte);
        fonts.add(RvB.fonts.get("normalXL"));
        
        buttonsText.add("Ok");
    }
    
    public void chooseDifficulty(){
        initPopup(chooseDifficulty);
        lines.add(" ");
        fonts.add(RvB.fonts.get("normalXL"));
        lines.add("Select a difficulty");
        fonts.add(RvB.fonts.get("normalXL"));
        lines.add(" ");
        fonts.add(RvB.fonts.get("normalL"));
        lines.add("(Escape to cancel)");
        fonts.add(RvB.fonts.get("normalL"));
        
        buttonsText.add("Easy");
        buttonsText.add("Normal");
        buttonsText.add("Hard");
    }
    
    public void gameOver(){
        if(currentOverlay == gameOver)
            return;
        initPopup(gameOver);
        lines.add("Bazoo and his army have been stronger...");
        fonts.add(RvB.fonts.get("normalXL"));
        lines.add("He's won a battle, but not the war.");
        fonts.add(RvB.fonts.get("normalXL"));
        lines.add(" ");
        fonts.add(RvB.fonts.get("normalL"));
        lines.add("Wave "+game.waveNumber);
        fonts.add(RvB.fonts.get("normalXLB"));
        
        buttonsText.add("Return to menu");
    }
    
    public void enemiesUpgraded(String upgradeInfo){
        oldGameSpeed = game.gameSpeed;
        game.gameSpeed = 0;
        initPopup(enemiesUpgraded);
        lines.add("Bazoo has provided new technologies");
        fonts.add(RvB.fonts.get("normalXL"));
        lines.add("to his army.");
        fonts.add(RvB.fonts.get("normalXL"));
        lines.add(" ");
        fonts.add(RvB.fonts.get("normalL"));
        lines.add(upgradeInfo);
        fonts.add(RvB.fonts.get("normalXLB"));
        
        buttonsText.add("Crap !");
    }

    private void initPopup(Overlay overlay){
        lines.clear();
        buttonsText.clear();
        fonts.clear();
        currentOverlay = overlay;
        currentOverlay.display(true);
        if(menu != null)
            menu.disableAllButtons();
        if(game != null)
            game.disableAllButtons();
        if(creation != null)
            creation.disableAllButtons();
    }   
    
    public void closeCurrentPopup(){
        if(currentOverlay == null)
            return;
        currentOverlay.display(false);
        currentOverlay = null;
    }
    
    public boolean onPopup(){
        return currentOverlay != null;
    }
    
    public boolean onChoosingDifficulty(){
        return currentOverlay == chooseDifficulty;
    }
}
