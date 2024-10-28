package hu.kxtsoo.fungun.manager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager {
    private final Map<Player, Long> effectCooldowns = new HashMap<>();
    private final Map<Player, Long> abilityCooldowns = new HashMap<>();
    private final Map<Player, Long> lastCooldownMessage = new HashMap<>();
    private final JavaPlugin plugin;

    public CooldownManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startEffectCooldown(Player player, int seconds) {
        long cooldownEnd = System.currentTimeMillis() + (seconds * 1000);
        effectCooldowns.put(player, cooldownEnd);
        lastCooldownMessage.put(player, 0L);
        SchedulerManager.runAsyncLater(() -> clearEffectCooldown(player), seconds * 20L);
    }

    public void startAbilityCooldown(Player player, int seconds) {
        long cooldownEnd = System.currentTimeMillis() + (seconds * 1000);
        abilityCooldowns.put(player, cooldownEnd);
        lastCooldownMessage.put(player, 0L);
        SchedulerManager.runAsyncLater(() -> clearAbilityCooldown(player), seconds * 20L);
    }

    public boolean hasEffectCooldown(Player player) {
        return effectCooldowns.containsKey(player) && effectCooldowns.get(player) > System.currentTimeMillis();
    }

    public boolean hasAbilityCooldown(Player player) {
        return abilityCooldowns.containsKey(player) && abilityCooldowns.get(player) > System.currentTimeMillis();
    }

    public int getRemainingEffectCooldown(Player player) {
        if (hasEffectCooldown(player)) {
            long remainingMillis = effectCooldowns.get(player) - System.currentTimeMillis();
            int remainingSeconds = (int) (remainingMillis / 1000);
            if (shouldSendCooldownMessage(player, remainingSeconds)) {
                return remainingSeconds;
            }
        }
        return 0;
    }

    public int getRemainingAbilityCooldown(Player player) {
        if (hasAbilityCooldown(player)) {
            long remainingMillis = abilityCooldowns.get(player) - System.currentTimeMillis();
            int remainingSeconds = (int) (remainingMillis / 1000);
            if (shouldSendCooldownMessage(player, remainingSeconds)) {
                return remainingSeconds;
            }
        }
        return 0;
    }

    public void clearEffectCooldown(Player player) {
        effectCooldowns.remove(player);
    }

    public void clearAbilityCooldown(Player player) {
        abilityCooldowns.remove(player);
    }

    private boolean shouldSendCooldownMessage(Player player, int remainingSeconds) {
        long currentTime = System.currentTimeMillis() / 1000;
        if (!lastCooldownMessage.containsKey(player) || currentTime - lastCooldownMessage.get(player) >= 1) {
            lastCooldownMessage.put(player, currentTime);
            return true;
        }
        return false;
    }
}