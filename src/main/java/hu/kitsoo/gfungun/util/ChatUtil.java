package hu.kitsoo.gfungun.util;

import org.bukkit.ChatColor;

public class ChatUtil {
    public static String colorizeHex(String message) {
        if (message == null) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}







