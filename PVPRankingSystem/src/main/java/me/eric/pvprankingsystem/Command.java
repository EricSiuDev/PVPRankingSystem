package me.eric.pvprankingsystem;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class Command implements CommandExecutor {
    private Main plugin;
    private final CooldownManager cooldownManager = new CooldownManager();

    public Command(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            final String UUID = p.getUniqueId().toString();
            final String cmdstr = command.toString();
            //rank command
            if (label.equalsIgnoreCase("rank")) {
                if (args.length == 0) {
                    Long timeLeft = (System.currentTimeMillis() - cooldownManager.getCooldown(p.getUniqueId().toString()));
                    if (TimeUnit.MILLISECONDS.toSeconds(timeLeft) >= CooldownManager.DEFAULT_COOLDOWN) {
                        plugin.query(UUID);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.sqlloadingmsg));
                        cooldownManager.setCooldown(p.getUniqueId().toString(), System.currentTimeMillis());
                        new BukkitRunnable() {
                            public void run() {
                                try {
                                    int kills = plugin.kills.get(UUID);
                                    int deaths = plugin.deaths.get(UUID);
                                    int killstreak = plugin.killstreak.get(UUID);
                                    int highestrank = plugin.highestrank.get(UUID);
                                    String prank = plugin.rank.get(UUID);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.rankmsg) + ChatColor.translateAlternateColorCodes('&', prank));
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.killsmsg) + kills);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.deathsmsg) + deaths);
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.killstreakmsg) + killstreak);
                                } catch(Exception e){
                                    e.printStackTrace();
                                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',plugin.errormsg));
                                }
                            }
                        }.runTaskLater(plugin, 30L);
                        return true;
                    } else{
                        p.sendMessage(ChatColor.RED.toString()+ (CooldownManager.DEFAULT_COOLDOWN - TimeUnit.MILLISECONDS.toSeconds(timeLeft)) + " 秒後才能再次使用查詢指令");
                    }
                }
            }
        }
        return false;
    }
}