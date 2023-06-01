package Windows;

import Windows.Window;
import managers.TextManager;
import ui.Button;
import managers.PopupManager;
import managers.TextManager.Text;
import managers.TutoManager;
import rvb.Creation;
import rvb.Game;
import rvb.RvB;
import static rvb.RvB.*;
import ui.Overlay;


public class MenuWindow extends Window{
    
    private Button start, play, regenerate, create, option, exit, stats;
    public Button FR, ENG;
    
    public MenuWindow(){
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
        stats = new Button((int)(width*0.3)+(int)(50*ref), (int)(50*ref), (int)(width*0.6), (int)(height*0.6), colors.get("green_semidark"), colors.get("green_dark"));
        stats.setText(Text.STATS, RvB.fonts.get("normal"));
        stats.setFunction(__ -> {
            switchStateTo(State.STATS);
        });
                
                
        start = new Button(windWidth/2, windHeight/6, width, height, colors.get("green_semidark"), colors.get("green_dark"));
        start.lock();
        start.setText(Text.ADVENTURE, fonts.get("normalL"));
        start.setFunction(__ -> {
            // Faire comme les autres
            if(adventureGame == null || adventureGame.ended || adventureGame.waveNumber == 1)
                adventureGame = new Game("1", Difficulty.MEDIUM);

            game = adventureGame;
            game.saveBestScore = false;
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
        create.setText(Text.CREATION, fonts.get("normalL"));
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
        });
        ENG = new Button(unite*2+(int)(10*ref), 0, (int)(40*ref), (int)(40*ref), RvB.textures.get("ENG"), null, colors.get("green_semidark"));
        ENG.setFunction(__ -> {
            ENG.setSelected(true);
            FR.setSelected(false);
            TextManager.Instance.setLanguage("ENG");
        });
        ENG.setSelected(true);
        
        Overlay o;
        for(int i = 0 ; i < 5 ; i++){
            o = new Overlay(0, (i+1)*windHeight/6, windWidth, windHeight/6);
            overlays.add(o);
            switch(i){
                case 0:
                    overlays.remove(o);
                    o = new Overlay(0, 0, windWidth, windHeight/3);
                    o.addButton(option);
                    o.addButton(exit);
                    o.addButton(stats);
                    o.addImage(windWidth/2, (int)(windWidth/10f + 100*ref), windWidth/2, (int) (windWidth/5f), RvB.textures.get("title"));
                    overlays.add(o);
                    break;
                case 1:
                    o.addButton(start);
                    break;
                case 2:
                    o.addButton(play);
                    o.addButton(regenerate);
                    break;
                case 3:
                    o.addButton(create);
                    break;
                case 4:
                    overlays.remove(o);
                    o = new Overlay(0, windHeight-FR.getH(), windWidth, FR.getH());
                    o.addButton(FR);
                    o.addButton(ENG);
                    overlays.add(o);
                    break;
            }
        }
    }
    
    @Override
    public void update(){
        super.update();
        if(!TutoManager.Instance.hasDone(TutoManager.TutoStep.GM_NDD)){
            menu.getStart().lock();
            menu.getCreate().lock();
            regenerate.setHidden(true);
        }
        else{
            //menu.getStart().unlock();
            menu.getCreate().unlock();
            regenerate.setHidden(game == null || game.ended);
        }
        if(game == null || game.ended)
            play.setText(Text.FIGHT, fonts.get("normalL"));
        else
            play.setText(Text.CONTINUE, fonts.get("normalL"));
    }
    
    @Override
    public void render(){
        super.render();
        Overlay o = overlays.get(0);
        o.drawText(RvB.windWidth-(int)(20*ref), RvB.windHeight-(int)(20*ref), RvB.version, RvB.fonts.get("normal"), "bottomRight");
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