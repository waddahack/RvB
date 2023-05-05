package managers;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;
import managers.TextManager.Text;
import static rvb.RvB.game;
import static rvb.RvB.ref;
import static rvb.RvB.windHeight;
import static rvb.RvB.windWidth;
import towers.Tower;
import towers.Upgrade;

public final class TutoManager {
    
    public static int popupW = (int)(400*ref), popupH = (int)(225*ref);
    private static int tutoStepId = 0;
    
    public enum TutoStep{
        // FIRST GAME
        WLCM_RND(windWidth/2, windHeight/2, popupW, popupH, Text.WLCM_RND, null, null, true),
        WLCM_RND2(windWidth/2, windHeight/2, popupW, popupH, Text.WLCM_RND2, null, null, true),
        WLCM_RND3(windWidth/2, windHeight/2, popupW, popupH, Text.WLCM_RND3, null, null, true),
        WLCM_RND4(windWidth/2, windHeight/2, popupW, popupH, Text.WLCM_RND4, null, __ -> {
            game.disableAllButtons();
            game.getOverlays().get(1).getButtons().get(0).lock();
            game.getOverlays().get(1).getButtons().get(4).lock();
            game.getOverlays().get(1).getButtons().get(1).lock();
            game.getOverlays().get(0).getButtons().get(game.getOverlays().get(0).getButtons().size()-1).enable();
        }, false),
        RZTCH_PLCD(popupW/2+(int)(300*ref), windHeight-popupH/2-(int)(72*ref), popupW, popupH, Text.RZTCH_PLCD, new int[]{(int)(-10*ref), popupH+(int)(10*ref)}, null, true),
        RZTCH_PLCD2(windWidth/2, windHeight/2, popupW, popupH, Text.RZTCH_PLCD2, null, __ -> {
            game.disableAllButtons();
            game.getOverlays().get(0).getButtons().get(0).unlock();
            game.getOverlays().get(0).getButtons().get(game.getOverlays().get(0).getButtons().size()-1).lock();
        }, false),
        TWR_PLCD(0, 0, 0, 0, null, null, __ -> {
            Tower bt = (Tower)game.towers.get(0);
            for(Upgrade up : bt.getUpgrades())
                up.button.lock();
            game.getOverlays().get(1).getButtons().get(0).unlock();
        }, true),
        TWR_PLCD2(windWidth-popupW/2-(int)(240*ref), popupH/2+(int)(72*ref), popupW, popupH, Text.TWR_PLCD, new int[]{popupW+(int)(10*ref), (int)(-10*ref)}, null, true),
        TWR_PLCD3(windWidth-popupW/2-(int)(400*ref), popupH/2+(int)(72*ref), popupW, popupH, Text.TWR_PLCD2, new int[]{popupW+(int)(10*ref), (int)(-10*ref)}, null, false),
        FRST_WV(windWidth/2, windHeight/2, popupW, popupH, Text.FRST_WV, null, __ -> {
            game.disableAllButtons();
            game.getOverlays().get(1).getButtons().get(0).lock();
            game.selectTower(null);
            Tower bt = (Tower)game.towers.get(0);
            bt.getUpgrades().get(1).button.unlock();
        }, false),
        PWR_PGRDD(windWidth/2, windHeight/2, popupW, popupH, Text.PWR_PGRDD, null, null, true),
        PWR_PGRDD2(windWidth/2, windHeight/2, popupW, popupH, Text.PWR_PGRDD2, null, __ -> {
            game.getOverlays().get(1).getButtons().get(0).unlock();
            Tower bt = (Tower)game.towers.get(0);
            for(Upgrade up : bt.getUpgrades())
                up.button.unlock();
            game.selectTower(bt);
        }, false),
        SCND_WV(0, 0, 0, 0, null, null, __ -> {
            game.selectTower(null);
        }, true),
        SCND_WV2(popupW/2+(int)(260*ref), windHeight-popupH/2-(int)(70*ref), popupW, popupH, Text.SCND_WV, new int[]{(int)(-10*ref), popupH+(int)(10*ref)}, __ -> {
            game.selectTower((Tower)game.towers.get(0));
        }, true), // Montrer qu'on peut déplacer raztech
        SCND_WV3(windWidth-popupW/2-(int)(200*ref), windHeight-popupH/2-(int)(70*ref), popupW, popupH, Text.SCND_WV2, new int[]{popupW+(int)(10*ref), popupH+(int)(10*ref)}, null, true), // Changer le focus
        SCND_WV4(popupW/2+(int)(230*ref), windHeight-popupH/2-(int)(70*ref), popupW, popupH, Text.SCND_WV3, new int[]{(int)(-10*ref), popupH+(int)(10*ref)}, __ -> {
            game.getOverlays().get(0).getButtons().get(game.getOverlays().get(0).getButtons().size()-1).unlock();
        }, false), // Vendre
        THRD_WV(windWidth/2, windHeight/2, popupW, popupH, Text.THRD_WV, null, null, false), // Select un ennemi
        FRTH_WV(windWidth/2, windHeight/2, popupW, popupH, Text.FRTH_WV, null, null, true), // Pause
        FRTH_WV2(windWidth/2, windHeight/2, popupW, popupH, Text.FRTH_WV2, null, __ -> {
            game.getOverlays().get(1).getButtons().get(4).unlock();
        }, true), // TAB
        FRTH_WV3(windWidth-popupW/2-(int)(550*ref), popupH/2+(int)(72*ref), popupW, popupH, Text.FRTH_WV3, new int[]{popupW+(int)(10*ref), (int)(-10*ref)}, null, false), // Help
        LVL_P(windWidth/2-popupW/2-(int)(150*ref), windHeight/2-popupH/2-(int)(50*ref), popupW, popupH, Text.LVL_P, null, null, true), // Lvl up
        LVL_P2(windWidth/2, windHeight/2+(int)(300*ref), popupW, popupH, Text.LVL_P2, null, null, false),
        GM_NDD(0, 0, 0, 0, null, null, __ -> {
            game.getOverlays().get(1).getButtons().get(1).unlock();
        }, true),
        GM_NDD2(windWidth/2+(int)(550*ref), windHeight/2-(int)(40*ref), popupW, popupH, Text.GM_NDD, new int[]{(int)(-10*ref), (int)(-10*ref)}, null, true), // Download de map
        GM_NDD3(windWidth-popupW/2-(int)(40*ref), popupH/2+(int)(72*ref), popupW, popupH, Text.GM_NDD2, new int[]{popupW+(int)(10*ref), (int)(-10*ref)}, null, false); // Download de map
        
        
        // CREATION reste à faire LVL_P et creation
        // ...
        
        private final int id;
        private final Text text;
        private final int x, y, width, height;
        private final boolean hasNextPage;
        private final Consumer<Object> callback;
        private final int[] pointPos;
        
        TutoStep(int x, int y, int w, int h, Text t, int[] pp, Consumer<Object> cb, boolean hasNP){
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
    
    public void clearStepsDone(){
        /*ArrayList<TutoStep> tsList = new ArrayList<>();
        for(TutoStep ts : TutoStep.values())
            if(ts.id < 19)
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
