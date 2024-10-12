package hu.kitsoo.gfungun;

import hu.kitsoo.gfungun.events.CooldownListener;
import hu.kitsoo.gfungun.events.FunGunListener;
import hu.kitsoo.gfungun.util.TabComplete;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class GFunGun extends JavaPlugin {

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        System.out.println(ChatColor.GREEN + "The plugin successfully enabled.");
        System.out.println(ChatColor.GREEN + "Plugin developed by Glowing Studios. https://discord.gg/esxwNC4DmZ");

        CooldownListener cooldownHandler = new CooldownListener(this);

        getServer().getPluginManager().registerEvents(new FunGunListener(this, cooldownHandler), this);
        Objects.requireNonNull(getCommand("gfungun")).setTabCompleter(new TabComplete(this));
        Objects.requireNonNull(getCommand("gfungun")).setExecutor(new GFunGunCommand(this));

    }

    @Override
    public void onDisable() {
    }
}