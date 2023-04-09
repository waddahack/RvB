package Buffs;

import managers.TextManager.Text;
import static rvb.RvB.game;
import rvb.Shootable;
import towers.PowerTower;
import towers.Tower;

public class UpPowerTower extends Buff{
    
    public UpPowerTower(){
        super("UpPowerTower", Text.BUFF_UP_POWER_TOWER, Text.BUFF_UP_POWER_TOWER_DESC, 3);
    }
    
    @Override
    public void pick(){
        super.pick();
        float value = 0.05f;
        PowerTower pt = null;
        for(Shootable t : game.towers){
            if(t.name == Text.TOWER_POWER){
                pt = (PowerTower) t;
                break;
            }
        }
        if(pt != null){
            pt.power += value;
            pt.size += pt.growth;
            for(Tower t : pt.towers)
                t.bonusPower += value;
        }
    }
}