package hu.kxtsoo.fungun.abilities;

import dev.dejvokep.boostedyaml.YamlDocument;
import hu.kxtsoo.fungun.manager.CooldownManager;
import hu.kxtsoo.fungun.manager.SchedulerManager;
import hu.kxtsoo.fungun.util.ConfigUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BatSwarmAbility extends AbilityHandler {

    private final JavaPlugin plugin;
    private final int cooldown;
    private final int swarmSize;
    private final int flightDuration;
    private final double speed;
    private final Particle particle;
    private final Sound startSound;
    private final Sound endSound;
    private final Random random = new Random();

    public BatSwarmAbility(CooldownManager cooldownHandler, JavaPlugin plugin) {
        this.plugin = plugin;

        YamlDocument config = ConfigUtil.configUtil.getAbilitiesMap().get("BATSWARM");
        setName("BatSwarm");

        this.cooldown = config.getInt("ability.options.cooldown", 20);
        setCooldown(this.cooldown);
        setCooldownManager(cooldownHandler);

        this.swarmSize = config.getInt("ability.options.swarm-size", 10);
        this.flightDuration = config.getInt("ability.options.flight-duration-seconds", 5) * 20;
        this.speed = config.getDouble("ability.options.speed", 0.4);
        this.particle = Particle.valueOf(config.getString("ability.options.particle", "SMOKE_LARGE"));

        this.startSound = Sound.valueOf(config.getString("ability.options.start-sound", "ENTITY_BAT_AMBIENT"));
        this.endSound = Sound.valueOf(config.getString("ability.options.end-sound", "ENTITY_BAT_TAKEOFF"));
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

        List<Bat> bats = new ArrayList<>();
        Location spawnLocation = player.getLocation().add(player.getLocation().getDirection().multiply(2)).add(0, 1, 0);
        Vector direction = player.getLocation().getDirection().normalize().multiply(speed);

        for (int i = 0; i < swarmSize; i++) {
            Location batLocation = spawnLocation.clone().add(
                    (random.nextDouble() - 0.5) * 2,
                    random.nextDouble() * 1.5,
                    (random.nextDouble() - 0.5) * 2
            );
            Bat bat = (Bat) player.getWorld().spawn(batLocation, Bat.class);
            bat.setSilent(true);
            bats.add(bat);
        }

        final SchedulerManager.Task[] task = new SchedulerManager.Task[1];
        task[0] = SchedulerManager.runTimer(new Runnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= flightDuration) {
                    bats.forEach(Bat::remove);
                    player.getWorld().playSound(player.getLocation(), endSound, 1.0f, 0.8f);
                    task[0].cancel();
                    return;
                }

                for (Bat bat : bats) {
                    bat.setVelocity(direction);

                    if (ticks % 5 == 0) {
                        bat.getWorld().spawnParticle(particle, bat.getLocation(), 1, 0.2, 0.2, 0.2, 0.05);
                    }
                }

                ticks++;
            }
        }, 0L, 1L);

        player.getWorld().playSound(player.getLocation(), startSound, 1.0f, 0.8f);
    }
}