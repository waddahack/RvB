package managers;

public class TextManager {
    
    public enum Text{
        // RANDOM TEXT
        MS(
            new String[]{"Vitesse de déplacement"},
            new String[]{"Move speed"}),
        HP(
            new String[]{"Point de vie"},
            new String[]{"Health point"}),
        DEFENDING(
            new String[]{"Je défends..."},
            new String[]{"Defending..."}),
        START_WAVE(
            new String[]{"C'est parti !"},
            new String[]{"Go !"}),
        GAME_OVER(
            new String[]{"Bazoo a été plus fort...", "Il a gagné une bataille, mais pas la guerre."},
            new String[]{"Bazoo have been stronger...", "He's won a battle, but not the war."}),
        WAVE(
            new String[]{"Vague"},
            new String[]{"Wave"}),
        MENU(
            new String[]{"Retourner au menu"},
            new String[]{"Return to menu"}),
        ALL_ENEMIES(
            new String[]{"Tous les ennemies"},
            new String[]{"All enemies"}),
        SELL(
            new String[]{"Vendre"},
            new String[]{"Sell"}),
        NO_OPTIONS(
            new String[]{"Bazoo a hacké le système !", "Les options sont indisponibles..."},
            new String[]{"Bazoo hacked the system !", "Options are unavailable..."}),
        SECRET_REVEAL(
            new String[]{"Bazoo est en fait le gentil ici.", "Raztech défend quelque chose qu'il lui a prit."},
            new String[]{"Bazoo is actually the real good guy here.", "Raztech is defending something he took from him."}),
        WHAT(
            new String[]{"Quoi ?"},
            new String[]{"What ?"}),
        
        
        // MENU
        ADVENTURE(
            new String[]{"    Aventure\n(indisponible)"},
            new String[]{"   Adventure\n(inc... not soon)"}),
        RANDOM_MAP(
            new String[]{"Combattre Bazoo"},
            new String[]{"Fight Bazoo"}),
        CREATE_MAP(
            new String[]{"Créer une map"},
            new String[]{"Create a map"}),
        CONTINUE(
            new String[]{"Continuer"},
            new String[]{"Continue"}),
        REGENERATE(
            new String[]{"Regénérer"},
            new String[]{"Regenerate"}),
        MODIFY(
            new String[]{"Modifier"},
            new String[]{"Modify"}),
        MISSING_FILE_LEVELS(
            new String[]{"Hmmm... Étrange...", "Créer un dossier \"levels\" dans", "le même endroit que ton jeu."},
            new String[]{"Hmmm... Very strange...", "Create a directory \"levels\" in", "the same location than your game."}),
        
        // ENEMIES
        ENEMY_BASIC(
            new String[]{"Bazooldat"},
            new String[]{"Bazooldier"}),
        ENEMY_FAST(
            new String[]{"Quazoo"},
            new String[]{"Quazoo"}),
        ENEMY_TRICKY(
            new String[]{"Groupe de Bazooldats"},
            new String[]{"Bazooldier group"}),
        ENEMY_STRONG(
            new String[]{"Bazank"},
            new String[]{"Bazank"}),
        ENEMY_FLYING(
            new String[]{"Bazooptère"},
            new String[]{"Bazoopter"}),
        ENEMY_BOSS(
            new String[]{"Bazoo"},
            new String[]{"Bazoo"}),
        
        // TOWERS
        TOWER_BASIC(
            new String[]{"Razanon"},
            new String[]{"Razannon"}),
        TOWER_CIRCLE(
            new String[]{"Razaillette"},
            new String[]{"Razingun"}),
        TOWER_BIG(
            new String[]{"Mortech"},
            new String[]{"Razkull"}),
        TOWER_FLAME(
            new String[]{"Flametech"},
            new String[]{"Flametech"}),
        FOCUS_SWITCH(
            new String[]{"Premier", "Dernier", "Plus fort", "Plus faible", "Plus proche"},
            new String[]{"First", "Last", "Strongest", "Weakest", "Closest"}),
        FOCUS(
            new String[]{"Vise le"},
            new String[]{"Focus the"}),
        
        // POPUPS
        SELECT_DIFF(
            new String[]{"Choisis une difficulté"},
            new String[]{"Select a difficulty"}),
        CANCEL(
            new String[]{"Échape pour annuler"},
            new String[]{"Escape to cancel"}),
        EASY(
            new String[]{"Facile"},
            new String[]{"Easy"}),
        NORMAL(
            new String[]{"Normal"},
            new String[]{"Normal"}),
        HARD(
            new String[]{"Difficile"},
            new String[]{"Hard"}),
        BOSS_DEFEATED(
            new String[]{"Je reviendrai plus fort !", "Tu ne devrais pas plaisanter avec moi minable insecte !", "Ce n'est qu'une question de temps...", "Je vais libérer ma vrai puissance !", "Plus je souffre, plus je deviens puissant !"},
            new String[]{"I will be back much stronger !", "You should not mess with me filthy bug !", "It is just a matter of time...", "I will unleash my true power !", "The more I suffer, the more powerful I get !"}),
        BOSS_NOT_DEFEATED(
            new String[]{"Tu es faible.", "Je suis la fin, et j'arrive vite !", "Tu MOURRAS !", "Ça a toujours été une question de temps.", "Je n'ai pas fait attention, m'as-tu touché ?"},
            new String[]{"You are weak.", "I am the end, and I am coming fast !", "You shall DIE !", "It has always been a matter of time.", "I have not noticed, did you even hit me ?"}),
        BOSS_DEFEATED_ANSWER(
            new String[]{"Toujours debout !", "Où es-tu ?", "J'attend...", "Même pas peur !", "Looser !"},
            new String[]{"Still standing !", "Where're you ?", "I'm waiting...", "Not afraid !", "Looser !"}),
        BOSS_NOT_DEFEATED_ANSWER(
            new String[]{"Noooon !", "Toujours vivant !", "Ouch !", "Attention à toi", "Grrrrr..."},
            new String[]{"Noooo !", "Still alive !", "Ouch !", "Watch yourself", "Grrrrr..."});

        private String[] FR_text, ENG_text;
        
        private Text(String[] FR_text, String[] ENG_text){
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
        currentLanguage = "ENG";
    }

    public static void initialize(){
        if(Instance == null)
            Instance = new TextManager();
    }
    
    public void setLanguage(String l){
        currentLanguage = l;
    }
}
