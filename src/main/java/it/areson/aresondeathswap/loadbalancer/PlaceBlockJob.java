package it.areson.aresondeathswap.loadbalancer;

import org.bukkit.Location;
import org.bukkit.Material;

public class PlaceBlockJob implements Job {

    private final Location location;
    private final Material material;

    public PlaceBlockJob(Location location, Material material) {
        this.location = location;
        this.material = material;
    }

    @Override
    public void compute() {
        location.getBlock().setType(material);
    }
}
