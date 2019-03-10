package me.eric.pvprankingsystem;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.HashMap;

//Setup
public class Main extends JavaPlugin{
    //MySQL
    public Connection connection;
    private String host, database, username, password;
    private int port;
    public PreparedStatement stmt,runnable;
    //Ranking
    public String rankmsg,killsmsg,deathsmsg,killstreakmsg,sqlloadingmsg,errormsg,rankuptitle,rankupsubtitle,rankupbroadcastmsg,topkillsplayer;
    public int maxrank,topkillskill;
    public HashMap<String, Integer> kills = new HashMap<String, Integer>();
    public HashMap<String, Integer> deaths = new HashMap<String, Integer>();
    public HashMap<String, Integer> killstreak = new HashMap<String, Integer>();
    public HashMap<String, Integer> highestrank = new HashMap<String, Integer>();
    public HashMap<String, String> rank = new HashMap<String, String>();

    //Config
    public File customConfigFile;
    FileConfiguration config = new YamlConfiguration();
    private FileConfiguration customConfig;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.createCustomConfig();
        // Load configuration
        try {
            config.load(new InputStreamReader(new FileInputStream(customConfigFile), Charset.forName("UTF-8")));
            // Save configuration
            Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(customConfigFile), Charset.forName("UTF-8")));
            fileWriter.write(config.saveToString());
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        rankmsg = this.getCustomConfig().getString("msg-rank");
        killsmsg = this.getCustomConfig().getString("msg-kills");
        deathsmsg = this.getCustomConfig().getString("msg-deaths");
        killstreakmsg = this.getCustomConfig().getString("msg-killstreak");
        maxrank = this.getConfig().getInt("max-rank");
        sqlloadingmsg = this.getCustomConfig().getString("msg-sqlloading");
        errormsg = this.getCustomConfig().getString("msg-error");
        rankuptitle = this.getCustomConfig().getString("msg-rankuptitle");
        rankupsubtitle = this.getCustomConfig().getString("msg-rankupsubtitle");
        rankupbroadcastmsg = this.getCustomConfig().getString("msg-rankupbroadcast");
        try {
            openConnection();
            Statement statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        createTable();
        Bukkit.getPluginManager().registerEvents(new PluginListener(this), this);
        this.getCommand("rank").setExecutor(new Command(this));
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Expansion(this).register();
            getLogger().info("PVPRankingSystem Placeholder has been register.");
        }
        getLogger().info("PVPRankingSystem plugin has been enabled.");
    }

        @Override
        public void onDisable () {
            try {
                if (connection != null && !connection.isClosed()) { //checking if connection isn't null to avoid receiving a nullpointer
                    connection.close(); //closing the connection field variable.
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            getLogger().info("PVPRankingSystem plugin has been disabled.");
        }

        //SQL related
        private void openConnection () {
            host = getConfig().getString("mysql-host");
            port = getConfig().getInt("mysql-port");
            database = getConfig().getString("mysql-database");
            username = getConfig().getString("mysql-username");
            password = getConfig().getString("mysql-password");
            try {
                if (connection != null && !connection.isClosed()) {
                    return;
                }
                synchronized (this) {
                    if (connection != null && !connection.isClosed()) {
                        return;
                    }
                    Class.forName("com.mysql.jdbc.Driver");
                    connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void createTable () {
            try {
                stmt = connection.prepareStatement("CREATE TABLE IF NOT EXISTS playerrankdata(UUID VARCHAR(100) PRIMARY KEY,KILLS INT,DEATHS INT,KILLSTREAK INT,HIGHESTRANK INT)");
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        BukkitRunnable r = new BukkitRunnable() {
            public void run() {
                try {
                    runnable.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };
        private void insertPreparedStatement ( final String sql, final String UUID){
            try {
                stmt = connection.prepareStatement(sql);
                stmt.setString(1, UUID);
                runnable = stmt;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            r.runTaskAsynchronously(this);
        }


        public void query ( final String UUID){
            final Rank rank = new Rank(this);
            new BukkitRunnable() {
                public void run() {
                    try {
                        stmt = connection.prepareStatement("SELECT * FROM playerrankdata WHERE UUID = ?");
                        stmt.setString(1, UUID);
                        ResultSet result = stmt.executeQuery();
                        if (result.next()) {
                            kills.put(UUID, result.getInt("KILLS"));
                            deaths.put(UUID, result.getInt("DEATHS"));
                            killstreak.put(UUID, result.getInt("KILLSTREAK"));
                            highestrank.put(UUID, result.getInt("HIGHESTRANK"));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(this);
            new BukkitRunnable() {
                public void run() {
                    rank.calculateRank(UUID, kills.get(UUID), deaths.get(UUID), highestrank.get(UUID));
                    ;
                }
            }.runTaskLater(this, 10L);
        }

//Function in test
/*    public void getTopKills(int topkillsposition){
        try{
            stmt = connection.prepareStatement("SELECT * FROM playerrankdata ORDER BY KILLS DESC");
            ResultSet result = stmt.executeQuery();
            int i = 1;
            while(result.next()){
                if(i==topkillsposition){
                    topkillsplayer=result.getString("UUID");
                    topkillskill=result.getInt("kills");
                    break;
                }else{
                i++;
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }*/

        //CustomConfig
        private FileConfiguration getCustomConfig () {
            return this.customConfig;
        }

        private void createCustomConfig(){
            this.customConfigFile = new File(getDataFolder(), "Language.yml");
            if (!this.customConfigFile.exists()) {
                this.customConfigFile.getParentFile().mkdirs();
                saveResource("Language.yml", false);
            }
            this.customConfig = new YamlConfiguration();
            try {
                this.customConfig.load(this.customConfigFile);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
    }