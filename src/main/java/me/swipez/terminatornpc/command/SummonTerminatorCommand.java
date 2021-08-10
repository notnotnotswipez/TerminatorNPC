package me.swipez.terminatornpc.command;

import me.swipez.terminatornpc.TerminatorNPC;
import me.swipez.terminatornpc.stuckaction.TerminatorStuckAction;
import me.swipez.terminatornpc.terminatorTrait.TerminatorFollow;
import me.swipez.terminatornpc.terminatorTrait.TerminatorTrait;
import net.citizensnpcs.Settings;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.trait.FollowTrait;
import net.citizensnpcs.trait.Gravity;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class SummonTerminatorCommand implements CommandExecutor {

    static int id = 0;
    public static List<NPC> terminators = new LinkedList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player){
            if (sender.hasPermission("terminatornpc.spawnterminator")){
                if (args.length == 1){
                    summonTerminator(args[0], 1, (Player) sender);
                }
                if (args.length == 2){
                    summonTerminator(args[0], Integer.parseInt(args[1]), (Player) sender);
                }
            }
        }
        return true;
    }


    private void summonTerminator(String playerName, int number, Player sender){
        for (int i = 0; i < number; i++){
            id++;
            NPC npc = new CitizensNPC(UUID.randomUUID(), id, playerName, EntityControllers.createForType(EntityType.PLAYER), CitizensAPI.getNPCRegistry());
            npc.spawn(((Player) sender).getLocation());
            npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, false);

            npc.getNavigator().getLocalParameters()
                    .attackRange(10)
                    .baseSpeed(1.6F)
                    .straightLineTargetingDistance(40)
                    .stuckAction(new TerminatorStuckAction())
                    .range(40);

            TerminatorFollow followTrait = new TerminatorFollow();
            followTrait.linkToNPC(npc);
            followTrait.run();
            followTrait.toggle((Player) sender, false);

            npc.addTrait(followTrait);

            TerminatorTrait terminatorTrait = new TerminatorTrait(npc);

            npc.addTrait(terminatorTrait);

            terminators.add(npc);
        }
    }
}
