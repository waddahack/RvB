package Buffs;

import managers.TextManager.Text;
import static rvb.RvB.game;

public class Slow extends Buff{
    
    public Slow(){
        super("slow", Text.BUFF_SLOW, Text.BUFF_SLOW_DESC, 3);
    }
    
    @Override
    public void pick(){
        super.pick();
        game.raztech.addSlowAmount(0.1f);
    }
}
