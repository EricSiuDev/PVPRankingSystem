package me.eric.pvprankingsystem;

import java.util.HashMap;

public class CooldownManager {

    private final HashMap<String, Long> cooldowns = new HashMap<String,Long>();

    public static final long DEFAULT_COOLDOWN = 5L;

    public void setCooldown(String UUID, long time){
        if(time < 1L) {
            cooldowns.remove(UUID);
        } else {
            cooldowns.put(UUID, time);
        }
    }

    public long getCooldown(String UUID){
        if(cooldowns.containsKey(UUID)){
            return cooldowns.get(UUID);
        } else {
            return 0L;
        }
    }
}
