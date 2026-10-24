package f2b4.Death_Swap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.*;

public class SwapManager {

    private final F2B4_DeathSwap plugin;

    public SwapManager(F2B4_DeathSwap plugin) {
        this.plugin = plugin;
    }

    public void performSwap() {
        List<Player> validPlayers = new ArrayList<>();
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!isSafe(p)) {
                validPlayers.add(p);
            }
        }
        
        if (validPlayers.size() < 2) {
            Bukkit.broadcastMessage("§c[F2B4] คนไม่พอสำหรับ Death Swap! (ต้องมี 2 คนขึ้นไป และไม่อยู่ในเขตปลอดภัย)");
            return;
        }

        Collections.shuffle(validPlayers);
        Map<Player, Location> locMap = new HashMap<>();
        for (Player p : validPlayers) {
            locMap.put(p, p.getLocation());
        }

        for (int i = 0; i < validPlayers.size(); i++) {
            Player p1 = validPlayers.get(i);
            Player p2 = validPlayers.get((i + 1) % validPlayers.size());
            Location target = locMap.get(p2);

            p1.teleport(target);
            
            p1.setNoDamageTicks(0); 
            p1.setInvulnerable(false); 
            
            p1.sendMessage("§6§lSWAP! §eสลับตำแหน่งกับ §f" + p2.getName());
            p1.playSound(p1.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
        Bukkit.broadcastMessage("§a[F2B4] สลับตำแหน่งทุกคนเรียบร้อย! ระวังตัวด้วย!");
    }

    private boolean isSafe(Player p) {
        if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) {
            return true;
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            return inSafeRegion(p);
        }
        return false;
    }

    private boolean inSafeRegion(Player p) {
        try {
            com.sk89q.worldguard.protection.regions.RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
            com.sk89q.worldguard.protection.regions.RegionQuery query = container.createQuery();
            com.sk89q.worldguard.protection.ApplicableRegionSet set = query.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(p.getLocation()));
            
            List<String> safeRegions = plugin.getConfig().getStringList("safe-regions");
            for (com.sk89q.worldguard.protection.regions.ProtectedRegion region : set) {
                if (safeRegions.contains(region.getId().toLowerCase())) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
}