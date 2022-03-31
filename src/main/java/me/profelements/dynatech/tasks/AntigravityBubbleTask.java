package me.profelements.dynatech.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * @author Fhoz https://github.com/Fhoz
*/
public class AntigravityBubbleTask {
    static Set<Location> bubbleLocations = new HashSet<>();
    static Set<UUID> bubbledPlayers = new HashSet<>();
    static Set<UUID> playerLog = new HashSet<>();
    public static void run() {
        for (Location bubble : bubbleLocations) {
            Collection<Entity> bubbledEntities = bubble.getWorld().getNearbyEntities(bubble, 25, 25, 25);
            for (Entity entity : bubbledEntities) {
                if (entity instanceof Player) {
                    Player p = (Player) entity;
                    UUID uuid = p.getUniqueId();
                    playerLog.add(uuid);
                    if (!p.getAllowFlight()) {
                        bubbledPlayers.add(uuid);
                        p.setAllowFlight(true);
                    }
                }
            }
        }
        if (bubbledPlayers != null) {
            for (UUID uuid : bubbledPlayers) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && !playerLog.contains(uuid)) {    
                    p.setAllowFlight(false);
                    p.setFlying(false);
                    p.setFallDistance(0.0f);
                    bubbledPlayers.remove(uuid);
                }
            }
        }
        
        playerLog.clear();
    }
 

    public static void addBubble(Location blockLocation) {
        bubbleLocations.add(blockLocation);
    }
    public static void removeBubble(Location blockLocation) {
        bubbleLocations.remove(blockLocation);
    }
}