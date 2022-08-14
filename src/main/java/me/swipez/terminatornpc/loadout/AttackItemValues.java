package me.swipez.terminatornpc.loadout;

public enum AttackItemValues {
    WOODEN_SWORD(4, 1.6),
    STONE_SWORD(5, 1.6),
    IRON_SWORD(4, 1.6),
    GOLDEN_SWORD(6, 1.6),
    DIAMOND_SWORD(7, 1.6),
    NETHERITE_SWORD(8, 1.6),

    WOODEN_AXE(7, 0.8),
    STONE_AXE(9, 0.8),
    IRON_AXE(9, 0.9),
    GOLDEN_AXE(7, 1),
    DIAMOND_AXE(9, 1),
    NETHERITE_AXE(10, 1),

    FIST(1, 4),
    ;

    private final int damage;
    private final int cooldown;

    AttackItemValues(int attackDamage, double attackCooldown) {
        this.damage = attackDamage;
        this.cooldown = (int) ((1 / attackCooldown) * 20)+4;
    }

    public int getCooldown(){
        return cooldown;
    }

    public int getDamage(){
        return damage;
    }
}