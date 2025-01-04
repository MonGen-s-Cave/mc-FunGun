package hu.kxtsoo.fungun.hooks;

import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.hooks.impl.PlaceholderAPIHook;
import hu.kxtsoo.fungun.util.ChatUtil;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public class HookManager {

    private static PlaceholderAPIHook papi = null;

    public void updateHooks() {

        if (FunGun.getInstance().getConfigUtil().getConfig().getBoolean("hooks.PlaceholderAPI.register", true) &&
                Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {

            papi = new PlaceholderAPIHook();
            Bukkit.getConsoleSender().sendMessage(ChatUtil.colorizeHex("&#33FF33[mc-FunGun] Hooked into PlaceholderAPI!"));
        }
    }

    @Nullable
    public static PlaceholderAPIHook getPapi() {
        return papi;
    }
}
