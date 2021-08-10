package me.swipez.terminatornpc.stuckaction;

import me.swipez.terminatornpc.terminatorTrait.TerminatorTrait;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.StuckAction;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;

import java.util.Random;

public class TerminatorStuckAction implements StuckAction {

    Random random = new Random();

    @Override
    public boolean run(NPC npc, Navigator navigator) {
        for (Trait trait : npc.getTraits()){
            if (trait instanceof TerminatorTrait){
                TerminatorTrait terminatorTrait = (TerminatorTrait) trait;
                if (terminatorTrait.getTarget().getWorld().getUID().equals(terminatorTrait.getLivingEntity().getWorld().getUID())){
                    if (terminatorTrait.getLivingEntity().getLocation().distance(terminatorTrait.getTarget().getLocation()) > 200){
                        terminatorTrait.setTeleportTimer(0);
                        terminatorTrait.setTeleportTimer(1);
                    }
                    else {
                        terminatorTrait.setTeleportTimer((random.nextInt(30)+30)*20);
                    }
                }
                else {
                    terminatorTrait.updateTargetOtherWorldLocation();
                    terminatorTrait.setDimensionFollow(5*20);
                }
            }
        }
        return true;
    }
}
