package me.swipez.terminatornpc.command;

import me.swipez.terminatornpc.terminatorTrait.TerminatorTrait;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TerminatorClear implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            if (sender.hasPermission("terminatornpc.deleteterminator")) {
                Player player = (Player) sender;
                player.sendMessage(ChatColor.WHITE + "Cleared " + ChatColor.RED + TerminatorSummon.terminators.size() + ChatColor.WHITE + " Terminators.");
                for (NPC npc : TerminatorSummon.terminators) {
                    TerminatorTrait terminatorTrait = npc.getOrAddTrait(TerminatorTrait.class);
                    terminatorTrait.delete = true;
                    npc.despawn();
                }
                TerminatorSummon.terminators.clear();
            }
        }
        return true;
    }
}