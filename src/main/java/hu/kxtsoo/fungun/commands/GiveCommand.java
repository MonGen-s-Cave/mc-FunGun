package hu.kxtsoo.fungun.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.Command;
import dev.triumphteam.cmd.core.annotation.Optional;
import dev.triumphteam.cmd.core.annotation.SubCommand;
import dev.triumphteam.cmd.core.annotation.Suggestion;
import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.model.FunGunItem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Command(value = "mcfungun", alias = {"fungun", "mc-fungun"})
public class GiveCommand extends BaseCommand {

    @SubCommand("give")
    @Permission("fungun.admin.give")
    public void execute(CommandSender sender, @Suggestion("online_players") String playerName, @Suggestion("slot_numbers") @Optional Integer slot) {
        Player target = Bukkit.getPlayer(playerName);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.give-command.player-not-found"));
            return;
        }

        ItemStack funGunItem = FunGunItem.createFunGunItem(FunGun.getInstance().getConfigUtil());

        if (slot != null) {
            if (slot < 0 || slot >= target.getInventory().getSize()) {
                sender.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.give-command.invalid-slot"));
                return;
            }
            target.getInventory().setItem(slot, funGunItem);
        } else {
            target.getInventory().addItem(funGunItem);
        }

        sender.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.give-command.success").replace("%player%", target.getName()));
        target.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.give-command.received"));
    }
}
