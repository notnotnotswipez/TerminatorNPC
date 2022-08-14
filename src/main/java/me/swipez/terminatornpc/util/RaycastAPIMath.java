
package me.swipez.terminatornpc.util;

import org.bukkit.util.Vector;

public class RaycastAPIMath {
    public static double cos(final double a) {
        return Math.cos(a);
    }
    
    public static double sin(final double a) {
        return Math.sin(a);
    }
    
    public static double tan(final double a) {
        return Math.tan(a);
    }
    
    public static double arccos(final double a) {
        return Math.acos(a);
    }
    
    public static double arcsin(final double a) {
        return Math.asin(a);
    }
    
    public static double arctan(final double a) {
        return Math.atan(a);
    }
    
    public static double toRadians(final double angdeg) {
        return Math.toRadians(angdeg);
    }
    
    public static double toDeg(final double angrad) {
        return Math.toDegrees(angrad);
    }
    
    public static double getAngle(double width, double height) {
        if (width < 0.0) {
            width *= -1.0;
        }
        if (height < 0.0) {
            height *= -1.0;
        }
        if (width == 0.0 || height == 0.0) {
            return 0.0;
        }
        return arctan(height / width);
    }
    
    public static Vector rotate(Vector vect, double yaw, double pitch) {
        yaw = toRadians(yaw);
        pitch = toRadians(pitch);
        vect = rotateX(vect, pitch);
        vect = rotateY(vect, -yaw);
        return vect;
    }
    
    public static Vector rotateX(final Vector vect, final double a) {
        final double y = cos(a) * vect.getY() - sin(a) * vect.getZ();
        final double z = sin(a) * vect.getY() + cos(a) * vect.getZ();
        return vect.setY(y).setZ(z);
    }
    
    public static Vector rotateY(final Vector vect, final double b) {
        final double x = cos(b) * vect.getX() + sin(b) * vect.getZ();
        final double z = -sin(b) * vect.getX() + cos(b) * vect.getZ();
        return vect.setX(x).setZ(z);
    }
    
    public static Vector rotateZ(final Vector vect, final double c) {
        final double x = cos(c) * vect.getX() - sin(c) * vect.getY();
        final double y = sin(c) * vect.getX() + cos(c) * vect.getY();
        return vect.setX(x).setY(y);
    }
}