package hu.kitsoo.gfungun.events;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class CooldownListener {
    private final Map<Player, Long> cooldowns = new HashMap<>();
    private final JavaPlugin plugin;

    public CooldownListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean hasCooldown(Player player) {
        return cooldowns.containsKey(player);
    }

    public void startCooldown(Player player, int seconds) {
        long cooldownEnd = System.currentTimeMillis() + (seconds * 1000);
        cooldowns.put(player, cooldownEnd);

        new BukkitRunnable() {
            @Override
            public void run() {
                clearCooldown(player);
            }
        }.runTaskLaterAsynchronously(plugin, seconds * 20L);
    }

        public double getRemainingCooldown(Player player) {
            if (cooldowns.containsKey(player)) {
                long remaining = cooldowns.get(player) - System.currentTimeMillis();
                return Math.max(remaining, 0) / 1000.0;
            }
            return 0;
        }

    public void clearCooldown(Player player) {
        cooldowns.remove(player);
    }
}