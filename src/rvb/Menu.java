package rvb;

import managers.TextManager;
import ui.Button;
import managers.PopupManager;
import managers.TextManager.Text;
import static rvb.RvB.*;
import ui.Overlay;


public class Menu {
    
    private Button start, random, regenerate, create, modify, option, exit, FR, ENG;
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
        
        start = new Button(windWidth/2, windHeight/6, width, height, null, colors.get("green_dark"));
        start.disable();
        start.setBG(textures.get("disabled"));
        start.setFunction(__ -> {
            if(adventureGame == null || adventureGame.ended || adventureGame.waveNumber == 1)
                adventureGame = new Game("1", Difficulty.MEDIUM);

            game = adventureGame;
            switchStateTo(State.GAME);
        });
        
        random = new Button(windWidth/2, windHeight/6, width, height, colors.get("green_semidark"), colors.get("green_dark"));
        random.setFunction(__ -> {
            if(randomGame == null){
                PopupManager.Instance.chooseDifficulty("random");
                // Then it does newRandomMap(difficulty)
            }
            else{
                game = randomGame;
                switchStateTo(State.GAME);
            }
        });
        regenerate = new Button(random.getX(), random.getY()+random.getH()/2+(int)(30*ref), (int)(120*ref), (int)(28*ref), colors.get("green"), colors.get("green_semidark"));
        regenerate.setFunction(__ -> {
            PopupManager.Instance.chooseDifficulty("random");
            // Then it does newRandomMap(difficulty)
        });
        
        create = new Button(windWidth/2, windHeight/6, width, height, colors.get("green_semidark"), colors.get("green_dark"));
        create.setFunction(__ -> {
            if(createEmptyMap()){
                if(createdGame == null){
                    creation = new Creation();
                    switchStateTo(State.CREATION);
                }
                else{
                    game = createdGame;
                    switchStateTo(State.GAME);
                }
            }
            else{
                PopupManager.Instance.popup(Text.MISSING_FILE_LEVELS.getLines());
            }
        });
        modify = new Button(create.getX(), create.getY()+create.getH()/2+(int)(30*ref), (int)(120*ref), (int)(28*ref), colors.get("green"), colors.get("green_semidark"));
        modify.setFunction(__ -> {
            creation = new Creation();
            switchStateTo(State.CREATION);
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
                    overlays[i].addButton(random);
                    overlays[i].addButton(regenerate);
                    break;
                case 3:
                    overlays[i].addButton(create);
                    overlays[i].addButton(modify);
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
        if(randomGame == null)
            regenerate.setHidden(true);
        else
            regenerate.setHidden(false);
        if(createdGame == null)
            modify.setHidden(true);
        else
            modify.setHidden(false);
    }
    
    private void render(){
        drawFilledRectangle(0, 0, windWidth, windHeight, null, 1, textures.get("grass"));
        for(Overlay o : overlays){
            o.render();
        }
        start.drawText(0, 0, Text.ADVENTURE.getText(), fonts.get("normalL"));
        if(randomGame == null)
            random.drawText(0, 0, Text.RANDOM_MAP.getText(), fonts.get("normalL"));
        else{
            random.drawText(0, 0, Text.CONTINUE.getText(), fonts.get("normalL"));
            regenerate.drawText(0, 0, Text.REGENERATE.getText(), fonts.get("normal"));
        }  
        if(createdGame == null)
            create.drawText(0, 0, Text.CREATE_MAP.getText(), fonts.get("normalL"));
        else{
            create.drawText(0, 0, Text.CONTINUE.getText(), fonts.get("normalL"));
            modify.drawText(0, 0, Text.MODIFY.getText(), fonts.get("normal"));
        }
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
        return random;
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