package me.swipez.terminatornpc;

import me.swipez.terminatornpc.command.*;
import me.swipez.terminatornpc.loadout.TerminatorLoadout;
import me.swipez.terminatornpc.util.TerminatorSummoner;;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.Injector;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.StringHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public final class TerminatorNPC extends JavaPlugin {
    private final CommandManager commands = new CommandManager();
    private static TerminatorNPC plugin;

    // Data for the plugin
    public static HashMap<UUID, TerminatorLoadout> terminatorLoadoutHashMap = new HashMap<>();
    public static List<UUID> ignoredPlayers = new LinkedList<>();
    public static List<NPC> terminators = new LinkedList<>();

    // All locations a terminator should be automatically summoned at
    public static List<Integer> bukkitSchedules = new LinkedList<>();

    private static int currentID = 0;

    public static TerminatorNPC getPlugin() {
        return plugin;
    }

    public static int getUniqueID() {
        currentID++;
        return currentID;
    }

    @Override
    public void onEnable() {
        plugin = this;

        // Register commands
        registerCommands();
        commands.registerTabCompletion(this);
    }

    @Override
    public void onDisable() {
       for (NPC npc : terminators){
           npc.despawn();
       }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String cmdName, String[] args) {
        String modifier = args.length > 0 ? args[0] : "";
        if (!commands.hasCommand(command, modifier) && !modifier.isEmpty()) {
            return suggestClosestModifier(sender, command.getName(), modifier);
        }

        Object[] methodArgs = { sender, null };
        return commands.executeSafe(command, args, sender, methodArgs);
    }

    private void registerCommands() {
        commands.setInjector(new Injector(this));
        // Register command classes
        commands.register(TerminatorCommands.class);
    }

    private boolean suggestClosestModifier(CommandSender sender, String command, String modifier) {
        String closest = commands.getClosestCommandModifier(command, modifier);
        if (!closest.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + Messaging.tr(Messages.UNKNOWN_COMMAND));
            sender.sendMessage(StringHelper.wrap(" /") + command + " " + StringHelper.wrap(closest));
            return true;
        }
        return false;
    }
}