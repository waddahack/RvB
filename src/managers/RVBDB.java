package managers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.logging.Level;
import java.util.logging.Logger;
import rvb.RvB;

public class RVBDB {
    
    public static RVBDB Instance;
    
    private static Connection connection;
    private static boolean updateTables = false;
    
    public RVBDB(){
        initDB();
    }
    
    public static void initialize(){
        if(Instance == null)
            Instance = new RVBDB();
    }
    
    private static void initDB(){
        String userHome = System.getProperty("user.home");
        File appFolder = new File(userHome, "RvB");
        if (!appFolder.exists()) {
            appFolder.mkdir();
        }
        // Création de la connection
        try {
            String dataBaseURL = "jdbc:derby:" + appFolder.getAbsolutePath() + File.separator + "RvBDB;create=true";
            connection = DriverManager.getConnection(dataBaseURL);
            System.out.println("Connected to the data base");
        } catch (SQLException ex) {
            Logger.getLogger(RVBDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Regarde si les tables existent, si non, les créer
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM properties WHERE 1 = 0");
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            // si la requête s'est exécutée sans erreur, la table existe
        } catch (SQLException e) {
            // si une exception est levée, la table n'existe pas
            try {
                createTables();
            } catch (SQLException ex) {
                Logger.getLogger(RVBDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // Fetch les données et les initialises
        try {
            Statement stmt = connection.createStatement();
            ResultSet prop = getProperties(stmt);
            if(prop.getBoolean("ingame")){
                Statement stmt2 = connection.createStatement();
                ResultSet gameSet = stmt2.executeQuery("SELECT * FROM game FETCH FIRST 1 ROWS ONLY");
                gameSet.next();
                RvB.initPropertiesAndGame(true, prop.getBoolean("cheatson"), prop.getString("language"), prop.getString("stats"), prop.getString("tutosteps"), gameSet.getString("path"), gameSet.getString("holes"), gameSet.getString("difficulty"), gameSet.getInt("life"), gameSet.getInt("money"), gameSet.getInt("wavenumber"), gameSet.getString("towers"), gameSet.getString("buffs"), gameSet.getString("buffsused"));
                gameSet.close();
                stmt2.close();
            }
            else
                RvB.initPropertiesAndGame(false, prop.getBoolean("cheatson"), prop.getString("language"), prop.getString("stats"), prop.getString("tutosteps"), "", "","", 0, 0, 0, null, null, null);
            
            prop.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(RVBDB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RVBDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void createTables() throws SQLException{
        Statement createProperties = connection.createStatement();
        String sql = "CREATE TABLE properties (\n" +
                    "    ingame BOOLEAN DEFAULT false,\n" +
                    "    cheatson BOOLEAN DEFAULT false,\n" +
                    "    language VARCHAR(5) DEFAULT 'ENG',\n" +
                    "    stats VARCHAR(2000) DEFAULT null,\n" +
                    "    tutosteps VARCHAR(5000) DEFAULT ''\n" +
                    ")";
        createProperties.executeUpdate(sql);
        createProperties.close();

        Statement createGame = connection.createStatement();
        sql = "CREATE TABLE game (\n" +
            "    wavenumber INTEGER DEFAULT 1,\n" +
            "    money INTEGER DEFAULT 0,\n" +
            "    life INTEGER DEFAULT 0,\n" +
            "    path VARCHAR(4600) DEFAULT '',\n" +
            "    holes VARCHAR(5000) DEFAULT '',\n" +
            "    difficulty VARCHAR(20) DEFAULT '',\n" +
            "    towers VARCHAR(10000) DEFAULT null,\n" +
            "    buffs VARCHAR(5000) DEFAULT '',\n" +
            "    buffsused VARCHAR(2000) DEFAULT ''\n" +
            ")";
        createGame.executeUpdate(sql);
        createGame.close();
    }
    
    private static ResultSet getProperties(Statement stmt) throws SQLException{
        ResultSet prop = stmt.executeQuery("SELECT * FROM properties FETCH FIRST 1 ROWS ONLY");
        if(!prop.next()){
            Statement stmt2 = connection.createStatement();
            stmt2.executeUpdate("INSERT INTO properties (ingame, cheatson, language, stats, tutosteps) VALUES (false, false, 'ENG', null, '')");
            stmt2.close();
            prop = stmt.executeQuery("SELECT * FROM properties FETCH FIRST 1 ROWS ONLY");
            prop.next();
        }
        else if(updateTables){
            prop.close();
            updateTables();
            prop = stmt.executeQuery("SELECT * FROM properties FETCH FIRST 1 ROWS ONLY");
            prop.next();
        }
        return prop;
    }
    
    private static void updateTables() throws SQLException{
        // SI BESOIN DE CHANGER LA STRUCT DE LA DB, IL FAUT : 1) prendre les infos de la DB 2) delete la DB 3) créer une DB vierge 4) remplir les datas dispo
        Statement stmt = connection.createStatement();

        Statement propStmt = connection.createStatement();
        propStmt.executeUpdate("ALTER TABLE properties ADD COLUMN tutosteps VARCHAR(5000) DEFAULT ''");
        propStmt.close();
        Statement propStmt2 = connection.createStatement();
        propStmt2.executeUpdate("UPDATE properties SET ingame = false");
        propStmt2.close();
    }
    
    public static boolean saveGame(int waveNumber, int money, int life, String pathString, String holesString, String difficulty, String towers, String buffs, String buffsUsed) throws SQLException{
        Statement stmt1 = connection.createStatement();
        ResultSet gameSet = stmt1.executeQuery("SELECT * FROM game FETCH FIRST 1 ROWS ONLY");
        String sql;
        if(!gameSet.next())
            sql = "INSERT INTO game (wavenumber, money, life, path, holes, difficulty, towers, buffs, buffsused) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        else
            sql = "UPDATE game SET wavenumber = ?, money = ?, life = ?, path = ?, holes = ?, difficulty = ?, towers = ?, buffs = ?, buffsused = ?";
        gameSet.close();
        stmt1.close();
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, waveNumber);
        pstmt.setInt(2, money);
        pstmt.setInt(3, life);
        pstmt.setString(4, pathString);
        pstmt.setString(5, holesString);
        pstmt.setString(6, difficulty);
        pstmt.setString(7, towers);
        pstmt.setString(8, buffs);
        pstmt.setString(9, buffsUsed);
        int rows = pstmt.executeUpdate();
        pstmt.close();
        if(rows > 0)
            return true;
        return false;
    }
    
    public static boolean updateInGame(boolean inGame) throws SQLException{
        PreparedStatement pstmt = connection.prepareStatement("UPDATE properties SET ingame = ?");
        pstmt.setBoolean(1, inGame);
        int rows = pstmt.executeUpdate();
        pstmt.close();
        if(rows > 0)
            return true;
        return false;
    }
    
    public static boolean updateStats(String stats) throws SQLException{
        PreparedStatement pstmt = connection.prepareStatement("UPDATE properties SET stats = ?");
        pstmt.setString(1, stats);
        int rows = pstmt.executeUpdate();
        pstmt.close();
        if(rows > 0)
            return true;
        return false;
    }
    
    public static boolean updateTutoSteps(String steps) throws SQLException{
        PreparedStatement pstmt = connection.prepareStatement("UPDATE properties SET tutosteps = ?");
        pstmt.setString(1, steps);
        int rows = pstmt.executeUpdate();
        pstmt.close();
        if(rows > 0)
            return true;
        return false;
    }
    
    public static boolean updateLanguage(String language) throws SQLException{
        PreparedStatement pstmt = connection.prepareStatement("UPDATE properties SET language = ?");
        pstmt.setString(1, language);
        int rows = pstmt.executeUpdate();
        pstmt.close();
        if(rows > 0)
            return true;
        return false;
    }
    
    public static void exitDB(){
        try {
            String shutdownURL = "jdbc:derby:;shutdown=true"; 
            connection = DriverManager.getConnection(shutdownURL);
            connection.close();
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("XJ015")) {
                System.out.println("Data base closed");
            } else {
                Logger.getLogger(RVBDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
