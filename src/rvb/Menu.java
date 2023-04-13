package rvb;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import managers.TextManager;
import ui.Button;
import managers.PopupManager;
import managers.RVBDB;
import managers.TextManager.Text;
import static rvb.RvB.*;
import ui.Overlay;


public class Menu {
    
    private Button start, play, regenerate, create, option, exit;
    public Button FR, ENG;
    private Overlay[] overlays = new Overlay[5];
    
    public Menu(){
        int width = (int) (250*ref);
        int height = (int) (60*ref);
        
        option = new Button(windWidth-(int)(120*ref), (int)(50*ref), (int)(32*ref), (int)(32*ref), RvB.textures.get("optionIcon"), null, colors.get("green_dark"));
        option.setFunction(__ -> {
            PopupManager.Instance.popup(Text.NO_OPTIONS.getLines());
        });
        exit = new Button(windWidth-(int)(50*ref), (int)(50*ref), (int)(32*ref), (int)(32*ref), RvB.textures.get("exitIcon"), null, colors.get("green_dark"));
        exit.setFunction(__ -> {
            switchStateTo(State.EXIT);
        });
        
        start = new Button(windWidth/2, windHeight/6, width, height, colors.get("green_semidark"), colors.get("green_dark"));
        start.lock();
        start.setText(Text.ADVENTURE, fonts.get("normalL"));
        start.setFunction(__ -> {
            if(adventureGame == null || adventureGame.ended || adventureGame.waveNumber == 1)
                adventureGame = new Game("1", Difficulty.MEDIUM);

            game = adventureGame;
            switchStateTo(State.GAME);
        });
        
        play = new Button(windWidth/2, windHeight/6, width, height, colors.get("green_semidark"), colors.get("green_dark"));
        play.setFunction(__ -> {
            if(game == null || game.ended)
                PopupManager.Instance.chooseMap();
            else
                switchStateTo(State.GAME);
        });
        regenerate = new Button(play.getX(), play.getY()+play.getH()/2+(int)(30*ref), (int)(160*ref), (int)(32*ref), colors.get("green"), colors.get("green_semidark"));
        regenerate.setText(Text.NEW_GAME, fonts.get("normal"));
        regenerate.setFunction(__ -> {
            PopupManager.Instance.chooseMap();
        });
        
        create = new Button(windWidth/2, windHeight/6, width, height, colors.get("green_semidark"), colors.get("green_dark"));
        create.setFunction(__ -> {
            if(createLevelCreatedFile()){
                if(creation == null)
                    creation = new Creation();
                switchStateTo(State.CREATION);
            }
            else{
                PopupManager.Instance.popup(Text.MISSING_FILE_LEVELS.getLines());
            }
        });
        
        FR = new Button(unite, 0, (int)(40*ref), (int)(40*ref), RvB.textures.get("FR"), null, colors.get("green_semidark"));
        FR.setFunction(__ -> {
            FR.setSelected(true);
            ENG.setSelected(false);
            TextManager.Instance.setLanguage("FR");
            try {
                RVBDB.Instance.updateLanguage("FR");
            } catch (SQLException ex) {
                Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        ENG = new Button(unite*2+(int)(10*ref), 0, (int)(40*ref), (int)(40*ref), RvB.textures.get("ENG"), null, colors.get("green_semidark"));
        ENG.setFunction(__ -> {
            ENG.setSelected(true);
            FR.setSelected(false);
            TextManager.Instance.setLanguage("ENG");
            try {
                RVBDB.Instance.updateLanguage("ENG");
            } catch (SQLException ex) {
                Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        for(int i = 0 ; i < 5 ; i++){
            overlays[i] = new Overlay(0, (i+1)*windHeight/6, windWidth, windHeight/6);
            switch(i){
                case 0:
                    overlays[i] = new Overlay(0, 0, windWidth, windHeight/3);
                    overlays[i].addButton(option);
                    overlays[i].addButton(exit);
                    overlays[i].addImage(windWidth/2, (int)(windWidth/10f + 100*ref), windWidth/2, (int) (windWidth/5f), RvB.textures.get("title"));
                    break;
                case 1:
                    overlays[i].addButton(start);
                    break;
                case 2:
                    overlays[i].addButton(play);
                    overlays[i].addButton(regenerate);
                    break;
                case 3:
                    overlays[i].addButton(create);
                    break;
                case 4:
                    overlays[4] = new Overlay(0, windHeight-FR.getH(), windWidth, FR.getH());
                    overlays[4].addButton(FR);
                    overlays[4].addButton(ENG);
                    break;
            }
        }
    }
    
    public void update(){
        render();
        if(game == null || game.ended)
            regenerate.setHidden(true);
        else
            regenerate.setHidden(false);
    }
    
    private void render(){
        drawFilledRectangle(0, 0, windWidth, windHeight, null, 1, textures.get("grass"));
        for(Overlay o : overlays){
            o.render();
        }
        if(regenerate.isHidden())
            play.drawText(Text.FIGHT.getText(), fonts.get("normalL"));
        else
            play.drawText(Text.CONTINUE.getText(), fonts.get("normalL"));
        create.drawText(Text.CREATION.getText(), fonts.get("normalL"));
    }
    
    public void disableAllButtons(){
        for(Overlay o : overlays)
            for(Button b : o.getButtons())
                b.disable();
    }
    
    public void enableAllButtons(){
        for(Overlay o : overlays)
            for(Button b : o.getButtons())
                b.enable();
        start.enable();
    }
    
    public Button getStart(){
        return start;
    }
    
    public Button getRandom(){
        return play;
    }
    
    public Button getCreate(){
        return create;
    }
    
    public Button getOption(){
        return option;
    }
    
    public Button getExit(){
        return exit;
    }
}