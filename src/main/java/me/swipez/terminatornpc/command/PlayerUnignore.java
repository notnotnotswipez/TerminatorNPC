package me.swipez.terminatornpc.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerUnignore implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            if (sender.hasPermission("terminatornpc.ignore") && args.length == 1) {
                Player player = (Player) sender;
                Player ignoredPlayer = Bukkit.getPlayer(args[0]);

                // Player can be offline, which causes bukkit to not find said player
                if (ignoredPlayer == null || !PlayerIgnore.ignoredPlayers.contains(ignoredPlayer.getUniqueId())) {
                    return true;
                }

                PlayerIgnore.ignoredPlayers.remove(ignoredPlayer.getUniqueId());
                player.sendMessage(ChatColor.WHITE + "Terminators will no longer ignore " + ChatColor.RED + args[0] + ChatColor.WHITE + " Terminators.");
            }
        }
        return true;
    }
}