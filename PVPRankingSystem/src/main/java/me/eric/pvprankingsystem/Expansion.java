package me.eric.pvprankingsystem;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Expansion extends PlaceholderExpansion {
    private Main plugin;
    public Expansion(Main plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean canRegister(){
        return true;
    }
    @Override
    public boolean register(){
        if(!canRegister()){
            return false;
        }
        if(plugin == null){
            return false;
        }
        return PlaceholderAPI.registerPlaceholderHook(getIdentifier(),this);
    }
    @Override
    public String getAuthor(){
        return "Eric Siu";
    }
    @Override
    public String getIdentifier(){
        return "pvprankingsystem";
    }
    @Override
    public String getRequiredPlugin(){
        return "PVPRankingSystem";
    }
    @Override
    public String getVersion(){
        return "1.0.0";
    }
    @Override
    public String onPlaceholderRequest(Player player,String identifier){
        if(identifier.equals("prefix")){
            return plugin.rank.get(player.getUniqueId().toString());
        }
        return null;
    }
}
