package hu.kxtsoo.fungun.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import hu.kxtsoo.fungun.FunGun;
import org.bukkit.command.CommandSender;

@Command("mcfungun")
public class ReloadCommand extends BaseCommand {

    @SubCommand("reload")
    @Permission("mcfungun.admin.reload")
    public void executor(CommandSender sender) {

        FunGun.getInstance().getConfigUtil().reloadConfig();
        sender.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.reload-command.success"));

        FunGun.getInstance().getConfigUtil().reloadEffects();
        sender.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.reload-command.effects-loaded").replace("%amount%", String.valueOf(FunGun.getInstance().getConfigUtil().getEffectCount())));

    }
}