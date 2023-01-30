package towers;

import managers.SoundManager;
import org.newdawn.slick.opengl.Texture;
import rvb.RvB;
import ui.Button;
import static rvb.RvB.ref;
import static rvb.RvB.game;

public class Upgrade {
    
    public Texture icon;
    public String name;
    public float price;
    public int maxClick;
    public int nbNumberToRound;
    private String addOrMultiplicate;
    public float value, addOrMultiplicateValue, multiplicatePrice;
    private int x = 0, y = 0;
    public Button button;
    
    public Upgrade(String name, float value, float addOrMultiplicateValue, String addOrMultiplicate, float price, float multiplicatePrice, int maxClick){
        this.name = name;
        switch(name){
            case "Range":
                nbNumberToRound = 0;
                icon = RvB.textures.get("rangeIcon");
                break;
            case "Power":
                nbNumberToRound = 0;
                icon = RvB.textures.get("powerIcon");
                break;
            case "Attack speed":
                nbNumberToRound = 1;
                icon = RvB.textures.get("attackSpeedIcon");
                break;
            case "Bullet speed":
                nbNumberToRound = 0;
                icon = RvB.textures.get("bulletSpeedIcon");
                break;  
            case "Explode radius":
                nbNumberToRound = 0;
                icon = RvB.textures.get("explodeRadiusIcon");
                break;  
        }
        setValue(value);
        this.addOrMultiplicateValue = addOrMultiplicateValue;
        this.addOrMultiplicate = addOrMultiplicate;
        this.price = price;
        this.multiplicatePrice = multiplicatePrice;
        this.maxClick = maxClick;
    }
    
    public void initPosAndButton(int x, int y, Tower tower){
        this.x = x;
        this.y = y;
        button = new Button(x+(int)(40*ref), y-(int)(9*ref), (int)(32*ref), (int)(32*ref), RvB.colors.get("green_semidark"), RvB.colors.get("green_dark"), maxClick);
        button.setBG(RvB.textures.get("plus"));
        button.setFunction(__ -> {
            switch(name){
                case "Range":
                    tower.range = (int) setNewValue();
                    break;
                case "Power":
                    tower.power = (int) setNewValue();
                    break;
                case "Attack speed":
                    tower.shootRate = setNewValue();
                    break;
                case "Bullet speed":
                    tower.bulletSpeed = (int) setNewValue();
                    break;
                case "Explode radius":
                    tower.explodeRadius = (int) setNewValue();
                    break;
            }
            tower.size += tower.growth;
            game.money -= price;
            tower.totalMoneySpent += price;
            increasePrice();
        });
        if(maxClick <= 0)
            button.setHidden(true);
        button.setClickSound(SoundManager.Instance.getClip("upgrade"), SoundManager.Volume.SEMI_HIGH);
    }
    
    
    public void render(){
        if(game.money < price && button.isEnabled()){
            button.disable();
            RvB.setCursor(RvB.Cursor.DEFAULT);
        }    
        else if(game.money >= price)
            button.enable();
        button.update();
        
        String up, nextUp, upPrice = (int)Math.floor(price)+"";
        up = nbNumberToRound == 0 ? (int)getValue()+"" : getValue()+"";
        nextUp = nbNumberToRound == 0 ? (int)getIncreasedValue()+"" : getIncreasedValue()+"";
        
        if(!button.isHidden()){
            RvB.drawFilledRectangle(x-(int)(40*ref), y-(int)(9*ref), (int)(32*ref), (int)(32*ref), icon, 0, 1);
            if(button.isHovered())
                RvB.drawString(x, y-(int)(9*ref), nextUp, RvB.fonts.get("bonus"));
            else   
                RvB.drawString(x, y-(int)(9*ref), up, RvB.fonts.get("normal"));
            if(game.money >= (int)Math.floor(price)){
                RvB.drawString(x-(int)(10*ref), y+(int)(18*ref), upPrice, RvB.fonts.get("canBuy"));
                RvB.drawFilledRectangle(x+(int)(24*ref), y+(int)(18*ref), (int)(28*ref), (int)(28*ref), RvB.textures.get("coins"), 0, 1);
            } 
            else{
                RvB.drawString(x-(int)(10*ref), y+(int)(18*ref), upPrice, RvB.fonts.get("cantBuy"));
                RvB.drawFilledRectangle(x+(int)(24*ref), y+(int)(18*ref), (int)(28*ref), (int)(28*ref), RvB.textures.get("coinsCantBuy"), 0, 1);
            }
        }
        else{
            RvB.drawFilledRectangle(x-(int)(20*ref), y, (int)(32*ref), (int)(32*ref), icon, 0, 1);   
            RvB.drawString(x+(int)(20*ref), y, up, RvB.fonts.get("normal"));   
        }
    }
    
    public void increasePrice(){
        price = price * multiplicatePrice;
    }
    
    public float setNewValue(){
        switch(addOrMultiplicate){
            case "+":
                setValue(value + addOrMultiplicateValue);
                break;
            case "*":
                setValue(value * addOrMultiplicateValue);
                break;
        }
        return value;
    }
    
    public float getValue(){
        return value;
    }
    
    public void setValue(float v){
        value = (float) (Math.round(Math.pow(10, nbNumberToRound)*v)/Math.pow(10, nbNumberToRound));
    }
    
    public float getIncreasedValue(){
        switch(addOrMultiplicate){
            case "+":
                return (float) (Math.ceil(Math.pow(10, nbNumberToRound)*(value+addOrMultiplicateValue))/Math.pow(10, nbNumberToRound));
            case "*":
                return (float) (Math.ceil(Math.pow(10, nbNumberToRound)*((value+value*addOrMultiplicateValue)-value))/Math.pow(10, nbNumberToRound));
        };
        return 0;
    }
}
