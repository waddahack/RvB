package Buffs;

import managers.TextManager.Text;

public class Upgrade extends Buff{
    
    public Upgrade(){
        super("upgrade", Text.BUFF_UPGRADE, Text.BUFF_UPGRADE_DESC, -1);
    }
    
    @Override
    public void pick(){
        super.pick();
        System.out.println("buff upgrade");
    }
}
