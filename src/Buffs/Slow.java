package Buffs;

import managers.TextManager.Text;

public class Slow extends Buff{
    
    public Slow(){
        super("slow", Text.BUFF_SLOW, Text.BUFF_SLOW_DESC, 1);
    }
    
    @Override
    public void pick(){
        super.pick();
        System.out.println("buff slow");
    }
}
