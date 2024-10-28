package hu.kxtsoo.fungun.abilities;

import hu.kxtsoo.fungun.manager.CooldownManager;
import org.bukkit.entity.Player;

public abstract class AbilityHandler {
    private String name;
    private int cooldown;
    private CooldownManager cooldownManager;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public void setCooldownManager(CooldownManager cooldownManager) {
        this.cooldownManager = cooldownManager;
    }

    public abstract void useAbility(Player player);
}