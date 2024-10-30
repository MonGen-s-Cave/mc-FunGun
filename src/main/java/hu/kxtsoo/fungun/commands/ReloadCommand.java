package hu.kxtsoo.fungun.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.database.DatabaseManager;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

@Command("mcfungun")
public class ReloadCommand extends BaseCommand {

    @SubCommand("reload")
    @Permission("mcfungun.admin.reload")
    public void executor(CommandSender sender) {

        FunGun.getInstance().getConfigUtil().reloadConfig();
        sender.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.reload-command.success"));

        FunGun.getInstance().getConfigUtil().reloadEffects();
        FunGun.getInstance().getConfigUtil().reloadAbilities();
        sender.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.reload-command.effects-loaded").replace("%amount%", String.valueOf(FunGun.getInstance().getConfigUtil().getEffectCount())));

        try {
            DatabaseManager.initialize(FunGun.getInstance().getConfigUtil(), FunGun.getInstance());
            sender.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.reload-command.db-success"));
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.database-error"));
        }

    }
}