package Buffs;

import managers.TextManager.Text;
import rvb.RvB;

public class OS extends Buff{
    
    public OS(){
        super("os", Text.BUFF_OS, Text.BUFF_OS_DESC, 3);
    }
    
    @Override
    public void pick(){
        super.pick();
        RvB.game.raztech.addChanceToKill(0.01f);
        // A FAIRE LE FONCTIONNEMENT
    }
}
