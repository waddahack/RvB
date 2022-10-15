package rvb;

import ui.Button;
import managers.PopupManager;
import static rvb.RvB.*;
import ui.Overlay;


public class Menu {
    
    private Button start, random, regenerate, create, modify, option, exit;
    private Overlay[] overlays = new Overlay[4];
    
    public Menu(){
        int width = (int) (250*ref);
        int height = (int) (60*ref);
        
        option = new Button(windWidth-(int)(120*ref), (int)(50*ref), (int)(32*ref), (int)(32*ref), RvB.textures.get("optionIcon"), null, colors.get("green_dark"));
        exit = new Button(windWidth-(int)(50*ref), (int)(50*ref), (int)(32*ref), (int)(32*ref), RvB.textures.get("exitIcon"), null, colors.get("green_dark"));
        
        start = new Button(windWidth/2, windHeight/6, width, height, null, colors.get("green_dark"));
        start.setDisabled(true);
        start.setBG(textures.get("disabled"));
        
        random = new Button(windWidth/2, windHeight/6, width, height, colors.get("green_semidark"), colors.get("green_dark"));
        regenerate = new Button(random.getX(), random.getY()+random.getH()/2+(int)(30*ref), (int)(120*ref), (int)(28*ref), colors.get("green"), colors.get("green_semidark"));
        
        create = new Button(windWidth/2, windHeight/6, width, height, colors.get("green_semidark"), colors.get("green_dark"));
        modify = new Button(create.getX(), create.getY()+create.getH()/2+(int)(30*ref), (int)(120*ref), (int)(28*ref), colors.get("green"), colors.get("green_semidark"));
        
        for(int i = 0 ; i < 4 ; i++){
            overlays[i] = new Overlay(0, (i+1)*windHeight/6, windWidth, windHeight/6);
            switch(i){
                case 0:
                    overlays[i] = new Overlay(0, 0, windWidth, windHeight/3);
                    overlays[i].addButton(option);
                    overlays[i].addButton(exit);
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
            }
        }
    }
    
    public void update(){
        render();
        checkInput();
    }
    
    private void render(){
        drawFilledRectangle(0, 0, windWidth, windHeight, null, 1, textures.get("grass"));
        for(Overlay o : overlays){
            o.render();
        }
        overlays[0].drawImage(windWidth/4, (int)(100*ref), windWidth/2, (int) (windWidth/5f), RvB.textures.get("title"));
        start.drawText(0, 0, "    Adventure\n(inc... not soon)", fonts.get("normalL"));
        if(randomGame == null)
            random.drawText(0, 0, "Fight Bazoo", fonts.get("normalL"));
        else{
            random.drawText(0, 0, "Continue", fonts.get("normalL"));
            regenerate.drawText(0, 0, "Regenerate", fonts.get("normal"));
        }  
        if(createdGame == null)
            create.drawText(0, 0, "Create a map", fonts.get("normalL"));
        else{
            create.drawText(0, 0, "Continue", fonts.get("normalL"));
            modify.drawText(0, 0, "Modify", fonts.get("normal"));
        }
    }
    
    private void checkInput(){
        if(randomGame == null)
            regenerate.setHidden(true);
        else
            regenerate.setHidden(false);
        if(createdGame == null)
            modify.setHidden(true);
        else
            modify.setHidden(false);
        
        if(start.isClicked(0)){
            if(adventureGame == null || adventureGame.ended || adventureGame.waveNumber == 1)
                adventureGame = new Game("1", Difficulty.MEDIUM);

            game = adventureGame;
            switchStateTo(State.GAME);
        }  
        if(random.isClicked(0)){
            if(randomGame == null){
                PopupManager.Instance.chooseDifficulty("random");
                // Then it does newRandomMap(difficulty)
            }
            else{
                game = randomGame;
                switchStateTo(State.GAME);
            }   
        }
        if(regenerate.isClicked(0)){
            PopupManager.Instance.chooseDifficulty("random");
            // Then it does newRandomMap(difficulty)
        }
        if(create.isClicked(0)){
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
                PopupManager.Instance.popup("\n        Hmmm... Very strange...\n    Create a directory \"levels\" in\nthe same location than your game.");
            }
        }  
        if(modify.isClicked(0)){
            creation = new Creation();
            switchStateTo(State.CREATION);
        }
        if(exit.isClicked(0))
            switchStateTo(State.EXIT);
    }
    
    public void disableAllButtons(){
        for(Overlay o : overlays)
            for(Button b : o.getButtons())
                b.setDisabled(true);
    }
    
    public void enableAllButtons(){
        for(Overlay o : overlays)
            for(Button b : o.getButtons())
                b.setDisabled(false);
        start.setDisabled(true);
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