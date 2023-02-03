package Buffs;

import managers.TextManager.Text;
import static rvb.RvB.game;

public class Upgrade extends Buff{
    
    public Upgrade(){
        super("upgrade", Text.BUFF_UPGRADE, Text.BUFF_UPGRADE_DESC, 5);
    }
    
    @Override
    public void pick(){
        super.pick();
        game.raztech.bonusRange += 0.08f;
        game.raztech.bonusPower += 0.1f;
        game.raztech.bonusShootRate += 0.08f;
    }
}
