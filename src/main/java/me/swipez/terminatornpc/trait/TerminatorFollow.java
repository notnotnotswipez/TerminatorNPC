package me.swipez.terminatornpc.trait;

import me.swipez.terminatornpc.TerminatorNPC;
import net.citizensnpcs.api.ai.flocking.Flocker;
import net.citizensnpcs.api.ai.flocking.RadiusNPCFlock;
import net.citizensnpcs.api.ai.flocking.SeparationBehavior;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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

    public boolean canTarget(Player player) {
        return !(player == null
                || player.getGameMode().equals(GameMode.CREATIVE)
                || player.getGameMode().equals(GameMode.SPECTATOR)
                || player.isInvulnerable()
                || TerminatorNPC.ignoredPlayers.contains(player.getUniqueId()));
    }

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
            Entity damagerEntity = event.getDamager();
            Player damager = null;
            
            if (damagerEntity instanceof Player) {
                damager = (Player)damagerEntity;
            } else if (damager instanceof Projectile) {
                Projectile projectile = (Projectile)damager;
                if (projectile.getShooter() instanceof Player) {
                    damager = (Player)projectile.getShooter();
                }
            }

            if (damager != null && canTarget(damager)) {
                this.npc.getNavigator().setTarget(damager, true);
            }
        }

    }

    public void onSpawn() {
        this.flock = new Flocker(this.npc, new RadiusNPCFlock(4.0D, 0), new SeparationBehavior(1.0D));
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

        if (this.isActive() && canTarget(player)) {
            if (!this.npc.getNavigator().isNavigating()) {
                this.npc.getNavigator().setTarget(this.player, false);
            } else {
                this.flock.run();
            }

            return;
        }

        this.npc.getNavigator().cancelNavigation();
    }

    public boolean toggle(OfflinePlayer player, boolean protect) {
        if (player == null) {
            return this.enabled;
        }
        
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