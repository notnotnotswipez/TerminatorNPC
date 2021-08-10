package me.swipez.terminatornpc;

import me.swipez.terminatornpc.command.RemoveTerminatorsCommand;
import me.swipez.terminatornpc.command.SummonTerminatorCommand;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.plugin.java.JavaPlugin;

public final class TerminatorNPC extends JavaPlugin {

    private static TerminatorNPC plugin;

    @Override
    public void onEnable() {
        plugin = this;
        getCommand("terminator").setExecutor(new SummonTerminatorCommand());
        getCommand("clearterminators").setExecutor(new RemoveTerminatorsCommand());
    }

    @Override
    public void onDisable() {
       for (NPC npc : SummonTerminatorCommand.terminators){
           npc.despawn();
       }
    }

    public static TerminatorNPC getPlugin() {
        return plugin;
    }
}
