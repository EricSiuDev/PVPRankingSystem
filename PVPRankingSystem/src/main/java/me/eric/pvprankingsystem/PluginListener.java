package me.eric.pvprankingsystem;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PluginListener implements Listener {
    private Main plugin;

    public PluginListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String player = event.getUniqueId().toString();
        try {
            PreparedStatement statement = plugin.connection.prepareStatement("INSERT INTO playerrankdata (UUID,KILLS,DEATHS,KILLSTREAK,HIGHESTRANK) VALUES (?,0, 0, 0,1) ON DUPLICATE KEY UPDATE UUID = UUID");
            statement.setString(1, player);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        plugin.query(player);
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            final String killer = event.getEntity().getKiller().getUniqueId().toString();
            final String victim = event.getEntity().getPlayer().getUniqueId().toString();
            new BukkitRunnable() {
                public void run() {
                    try {
                        PreparedStatement statement = plugin.connection.prepareStatement("UPDATE playerrankdata SET KILLS = KILLS+1, KILLSTREAK = KILLSTREAK+1 WHERE UUID=?");
                        statement.setString(1, killer);
                        statement.executeUpdate();
                        PreparedStatement statement2 = plugin.connection.prepareStatement("UPDATE playerrankdata SET DEATHS = DEATHS+1, KILLSTREAK = 0 WHERE UUID=?");
                        statement2.setString(1, victim);
                        statement2.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(plugin);
            plugin.query(killer);
            plugin.query(victim);
        }
    }

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event){
        String UUID = event.getPlayer().getUniqueId().toString();
        String rankname = plugin.rank.get(UUID);
        event.setFormat(ChatColor.translateAlternateColorCodes('&',"<"+rankname+"&r>")+ event.getFormat());
    }
}
