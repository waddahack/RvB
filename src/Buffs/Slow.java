package Buffs;

import managers.TextManager.Text;

public class Slow extends Buff{
    
    public Slow(){
        super("slow", Text.BUFF_SLOW, "Raztech désormais slow", 1);
    }
    
    @Override
    public void pick(){
        super.pick();
        System.out.println("buff slow");
    }
}
