package managers;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;
import managers.TextManager.Text;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import static rvb.RvB.unite;
import static rvb.RvB.windHeight;
import static rvb.RvB.windWidth;
import rvb.Shootable;
import towers.Tower;
import towers.Upgrade;
import ui.Button;

public final class TutoManager {
    
    public static int popupW = (int)(400*ref), popupH = (int)(225*ref);
    private static int tutoStepId = 0;
    
    public enum TutoStep{
        // FIRST GAME
        WLCM_RND(windWidth/2, windHeight/2, popupW, popupH, Text.WLCM_RND, null, null, true),
        WLCM_RND2(popupW/2+4*unite, popupH/2+unite, popupW, popupH, Text.WLCM_RND2, "topLeft", null, true),
        WLCM_RND3(popupW/2+6*unite, popupH/2+unite, popupW, popupH, Text.WLCM_RND3, "topLeft", null, true),
        WLCM_RND4(popupW/2+unite, windHeight/2+2*unite, popupW, popupH, Text.WLCM_RND4, "topLeft", null, true),
        WLCM_RND5(windWidth/2, windHeight/2, popupW, popupH, Text.WLCM_RND5, null, __ -> {
            game.disableAllButtons();
            game.OvMain.getButtons().get(0).lock();
            game.OvMain.getButtons().get(4).lock();
            game.OvMain.getButtons().get(1).lock();
            game.OvShop.getButtons().get(game.OvShop.getButtons().size()-1).enable();
            game.showTile(4, 4, true);
        }, false),
        RZTCH_PLCD(popupW/2+(int)(300*ref), windHeight-popupH/2-(int)(72*ref), popupW, popupH, Text.RZTCH_PLCD, "bottomLeft", null, true),
        RZTCH_PLCD2(windWidth/2, windHeight/2, popupW, popupH, Text.RZTCH_PLCD2, null, __ -> {
            game.disableAllButtons();
            game.OvShop.getButtons().get(0).unlock();
            game.OvShop.getButtons().get(game.OvShop.getButtons().size()-1).lock();
            game.showTile(4, 4, false);
        }, false),
        TWR_BGHT(windWidth/2, windHeight/2, popupW, popupH, Text.TWR_BGHT, null, null, false),
        TWR_PLCD(0, 0, 0, 0, null, null, __ -> {
            Tower bt = (Tower)game.towers.get(0);
            for(Upgrade up : bt.getUpgrades())
                up.button.lock();
            game.OvMain.getButtons().get(0).unlock();
        }, true),
        TWR_PLCD2(windWidth-popupW/2-(int)(240*ref), popupH/2+(int)(72*ref), popupW, popupH, Text.TWR_PLCD, "topRight", null, true),
        TWR_PLCD3(windWidth-popupW/2-(int)(400*ref), popupH/2+(int)(72*ref), popupW, popupH, Text.TWR_PLCD2, "topRight", null, false),
        FRST_WV(windWidth/2, windHeight/2, popupW, popupH, Text.FRST_WV, null, __ -> {
            game.disableAllButtons();
            game.OvMain.getButtons().get(0).lock();
            game.selectTower(null);
            Tower bt = (Tower)game.towers.get(0);
            bt.getUpgrades().get(1).button.unlock();
        }, false),
        PWR_PGRDD(windWidth/2, windHeight/2, popupW, popupH, Text.PWR_PGRDD, null, null, true),
        PWR_PGRDD2(windWidth/2, windHeight/2, popupW, popupH, Text.PWR_PGRDD2, null, __ -> {
            game.OvMain.getButtons().get(0).unlock();
            Tower bt = (Tower)game.towers.get(0);
            for(Upgrade up : bt.getUpgrades())
                up.button.unlock();
            game.selectTower(bt);
        }, false),
        SCND_WV(0, 0, 0, 0, null, null, __ -> {
            game.selectTower(null);
        }, true),
        SCND_WV2(popupW/2+(int)(260*ref), windHeight-popupH/2-(int)(70*ref), popupW, popupH, Text.SCND_WV, "bottomLeft", __ -> {
            game.selectTower((Tower)game.towers.get(0));
        }, true), // Montrer qu'on peut déplacer raztech
        SCND_WV3(windWidth-popupW/2-(int)(200*ref), windHeight-popupH/2-(int)(70*ref), popupW, popupH, Text.SCND_WV2, "bottomRight", null, true), // Changer le focus
        SCND_WV4(popupW/2+(int)(230*ref), windHeight-popupH/2-(int)(70*ref), popupW, popupH, Text.SCND_WV3, "bottomLeft", __ -> {
            game.OvShop.getButtons().get(game.OvShop.getButtons().size()-1).unlock();
        }, true), // Vendre
        SCND_WV5(popupW/2+(int)(380*ref), windHeight-popupH/2-(int)(70*ref), popupW, popupH, Text.SCND_WV4, "bottomLeft", __ -> {
            game.OvShop.getButtons().get(game.OvShop.getButtons().size()-1).unlock();
        }, false), // Réparer
        THRD_WV(windWidth/2, windHeight/2, popupW, popupH, Text.THRD_WV, null, null, false), // Select un ennemi
        FRTH_WV(windWidth/2, windHeight/2, popupW, popupH, Text.FRTH_WV, null, null, true), // Pause
        FRTH_WV2(windWidth/2, windHeight/2, popupW, popupH, Text.FRTH_WV2, null, __ -> {
            game.OvMain.getButtons().get(4).unlock();
        }, true), // TAB
        FRTH_WV3(windWidth-popupW/2-(int)(550*ref), popupH/2+(int)(72*ref), popupW, popupH, Text.FRTH_WV3, "topRight", null, false), // Help
        LVL_P(windWidth/2-popupW/2-(int)(150*ref), windHeight/2-popupH/2-(int)(50*ref), popupW, popupH, Text.LVL_P, null, null, true), // Lvl up
        LVL_P2(windWidth/2, windHeight/2+(int)(300*ref), popupW, popupH, Text.LVL_P2, null, null, false),
        GM_NDD(0, 0, 0, 0, null, null, __ -> {
            game.OvMain.getButtons().get(1).unlock();
        }, true),
        GM_NDD2(windWidth/2+(int)(600*ref), windHeight/2-(int)(40*ref), popupW, popupH, Text.GM_NDD, "topLeft", null, true), // Download de map
        GM_NDD3(windWidth-popupW/2-(int)(40*ref), popupH/2+(int)(72*ref), popupW, popupH, Text.GM_NDD2, "topRight", null, false), // Download de map
        
        
        // CREATION reste à faire creation
        WLCM_CRTN(windWidth/2, windHeight/2, popupW, popupH, Text.WLCM_CRTN, null, null, true),
        WLCM_CRTN2(windWidth/2+popupW/2+(int)(100*ref), popupH/2+(int)(70*ref), popupW, popupH, Text.WLCM_CRTN2, "topLeft", null, true),
        WLCM_CRTN3(windWidth/2, windHeight/2, popupW, popupH, Text.WLCM_CRTN3, null, null, true),
        WLCM_CRTN4(windWidth/2, popupH/2+(int)(70*ref), popupW, popupH, Text.WLCM_CRTN4, "topLeft", null, true),
        WLCM_CRTN5(windWidth/2, popupH/2+(int)(70*ref), popupW, popupH, Text.WLCM_CRTN5, "topRight", null, true),
        WLCM_CRTN6(windWidth-popupW/2-(int)(100*ref), popupH/2+(int)(70*ref), popupW, popupH, Text.WLCM_CRTN6, "topRight", null, false);
        
