package me.swipez.terminatornpc.command;

import me.swipez.terminatornpc.TerminatorNPC;
import me.swipez.terminatornpc.loadout.TerminatorLoadout;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class TerminatorLoadoutCommand implements CommandExecutor {

    public static HashMap<UUID, TerminatorLoadout> terminatorLoadoutHashMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            Player player = (Player) sender;
            terminatorLoadoutHashMap.putIfAbsent(player.getUniqueId(), new TerminatorLoadout(TerminatorNPC.getPlugin()));

            TerminatorLoadout terminatorLoadout = terminatorLoadoutHashMap.get(player.getUniqueId());
            terminatorLoadout.display(player);
        }
        return true;
    }
}
