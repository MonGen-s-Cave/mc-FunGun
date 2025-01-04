package hu.kxtsoo.fungun.hooks.impl;

import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook {

    public String parsePlaceholders(@NotNull String string) {
        return PlaceholderAPI.setPlaceholders(null, string);
    }
}
