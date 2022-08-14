package me.swipez.terminatornpc.command;

import me.swipez.terminatornpc.TerminatorNPC;
import me.swipez.terminatornpc.trait.TerminatorTrait;
import me.swipez.terminatornpc.util.TerminatorSummoner;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Requirements(ownership = false, selected = false)
public class TerminatorCommands {
    @Command(
            aliases = { "terminator" },
            usage = "loadout",
            desc = "View and modify the loadout for terminators",
            modifiers = { "loadout" },
            min = 1,
            max = 1,
            permission = "terminatornpc.loadout"
    )
    public void loadout(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
            return;
        }

        Player player = (Player) sender;
        TerminatorNPC.terminatorLoadoutHashMap.putIfAbsent(player.getUniqueId(), new me.swipez.terminatornpc.loadout.TerminatorLoadout(TerminatorNPC.getPlugin()));

        me.swipez.terminatornpc.loadout.TerminatorLoadout terminatorLoadout = TerminatorNPC.terminatorLoadoutHashMap.get(player.getUniqueId());
        terminatorLoadout.display(player);
    }

    @Command(
            aliases = { "terminator" },
            usage = "ignore [player]",
            desc = "Make terminators ignore player",
            modifiers = { "ignore" },
            min = 2,
            max = 2,
            permission = "terminatornpc.ignore"
    )
    public void ignore(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Player ignoredPlayer = Bukkit.getPlayer(args.getString(1));

        // Player can be offline, which causes bukkit to not find said player
        if (ignoredPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        if (TerminatorNPC.ignoredPlayers.contains(ignoredPlayer.getUniqueId())) {
            TerminatorNPC.ignoredPlayers.remove(ignoredPlayer.getUniqueId());
            sender.sendMessage(ChatColor.WHITE + "Terminators will no longer ignore " + ChatColor.RED + ignoredPlayer.getDisplayName() + ChatColor.WHITE + ".");
            return;
        }

        TerminatorNPC.ignoredPlayers.add(ignoredPlayer.getUniqueId());
        sender.sendMessage(ChatColor.WHITE + "Terminators will now ignore " + ChatColor.RED + ignoredPlayer.getDisplayName() + ChatColor.WHITE + ".");
    }

    @Command(
            aliases = { "terminator" },
            usage = "add [player] (count)",
            desc = "Summon terminator(s)",
            modifiers = { "add" },
            min = 2,
            max = 3,
            permission = "terminatornpc.spawnterminator"
    )
    public void add(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
            return;
        }

        int amount = 1;
        if (args.length() == 4){
            amount = args.getInteger(2);
        }

        TerminatorSummoner summoner = new TerminatorSummoner(args.getString(1), (Player) sender, ((Player) sender).getLocation(), amount);
        summoner.summon();
        sender.sendMessage("Summoned terminator(s) with name " + ChatColor.RED + args.getString(1) + ChatColor.RESET + ".");
    }

    @Command(
            aliases = { "terminator" },
            usage = "clear",
            desc = "Remove all terminators",
            modifiers = { "clear" },
            min = 1,
            max = 1,
            permission = "terminatornpc.deleteterminator"
    )
    public void clear(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Player player = (Player) sender;
        player.sendMessage("Clearing " + ChatColor.RED + TerminatorNPC.terminators.size() + ChatColor.RESET + " Terminators.");

        for (NPC terminator : TerminatorNPC.terminators) {
            TerminatorTrait terminatorTrait = terminator.getOrAddTrait(TerminatorTrait.class);
            terminatorTrait.delete = true;
            terminator.despawn();
        }

        TerminatorNPC.terminators.clear();
    }

    @Command(
            aliases = { "terminator" },
            usage = "summoner [player] [ticks] (amount)",
            desc = "Create a terminator summoner at your location",
            modifiers = { "summoner" },
            min = 3,
            max = 4,
            permission = "terminatornpc.spawnterminator"
    )
    public void addSummoner(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
            return;
        }

        int amount = 1;
        if (args.length() == 5){
            amount = args.getInteger(3);
        }

        // Register the task that will summon a new terminator every x ticks
        TerminatorSummoner summoner = new TerminatorSummoner(args.getString(1), (Player) sender, ((Player) sender).getLocation(), amount);
        int summonerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(TerminatorNPC.getPlugin(), new Runnable() {
            @Override
            public void run() {
                summoner.summon();

                // Send a message to all players announcing that a new terminator has been summoned
                Bukkit.getOnlinePlayers().forEach(
                        p -> p.sendMessage(ChatColor.GOLD + "A new " + ChatColor.RESET + summoner.getName() + ChatColor.GOLD + " has been spawned!")
                );
            }
        }, args.getInteger(2), args.getInteger(2));

        sender.sendMessage("Created summoner with id "
                + ChatColor.RED + TerminatorNPC.bukkitSchedules.size() + ChatColor.RESET + " at location "
                + ChatColor.RED + ((Player) sender).getLocation().getBlockX()
                + " " + ((Player) sender).getLocation().getBlockY()
                + " " + ((Player) sender).getLocation().getBlockZ() + ChatColor.RESET + ".");

        TerminatorNPC.bukkitSchedules.add(summonerId);
    }

    @Command(
            aliases = { "terminator" },
            usage = "delsum [id]",
            desc = "Remove a terminator summoner",
            modifiers = { "delsum" },
            min = 2,
            max = 2,
            permission = "terminatornpc.spawnterminator"
    )
    public void removeSummoner(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int index = args.getInteger(1);

        if (index > TerminatorNPC.bukkitSchedules.size() -1) {
            sender.sendMessage(ChatColor.RED + "Summoner with id " + index + " not found!");
            return;
        }

        int task = TerminatorNPC.bukkitSchedules.get(index);
        Bukkit.getScheduler().cancelTask(task);

        TerminatorNPC.bukkitSchedules.remove(index);

        sender.sendMessage("Removed summoner with id " + ChatColor.RED + index + ChatColor.RESET + ".");
    }
}