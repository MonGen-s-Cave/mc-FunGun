package hu.kxtsoo.fungun.abilities;

import dev.dejvokep.boostedyaml.YamlDocument;
import hu.kxtsoo.fungun.manager.CooldownManager;
import hu.kxtsoo.fungun.manager.SchedulerManager;
import hu.kxtsoo.fungun.util.ConfigUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class MountFlightAbility extends AbilityHandler {

    private final JavaPlugin plugin;
    private final int cooldown;
    private final int flightDuration;
    private final double speed;
    private final EntityType mountType;
    private final Particle particle;
    private final int particleCount;
    private final double particleOffset;
    private final double particleExtra;

    public MountFlightAbility(CooldownManager cooldownHandler, JavaPlugin plugin) {
        this.plugin = plugin;

        YamlDocument config = ConfigUtil.configUtil.getAbilitiesMap().get("MOUNTFLIGHT");
        setName("MountFlight");

        this.cooldown = config.getInt("ability.options.cooldown", 30);
        setCooldown(this.cooldown);
        setCooldownManager(cooldownHandler);

        this.flightDuration = config.getInt("ability.options.flight-duration-seconds", 5) * 20;
        this.speed = config.getDouble("ability.options.speed", 0.6);
        String entityType = config.getString("ability.options.mount-type", "BAT");
        this.mountType = EntityType.valueOf(entityType.toUpperCase());
        this.particle = Particle.valueOf(config.getString("ability.options.particle", "CLOUD"));
        this.particleCount = config.getInt("ability.options.particle-count", 5);
        this.particleOffset = config.getDouble("ability.options.particle-offset", 0.2);
        this.particleExtra = config.getDouble("ability.options.particle-extra", 0.02);
    }

    @Override
    public void useAbility(Player player) {
        if (getCooldownManager().hasAbilityCooldown(player)) {
            int remainingCooldown = getCooldownManager().getRemainingAbilityCooldown(player);
            if (remainingCooldown > 0 && remainingCooldown == Math.floor(remainingCooldown)) {
                player.sendMessage(ConfigUtil.configUtil.getMessage("messages.fungun.ability-cooldown")
                        .replace("%cooldown%", String.valueOf((int) remainingCooldown)));

                String actionBarMessage = ConfigUtil.configUtil.getMessage("messages.fungun.ability-cooldown-actionbar");
                if (actionBarMessage != null && !actionBarMessage.isEmpty()) {
                    actionBarMessage = actionBarMessage.replace("%cooldown%", String.valueOf((int) remainingCooldown));
                    player.sendActionBar(actionBarMessage);
                }
            }
            return;
        }
        getCooldownManager().startAbilityCooldown(player, getCooldown());

        Location spawnLocation = player.getLocation();
        LivingEntity mount = (LivingEntity) player.getWorld().spawnEntity(spawnLocation, mountType);
        mount.setInvulnerable(true);
        mount.setSilent(true);
        mount.setGravity(false);

        mount.addPassenger(player);

        final SchedulerManager.Task[] task = new SchedulerManager.Task[1];
        task[0] = SchedulerManager.runTimer(new Runnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= flightDuration) {
                    mount.removePassenger(player);
                    mount.remove();
                    task[0].cancel();
                    return;
                }

                Vector direction = player.getLocation().getDirection().normalize().multiply(speed);
                mount.setVelocity(direction);

                mount.getWorld().spawnParticle(particle, mount.getLocation(), particleCount, particleOffset, particleOffset, particleOffset, particleExtra);

                ticks++;
            }
        }, 0L, 1L);
    }
}
