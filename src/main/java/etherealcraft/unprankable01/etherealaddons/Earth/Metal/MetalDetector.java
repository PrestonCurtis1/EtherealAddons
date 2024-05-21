package etherealcraft.unprankable01.etherealaddons.Earth.Metal;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import etherealcraft.unprankable01.etherealaddons.EtherealAddonsListener;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MetalDetector extends MetalAbility implements AddonAbility {
    private Location eyeLoc;
    private Listener MDL;
    private long startTime;
    private List<Location> oreList;
    private ProtocolManager protocolManager;
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
        player.sendMessage("MetalDetector Active");
        if (bPlayer.isOnCooldown(this)) {
            player.sendMessage("MetalDetector on Cooldown");
            return;
        }
        if (!bPlayer.canBend(this)) {
            player.sendMessage("you can't bend");
            return;
        }
        if (hasAbility(player, MetalDetector.class)) {
            player.sendMessage("you have metal Detector");
            MetalDetector md = getAbility(player, MetalDetector.class);
            if (md.isStarted()) {
                player.sendMessage("MetalDetector Already Started");
                return;
            }
        }
        this.startTime = System.currentTimeMillis();
        setFields();

        eyeLoc = player.getEyeLocation();
        player.sendMessage("" + this.reach);
        oreList = findOres(this.eyeLoc, this.reach);
        player.sendMessage("" + oreList.size());



        start();
        player.sendMessage("MetalDetector Has now started");
    }





    public void setFields(){
        this.cooldown = ConfigManager.defaultConfig.get().getLong(path + "Cooldown");
        this.duration = ConfigManager.defaultConfig.get().getLong(path+"Duration");
        this.reach = ConfigManager.defaultConfig.get().getInt(path+"Reach");
    }


    public void applyGlowingBlock(Player player, Location location) throws InvocationTargetException {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);

        // Specify the block's position
        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        packet.getBlockPositionModifier().write(0, blockPosition);

        // Set the entity ID to 0, indicating it's a block
        packet.getIntegers().write(0, 0);

        WrappedDataWatcher watcher = new WrappedDataWatcher();
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);

        // Add the glowing effect to the data watcher for the block
        watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, serializer), (byte) 0x40); // Set glowing flag

        packet.getDataWatcherModifier().write(0, watcher);

        protocolManager.sendServerPacket(player, packet);
    }



    public boolean MetalBendable(Material material){
        return loadMetalbendableBlocks().contains(material);
    }


    private Set<Material> loadMetalbendableBlocks() {
        Set<Material> materials = new HashSet<>();
        List<String> blockNames = ConfigManager.getConfig().getStringList("Properties.Earth.MetalBlocks");
        for (String blockName : blockNames) {
            Material material = Material.getMaterial(blockName);
            if (material != null) {
                materials.add(material);
            }
        }
        return materials;
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
        if (elapsedTime >= duration) {
            // Ability duration has elapsed, deactivate the ability
            endMD();
        }else{
            for(Location ore : oreList){
                try {
                    applyGlowingBlock(player, ore);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        // Ability logic for each tick goes here
    }


    public void endMD(){
        bPlayer.addCooldown(this);
        remove();
    }


    @Override
    public void stop() {
        ProjectKorra.log.info("Successfully disabled " + getName() + "by" + getAuthor());
        // Clean up any resources or effects when the ability is manually stopped
        ProjectKorra.plugin.getServer().getPluginManager().removePermission(this.perm);
        HandlerList.unregisterAll(this.MDL);
        super.remove();
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
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        ProjectKorra.log.info(this.getName() + " by " + this.getAuthor() + " " + this.getVersion() + " has been loaded!");

    }
}