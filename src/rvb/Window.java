package rvb;

import java.util.ArrayList;
import ui.Button;
import ui.Overlay;

public abstract class Window {
    
    protected ArrayList<Overlay> overlays;
    
    public Window(){
        overlays = new ArrayList<>();
    }
    
    public void update(){
        render();
    }
    
    protected void render(){
        RvB.drawFilledRectangle(0, 0, RvB.windWidth, RvB.windHeight, null, 1, RvB.textures.get("grass"));
        for(Overlay o : overlays){
            o.render();
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
    }
}
