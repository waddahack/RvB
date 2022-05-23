package towser;

import ui.Button;
import managers.PopupManager;
import managers.SoundManager;
import static towser.Towser.*;
import ui.Overlay;


public class Menu {
    
    private Button start, random, regenerate, create, option, exit;
    private Overlay[] overlays = new Overlay[4];
    
    public Menu(){
        int width = (int) (250*ref);
        int height = (int) (60*ref);
        
        option = new Button(windWidth-(int)(120*ref), (int)(50*ref), (int)(32*ref), (int)(32*ref), Towser.textures.get("optionIcon"), null, colors.get("green_dark"));
        exit = new Button(windWidth-(int)(50*ref), (int)(50*ref), (int)(32*ref), (int)(32*ref), Towser.textures.get("exitIcon"), null, colors.get("green_dark"));
        
        start = new Button(windWidth/2, windHeight/6, width, height, null, colors.get("green_dark"));
        start.setDisabled(true);
        start.setBG(textures.get("disabled"));
        
        random = new Button(windWidth/2, windHeight/6, width, height, colors.get("green_semidark"), colors.get("green_dark"));
        regenerate = new Button(random.getX(), random.getY()+random.getH()/2+(int)(30*ref), (int)(120*ref), (int)(28*ref), colors.get("green"), colors.get("green_semidark"));
        
        create = new Button(windWidth/2, windHeight/6, width, height, colors.get("green_semidark"), colors.get("green_dark"));
        
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
                    break;
            }
        }
    }
    
    public void update(){
        render();
        checkInput();
        PopupManager.Instance.update();
    }
    
    private void render(){
        drawFilledRectangle(0, 0, windWidth, windHeight, null, 1, textures.get("grass"));
        for(Overlay o : overlays){
            o.render();
        }
        overlays[0].drawImage(overlays[0].getW()/2-(int)(50*ref), overlays[0].getH()/2, (int)(100*ref), (int)(100*ref), Towser.textures.get("heart"));
        start.drawText(0, 0, "    Adventure\n(inc... not soon)", fonts.get("normalL"));
        if(randomGame == null)
            random.drawText(0, 0, "New random map", fonts.get("normalL"));
        else{
            random.drawText(0, 0, "Continue", fonts.get("normalL"));
            regenerate.drawText(0, 0, "Regenerate", fonts.get("normal"));
        }  
        create.drawText(0, 0, "Create", fonts.get("normalL"));
    }
    
    private void checkInput(){
        if(randomGame == null)
            regenerate.setHidden(true);
        else
            regenerate.setHidden(false);
        
        if(start.isClicked(0)){
            if(adventureGame == null || adventureGame.ended || adventureGame.waveNumber == 1)
                adventureGame = new Game("1");
            else
                SoundManager.Instance.unpauseAll();

            game = adventureGame;
            switchStateTo(State.GAME);
        }  
        if(random.isClicked(0)){
            if(randomGame == null){
                PopupManager.Instance.chooseDifficulty();
                // Then it does newRandomMap(difficulty)
            }
            else{
                SoundManager.Instance.unpauseAll();
                game = randomGame;
                switchStateTo(State.GAME);
            }   
        }
        if(regenerate.isClicked(0)){
            PopupManager.Instance.chooseDifficulty();
            // Then it does newRandomMap(difficulty)
        }
        if(create.isClicked(0)){
            if(generateEmptyMap()){
                creation = new Creation();
                switchStateTo(State.CREATION);
            }
            else{
                PopupManager.Instance.popup("\n        Hmmm... Very strange...\n    Create a directory \"levels\" in\nthe same location than your game.");
            }
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