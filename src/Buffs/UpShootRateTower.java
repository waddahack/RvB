package Buffs;

import managers.TextManager.Text;
import static rvb.RvB.game;
import rvb.Shootable;
import towers.ShootRateTower;
import towers.Tower;

public class UpShootRateTower extends Buff{
    
    public UpShootRateTower(){
        super("upShootRateTower", Text.BUFF_UP_SHOOTRATE_TOWER, Text.BUFF_UP_SHOOTRATE_TOWER_DESC, 3);
    }
    
    @Override
    public void pick(){
        super.pick();
        float value = 0.05f;
        ShootRateTower srt = null;
        for(Shootable t : game.towers){
            if(t.name == Text.TOWER_SHOOTRATE){
                srt = (ShootRateTower) t;
                break;
            }
        }
        if(srt != null){
            srt.power += value;
            srt.size += srt.growth;
            for(Tower t : srt.towers)
                t.bonusShootRate += value;
        }
    }
}
