package Buffs;

import managers.TextManager.Text;
import static rvb.RvB.game;
import rvb.Shootable;
import towers.RangeTower;
import towers.Tower;

public class UpRangeTower extends Buff{
    
    public UpRangeTower(){
        super("upRangeTower", Text.BUFF_UP_RANGE_TOWER, Text.BUFF_UP_RANGE_TOWER_DESC, 3);
    }
    
    @Override
    public void pick(){
        super.pick();
        float value = 0.05f;
        RangeTower rt = null;
        for(Shootable t : game.towers){
            if(t.name == Text.TOWER_RANGE){
                rt = (RangeTower) t;
                break;
            }
        }
        if(rt != null){
            rt.power += value;
            rt.size += rt.growth;
            for(Tower t : rt.towers)
                t.bonusRange += value;
        }
    }
}
