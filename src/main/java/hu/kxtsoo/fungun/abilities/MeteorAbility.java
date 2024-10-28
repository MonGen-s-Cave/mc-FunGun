package hu.kxtsoo.fungun.abilities;

import dev.dejvokep.boostedyaml.YamlDocument;
import hu.kxtsoo.fungun.manager.CooldownManager;
import hu.kxtsoo.fungun.manager.SchedulerManager;
import hu.kxtsoo.fungun.util.ConfigUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;

public class MeteorAbility extends AbilityHandler {

    private final JavaPlugin plugin;
    private final int cooldown;
    private final double meteorSpeed;
    private final Material meteorMaterial;
    private final int particleCount;
    private final List<String> impactParticles;
    private final List<String> impactSounds;

    public MeteorAbility(CooldownManager cooldownHandler, JavaPlugin plugin) {
        YamlDocument meteorConfig = ConfigUtil.configUtil.getAbilitiesMap().get("METEOR");
        String name = "Meteor";
        this.cooldown = meteorConfig != null ? meteorConfig.getInt("ability.options.cooldown", 20) : 20;

        setName(name);
        setCooldown(this.cooldown);
        setCooldownManager(cooldownHandler);
        this.plugin = plugin;

        this.meteorSpeed = meteorConfig.getDouble("ability.options.meteor-speed", 0.5);
        String materialName = meteorConfig.getString("ability.options.meteor-material", "MAGMA_BLOCK");
        this.meteorMaterial = Material.matchMaterial(materialName) != null ? Material.matchMaterial(materialName) : Material.MAGMA_BLOCK;
        this.particleCount = meteorConfig.getInt("ability.options.particle-count", 10);

        this.impactParticles = meteorConfig.getStringList("particles");
        this.impactSounds = meteorConfig.getStringList("sounds");
    }

    @Override
    public void useAbility(Player player) {
        if (getCooldownManager().hasAbilityCooldown(player)) {
            int remainingCooldown = getCooldownManager().getRemainingAbilityCooldown(player);
            if (remainingCooldown > 0 && remainingCooldown == Math.floor(remainingCooldown)) {
                player.sendMessage(ConfigUtil.configUtil.getMessage("messages.fungun.ability-cooldown")
                        .replace("%cooldown%", String.valueOf((int) remainingCooldown)));
            }
            return;
        }
        getCooldownManager().startAbilityCooldown(player, getCooldown());

        Block targetBlock = player.getTargetBlockExact(50);
        if (targetBlock == null) {
            return;
        }

        Location targetLocation = targetBlock.getLocation();
        Location startLocation = targetLocation.clone().add(10, 15, 10);

        Vector direction = targetLocation.toVector().subtract(startLocation.toVector()).normalize().multiply(meteorSpeed);
        direction.setY(-meteorSpeed);

        FallingBlock meteor = startLocation.getWorld().spawnFallingBlock(startLocation, meteorMaterial.createBlockData());
        meteor.setDropItem(false);
        meteor.setHurtEntities(false);

        final SchedulerManager.Task[] task = new SchedulerManager.Task[1];
        task[0] = SchedulerManager.runTimer(new Runnable() {
            @Override
            public void run() {
                if (meteor.isDead() || meteor.isOnGround()) {
                    createExplosionEffect(meteor.getLocation());
                    meteor.remove();
                    task[0].cancel();
                    return;
                }

                meteor.setVelocity(direction);

                meteor.getWorld().spawnParticle(Particle.FLAME, meteor.getLocation(), particleCount, 0.2, 0.2, 0.2, 0.05);
            }
        }, 0L, 2L);
    }

    private void createExplosionEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        for (String soundConfig : impactSounds) {
            String[] soundParams = soundConfig.split(",");
            try {
                Sound sound = Sound.valueOf(soundParams[0]);
                float volume = Float.parseFloat(soundParams[1]);
                float pitch = Float.parseFloat(soundParams[2]);
                world.playSound(location, sound, volume, pitch);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (String particleConfig : impactParticles) {
            String[] particleParams = particleConfig.split(",");
            try {
                Particle particle = Particle.valueOf(particleParams[0]);
                int count = Integer.parseInt(particleParams[1]);
                double offsetX = Double.parseDouble(particleParams[2]);
                double offsetY = Double.parseDouble(particleParams[3]);
                double offsetZ = Double.parseDouble(particleParams[4]);
                double extra = Double.parseDouble(particleParams[5]);
                world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}