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
    private Tower tower;
    
    public Upgrade(Tower tower, String name, float value, float addOrMultiplicateValue, String addOrMultiplicate, float price, float multiplicatePrice, int maxClick){
        this.tower = tower;
        this.name = name;
        switch(name){
            case "Range":
                nbNumberToRound = 0;
                icon = RvB.textures.get("rangeIcon");
                break;
            case "Power":
                nbNumberToRound = 1;
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
                    tower.power = setNewValue();
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
        button.setOnHoverFunction(__ -> {
            RvB.drawString(x, y-(int)(9*ref), nbNumberToRound == 0 ? (int)getIncreasedValueWithBonus()+"" : getIncreasedValueWithBonus()+"", RvB.fonts.get("bonus"));
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
        if(tower.isPlaced)
            button.update();
        
        String up, upPrice = (int)Math.floor(price)+"";
        up = nbNumberToRound == 0 ? (int)getValueWithBonus()+"" : getValueWithBonus()+"";
        
        if(!button.isHidden()){
            RvB.drawFilledRectangle(x-(int)(40*ref), y-(int)(9*ref), (int)(32*ref), (int)(32*ref), icon, 0, 1);
            if(!button.isHovered()) 
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
    
    public float getValueWithBonus(){
        switch(name){
            case "Range":
                return (int)(Math.round(value*(1+tower.bonusRange)*Math.pow(10, nbNumberToRound))/Math.pow(10, nbNumberToRound));
            case "Power":
                return (float)(Math.round(value*(1+tower.bonusPower)*Math.pow(10, nbNumberToRound))/Math.pow(10, nbNumberToRound));
            case "Attack speed":
                return (float)(Math.round(value*(1+tower.bonusShootRate)*Math.pow(10, nbNumberToRound))/Math.pow(10, nbNumberToRound));
            case "Bullet speed":
                return (float)(Math.round(value*(1+tower.bonusBulletSpeed)*Math.pow(10, nbNumberToRound))/Math.pow(10, nbNumberToRound));
            case "Explode radius":
                return (int)(Math.round(value*(1+tower.bonusExplodeRadius)*Math.pow(10, nbNumberToRound))/Math.pow(10, nbNumberToRound));
        }
        return 0;
    }
    
    public void setValue(float v){
        value = (float) (Math.round(Math.pow(10, nbNumberToRound)*v)/Math.pow(10, nbNumberToRound));
    }
    
    public float getIncreasedValueWithBonus(){
        float bonus = 0;
        switch(name){
            case "Range":
                bonus = tower.bonusRange;
                break;
            case "Power":
                bonus = tower.bonusPower;
                break;
            case "Attack speed":
                bonus = tower.bonusShootRate;
                break;
            case "Bullet speed":
                bonus = tower.bonusBulletSpeed;
                break;
            case "Explode radius":
                bonus = tower.bonusExplodeRadius;
                break;
        }
        switch(addOrMultiplicate){
            case "+":
                return (float) (Math.round(Math.pow(10, nbNumberToRound)*((value+addOrMultiplicateValue)*(1+bonus)))/Math.pow(10, nbNumberToRound));
            case "*":
                return (float) (Math.round(Math.pow(10, nbNumberToRound)*((value*addOrMultiplicateValue)*(1+bonus)))/Math.pow(10, nbNumberToRound));
        };
        return 0;
    }
}
