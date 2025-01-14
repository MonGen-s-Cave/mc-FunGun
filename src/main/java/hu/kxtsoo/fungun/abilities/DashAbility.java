package hu.kxtsoo.fungun.abilities;

import dev.dejvokep.boostedyaml.YamlDocument;
import hu.kxtsoo.fungun.manager.CooldownManager;
import hu.kxtsoo.fungun.util.ConfigUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class DashAbility extends AbilityHandler {

    public DashAbility(CooldownManager cooldownHandler) {
        YamlDocument dashConfig = ConfigUtil.configUtil.getAbilitiesMap().get("DASH");
        String name = "Dash";
        int cooldown = dashConfig != null ? dashConfig.getInt("ability.options.cooldown", 10) : 10;

        setName(name);
        setCooldown(cooldown);
        setCooldownManager(cooldownHandler);
    }

    public void useAbility(Player player) {
        YamlDocument dashConfig = ConfigUtil.configUtil.getAbilitiesMap().get("DASH");
        if (dashConfig == null || !dashConfig.getBoolean("ability.enabled", true)) {
            return;

        }

        String permission = dashConfig.getString("ability.options.permission", "fungun.ability.dash");
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

        double strength = dashConfig.getDouble("ability.options.strength", 1.5);
        player.setVelocity(player.getLocation().getDirection().multiply(strength));

        playSounds(player, dashConfig);
        spawnParticles(player, dashConfig);

        getCooldownManager().startAbilityCooldown(player, getCooldown());
    }

    private void playSounds(Player player, YamlDocument dashConfig) {
        List<String> sounds = dashConfig.getStringList("sounds");
        for (String soundConfig : sounds) {
            String[] soundParams = soundConfig.split(",");
            try {
                Sound sound = Sound.valueOf(soundParams[0]);
                float volume = Float.parseFloat(soundParams[1]);
                float pitch = Float.parseFloat(soundParams[2]);
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void spawnParticles(Player player, YamlDocument dashConfig) {
        List<String> particles = dashConfig.getStringList("particles");
        for (String particleConfig : particles) {
            String[] particleParams = particleConfig.split(",");
            try {
                Particle particle = Particle.valueOf(particleParams[0]);
                int count = Integer.parseInt(particleParams[1]);
                double offsetX = Double.parseDouble(particleParams[2]);
                double offsetY = Double.parseDouble(particleParams[3]);
                double offsetZ = Double.parseDouble(particleParams[4]);
                double extra = Double.parseDouble(particleParams[5]);
                player.getWorld().spawnParticle(particle, player.getLocation(), count, offsetX, offsetY, offsetZ, extra);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}