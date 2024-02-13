package me.swipez.terminatornpc.util;

import me.swipez.terminatornpc.TerminatorNPC;
import me.swipez.terminatornpc.trait.TerminatorStuckAction;
import me.swipez.terminatornpc.trait.TerminatorFollow;
import me.swipez.terminatornpc.trait.TerminatorTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.EntityControllers;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

// This class contains all data required to summon terminators
public class TerminatorSummoner {
    private String name;
    private Location location;
    private Player owner;
    private int amount;

    public String getName() { return this.name; }
    public Location getLocation() { return this.location; }
    public Player getOwner() { return this.owner; }
    public int getAmount() { return this.amount; }

    public TerminatorSummoner(String name, Player owner, Location location, int amount) {
        this.name = name;
        this.location = location;
        this.owner = owner;
        this.amount = amount;
    }

    public void summon(){
        TerminatorNPC.terminatorLoadoutHashMap.putIfAbsent(owner.getUniqueId(), new me.swipez.terminatornpc.loadout.TerminatorLoadout(TerminatorNPC.getPlugin()));
        me.swipez.terminatornpc.loadout.TerminatorLoadout terminatorLoadout = TerminatorNPC.terminatorLoadoutHashMap.get(owner.getUniqueId());

        for (int i = 0; i < amount; i++){
            NPC npc = new CitizensNPC(UUID.randomUUID(), TerminatorNPC.getUniqueID(), name, EntityControllers.createForType(EntityType.PLAYER), CitizensAPI.getNPCRegistry());
            npc.spawn(location);
            npc.data().set(NPC.Metadata.DEFAULT_PROTECTED, false);
            npc.data().set(NPC.Metadata.TARGETABLE, false);
            npc.data().set(NPC.Metadata.COLLIDABLE, true);
            npc.data().set(NPC.Metadata.FLUID_PUSHABLE, true);
            npc.data().set(NPC.Metadata.SWIMMING, true);

            npc.getNavigator().getLocalParameters()
                    .attackRange(10)
                    .baseSpeed(1.6F * 10F)
                    .straightLineTargetingDistance(100)
                    .stuckAction(new TerminatorStuckAction())
                    .range(40);

            TerminatorFollow followTrait = new TerminatorFollow();
            followTrait.linkToNPC(npc);
            followTrait.run();
            followTrait.toggle(owner, false);

            npc.addTrait(followTrait);

            TerminatorTrait terminatorTrait = new TerminatorTrait(npc, terminatorLoadout.clone());

            npc.addTrait(terminatorTrait);

            SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
            skinTrait.setSkinName(name);

            TerminatorNPC.terminators.add(npc);
        }
    }
}