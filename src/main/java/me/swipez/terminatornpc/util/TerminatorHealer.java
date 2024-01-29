package me.swipez.terminatornpc.util;

import me.swipez.terminatornpc.TerminatorNPC;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.GameRule;
import org.bukkit.attribute.Attribute;

import java.lang.Runnable;

public class TerminatorHealer {
    public TerminatorHealer(JavaPlugin plugin) {
        registerHealer(plugin);
    }
    
    private void registerHealer(JavaPlugin plugin) {
        // We regenerate health of the NPCs by 1 hp (half heart) per 4 seconds
        // This is the same as the player
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (NPC npc : TerminatorNPC.terminators) {
                LivingEntity entity = (LivingEntity) npc.getEntity();
                if (entity == null) continue;
                if (!entity.getWorld().getGameRuleValue(GameRule.NATURAL_REGENERATION)) continue;
                
                double healedHealth = entity.getHealth() + 1;
                if (healedHealth > entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
                    healedHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                }
                entity.setHealth(healedHealth);
            }
            }
        }, 0L, 80);
    }
}