package towser;

import ui.Button;
import java.util.ArrayList;
import static towser.Towser.ref;
import static towser.Towser.windHeight;
import static towser.Towser.windWidth;
import ui.Overlay;


public class Menu {
    
    private Button start, random, create, option, exit;
    private Overlay[] overlays = new Overlay[5];
    
    public Menu(){
        int width = (int) (200*ref);
        int height = (int) (50*ref);
        start = new Button(windWidth/2, windHeight/6, width, height, Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        start.setDisabled(true);
        random = new Button(windWidth/2, windHeight/6, width, height, Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        create = new Button(windWidth/2, windHeight/6, width, height, Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        option = new Button(windWidth/2, windHeight/6, width, height, Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        exit = new Button(windWidth/2, windHeight/6, width, height, Towser.colors.get("green_semidark"), Towser.colors.get("green_dark"));
        for(int i = 0 ; i < 5 ; i++){
            overlays[i] = new Overlay(0, i*windHeight/6, windWidth, windHeight/6);
            switch(i){
                case 0:
                    overlays[i].addButton(start);
                    break;
                case 1:
                    overlays[i].addButton(random);
                    break;
                case 2:
                    overlays[i].addButton(create);
                    break;
                case 3:
                    overlays[i].addButton(option);
                    break;
                case 4:
                    overlays[i].addButton(exit);
                    break;
            }
        }
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
    
    public void render(){
        Towser.drawFilledRectangle(0, 0, windWidth, windHeight, null, 1, Towser.textures.get("grass"));
        for(Overlay o : overlays){
            o.render();
        }
        start.drawText(0, 0, "    Adventure\n(inc... not soon)", Towser.fonts.get("normalL"));
        random.drawText(0, 0, "Random map", Towser.fonts.get("normalL"));
        create.drawText(0, 0, "Create map", Towser.fonts.get("normalL"));
        option.drawText(0, 0, "Options", Towser.fonts.get("normalL"));
        exit.drawText(0, 0, "Exit", Towser.fonts.get("normalL"));
    }
}
