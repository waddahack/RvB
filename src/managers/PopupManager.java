package managers;

import java.util.ArrayList;
import org.newdawn.slick.UnicodeFont;
import towser.Game;
import towser.Towser;
import static towser.Towser.creation;
import static towser.Towser.game;
import static towser.Towser.ref;
import static towser.Towser.stateChanged;
import ui.Button;
import ui.Overlay;


public class PopupManager {
    public static PopupManager Instance;
    
    private static int width = (int) (650*ref), height = (int) (370*ref);
    private static int oldGameSpeed;
    private Overlay currentOverlay;
    private Overlay gameOver, enemiesUpgraded, popup;
    private ArrayList<String> lines;
    private ArrayList<UnicodeFont> fonts;
    private ArrayList<String> buttonsText;

    public PopupManager(){
        currentOverlay = null;
        buttonsText = new ArrayList<>();
        lines = new ArrayList<>();
        fonts = new ArrayList<>();
        initOverlays();
    }

    public static void initialize(){
        if(Instance == null)
            Instance = new PopupManager();
    }
    
    public void initOverlays(){
        Button b;
        
        // POPUP AFFICHAGE
        popup = new Overlay(Towser.windWidth/2-width/2, Towser.windHeight/2-height/2, width, height);
        popup.display(false);
        popup.setBG(Towser.textures.get("board"));
        popup.setA(0.8f);
        popup.setBorder(Towser.colors.get("green_dark"), 4);
        b = new Button(popup.getW()/2, 3*popup.getH()/4, (int) (150*ref), (int)(ref*30), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        popup.addButton(b);
        //
        // GAME OVER
        gameOver = new Overlay(Towser.windWidth/2-width/2, Towser.windHeight/2-height/2, width, height);
        gameOver.display(false);
        gameOver.setBG(Towser.textures.get("board"));
        gameOver.setA(0.8f);
        gameOver.setBorder(Towser.colors.get("green_dark"), 4);
        b = new Button(gameOver.getW()/2, 3*gameOver.getH()/4, (int) (250*ref), (int)(ref*50), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        gameOver.addButton(b);
        // ENEMIES UPGRADED
        enemiesUpgraded = new Overlay(Towser.windWidth/2-width/2, Towser.windHeight/2-height/2, width, height);
        enemiesUpgraded.display(false);
        enemiesUpgraded.setBG(Towser.textures.get("board"));
        enemiesUpgraded.setA(0.8f);
        enemiesUpgraded.setBorder(Towser.colors.get("green_dark"), 4);
        b = new Button(enemiesUpgraded.getW()/2, 3*enemiesUpgraded.getH()/4, (int) (150*ref), (int)(ref*30), Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        enemiesUpgraded.addButton(b);
    }
    
    public void update(){
        checkPopupInput();
        if(currentOverlay == null)
            return;
        Towser.drawFilledRectangle(0, 0, Towser.windWidth, Towser.windHeight, null, 0.4f, Towser.textures.get("board"));
        currentOverlay.render();
        for(int i = 0 ; i < lines.size() ; i++)
            currentOverlay.drawText(currentOverlay.getW()/2, height/4+i*fonts.get(i).getHeight(lines.get(i)), lines.get(i), fonts.get(i));
        for(int i = 0 ; i < currentOverlay.getButtons().size() ; i++)
            currentOverlay.getButtons().get(i).drawText(buttonsText.get(i), Towser.fonts.get("normalLB"));
    }
    
    private void checkPopupInput(){
        if(currentOverlay == null)
            return;
        if(currentOverlay == gameOver){
            if(gameOver.getButtons().get(0).isClicked(0)){
                stateChanged = true;
                for(int i = 0 ; i < game.enemies.size() ; i++){
                    game.enemies.get(i).putInBase();
                    game.enemies.get(i).die();
                } 
                SoundManager.Instance.closeAllClips();
                game.ended = true;
                Towser.state = Towser.State.MENU;
                currentOverlay = null;
                gameOver.display(false);
            }
        }
        else if(currentOverlay == enemiesUpgraded){
            if(enemiesUpgraded.getButtons().get(0).isClicked(0)){
                stateChanged = true;
                currentOverlay = null;
                enemiesUpgraded.display(false);
                game.gameSpeed = oldGameSpeed;
                game.enableAllButtons();
            }
        }
        else if(currentOverlay == popup){
            if(popup.getButtons().get(0).isClicked(0)){
                stateChanged = true;
                currentOverlay = null;
                popup.display(false);
                if(game != null){
                    game.gameSpeed = oldGameSpeed;
                    game.enableAllButtons();
                }
                if(creation != null)
                    creation.enableAllButtons();
            }
        }
    }
    
    public void popup(String texte){
        initPopup(popup);
        lines.add(texte);
        fonts.add(Towser.fonts.get("normalXL"));
        
        buttonsText.add("Ok");
    }
    
    public void gameOver(){
        initPopup(gameOver);
        lines.add("Bazoo and his army have been stronger...");
        fonts.add(Towser.fonts.get("normalXL"));
        lines.add("He's won a battle, but not the war.");
        fonts.add(Towser.fonts.get("normalXL"));
        lines.add(" ");
        fonts.add(Towser.fonts.get("normalL"));
        lines.add("Wave "+game.waveNumber);
        fonts.add(Towser.fonts.get("normalXLB"));
        
        buttonsText.add("Return to menu");
    }
    
    public void enemiesUpgraded(String upgradeInfo){
        oldGameSpeed = game.gameSpeed;
        game.gameSpeed = 0;
        initPopup(enemiesUpgraded);
        lines.add("Bazoo has provided new technologies");
        fonts.add(Towser.fonts.get("normalXL"));
        lines.add("to his army.");
        fonts.add(Towser.fonts.get("normalXL"));
        lines.add(" ");
        fonts.add(Towser.fonts.get("normalL"));
        lines.add(upgradeInfo);
        fonts.add(Towser.fonts.get("normalXLB"));
        
        buttonsText.add("Crap !");
    }

    private void initPopup(Overlay overlay){
        lines.clear();
        fonts.clear();
        buttonsText.clear();
        currentOverlay = overlay;
        currentOverlay.display(true);
        if(game != null)
            game.disableAllButtons();
        if(creation != null)
            creation.disableAllButtons();
    }   
    
    public boolean onPopup(){
        return currentOverlay != null;
    }
}
