// 
// Decompiled by Procyon v0.5.36
// 

package me.swipez.terminatornpc.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;

public class Raycast  {
    private final double divider = 100.0;
    private ArrayList<Material> passthroughMaterials;
    private final ArrayList<Location> testedLocations;
    private World world;
    private double x;
    private double y;
    private double z;
    private double yaw;
    private double pitch;
    private double size;
    private RaycastType rayCastType;
    private Entity hurtEntity;
    private Block hurtBlock;
    private Location hurtLocation;
    private boolean showRayCast;
    private Entity owner;
    private Location rayCastLocation;
    
    public Raycast(final Location loc, final double size) {
        this(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(), size);
    }
    
    public Raycast(final World world, final double x, final double y, final double z, final double yaw, final double pitch, final double size) {
        this.passthroughMaterials = new ArrayList<Material>();
        this.testedLocations = new ArrayList<Location>();
        this.showRayCast = false;
        this.addPassthroughMaterial(Material.AIR);
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.size = size;
    }
    
    public boolean compute(final RaycastType rayCastType) {
        this.testedLocations.clear();
        int length = 0;
        this.computeLocation(new Vector(0.0, 0.0, length + 50.0));
        if (rayCastType == RaycastType.BLOCK) {
            this.rayCastType = RaycastType.BLOCK;
            while (this.passthroughMaterials.contains(this.rayCastLocation.getBlock().getType()) && length <= this.size * 100.0) {
                this.testedLocations.add(this.rayCastLocation);
                ++length;
                this.computeLocation(new Vector(0.0, 0.0, length + 50.0));
                if (this.showRayCast) {
                    this.world.spawnParticle(Particle.CLOUD, this.rayCastLocation.getX(), this.rayCastLocation.getY(), this.rayCastLocation.getZ(), 0, 0.0, 0.0, 0.0);
                }
            }
            if (!this.passthroughMaterials.contains(this.rayCastLocation.getBlock().getType())) {
                this.hurtBlock = this.rayCastLocation.getBlock();
                this.hurtLocation = this.rayCastLocation;
                return true;
            }
        }
        else if (rayCastType == RaycastType.ENTITY) {
            this.rayCastType = RaycastType.ENTITY;
            Collection<Entity> entities = this.world.getNearbyEntities(this.rayCastLocation, 0.01, 0.01, 0.01);
            while ((entities.size() <= 0 || entities.contains(this.owner)) && length <= this.size * 100.0) {
                this.testedLocations.add(this.rayCastLocation);
                ++length;
                this.computeLocation(new Vector(0.0, 0.0, length + 50.0));
                entities = this.world.getNearbyEntities(this.rayCastLocation, 0.01, 0.01, 0.01);
                if (this.showRayCast) {
                    this.world.spawnParticle(Particle.CLOUD, this.rayCastLocation.getX(), this.rayCastLocation.getY(), this.rayCastLocation.getZ(), 0, 0.0, 0.0, 0.0);
                }
            }
            if (entities.size() > 0) {
                for (final Entity entity : entities) {
                    this.hurtEntity = entity;
                    this.hurtLocation = this.rayCastLocation;
                }
                return true;
            }
        }
        else if (rayCastType == RaycastType.ENTITY_AND_BLOCK) {
            Collection<Entity> entities = this.world.getNearbyEntities(this.rayCastLocation, 0.01, 0.01, 0.01);
            while (this.passthroughMaterials.contains(this.rayCastLocation.getBlock().getType()) && (entities.size() <= 0 || entities.contains(this.owner)) && length <= this.size * 100.0) {
                this.testedLocations.add(this.rayCastLocation);
                ++length;
                this.computeLocation(new Vector(0.0, 0.0, length + 50.0));
                entities = this.world.getNearbyEntities(this.rayCastLocation, 0.01, 0.01, 0.01);
                if (this.showRayCast) {
                    this.world.spawnParticle(Particle.CLOUD, this.rayCastLocation.getX(), this.rayCastLocation.getY(), this.rayCastLocation.getZ(), 0, 0.0, 0.0, 0.0);
                }
            }
            if (!this.passthroughMaterials.contains(this.rayCastLocation.getBlock().getType())) {
                this.rayCastType = RaycastType.BLOCK;
                this.hurtBlock = this.rayCastLocation.getBlock();
                this.hurtLocation = this.rayCastLocation;
                return true;
            }
            if (entities.size() > 0) {
                this.rayCastType = RaycastType.ENTITY;
                for (final Entity entity : entities) {
                    this.hurtEntity = entity;
                    this.hurtLocation = this.rayCastLocation;
                }
                return true;
            }
        }
        return false;
    }
    
