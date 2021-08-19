package me.swipez.terminatornpc.terminatorTrait;

import me.swipez.terminatornpc.TerminatorNPC;
import me.swipez.terminatornpc.helper.TerminatorUtils;
import me.swipez.terminatornpc.loadout.ArmorItemValues;
import me.swipez.terminatornpc.loadout.AttackItemValues;
import me.swipez.terminatornpc.loadout.TerminatorLoadout;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.Util;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

public class TerminatorTrait extends Trait {

    Random random = new Random();

    private final NPC terminator;
    private UUID activeTarget = null;
    private int attackCooldown = 0;
    private int jumpCooldown = 0;
    private final List<Block> scheduledBrokenBlocks = new LinkedList<>();
    private final int state = 0;

    // Built in stuck features only apply if the NPC is just far away it seems. The name is misleading
    boolean isStuck = false;
    boolean hasLineOfSight = true;

    boolean updatedThisTick = false;
    int followThroughDimension = 0;


    boolean shouldBeStopped = false;

    boolean isCurrentlyPlacingUpwards = false;
    boolean isCurrentlyDiggingDownwards = false;

    boolean isCurrentlyBreakingABlock = false;
    Block blockCurrentlyBeingBroken = null;

    boolean isBreakingWhileUpwards = false;
    boolean teleportedRecently = false;

    int blockPlaceCooldown = 0;
    int targetSearchTimer = 0;
    int respawnTimer = 0;

    boolean shouldJump = true;
    public int blockPlaceTimeout = 0;
    Location targetsSwitchedWorldsLocation = null;
    int boatClutchCooldown = 0;

    List<BlockFace> placeableBlockFaces = new LinkedList<>();

    Location location;

    boolean isInWater = false;

    int teleportTimer = 0;

    public boolean delete = false;
    public boolean isFullSwimming = false;

    public boolean needsArmorUpdate = true;

    private final TerminatorLoadout terminatorLoadout;

    public boolean debug = false;

    public TerminatorTrait(NPC terminator, TerminatorLoadout terminatorLoadout) {
        super("terminator");
        this.terminator = terminator;
        location = terminator.getEntity().getLocation();

        placeableBlockFaces.add(BlockFace.UP);
        placeableBlockFaces.add(BlockFace.DOWN);
        placeableBlockFaces.add(BlockFace.NORTH);
        placeableBlockFaces.add(BlockFace.SOUTH);
        placeableBlockFaces.add(BlockFace.EAST);
        placeableBlockFaces.add(BlockFace.WEST);

        this.terminatorLoadout = terminatorLoadout;
    }

    public void updateTargetOtherWorldLocation(){
        if (targetsSwitchedWorldsLocation == null){
            targetsSwitchedWorldsLocation = getTarget().getLocation();
        }
    }

    public void setDimensionFollow(int dimensionFollow){
        if (followThroughDimension == 0){
            followThroughDimension = dimensionFollow;
        }
    }

    public void setTeleportTimer(int teleportTimer){
        if (teleportTimer == 0){
            this.teleportTimer = 0;
        }
        else {
            if (this.teleportTimer == 0){
                this.teleportTimer = teleportTimer;
                if (debug){
                    Bukkit.getLogger().log(Level.WARNING, "Teleport timer set to: "+teleportTimer+" ticks");
                }
            }
        }
    }

    public void setCurrentlyBreakingABlock(boolean isCurrentlyBreakingABlock) {
        this.isCurrentlyBreakingABlock = isCurrentlyBreakingABlock;
    }

    public void setShouldBeStopped(boolean shouldBeStopped){
        this.shouldBeStopped = shouldBeStopped;
    }

    public boolean isTargeting(){
        for (Trait trait : npc.getTraits()){
            if (trait instanceof TerminatorFollow){
                TerminatorFollow followTrait = (TerminatorFollow) trait;
                return followTrait.isEnabled();
            }
        }
        return false;
    }

    int blockBreakTimeout = 0;


