package hu.kxtsoo.fungun.abilities;

import dev.dejvokep.boostedyaml.YamlDocument;
import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.manager.CooldownManager;
import hu.kxtsoo.fungun.manager.SchedulerManager;
import hu.kxtsoo.fungun.util.ConfigUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public class ExplosionAbility extends AbilityHandler {

    private final JavaPlugin plugin;
    private final int maxBlocks;
    private final int maxHeight;
    private final int explosionRadius;
    private final int removeDelayTicks;

    public ExplosionAbility(CooldownManager cooldownHandler, JavaPlugin plugin) {
        YamlDocument explosionConfig = ConfigUtil.configUtil.getAbilitiesMap().get("EXPLOSION");
        String name = "Explosion";
        int cooldown = explosionConfig != null ? explosionConfig.getInt("ability.options.cooldown", 15) : 15;

        setName(name);
        setCooldown(cooldown);
        setCooldownManager(cooldownHandler);
        this.plugin = plugin;

        this.maxBlocks = explosionConfig.getInt("ability.options.max-blocks", 10);
        this.maxHeight = explosionConfig.getInt("ability.options.max-height", 3);
        this.explosionRadius = explosionConfig.getInt("ability.options.explosion-radius", 2);
        this.removeDelayTicks = explosionConfig.getInt("ability.options.remove-delay-ticks", 35);
    }

    @Override
    public void useAbility(Player player) {
        YamlDocument explosionConfig = ConfigUtil.configUtil.getAbilitiesMap().get("EXPLOSION");
        if (explosionConfig == null || !explosionConfig.getBoolean("ability.enabled", true)) {
            return;
        }

        String permission = explosionConfig.getString("ability.options.permission", "fungun.ability.explosion");
        if (!player.hasPermission(permission)) {
            player.sendMessage(ConfigUtil.configUtil.getMessage("messages.fungun.no-ability-permission"));
            return;
        }

        if (getCooldownManager().hasAbilityCooldown(player)) {
            int remainingCooldown = getCooldownManager().getRemainingAbilityCooldown(player);
            if (remainingCooldown > 0 && remainingCooldown == Math.floor(remainingCooldown)) {
                if(!ConfigUtil.configUtil.getMessage("messages.fungun.ability-cooldown").isEmpty()) {
                    player.sendMessage(ConfigUtil.configUtil.getMessage("messages.fungun.ability-cooldown")
                            .replace("%cooldown%", String.valueOf((int) remainingCooldown)));
                }

                String actionBarMessage = ConfigUtil.configUtil.getMessage("messages.fungun.ability-cooldown-actionbar");
                if (actionBarMessage != null && !actionBarMessage.isEmpty()) {
                    actionBarMessage = actionBarMessage.replace("%cooldown%", String.valueOf((int) remainingCooldown));
                    player.sendActionBar(actionBarMessage);
                }
            }
            return;
        }
        getCooldownManager().startAbilityCooldown(player, getCooldown());

        int maxSteps = explosionConfig.getInt("ability.options.steps", 4);
        long stepDelayTicks = explosionConfig.getLong("ability.options.step-delay-ticks", 5L);
        String trailParticleName = explosionConfig.getString("ability.options.trail-particle", "FLAME");
        int particleCount = explosionConfig.getInt("ability.options.particle-count", 5);

        Particle trailParticle;
        try {
            trailParticle = Particle.valueOf(trailParticleName);
        } catch (IllegalArgumentException e) {
            trailParticle = Particle.FLAME;
            FunGun.getInstance().getLogger().warning("Invalid particle type in config for Explosion ability. Defaulting to FLAME.");
        }

        Location startLocation = player.getLocation();
        Vector direction = player.getLocation().getDirection().normalize().setY(0).multiply(1.0);

        final SchedulerManager.Task[] task = new SchedulerManager.Task[1];
        Particle finalTrailParticle = trailParticle;

        task[0] = SchedulerManager.runTimer(new Runnable() {
            int steps = 0;
            Location currentLocation = startLocation.clone();

            @Override
            public void run() {
                if (steps >= maxSteps) {
                    createExplosionEffect(currentLocation);
                    task[0].cancel();
                    return;
                }

                Location groundLocation = currentLocation.clone();
                while (groundLocation.getBlock().getType() == Material.AIR && groundLocation.getY() > 0) {
                    groundLocation.subtract(0, 1, 0);
                }

                currentLocation = groundLocation.add(direction);
                currentLocation.getWorld().spawnParticle(finalTrailParticle, currentLocation, particleCount, 0.1, 0.0, 0.1, 0.05);

                steps++;
            }
        }, 0L, stepDelayTicks);

    }

    private void createExplosionEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        playSounds(location);
        spawnParticles(location);

        simulateBlockExplosion(location);
        spawnFallingBlockEffect(location);
    }

    private void playSounds(Location location) {
        List<String> sounds = ConfigUtil.configUtil.getAbilitiesMap().get("EXPLOSION").getStringList("sounds");
        for (String soundConfig : sounds) {
            String[] soundParams = soundConfig.split(",");
            try {
                Sound sound = Sound.valueOf(soundParams[0]);
                float volume = Float.parseFloat(soundParams[1]);
                float pitch = Float.parseFloat(soundParams[2]);
                location.getWorld().playSound(location, sound, volume, pitch);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void spawnParticles(Location location) {
        List<String> particles = ConfigUtil.configUtil.getAbilitiesMap().get("EXPLOSION").getStringList("particles");
        for (String particleConfigEntry : particles) {
            String[] particleParams = particleConfigEntry.split(",");
            try {
                Particle particle = Particle.valueOf(particleParams[0]);
                int count = Integer.parseInt(particleParams[1]);
                double offsetX = Double.parseDouble(particleParams[2]);
                double offsetY = Double.parseDouble(particleParams[3]);
                double offsetZ = Double.parseDouble(particleParams[4]);
                double extra = Double.parseDouble(particleParams[5]);
                location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void simulateBlockExplosion(Location location) {
        Random random = new Random();
        World world = location.getWorld();

        for (int i = 0; i < maxBlocks; i++) {
            Location particleLocation = location.clone().add(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5);
            Material blockType = Material.STONE;

            world.spawnParticle(Particle.BLOCK_CRACK, particleLocation, 15, 0.3, 0.3, 0.3, blockType.createBlockData());
        }
    }

    private void spawnFallingBlockEffect(Location location) {
        int blockCount = 0;

        for (int x = -explosionRadius; x <= explosionRadius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -explosionRadius; z <= explosionRadius; z++) {
                    if (blockCount >= maxBlocks) return;

                    Location blockLocation = location.clone().add(x, y, z);
                    Block block = blockLocation.getBlock();

                    if (block.getType() != Material.AIR) {
                        BlockData blockData = block.getBlockData();
                        FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(blockLocation, blockData);

                        double upwardVelocity = 0.3 + Math.random() * (maxHeight / 2.0);
                        Vector velocity = new Vector((Math.random() - 0.5) * 0.5, upwardVelocity, (Math.random() - 0.5) * 0.5);
                        fallingBlock.setVelocity(velocity);

                        fallingBlock.setDropItem(false);
                        fallingBlock.setHurtEntities(false);
                        fallingBlock.setPersistent(false);
                        fallingBlock.setGravity(true);

                        blockCount++;
                    }
                }
            }
        }
    }
}