    private void computeLocation(Vector rayCastPos) {
        rayCastPos = RaycastAPIMath.rotate(rayCastPos, this.yaw, this.pitch);
        this.rayCastLocation = new Location(this.world, this.x, this.y, this.z).clone().add(rayCastPos.getX() / 100.0, rayCastPos.getY() / 100.0, rayCastPos.getZ() / 100.0);
    }
    
    public ArrayList<Material> getPassthroughMaterials() {
        return this.passthroughMaterials;
    }
    
    public void setPassthroughMaterials(final ArrayList<Material> passthroughMaterials) {
        this.passthroughMaterials = passthroughMaterials;
    }
    
    public void addPassthroughMaterial(final Material mat) {
        if (!this.passthroughMaterials.contains(mat)) {
            this.passthroughMaterials.add(mat);
        }
    }
    
    public ArrayList<Location> getTestedLocations() {
        return this.testedLocations;
    }
    
    public World getWorld() {
        return this.world;
    }
    
    public void setWorld(final World world) {
        this.world = world;
    }
    
    public double getX() {
        return this.x;
    }
    
    public void setX(final double x) {
        this.x = x;
    }
    
    public double getY() {
        return this.y;
    }
    
    public void setY(final double y) {
        this.y = y;
    }
    
    public double getZ() {
        return this.z;
    }
    
    public void setZ(final double z) {
        this.z = z;
    }
    
    public double getYaw() {
        return this.yaw;
    }
    
    public void setYaw(final double yaw) {
        this.yaw = yaw;
    }
    
    public double getPitch() {
        return this.pitch;
    }
    
    public void setPitch(final double pitch) {
        this.pitch = pitch;
    }
    
    public double getSize() {
        return this.size;
    }
    
    public void setSize(final double size) {
        this.size = size;
    }
    
    public RaycastType getRayCastType() {
        return this.rayCastType;
    }
    
    public void setRayCastType(final RaycastType rayCastType) {
        this.rayCastType = rayCastType;
    }
    
    public Entity getHurtEntity() {
        return this.hurtEntity;
    }
    
    public void setHurtEntity(final Entity hurtEntity) {
        this.hurtEntity = hurtEntity;
    }
    
    public Block getHurtBlock() {
        return this.hurtBlock;
    }
    
    public void setHurtBlock(final Block hurtBlock) {
        this.hurtBlock = hurtBlock;
    }
    
    public Location getHurtLocation() {
        return this.hurtLocation;
    }
    
    public void setHurtLocation(final Location hurtLocation) {
        this.hurtLocation = hurtLocation;
    }
    
    public boolean isShowRayCast() {
        return this.showRayCast;
    }
    
    public void setShowRayCast(final boolean showRayCast) {
        this.showRayCast = showRayCast;
    }
    
    public double getDivider() {
        return 100.0;
    }
    
    public Entity getOwner() {
        return this.owner;
    }
    
    public void setOwner(final Entity owner) {
        this.owner = owner;
    }
    
    public enum RaycastType
    {
        ENTITY_AND_BLOCK("ENTITY_AND_BLOCK", 0), 
        ENTITY("ENTITY", 1), 
        BLOCK("BLOCK", 2);
        
        RaycastType(final String name, final int ordinal) {
        }
    }
}