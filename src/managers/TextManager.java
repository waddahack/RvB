package managers;

public class TextManager {
    
    public enum Text{
        ADVENTURE(new String[]{"    Aventure\n(indisponible)"}, new String[]{"   Adventure\n(inc... not soon)"}),
        RANDOM_MAP(new String[]{"Combattre Bazoo"}, new String[]{"Fight Bazoo"}),
        CREATE_MAP(new String[]{"Créer une map"}, new String[]{"Create a map"}),
        CONTINUE(new String[]{"Continuer"}, new String[]{"Continue"}),
        REGENERATE(new String[]{"Regénérer"}, new String[]{"Regenerate"}),
        MODIFY(new String[]{"Modifier"}, new String[]{"Modify"}),
        MISSING_FILE_LEVELS(new String[]{"Hmmm... Étrange...", "Créer un dossier \"levels\" dans", "le même endroit que ton jeu."}, new String[]{"Hmmm... Very strange...", "Create a directory \"levels\" in", "the same location than your game."});
        
        private String[] FR_text, ENG_text;
        
        Text(String[] FR_text, String[] ENG_text){
            this.FR_text = FR_text;
            this.ENG_text = ENG_text;
        }
        
        public String getText(){
            if(currentLanguage.equals("FR"))
                return FR_text[0];
            return ENG_text[0];
        }
        
        public String[] getLines(){
            if(currentLanguage.equals("FR"))
                return FR_text;
            return ENG_text;
        }
    }
    
    public static TextManager Instance;
    
    private static String currentLanguage;
    
    public TextManager(){
        currentLanguage = "FR";
    }

    public static void initialize(){
        if(Instance == null)
            Instance = new TextManager();
    }
    
    public void setLanguage(String l){
        currentLanguage = l;
    }
}