    @Override
    public void run() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!delete){
                    try {
                        if (npc.isSpawned()) {
                            if (needsArmorUpdate){
                                if (debug){
                                    Bukkit.getLogger().log(Level.INFO, "NPC requested an armor update!");
                                }
                                updateArmor();
                                needsArmorUpdate = false;
                            }
                            checkForNewTarget();
                            cooldownsDecrement();
                            if (isInWater){
                                if (!getLivingEntity().isSwimming()){
                                    if (debug){
                                        Bukkit.getLogger().log(Level.INFO, "NPC was set to swim!");
                                    }
                                    ((Player) getLivingEntity()).setSprinting(true);
                                    getLivingEntity().setSwimming(true);
                                }
                                if (!isFullSwimming){
                                    if (getLivingEntity().getLocation().clone().add(0,1,0).getBlock().isLiquid()){
                                        if (debug){
                                            Bukkit.getLogger().log(Level.INFO, "NPC was set to fully swim!");
                                        }
                                        getLivingEntity().setGliding(true);
                                        isFullSwimming = true;
                                    }
                                }
                                else {
                                    if (!getLivingEntity().getLocation().clone().add(0,1,0).getBlock().isLiquid()){
                                        if (debug){
                                            Bukkit.getLogger().log(Level.INFO, "NPC was set to stop swimming!");
                                        }
                                        isFullSwimming = false;
                                        getLivingEntity().setGliding(false);
                                    }
                                }
                            }
                            else {
                                isFullSwimming = false;
                                getLivingEntity().setSwimming(false);
                                ((Player) getLivingEntity()).setSprinting(false);
                            }
                            if (!shouldBeStopped) {
                                if (blockPlaceCooldown == 0){
                                    if (distanceToGround(4) && !isInWater) {
                                        placeBlockUnderFeet(terminatorLoadout.getBlockMaterial());
                                    }
                                    if (getLivingEntity().getLocation().clone().subtract(0,1,0).getBlock().getType().equals(Material.WATER) && !isFullSwimming){
                                        placeBlockUnderFeet(terminatorLoadout.getBlockMaterial());
                                    }
                                }
                                if (getLivingEntity().getLocation().clone().subtract(0,1,0).getBlock().getType().equals(Material.LAVA)){
                                    if (boatClutchCooldown == 0){
                                        setMainHandItem(new ItemStack(Material.OAK_BOAT));
                                        PlayerAnimation.ARM_SWING.play((Player) getLivingEntity());
                                        getLivingEntity().getWorld().spawnEntity(getLivingEntity().getLocation().clone().subtract(0,0.9,0), EntityType.BOAT);
                                        tryJump(0.7, true);
                                        boatClutchCooldown = 10;
                                        lookDown();
                                    }
                                }
                            }
                            if (!scheduledBrokenBlocks.isEmpty()) {
                                shouldJump = false;
                            }
                            if (getTarget() != null && getLivingEntity() != null && getTarget().getWorld().getUID().equals(getLivingEntity().getWorld().getUID())) {
                                if (isCurrentlyDiggingDownwards) {
                                    if (getLivingEntity().isOnGround()) {
                                        if (!getLivingEntity().getLocation().clone().subtract(0, 1, 0).getBlock().getType().isAir()) {
                                            centerOnBlock();
                                            if (!scheduledBrokenBlocks.contains(getLivingEntity().getLocation().clone().subtract(0, 1, 0).getBlock())) {
                                                if (debug){
                                                    Bukkit.getLogger().log(Level.INFO, "Added block underneath me to queued blocks. (Dig downwards)");
                                                }
                                                scheduledBrokenBlocks.add(getLivingEntity().getLocation().clone().subtract(0, 1, 0).getBlock());
                                            }
                                        }
                                    }
                                }
                                if (isCurrentlyPlacingUpwards && getLivingEntity().isOnGround()) {
                                    shouldBeStopped = true;
                                    if (blockPlaceCooldown == 0) {
                                        blockPlaceTimeout++;
                                        if (blockPlaceTimeout == 10) {
                                            isCurrentlyPlacingUpwards = false;
                                            enableMovement();
                                            blockPlaceTimeout = 0;
                                        }
                                        if (isTargeting()) {
                                            disableMovement();
                                        }
                                        if (getLivingEntity().isOnGround()) {
                                            if (aboveHeadIsAir()) {
                                                if (debug){
                                                    Bukkit.getLogger().log(Level.INFO, "Placed block underneath to go upwards.");
                                                }
                                                getLivingEntity().teleport(getLivingEntity().getLocation().clone().add(0,1,0));
                                                lookDown();
                                                placeBlockUnderFeet(terminatorLoadout.getBlockMaterial());
                                            } else {
                                                if (debug){
                                                    Bukkit.getLogger().log(Level.INFO, "Breaking Block above head.");
                                                }
                                                isBreakingWhileUpwards = true;
                                                Block chosenBrokenBlock = getLivingEntity().getLocation().clone().add(0, 2, 0).getBlock();
                                                getProperToolToBreakBlock(chosenBrokenBlock);
                                                breakBlock(getLivingEntity().getLocation().clone().add(0, 2, 0));
                                            }
                                        }
                                        blockPlaceCooldown = 10;
                                    }
                                }
                                else {
                                    if (!isTargeting()) {
                                        enableMovement();
                                    }
                                }
                                if (!isCurrentlyBreakingABlock && blockCurrentlyBeingBroken == null && !isCurrentlyPlacingUpwards) {
                                    if (!scheduledBrokenBlocks.isEmpty()) {
                                        Block chosenBrokenBlock = null;
                                        for (Block block : scheduledBrokenBlocks) {
                                            chosenBrokenBlock = block;
                                            break;
                                        }
                                        if (chosenBrokenBlock != null && !chosenBrokenBlock.isEmpty()) {
                                            if (debug){
                                                Bukkit.getLogger().log(Level.INFO, "Selected a block to break.");
                                            }
                                            blockCurrentlyBeingBroken = chosenBrokenBlock;
                                            isCurrentlyBreakingABlock = true;
                                            getProperToolToBreakBlock(chosenBrokenBlock);
                                            breakBlock(chosenBrokenBlock.getLocation());
                                            disableMovement();
                                        }
                                    } else {
                                        if (!isTargeting()) {
                                            enableMovement();
                                        }
                                    }
                                } else {
                                    if (!isCurrentlyPlacingUpwards) {
                                        blockBreakTimeout++;
                                        if (blockBreakTimeout == 20) {
                                            cancelBlockBreaking();
                                            scheduledBrokenBlocks.clear();
                                            blockBreakTimeout = 0;
                                        }
                                        if (scheduledBrokenBlocks.isEmpty()) {
                                            cancelBlockBreaking();
                                        }
                                    }
                                }
                                if (!isCurrentlyBreakingABlock && !isCurrentlyPlacingUpwards && !isCurrentlyDiggingDownwards && !isBreakingWhileUpwards){
                                    shouldJump = true;
                                }
                                update();
                                checkForLocationUpdate();
                                if (shouldJump && !isCurrentlyBreakingABlock) {
                                    tryJump(0.5, false);
                                }
                            }
                        }
                        else {
                            if (respawnTimer == 0){
                                if (debug){
                                    Bukkit.getLogger().log(Level.INFO, "Respawn timer set to 30 seconds.");
                                }
                                respawnTimer = 30*20;
                            }
                            else {
                                respawnTimer--;
                                if (respawnTimer == 0) {
                                    if (debug){
                                        Bukkit.getLogger().log(Level.WARNING, "NPC spawned at: "+location.toString());
                                    }
                                    npc.spawn(getRandomLocation(getTarget().getLocation(), 10, 20));
                                    npc.data().set(NPC.DEFAULT_PROTECTED_METADATA, false);
                                    needsArmorUpdate = true;
                                    teleportToAvailableSlot();
                                }
                            }
                        }
                    } catch (NullPointerException exception) {
                        // Ignore
                    }
                }
                else {
                    cancel();
                }
            }
        }.runTaskTimer(TerminatorNPC.getPlugin(), 1, 1);
        super.run();
    }

    public void getProperToolToBreakBlock(Block chosenBrokenBlock){
        if (chosenBrokenBlock.getType().equals(Material.DIRT) || chosenBrokenBlock.getType().equals(Material.GRASS_BLOCK) || chosenBrokenBlock.getType().equals(Material.GRAVEL) || chosenBrokenBlock.getType().equals(Material.SAND) || chosenBrokenBlock.getType().equals(Material.SOUL_SAND) || chosenBrokenBlock.getType().equals(Material.SOUL_SOIL) ) {
            setMainHandItem(new ItemStack(Material.DIAMOND_SHOVEL));
        } else if (chosenBrokenBlock.getType().toString().toLowerCase().contains("leaves") || chosenBrokenBlock.getType().toString().toLowerCase().contains("wart")) {
            setMainHandItem(new ItemStack(Material.SHEARS));
        } else if (chosenBrokenBlock.getType().toString().toLowerCase().contains("log") || chosenBrokenBlock.getType().toString().toLowerCase().contains("plank") || chosenBrokenBlock.getType().toString().toLowerCase().contains("stem")) {
            setMainHandItem(new ItemStack(Material.DIAMOND_AXE));
        } else {
            setMainHandItem(new ItemStack(Material.DIAMOND_PICKAXE));
        }
    }

    public void teleportToAvailableSlot(){
        if (!teleportedRecently){
            Location location = getRandomLocation(getTarget().getLocation(), 10, 20);
            int checks = 0;
            while (!locationIsTeleportable(location) && checks != 100 && locationIsVisible(getTarget(), location)){
                location = getRandomLocation(getTarget().getLocation(), 15, 20);
                checks++;
            }

            if (!locationIsVisible(getTarget(), location)){
                isCurrentlyBreakingABlock = false;
                isCurrentlyDiggingDownwards = false;
                isCurrentlyPlacingUpwards = false;
                enableMovement();
                getLivingEntity().teleport(location);
                if (debug){
                    Bukkit.getLogger().log(Level.WARNING, "NPC teleported to: "+location.toString());
                }
            }
            else {
                setTeleportTimer((random.nextInt(10))*20);
                if (debug){
                    Bukkit.getLogger().log(Level.SEVERE, "NPC couldnt teleport to spot. Reverted back to timer.");
                }
            }
            teleportedRecently = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    teleportedRecently = false;
                }
            }.runTaskLater(TerminatorNPC.getPlugin(), 40);
        }
    }

    private boolean locationIsTeleportable(Location location) {
        if (location.clone().subtract(0, 1, 0).getBlock().getType().isSolid() && !location.clone().subtract(0, 1, 0).getBlock().isLiquid() && location.getBlock().getType().isAir()) {
            return location.clone().add(0, 1, 0).getBlock().getType().isAir();
        }
        return false;
    }

    private Location getRandomLocation(Location location, int min, int max){
        int randomX = random.nextInt(max-min)+min;
        int randomZ = random.nextInt(max-min)+min;

        if (random.nextBoolean()){
            randomX *= -1;
        }
        if (random.nextBoolean()){
            randomZ *= -1;
        }

        Location clone = location.clone();
        clone.setX(clone.getX()+randomX);
        clone.setZ(clone.getZ()+randomZ);

        clone.setY(clone.getY()+20);

        int checksUpwards = 0;
        while (clone.getBlock().getRelative(BlockFace.DOWN).getType().isAir() && checksUpwards != 40){
            clone.subtract(0,1,0);
            checksUpwards++;
        }

        while (!clone.getBlock().isEmpty()){
            clone.add(0,1,0);
        }
        return clone;
    }

    boolean locationIsVisible(Player player,Location location) {
        Vector facingNormal = player.getLocation().getDirection().normalize();
        Vector playerEntityVecNormal = player.getEyeLocation().toVector().subtract(location.toVector()).normalize();
        return playerEntityVecNormal.dot(facingNormal) < 0;
    }

    private boolean distanceToGround(int distance){
        Location location = getLivingEntity().getLocation().clone();
        Block testBlock = location.getBlock();
        boolean isAirAllTheWay = true;
        for (int i = 0; i < distance; i++){
            testBlock = location.clone().subtract(0,i,0).getBlock();
            if (testBlock.getType().isSolid() || testBlock.isLiquid()){
                isAirAllTheWay = false;
                break;
            }
        }
        return isAirAllTheWay;
    }

    private boolean aboveHeadIsAir(){
        return getLivingEntity().getLocation().clone().add(0,2,0).getBlock().isEmpty();
    }

    private void placeBlockUnderFeet(Material material){
        blockBreakTimeout = 0;
        shouldBeStopped = false;
        setMainHandItem(new ItemStack(material));
        if (canPlaceBlock(getLivingEntity().getLocation().clone().subtract(0,1,0))){
            lookDown();
            PlayerAnimation.ARM_SWING.play((Player) getLivingEntity());
            getLivingEntity().getLocation().clone().subtract(0,1,0).getBlock().setType(material);
            getLivingEntity().getWorld().playSound(getLivingEntity().getLocation().clone().subtract(0,1,0), Sound.BLOCK_STONE_PLACE, 1, 1);
            blockPlaceCooldown = 10;
        }
    }

    private void lookDown(){
        Util.assumePose(getLivingEntity(), getLivingEntity().getLocation().getYaw(), 90);
    }

    private boolean canPlaceBlock(Location location){
        if (location.distance(getLivingEntity().getLocation()) <= 5){
            if (location.getBlock().getType().isAir() || !location.getBlock().getType().isSolid() || location.getBlock().isLiquid()){
                for (BlockFace blockFace : placeableBlockFaces){
                    if (location.getBlock().getRelative(blockFace).getType().isSolid() && !location.getBlock().getRelative(blockFace).isLiquid()){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void breakBlock(Location location){
        BlockBreaker.BlockBreakerConfiguration config = new BlockBreaker.BlockBreakerConfiguration();
        config.item(((Player) getLivingEntity()).getInventory().getItemInMainHand());
        config.radius(3);

        if (!location.getBlock().getType().isAir()){
            BlockBreaker breaker = npc.getBlockBreaker(location.getBlock(), config);
            if (breaker.shouldExecute()) {
                TaskRunnable run = new TaskRunnable(breaker, this, location);
                run.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(TerminatorNPC.getPlugin(), run, 0, 1);
            }
        }
    }

    private double getTargetYDifference(){
        return getTarget().getLocation().getY() - getLivingEntity().getLocation().getY();
    }

    public Block getBlockCurrentlyBeingBroken() {
        return blockCurrentlyBeingBroken;
    }

    private static class TaskRunnable implements Runnable {
        private int taskId;
        private final BlockBreaker breaker;
        private final TerminatorTrait terminatorTrait;
        private final Location location;

        public TaskRunnable(BlockBreaker breaker, TerminatorTrait terminatorTrait, Location location) {
            this.location = location;
            this.breaker = breaker;
            this.terminatorTrait = terminatorTrait;
        }

        @Override
        public void run() {
            if (terminatorTrait.isCurrentlyBreakingABlock || terminatorTrait.isBreakingWhileUpwards){
                if (breaker.run() != BehaviorStatus.RUNNING) {
                    Bukkit.getScheduler().cancelTask(taskId);
                    breaker.reset();
                    location.getWorld().playSound(location, Sound.BLOCK_STONE_BREAK, 1, 1);
                    if (terminatorTrait.isCurrentlyBreakingABlock){
                        terminatorTrait.cancelBlockBreaking();
                    }
                    if (terminatorTrait.isBreakingWhileUpwards){
                        terminatorTrait.isBreakingWhileUpwards = false;
                    }

                }
                else {
                    terminatorTrait.disableMovement();
                }
            }
            else {
                Bukkit.getScheduler().cancelTask(taskId);
            }
        }
    }

    private void setNewTarget(Player player){
        for (Trait trait : npc.getTraits()){
            if (trait instanceof TerminatorFollow){
                TerminatorFollow followTrait = (TerminatorFollow) trait;
                followTrait.toggle(player, false);
            }
        }
    }

    private void update(){
        isInWater = getLivingEntity().getLocation().getBlock().getType().equals(Material.WATER);
        if (activeTarget != null){
            Player player = Bukkit.getPlayer(activeTarget);
            if (!shouldBeStopped){
                attemptAttack(player);
            }
            hasLineOfSight = canSee(getTarget());
            if (isCurrentlyBreakingABlock && blockCurrentlyBeingBroken != null){
                if (isFarFromChosenBlock()){
                    cancelBlockBreaking();
                }
            }
        }
    }

    private boolean isFarFromChosenBlock(){
        return !withinMargin(blockCurrentlyBeingBroken.getLocation(), getLivingEntity().getLocation(), 3) || blockCurrentlyBeingBroken.getType().isAir();
    }

    private void updateArmor(){
        Equipment equipment = npc.getOrAddTrait(Equipment.class);
        List<Double> heartAdditions = new LinkedList<>();
        if (terminatorLoadout.getHelmetMaterial() != null){
            equipment.set(Equipment.EquipmentSlot.HELMET, new ItemStack(terminatorLoadout.getHelmetMaterial()));
            heartAdditions.add(((20*ArmorItemValues.valueOf(terminatorLoadout.getHelmetMaterial().toString()).getHealthMultiplier())-20));
        }
        if (terminatorLoadout.getChestplateMaterial() != null){
            equipment.set(Equipment.EquipmentSlot.CHESTPLATE, new ItemStack(terminatorLoadout.getChestplateMaterial()));
            heartAdditions.add(((20*ArmorItemValues.valueOf(terminatorLoadout.getChestplateMaterial().toString()).getHealthMultiplier())-20));
        }
        if (terminatorLoadout.getLeggingsMaterial() != null){
            equipment.set(Equipment.EquipmentSlot.LEGGINGS, new ItemStack(terminatorLoadout.getLeggingsMaterial()));
            heartAdditions.add(((20*ArmorItemValues.valueOf(terminatorLoadout.getLeggingsMaterial().toString()).getHealthMultiplier())-20));
        }
        if (terminatorLoadout.getBootsMaterial() != null){
            equipment.set(Equipment.EquipmentSlot.BOOTS, new ItemStack(terminatorLoadout.getBootsMaterial()));
            heartAdditions.add(((20*ArmorItemValues.valueOf(terminatorLoadout.getBootsMaterial().toString()).getHealthMultiplier())-20));
        }

        double total = 0;

        for (Double doubles : heartAdditions){
            total += doubles;
        }

        getLivingEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20+total);
    }

    private double getAttackRangeAddition(){
        if (terminatorLoadout.getSwordMaterial() == null){
            return 0.5;
        }
        else {
            return 0;
        }
    }

    private boolean attemptAttack(Player player){
        Location targetLocation = player.getLocation();
        double attackRange = 2.5;
        double rangeAddition = getAttackRangeAddition();
        if (getLivingEntity().getLocation().distance(targetLocation) <= (attackRange+rangeAddition)){
            if (attackCooldown == 0){
                if (!shouldBeStopped){
                    Util.faceLocation(npc.getEntity(), player.getLocation());
                    Block block = getBlockInFront(1).getBlock();
                    if (block.isEmpty()){
                        if (debug){
                            Bukkit.getLogger().log(Level.INFO, "NPC is attacking.");
                        }
                        double health = player.getHealth();
                        double attackDamage;
                        int cooldown;
                        if (terminatorLoadout.getSwordMaterial() != null){
                            setMainHandItem(new ItemStack(terminatorLoadout.getSwordMaterial()));
                            AttackItemValues attackItemValues = AttackItemValues.valueOf(terminatorLoadout.getSwordMaterial().toString());
                            attackDamage = attackItemValues.getDamage();
                            cooldown = attackItemValues.getCooldown();
                        }
                        else {
                            setMainHandItem(null);
                            attackDamage = AttackItemValues.FIST.getDamage();
                            cooldown = AttackItemValues.FIST.getCooldown();
                        }


                        player.damage(attackDamage, getLivingEntity());
                        double newHealth = player.getHealth();
                        if (newHealth < health){
                            player.setVelocity(player.getVelocity().add(getLivingEntity().getLocation().getDirection().multiply(0.3)));
                        }
                        PlayerAnimation.ARM_SWING.play((Player) getLivingEntity());
                        scheduledBrokenBlocks.clear();
                        attackCooldown = cooldown;
                        return true;
                    }
                    else {
                        if (debug){
                            Bukkit.getLogger().log(Level.INFO, "Added block underneath me to queued blocks. (Block in way while attacking)");
                        }
                        scheduledBrokenBlocks.add(block);
                    }
                }
            }
        }
        return false;
    }

    private void cancelBlockBreaking(){
        isCurrentlyBreakingABlock = false;
        scheduledBrokenBlocks.remove(blockCurrentlyBeingBroken);
        blockCurrentlyBeingBroken = null;
        enableMovement();
    }

    private Location getBlockInFront(int length){
        Location eye = getLivingEntity().getEyeLocation();
        Vector vector = eye.getDirection();

        return eye.clone().add(vector.multiply(length));
    }

    private void setMainHandItem(ItemStack item){
        ((Player) getLivingEntity()).getInventory().setItemInMainHand(item);
    }

    private void cooldownsDecrement(){
        if (attackCooldown > 0){
            attackCooldown--;
        }
        if (jumpCooldown > 0){
            jumpCooldown--;
        }
        if (blockPlaceCooldown > 0){
            blockPlaceCooldown--;
        }
        if (targetSearchTimer > 0){
            targetSearchTimer--;
        }
        if (boatClutchCooldown > 0){
            boatClutchCooldown--;
        }
        if (followThroughDimension > 0){
            followThroughDimension--;
            if (followThroughDimension == 0){
                if (getTarget().getWorld().getUID() != getLivingEntity().getWorld().getUID()){
                    getLivingEntity().teleport(targetsSwitchedWorldsLocation);
                    if (debug){
                        Bukkit.getLogger().log(Level.INFO, "Travelling after target through dimension.");
                    }
                }
                targetsSwitchedWorldsLocation = null;
            }
        }
        if (teleportTimer > 0){
            teleportTimer--;
            if (teleportTimer == 0){
                if (!locationIsVisible(getTarget(), getLivingEntity().getLocation()) || !withinMargin(getLivingEntity().getLocation(), getTarget().getLocation(), 60)){
                    teleportToAvailableSlot();
                    if (debug){
                        Bukkit.getLogger().log(Level.WARNING, "NPC is requesting teleport.");
                    }
                }
            }
        }
    }

    private void checkForLocationUpdate(){
        if (!updatedThisTick){
            location = getLivingEntity().getLocation().clone();
            updatedThisTick = true;
        }
        else {
            if (withinMargin(location, getLivingEntity().getLocation(), 0.05) && !isInWater){
                if (!isStuck){
                    if (debug){
                        Bukkit.getLogger().log(Level.INFO, "Im stuck!");
                    }
                }
                isStuck = true;
                if (!isCurrentlyPlacingUpwards && !isCurrentlyBreakingABlock && shouldJump && !shouldBeStopped){
                    if (!getLivingEntity().getLocation().clone().add(0,2,0).getBlock().isEmpty()){
                        if (debug){
                            Bukkit.getLogger().log(Level.INFO, "Block is above me, breaking.");
                        }
                        scheduledBrokenBlocks.add(getLivingEntity().getLocation().clone().add(0,2,0).getBlock());
                    }
                }
                if (!canSee(getTarget()) && !isCurrentlyBreakingABlock && !isCurrentlyPlacingUpwards && !isCurrentlyDiggingDownwards) {
                    if (debug){
                        Bukkit.getLogger().log(Level.INFO, "Added block infront of me to queued block breaks. (Mining towards target)");
                    }
                    if (!getBlockInFront(1).getBlock().isEmpty() && !scheduledBrokenBlocks.contains(getBlockInFront(1).getBlock())){
                        scheduledBrokenBlocks.add(getBlockInFront(1).getBlock());
                    }
                    if (!getBlockInFront(1).subtract(0, 1, 0).getBlock().isEmpty() && !scheduledBrokenBlocks.contains(getBlockInFront(1).clone().subtract(0, 1, 0).getBlock())){
                        scheduledBrokenBlocks.add(getBlockInFront(1).clone().subtract(0, 1, 0).getBlock());
                    }
                }
                double yDifference = getTargetYDifference();
                if (!isCurrentlyPlacingUpwards && !isCurrentlyDiggingDownwards && !isCurrentlyBreakingABlock){
                    if (yDifference >= 2){
                        if (debug){
                            Bukkit.getLogger().log(Level.INFO, "Starting to place upwards.");
                        }
                        isCurrentlyPlacingUpwards = true;
                        centerOnBlock();
                    }
                }
                else {
                    if (isCurrentlyPlacingUpwards){
                        if (yDifference <= 1){
                            if (debug){
                                Bukkit.getLogger().log(Level.INFO, "Stopping placing upwards.");
                            }
                            enableMovement();
                            shouldJump = true;
                            isCurrentlyPlacingUpwards = false;
                        }
                    }
                }
                if (!isCurrentlyDiggingDownwards && !isCurrentlyPlacingUpwards && !isCurrentlyBreakingABlock){
                    if (yDifference <= -2){
                        if (debug){
                            Bukkit.getLogger().log(Level.INFO, "Starting to dig downwards.");
                        }
                        shouldJump = false;
                        isCurrentlyDiggingDownwards = true;
                        centerOnBlock();
                    }
                }
                else {
                    if (isCurrentlyDiggingDownwards){
                        if (yDifference >= -1){
                            if (debug){
                                Bukkit.getLogger().log(Level.INFO, "Stopping digging downwards.");
                            }
                            isCurrentlyDiggingDownwards = false;
                            shouldJump = true;
                            enableMovement();
                        }
                    }
                }
            }
            else {
                isStuck = false;
            }
            updatedThisTick = false;
        }
    }

    private void centerOnBlock(){
        getLivingEntity().teleport(getLivingEntity().getLocation().getBlock().getLocation().clone().add(0.5, 0, 0.5));
    }

    // This is literally just ripped code from Sentinel. Thanks McMonkey
    private boolean canSee(LivingEntity target){
        if (!getLivingEntity().getWorld().equals(target.getWorld())) {
            return false;
        }
        if (!TerminatorUtils.isLookingTowards(getLivingEntity().getEyeLocation(), target.getEyeLocation(), 100, 180)){
            return false;
        }
        return getLivingEntity().hasLineOfSight(target);
    }

    private boolean withinMargin(Location firstLocation, Location secondLocation, double margin){
        Location firstClone = firstLocation.clone();
        Location secondClone = secondLocation.clone();
        firstClone.setY(1000);
        secondClone.setY(1000);
        if (firstClone.getWorld().getUID().equals(secondClone.getWorld().getUID())){
            return firstClone.distance(secondClone) <= margin;
        }
        else {
            return false;
        }
    }

    private void enableMovement(){
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isCurrentlyBreakingABlock && !isBreakingWhileUpwards && !isCurrentlyPlacingUpwards && !isCurrentlyDiggingDownwards){
                    shouldBeStopped = false;
                    shouldJump = true;
                    for (Trait trait : npc.getTraits()){
                        if (trait instanceof TerminatorFollow){
                            TerminatorFollow followTrait = (TerminatorFollow) trait;
                            if (!followTrait.isEnabled()){
                                followTrait.toggle(getTarget(), false);
                            }
                        }
                    }
                }
            }
        }.runTaskLater(TerminatorNPC.getPlugin(), 1);
    }

    private void disableMovement(){
        shouldBeStopped = true;
        shouldJump = false;
        for (Trait trait : npc.getTraits()){
            if (trait instanceof TerminatorFollow){
                TerminatorFollow followTrait = (TerminatorFollow) trait;
                if (followTrait.isEnabled()){
                    followTrait.toggle(getTarget(), false);
                }
            }
        }
    }

    private void tryJump(double height, boolean force) {
        if (jumpCooldown == 0){
            if (getLivingEntity().isOnGround() || force) {
                npc.getNavigator().setTarget(getTarget(), false);
                getLivingEntity().setVelocity(getLivingEntity().getVelocity().add(new Vector(0, height, 0)));
            }
            jumpCooldown = 3;
        }
    }

    private void checkForNewTarget() {
        double smallestDistance = 1000;
        UUID closestCandidate = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getUID().equals(getLivingEntity().getWorld().getUID()) && !player.getGameMode().equals(GameMode.SPECTATOR)){
                if (getLivingEntity().getLocation().distance(player.getLocation()) < smallestDistance) {
                    closestCandidate = player.getUniqueId();
                    smallestDistance = getLivingEntity().getLocation().distance(player.getLocation());
                }
            }
        }
        if (closestCandidate != null){
            if (activeTarget != closestCandidate){
                activeTarget = closestCandidate;
                setNewTarget(getTarget());
            }
        }
    }

    public Player getTarget(){
        return Bukkit.getPlayer(activeTarget);
    }

    private List<Entity> getNearbyEntities(int range){
        return terminator.getEntity().getNearbyEntities(range, range, range);
    }

    public LivingEntity getLivingEntity(){
        if (npc.getEntity() != null){
            return (LivingEntity) npc.getEntity();
        }
        else {
            return null;
        }
    }
}
