package hu.kxtsoo.fungun.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;

public class PlaceHolderAPIHook {

    public String parsePlaceholders(@NotNull String string) {
        return PlaceholderAPI.setPlaceholders(null, string);
    }
}
