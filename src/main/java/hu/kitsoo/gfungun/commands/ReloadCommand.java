package hu.kitsoo.gfungun.commands;

import hu.kitsoo.gfungun.GFunGun;
import hu.kitsoo.gfungun.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

public class ReloadCommand implements CommandExecutor, Listener {

    private final GFunGun plugin;

    public ReloadCommand(GFunGun plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String prefix = this.plugin.getConfig().getString("prefix");
        prefix = ChatUtil.colorizeHex(prefix);

        String noPermission = this.plugin.getConfig().getString("messages.no-permission");
        noPermission = ChatUtil.colorizeHex(noPermission);

        if (!(sender.hasPermission("gfungun.reload") || sender.hasPermission("gfungun.*") || sender.isOp())) {
            sender.sendMessage(prefix + noPermission);
            return true;
        }

        String reloadSuccess = this.plugin.getConfig().getString("messages.reload-success");
        reloadSuccess = ChatUtil.colorizeHex(reloadSuccess);
        this.plugin.reloadConfig();
        sender.sendMessage(prefix + reloadSuccess);

        return true;
    }
}