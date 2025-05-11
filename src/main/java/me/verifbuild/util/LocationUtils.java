package me.verifbuild.util;

import org.bukkit.Location;

public class LocationUtils {
    
    /**
     * Compara si dos ubicaciones representan el mismo bloque.
     *
     * @param loc1 Primera ubicación
     * @param loc2 Segunda ubicación
     * @return true si representan el mismo bloque
     */
    public static boolean isSameBlock(Location loc1, Location loc2) {
        return loc1.getWorld().equals(loc2.getWorld()) &&
                loc1.getBlockX() == loc2.getBlockX() &&
                loc1.getBlockY() == loc2.getBlockY() &&
                loc1.getBlockZ() == loc2.getBlockZ();
    }
    
    /**
     * Convierte una ubicación a formato string.
     *
     * @param location La ubicación
     * @return String en formato "x,y,z"
     */
    public static String locationToString(Location location) {
        return String.format("%d,%d,%d",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }
    
    /**
     * Calcula la distancia en bloques entre dos ubicaciones.
     *
     * @param loc1 Primera ubicación
     * @param loc2 Segunda ubicación
     * @return Distancia en bloques
     */
    public static int getBlockDistance(Location loc1, Location loc2) {
        return Math.abs(loc1.getBlockX() - loc2.getBlockX()) +
                Math.abs(loc1.getBlockY() - loc2.getBlockY()) +
                Math.abs(loc1.getBlockZ() - loc2.getBlockZ());
    }
}