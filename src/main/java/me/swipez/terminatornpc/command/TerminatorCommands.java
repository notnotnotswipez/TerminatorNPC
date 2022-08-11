package me.swipez.terminatornpc.command;

import me.swipez.terminatornpc.TerminatorNPC;
import me.swipez.terminatornpc.stuckaction.TerminatorStuckAction;
import me.swipez.terminatornpc.terminatorTrait.TerminatorFollow;
import me.swipez.terminatornpc.terminatorTrait.TerminatorTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

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

        int number = 1;
        if (args.length() == 4){
            number = args.getInteger(2);
        }

        summonTerminator(args.getString(1), number, (Player) sender);
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

    private static void summonTerminator(String playerName, int number, Player sender){
        TerminatorNPC.terminatorLoadoutHashMap.putIfAbsent(sender.getUniqueId(), new me.swipez.terminatornpc.loadout.TerminatorLoadout(TerminatorNPC.getPlugin()));
        me.swipez.terminatornpc.loadout.TerminatorLoadout terminatorLoadout = TerminatorNPC.terminatorLoadoutHashMap.get(sender.getUniqueId());

        for (int i = 0; i < number; i++){
            NPC npc = new CitizensNPC(UUID.randomUUID(), TerminatorNPC.getUniqueID(), playerName, EntityControllers.createForType(EntityType.PLAYER), CitizensAPI.getNPCRegistry());
            npc.spawn(sender.getLocation());
            npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, false);

            npc.getNavigator().getLocalParameters()
                    .attackRange(10)
                    .baseSpeed(1.6F * 10F)
                    .straightLineTargetingDistance(100)
                    .stuckAction(new TerminatorStuckAction())
                    .range(40);

            TerminatorFollow followTrait = new TerminatorFollow();
            followTrait.linkToNPC(npc);
            followTrait.run();
            followTrait.toggle(sender, false);

            npc.addTrait(followTrait);

            TerminatorTrait terminatorTrait = new TerminatorTrait(npc, terminatorLoadout.clone());

            npc.addTrait(terminatorTrait);

            SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
            skinTrait.setSkinName(playerName);

            TerminatorNPC.terminators.add(npc);
        }
    }
}