        private final int id;
        private final Text text;
        private final int x, y, width, height;
        private final boolean hasNextPage;
        private final Consumer<Object> callback;
        private final String pointPos;
        
        TutoStep(int x, int y, int w, int h, Text t, String pp, Consumer<Object> cb, boolean hasNP){
            id = tutoStepId++;
            this.x = x;
            this.y = y;
            width = w;
            height = h;
            text = t;
            pointPos = pp;
            callback = cb;
            hasNextPage = hasNP;
        }
        
        @JsonValue
        public int getId(){
            return id;
        }
        
        public static TutoStep valueOf(int id){
            for(TutoStep ts : TutoStep.values())
                if(ts.id == id)
                    return ts;
            return null;
        }
    }
    
    public static TutoManager Instance;
    
    private TutoStep[] stepsDoneTab;
    private ArrayList<TutoStep> stepsDone = null;
    public static boolean onTuto = false;
    
    public TutoManager(){
        stepsDone = new ArrayList<>();
    }
    
    public static void initialize(){
        if(Instance == null)
            Instance = new TutoManager();
    }
    
    public void showTutoIfNotDone(TutoStep ts){
        if(PopupManager.Instance.onPopupTuto() || stepsDone.contains(ts) || ts == null)
            return;
        if(ts.width == 0 && ts.height == 0){
            stepsDone.add(ts);
            ts.callback.accept(null);
            showTutoIfNotDone(TutoStep.valueOf(ts.id+1));
            return;
        }
        onTuto = true;
        PopupManager.Instance.popupTuto(ts.x, ts.y, ts.width, ts.height, ts.text, ts.pointPos, __ -> {
            stepsDone.add(ts);
            if(ts.callback != null)
                ts.callback.accept(null);
            if(ts.hasNextPage)
                showTutoIfNotDone(TutoStep.valueOf(ts.id+1));
        });
    }
    
    public boolean hasDone(TutoStep ts){
        return stepsDone.contains(ts);
    }
    
    public void setupSteps(String steps) throws IOException{
        if(steps == null || steps.isEmpty())
            return;
        ObjectMapper mapper = new ObjectMapper();
        stepsDoneTab = mapper.readValue(steps, TutoStep[].class);
        boolean hasDoneGameTuto = false;
        for(TutoStep ts : stepsDoneTab)
            if(ts == TutoStep.GM_NDD)
                hasDoneGameTuto = true;
        for(TutoStep ts : stepsDoneTab){
            if(!hasDoneGameTuto && game != null && ts.callback != null)
                ts.callback.accept(null);
            stepsDone.add(ts);
        }
    }
    
    public void addStepDone(TutoStep stepDone){
        stepsDone.add(stepDone);
    }
    
    public void completeAllTuto(){
        clearStepsDone();
        for(TutoStep ts : TutoStep.values()){
            addStepDone(ts);
        }
        if(game != null){
            for(Button b : game.OvMain.getButtons())
                b.unlock();
            for(Shootable tower : game.towers){
                Tower t = (Tower) tower;
                for(Upgrade up : t.getUpgrades())
                    up.button.unlock();
            }   
        }
    }
    
    public void clearStepsDone(){
        /*ArrayList<TutoStep> tsList = new ArrayList<>();
        for(TutoStep ts : TutoStep.values())
            if(ts.id < 30)
                tsList.add(ts);
        stepsDone = tsList;*/
        stepsDone.clear();
    }
    
    public String getStepsJSON(){
        String steps = "[";
        for(int i = 0 ; i < stepsDone.size() ; i++){
            steps += ""+stepsDone.get(i).id+""+(i < stepsDone.size()-1 ? "," : "");
        }
        steps += "]";
        return steps;
    }
}
