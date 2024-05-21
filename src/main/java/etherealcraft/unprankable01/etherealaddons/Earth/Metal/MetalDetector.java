package etherealcraft.unprankable01.etherealaddons.Earth.Metal;


import etherealcraft.unprankable01.etherealaddons.EtherealAddonsListener;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
//import org.bukkit.entity.Shulker;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;

import java.util.*;

import static org.bukkit.potion.PotionEffectType.INVISIBILITY;

public class MetalDetector extends MetalAbility implements AddonAbility {
    private Location eyeLoc;
    private Listener MDL;
    private long startTime;
    private List<MagmaCube> highlighted = new ArrayList<>();
    private List<Location> oreList;
    @Attribute(Attribute.COOLDOWN)
    private long cooldown;
    @Attribute(Attribute.DURATION)
    private long duration;
    @Attribute("Reach")
    private int reach;
    private Permission perm;
    static String path = "etherealcraft.unprankable01.etherealaddons.Earth.MetalDetector";
    public MetalDetector(Player player) {
        super(player);
        if (bPlayer.isOnCooldown(this)) {
            return;
        }
        if (!bPlayer.canBend(this)) {
            return;
        }
        if (hasAbility(player, MetalDetector.class)) {
            MetalDetector md = getAbility(player, MetalDetector.class);
            if (md.isStarted()) {
                return;
            }
        }
        this.startTime = System.currentTimeMillis();
        setFields();

        eyeLoc = player.getEyeLocation();
        oreList = findOres(this.eyeLoc, this.reach);



        start();
    }


    public void setFields(){
        this.cooldown = ConfigManager.getConfig().getLong(path + "Cooldown");
        this.duration = ConfigManager.getConfig().getLong(path+"Duration");
        this.reach = ConfigManager.getConfig().getInt(path+"Reach");
    }


    public void applyGlowingBlock(Location blockloc) {
        Location centerblockloc = blockloc.clone().add(-1.5, -2, -1.5);
        // Spawn an invisible Shulker at the location
        World world = centerblockloc.getWorld();
        if (world == null) return; // Ensure world is not null
        MagmaCube magmaCube = (MagmaCube) world.spawnEntity(centerblockloc.add(2,2,2), EntityType.MAGMA_CUBE);
        PotionEffect effect = new PotionEffect(INVISIBILITY,10000/100*20,0,false,false,false);
        magmaCube.addPotionEffect(effect);
        magmaCube.setAI(false);
        magmaCube.setInvulnerable(true);
        magmaCube.setGravity(false);
        magmaCube.setGlowing(true);
        magmaCube.setCustomNameVisible(false);
        magmaCube.setSilent(true);
        magmaCube.setCollidable(false);
        magmaCube.setSize(1);
        // Schedule the removal of the MagmaCube after the duration (optional)
        this.highlighted.add(magmaCube);
    }


    public boolean MetalBendable(Material material){
        return isMetal(material);
    }


    public List<Location> findOres(Location center, int radius){
        List<Location> ores = new ArrayList<>();
        World world = center.getWorld();
        int centerX = (int) center.getX();
        int centerY = (int) center.getY();
        int centerZ = (int) center.getZ();
        for (int x = centerX - radius; x <= centerX + radius; x++){
            for (int y = centerY - radius; y <= centerY + radius; y++){
                for (int z = centerZ - radius; z <= centerZ + radius; z++){
                    assert world != null;
                    Block block = world.getBlockAt(x, y ,z);
                    if (MetalBendable(block.getType())){
                        ores.add(block.getLocation());
                    }
                }
            }
        }
        return ores;
    }


    @Override
    public void progress() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        // Check if a second has passed since the last countdown log
        if (elapsedTime >= duration | !player.isSneaking()) {
            // Ability duration has elapsed, deactivate the ability
            endMD();
        }else {
            // Check if oreList is empty before iterating over it
            if (!oreList.isEmpty()) {
                for (Location ore : oreList) {
                    applyGlowingBlock(ore);
                }
            }
        }

        // Ability logic for each tick goes here
    }


    public void endMD(){
        bPlayer.addCooldown(this);
        for(MagmaCube highlight: this.highlighted){
            highlight.remove();
        }
        remove();
    }


    @Override
    public void stop() {
        ProjectKorra.log.info("Successfully disabled " + getName() + "by" + getAuthor());
        // Clean up any resources or effects when the ability is manually stopped
        ProjectKorra.plugin.getServer().getPluginManager().removePermission(this.perm);
        HandlerList.unregisterAll(this.MDL);
        endMD();
    }


    @Override
    public String getName() {
        return "MetalDetector";
    }


    @Override
    public String getDescription() {
        return ChatColor.DARK_GRAY + "applies glowing to metal bendable blocks within a certain range";
    }


    @Override
    public String getInstructions() {
        return ChatColor.GOLD + "Hold Sneak";
    }


    @Override
    public String getAuthor() {
        return ChatColor.DARK_BLUE + "Unprankable01";
    }


    @Override
    public String getVersion() {
        return ChatColor.YELLOW + "1.0";
    }


    @Override
    public boolean isHarmlessAbility() {
        return true;
    }


    @Override
    public boolean isSneakAbility() {
        return true;
    }


    @Override
    public long getCooldown() {
        return this.cooldown;
    }


    @Override
    public Location getLocation() {
        return this.eyeLoc;
    }


    @Override
    public void load() {
        this.MDL = new EtherealAddonsListener();
        Bukkit.getPluginManager().registerEvents(this.MDL, ProjectKorra.plugin);
        // Set default configuration values
        ConfigManager.defaultConfig.get().addDefault(path + "Cooldown", 10000);
        ConfigManager.defaultConfig.get().addDefault(path + "Duration", 7500);
        ConfigManager.defaultConfig.get().addDefault(path + "Reach",10);
        // Save the default configuration
        ConfigManager.defaultConfig.save();

        // Log that the ability has been loaded
        perm = new Permission("bending.ability.MetalDetector");
        perm.setDefault(PermissionDefault.TRUE);
        ProjectKorra.log.info(this.getName() + " by " + this.getAuthor() + " " + this.getVersion() + " has been loaded!");

    }
}