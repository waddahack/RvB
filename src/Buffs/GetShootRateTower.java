package Buffs;

import managers.TextManager.Text;
import rvb.RvB;

public class GetShootRateTower extends Buff{
    
    public GetShootRateTower(){
        super("getShootRateTower", Text.TOWER_SHOOTRATE, Text.BUFF_SHOOTRATE_TOWER_DESC, 1);
        this.logo = RvB.textures.get("shootrateTower");
    }
    
    @Override
    public void pick(){
        super.pick();
        RvB.game.buffs.add(new UpShootRateTower());
        RvB.game.createTower(103);
    }
}
