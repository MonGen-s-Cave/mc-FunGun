package hu.kxtsoo.fungun.hooks;

import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.util.ChatUtil;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public class HookManager {

    private static PlaceHolderAPIHook papi = null;

    public void updateHooks() {

        if (FunGun.getInstance().getConfigUtil().getConfig().getBoolean("hooks.PlaceholderAPI.register", true) &&
                Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {

            papi = new PlaceHolderAPIHook();
            Bukkit.getConsoleSender().sendMessage(ChatUtil.colorizeHex("&#33FF33[mc-FunGun] Hooked into PlaceholderAPI!"));
        }
    }

    @Nullable
    public static PlaceHolderAPIHook getPapi() {
        return papi;
    }
}
