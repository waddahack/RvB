package Buffs;

import managers.TextManager.Text;
import rvb.RvB;

public class GetPowerTower extends Buff{
    
    public GetPowerTower(){
        super("getPowerTower", Text.TOWER_POWER, Text.BUFF_POWER_TOWER_DESC, 1);
        this.logo = RvB.textures.get("powerTower");
    }
    
    @Override
    public void pick(){
        super.pick();
        RvB.game.buffs.add(new UpPowerTower());
        RvB.game.createTower(101);
    }
}
