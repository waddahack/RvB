package Buffs;

import managers.TextManager.Text;
import rvb.RvB;

public class XP extends Buff{
    
    public XP(){
        super("xp", Text.BUFF_XP, Text.BUFF_XP_DESC, 4);
    }
    
    @Override
    public void pick(){
        super.pick();
        RvB.game.raztech.addBonusXP(0.25f);
    }
}
