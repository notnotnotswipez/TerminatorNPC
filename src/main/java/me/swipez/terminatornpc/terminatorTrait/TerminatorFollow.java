package me.swipez.terminatornpc.terminatorTrait;

import net.citizensnpcs.Settings;
import net.citizensnpcs.api.ai.flocking.FlockBehavior;
import net.citizensnpcs.api.ai.flocking.Flocker;
import net.citizensnpcs.api.ai.flocking.RadiusNPCFlock;
import net.citizensnpcs.api.ai.flocking.SeparationBehavior;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public class TerminatorFollow extends Trait {
    @Persist("active")
    private boolean enabled = false;
    private Flocker flock;
    @Persist
    private UUID followingUUID;
    private Player player;
    @Persist
    private boolean protect;

    public TerminatorFollow() {
        super("terminatorFollow");
    }

    public Player getFollowingPlayer() {
        return this.player;
    }

    public boolean isActive() {
        return this.enabled && this.npc.isSpawned() && this.player != null;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void onDespawn() {
        this.flock = null;
    }

    @EventHandler
    private void onEntityDamage(EntityDamageByEntityEvent event) {
        if (this.isActive() && this.protect && event.getEntity().equals(this.player)) {
            Entity damager = event.getDamager();
            if (event.getEntity() instanceof Projectile) {
                Projectile projectile = (Projectile)event.getEntity();
                if (projectile.getShooter() instanceof Entity) {
                    damager = (Entity)projectile.getShooter();
                }
            }

            this.npc.getNavigator().setTarget(damager, true);
        }

    }

    public void onSpawn() {
        this.flock = new Flocker(this.npc, new RadiusNPCFlock(4.0D, 0), new FlockBehavior[]{new SeparationBehavior(1.0D)});
    }

    public void run() {
        if (this.player == null || !this.player.isValid()) {
            if (this.followingUUID == null) {
                return;
            }

            this.player = Bukkit.getPlayer(this.followingUUID);
            if (this.player == null) {
                return;
            }
        }

        if (this.isActive()) {
            if (!this.npc.getNavigator().isNavigating()) {
                this.npc.getNavigator().setTarget(this.player, false);
            } else {
                this.flock.run();
            }
        }
    }

    public boolean toggle(OfflinePlayer player, boolean protect) {
        this.protect = protect;
        if (player.getUniqueId().equals(this.followingUUID) || this.followingUUID == null) {
            this.enabled = !this.enabled;
        }

        this.followingUUID = player.getUniqueId();
        if (this.npc.getNavigator().isNavigating() && this.player != null && this.npc.getNavigator().getEntityTarget() != null && this.player == this.npc.getNavigator().getEntityTarget().getTarget()) {
            this.npc.getNavigator().cancelNavigation();
        }

        this.player = null;
        return this.enabled;
    }
}
