package Buffs;

import managers.TextManager.Text;
import rvb.RvB;

public class GetRangeTower extends Buff{
    
    public GetRangeTower(){
        super("GetRangeTower", Text.TOWER_RANGE, Text.BUFF_RANGE_TOWER_DESC, 1);
        this.logo = RvB.textures.get("rangeTower");
    }
    
    @Override
    public void pick(){
        super.pick();
        RvB.game.buffs.add(new UpRangeTower());
        RvB.game.createTower(102);
    }
}
