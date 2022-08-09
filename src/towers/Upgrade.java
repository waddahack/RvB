package towers;

import org.newdawn.slick.opengl.Texture;
import towser.Towser;

public class Upgrade {
    
    public Texture icon;
    public String name;
    public float price;
    public int maxClick;
    public int nbNumberToRound;
    private String addOrMultiplicate;
    private float value, addOrMultiplicateValue, multiplicatePrice;
    
    public Upgrade(String name, float value, float addOrMultiplicateValue, String addOrMultiplicate, float price, float multiplicatePrice, int maxClick){
        this.name = name;
        switch(name){
            case "Range":
                nbNumberToRound = 0;
                icon = Towser.textures.get("rangeIcon");
                break;
            case "Power":
                nbNumberToRound = 0;
                icon = Towser.textures.get("powerIcon");
                break;
            case "Attack speed":
                nbNumberToRound = 1;
                icon = Towser.textures.get("attackSpeedIcon");
                break;
            case "Bullet speed":
                nbNumberToRound = 0;
                icon = Towser.textures.get("bulletSpeedIcon");
                break;  
            case "Explode radius":
                nbNumberToRound = 0;
                icon = Towser.textures.get("explodeRadiusIcon");
                break;  
        }
        setValue(value);
        this.addOrMultiplicateValue = addOrMultiplicateValue;
        this.addOrMultiplicate = addOrMultiplicate;
        this.price = price;
        this.multiplicatePrice = multiplicatePrice;
        this.maxClick = maxClick;
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
    
    private void setValue(float v){
        value = (float) (Math.ceil(Math.pow(10, nbNumberToRound)*v)/Math.pow(10, nbNumberToRound));
    }
    
    public float getIncreaseValue(){
        switch(addOrMultiplicate){
            case "+":
                return (float) (Math.ceil(Math.pow(10, nbNumberToRound)*addOrMultiplicateValue)/Math.pow(10, nbNumberToRound));
            case "*":
                return (float) (Math.ceil(Math.pow(10, nbNumberToRound)*((value*addOrMultiplicateValue)-value))/Math.pow(10, nbNumberToRound));
        };
        return 0;
    }
}
