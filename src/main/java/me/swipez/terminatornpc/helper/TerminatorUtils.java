package me.swipez.terminatornpc.helper;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import static org.bukkit.Location.normalizeYaw;

public class TerminatorUtils {
    // More ripped code LOL. Dont worry theres original code in this plugin you just need to look really hard but trust me
    // You'll find it.

    public static boolean isLookingTowards(Location myLoc, Location theirLoc, float yawLimit, float pitchLimit) {
        Vector rel = theirLoc.toVector().subtract(myLoc.toVector()).normalize();
        float yaw = normalizeYaw(myLoc.getYaw());
        float yawHelp = getYaw(rel);
        if (!(Math.abs(yawHelp - yaw) < yawLimit ||
                Math.abs(yawHelp + 360 - yaw) < yawLimit ||
                Math.abs(yaw + 360 - yawHelp) < yawLimit)) {
            return false;
        }
        float pitch = myLoc.getPitch();
        float pitchHelp = getPitch(rel);
        return Math.abs(pitchHelp - pitch) < yawLimit;
    }

    public static float getPitch(Vector vector) {
        double dx = vector.getX();
        double dy = vector.getY();
        double dz = vector.getZ();
        double forward = Math.sqrt((dx * dx) + (dz * dz));
        double pitch = Math.atan2(dy, forward) * (180.0 / Math.PI);
        return (float) pitch;
    }

    public static float getYaw(Vector vector) {
        double dx = vector.getX();
        double dz = vector.getZ();
        double yaw = 0;
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                yaw = 1.5 * Math.PI;
            }
            else {
                yaw = 0.5 * Math.PI;
            }
            yaw -= Math.atan(dz / dx); // or atan2?
        }
        else if (dz < 0) {
            yaw = Math.PI;
        }
        return (float) (-yaw * (180.0 / Math.PI));
    }

}