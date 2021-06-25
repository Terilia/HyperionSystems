package data.scripts.world.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.scripts.world.procgen.HS_AddStuffs;
import data.scripts.world.procgen.HS_AutoGenerateFactions;
import java.awt.Color;
import java.util.Random;

/**
 *
 * @author NinjaSiren
 */
public class HS_Klat {
    
    // Roll the dice, system background
    private int rand_bg() {
        Random rand = new Random();
        final int max = 6;
        final int min = 1;
        return min + rand.nextInt(max - min + 1);
    }
    
    // Roll the dice
    private int rand(int min, int max) {
        Random rand = new Random();
        return min + rand.nextInt(max - min + 1);
    }
    
    // Roll the dice
    private float randFloat(float min, float max) {
        Random rand = new Random();
        return min + rand.nextFloat() * (max - min);
    }
    
    public void generate(SectorAPI sector) {
        
        // Add star system
        StarSystemAPI system = sector.createStarSystem("Klat");
        system.getLocation().set(-9750, 24500);
        system.setAge(StarAge.ANY);
        system.setBackgroundTextureFilename("graphics/backgrounds/background" + rand_bg() + ".jpg");
        ProcgenUsedNames.notifyUsed("Klat");
        
        // Initialize star variables
        int starSize = rand(1100, 1300);
        
        // Add stars, Klat
        PlanetAPI klat = system.initStar(
                    "hs_klat", // unique id for this star
                    StarTypes.BLUE_GIANT,  // id in planets.json
                    starSize,           // radius (in pixels at default zoom)
                    starSize * 1.5f,            // corona radius, from star edge
                    2.5f,             // solar wind burn level
                    0.7f,           // flare probability
                    2.2f);          // CR loss multipiers
        
        klat.setCustomDescriptionId("hs_star_klat"); 
        klat.setName("Klat");
        
        // Other system automation stuff
        system.setStar(klat);
        system.setHasSystemwideNebula(true);        
        
        // Sets whole system lighting color (R, G, B)
        system.setLightColor(new Color(255,255,255,255));
        klat.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "banded"));
        klat.getSpec().setGlowColor(new Color(0, 142, 253, 128));
        klat.getSpec().setAtmosphereThickness(0.5f);
        klat.applySpecChanges();
        
        // Randomly generates planets
        StarSystemGenerator.addOrbitingEntities(
                system, // System
                klat, // Star
                system.getAge(), // Sets the potential entities added depending on system age
                10, rand(10, 20), // Min-Max entities to add
                starSize * randFloat(1.75f, 2f), // Radius to start at
                1, // Naming offset
                false, // Custom or system based names
                true); // Should habitables appear
        
        // Automatically generates random factions in the system based on the values you added
        new HS_AutoGenerateFactions().generateFactions(
                system, // The system that will have the auto-generated planets
                klat, // The star that the planets will orbit on
                "HS_Corporation_Separatist", // Faction ID of the first faction
                Factions.REMNANTS, // Faction ID of the second faction
                0.6, // The percentage of factionA appearing vs factionB on the system, min=0 max=1
                true, // Do we generate factions
                false, // Do we generate stations
                true); // Do we generate an abandoned station
        
        // Adds nav buoy, comm relay, and sensor array
        new HS_AddStuffs().generateStuffs(system, klat, "HS_Corporation_Separatist");
        
        // Add jump points on habitable or working colonies
        new HS_AddStuffs().generateJumpPoints(system, klat);
        
        // Autogenerated jump points
        system.autogenerateHyperspaceJumpPoints(true, true);
        
        // Cleaning the hyperspace
        HyperspaceTerrainPlugin plugin =
                (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;

        float radius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0,
                radius + minRadius * 0.5f, 0, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0,
                radius + minRadius, 0, 360f, 0.25f);
    }
}
