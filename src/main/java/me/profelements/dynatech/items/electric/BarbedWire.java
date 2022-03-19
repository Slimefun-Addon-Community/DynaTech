package me.profelements.dynatech.items.electric;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.profelements.dynatech.DynaTech;
import me.profelements.dynatech.items.electric.abstracts.AMachine;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BarbedWire extends AMachine {
    private static final int MAX_DIRECTION_VEL = 50;
    private static final double MAX_RANGE = 9D;
    private static final double PUSH_POWER = 2D;

    public BarbedWire(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void tick(Block b) {
        if (getCharge(b.getLocation()) < getEnergyConsumption())  {
            return;
        }
        
        DynaTech.runSync(()->sendEntitiesFlying(b.getLocation(), b.getWorld()));
        removeCharge(b.getLocation(), getEnergyConsumption());
    }

    public void sendEntitiesFlying(@Nonnull Location loc, @Nonnull World w) {
        List<Entity> shotEntities = new ArrayList<>();
        int waitTime = 0;

        Vector wirePosition = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).add(new Vector(0.5, 0, 0.5));
        Collection<Entity> nearbyEntites = w.getNearbyEntities(loc, MAX_RANGE, MAX_RANGE, MAX_RANGE);
        for (Entity e : nearbyEntites) {
            Vector entityVelocity = e.getVelocity();
            if (isLaunchableEntity(shotEntities, e)) {
                Vector pushVelocity = calcPushVelocity(wirePosition, e, entityVelocity);
                if (NumberConversions.isFinite(pushVelocity.getX()) && NumberConversions.isFinite(pushVelocity.getY()) && NumberConversions.isFinite(pushVelocity.getZ())) {
                    e.setVelocity(pushVelocity);
                    shotEntities.add(e);
                } else if (!NumberConversions.isFinite(entityVelocity.getX()) || !NumberConversions.isFinite(entityVelocity.getY()) || !NumberConversions.isFinite(entityVelocity.getZ())) {
                    e.setVelocity(new Vector(0, 0, 0));
                }
            }

            if (shotEntities.contains(e) && waitTime > 8) {
                e.setVelocity(entityVelocity);
            }

            waitTime++;
        }
    }

    @Override
    public String getMachineIdentifier() {
        return "BARBED_WIRE";
    }

    @Override
    public boolean isGraphical() {
        return false;
    }

    @Override
    public ItemStack getProgressBar() {
        return new ItemStack(Material.IRON_BARS);
    }
    
    private boolean isLaunchableEntity(List<Entity> shotEntities, Entity e) {
        return e.getType() != EntityType.PLAYER
            && e.getType() != EntityType.ARMOR_STAND
            && e.getType() != EntityType.DROPPED_ITEM
            && !shotEntities.contains(e);
    }

    private Vector limitVelocity(Vector velocity) {
        if (velocity.getX() >= MAX_DIRECTION_VEL || velocity.getY() >= MAX_DIRECTION_VEL || velocity.getZ() >= MAX_DIRECTION_VEL) {
            velocity = new Vector(0, 0, 0);
        }
        return velocity;
    }
    
    private Vector calcPushVelocity(Vector wirePosition, Entity e, Vector entityVelocity) {
        Location entityLocation = e.getLocation();
        Vector entityPosition = new Vector(entityLocation.getX(), entityLocation.getY(), entityLocation.getZ());
        Vector offset = entityPosition.subtract(wirePosition);
        Vector unit = fastNormalize(offset);
        double distanceSq = offset.lengthSquared();
        Vector extraVelocity = unit.multiply(PUSH_POWER / distanceSq);
        return limitVelocity(entityVelocity.add(extraVelocity));
    }

    private Vector fastNormalize(Vector v) {
        float length = fastLength(v);
        v.multiply(length);

        return v;
    }

    private float fastLength(Vector v) {
        double x = v.getX();
        double y = v.getY();
        double z = v.getZ();

        return fastSqrt(x * x + y * y + z * z);
    }

    private float fastSqrt(double double_num) {
        int i;
        float x2, y;
        float threehalfs = 1.5F;
        float num = (float) double_num;

        x2 = num * 0.5F;
        y = num;
        i = Float.floatToIntBits(y);
        i = 0x5f3759df - (i >> 1);
        y = Float.intBitsToFloat(i);
        y = y * (threehalfs - (x2 * y * y));

        return y;
    }
}