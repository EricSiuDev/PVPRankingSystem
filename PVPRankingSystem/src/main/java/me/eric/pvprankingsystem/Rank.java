package me.eric.pvprankingsystem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.UUID;

public class Rank {
    private Main plugin;

    public Rank(Main plugin) {
        this.plugin = plugin;
    }

    private int minkills;
    private double minkd;
    private boolean broadcast;

    public void calculateRank(final String UUID, final int kills, final int deaths, final int highestrank) {
        new BukkitRunnable() {
            public void run() {
                int maxrankpreprocess = plugin.maxrank;
                //Warning: preprocess will only work when ranks have ascending min-kills
                int preprocessminkills = plugin.getConfig().getInt("1"+".min-kills");
                if(maxrankpreprocess>1){
                    preprocessminkills = plugin.getConfig().getInt(Integer.toString(maxrankpreprocess/2)+".min-kills");
                }
                if(kills<preprocessminkills){
                    maxrankpreprocess = maxrankpreprocess/2;
                }
                //Warning: preprocess will only work when ranks have ascending min-kills
                for (int i = maxrankpreprocess; i >= 1; i--) {
                    minkills = plugin.getConfig().getInt(i + ".min-kills");
                    minkd = Double.parseDouble(plugin.getConfig().getString(i + ".min-kd"));
                    broadcast = plugin.getConfig().getBoolean(i + ".broadcast");
                    double kd = 1.0;
                    if (deaths != 0) {
                        kd = ((double) kills / (double) deaths);
                    }
                    if (kills >= minkills && Double.compare(kd, minkd) >= 0){
                        plugin.rank.put(UUID, plugin.getConfig().getString(i + ".rank-name"));
                        if (i > highestrank) {
                            new BukkitRunnable() {
                                public void run() {
                                    try {
                                        plugin.stmt = plugin.connection.prepareStatement("UPDATE PLAYERRANKDATA SET HIGHESTRANK = HIGHESTRANK + 1 WHERE UUID = ?");
                                        plugin.stmt.setString(1, UUID);
                                        plugin.stmt.executeUpdate();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.runTaskAsynchronously(plugin);
                            Player p = Bukkit.getPlayer(java.util.UUID.fromString(UUID));
                            String prankuptitle = ChatColor.translateAlternateColorCodes('&', plugin.rankuptitle.replace("%player%",p.getDisplayName()).replace("%rank%",plugin.rank.get(UUID)));
                            String prankupsubtitle = ChatColor.translateAlternateColorCodes('&', plugin.rankupsubtitle.replace("%player%",p.getDisplayName()).replace("%rank%",plugin.rank.get(UUID)));
                            p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
                            p.sendTitle(prankuptitle,prankupsubtitle, 10, 70, 20);
                            if (broadcast == true) {
                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.rankupbroadcastmsg.replace("%player%", p.getDisplayName()).replace("%rank%", plugin.rank.get(UUID))));
                            }
                        }
                        break;
                    }
                }
            }
        }.runTaskLater(plugin, 10L);
    }
}