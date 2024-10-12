package hu.kitsoo.gfungun;

import hu.kitsoo.gfungun.commands.ReloadCommand;
import hu.kitsoo.gfungun.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class GFunGunCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public GFunGunCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String prefix = this.plugin.getConfig().getString("prefix");
        prefix = ChatUtil.colorizeHex(prefix);

        List<String> helpMenuList = this.plugin.getConfig().getStringList("help-menu");
        StringBuilder helpMenuBuilder = new StringBuilder();

        for (String line : helpMenuList) {
            helpMenuBuilder.append(line).append("\n");
        }

        String helpMenu = helpMenuBuilder.toString().trim();
        helpMenu = ChatUtil.colorizeHex(helpMenu);

        if (args.length == 0 || args.length < 1) {
            sender.sendMessage(helpMenu);
            return true;
        }

        String subCommand = args[0].toLowerCase();

            if (subCommand.equals("reload")) {
                ReloadCommand reloadCommand = new ReloadCommand((GFunGun) plugin);
                return reloadCommand.onCommand(sender, command, label, args);
            } else {
                sender.sendMessage(helpMenu);
                return true;
            }
    }
}