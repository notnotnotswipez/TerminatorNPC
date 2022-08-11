package me.swipez.terminatornpc;

import me.swipez.terminatornpc.command.*;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.plugin.java.JavaPlugin;

public final class TerminatorNPC extends JavaPlugin {
    private static TerminatorNPC plugin;

    @Override
    public void onEnable() {
        plugin = this;
        getCommand("terminator").setExecutor(new TerminatorSummon());
        getCommand("terminatorloadout").setExecutor(new TerminatorLoadout());
        getCommand("clearterminators").setExecutor(new TerminatorClear());
        getCommand("terminatorignore").setExecutor(new PlayerIgnore());
    }

    @Override
    public void onDisable() {
       for (NPC npc : TerminatorSummon.terminators){
           npc.despawn();
       }
    }

    public static TerminatorNPC getPlugin() {
        return plugin;
    }
}