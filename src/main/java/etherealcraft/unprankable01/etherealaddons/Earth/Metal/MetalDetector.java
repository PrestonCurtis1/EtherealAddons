package etherealcraft.unprankable01.etherealaddons.Earth.Metal;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MetalDetector extends MetalAbility implements AddonAbility {
    private Location location;

    private long duration;
    private long startTime;

    @Attribute(Attribute.COOLDOWN)
    private Long cooldown;

    static String path = "etherealcraft.unprankable01.etherealaddons.Earth.Metal";
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
            if (md.isStarted())
                return;
        }
        this.duration = 5000;
        this.startTime = System.currentTimeMillis();
        setFields();

        start();
    }
    public void setFields(){
        cooldown = ConfigManager.defaultConfig.get().getLong(path + "Cooldown");
    }
    @Override
    public void progress() {
        long currentTime = System.currentTimeMillis();
        location = player.getEyeLocation();
        long elapsedTime = currentTime - startTime;

        // Check if a second has passed since the last countdown log
        if (elapsedTime / 1000 != (currentTime - 1000) / 1000) {
            long secondsRemaining = (duration - elapsedTime) / 1000;
            ProjectKorra.log.info("MetalDetector: " + secondsRemaining + " seconds remaining");
        }

        if (currentTime - startTime >= duration) {
            // Ability duration has elapsed, deactivate the ability
            remove();
            return;
        }

        // Ability logic for each tick goes here
    }
    @Override
    public void stop() {
        // Clean up any resources or effects when the ability is manually stopped
        super.remove();
    }
    @Override
    public String getName() {
        return "MetalDetector";
    }

    @Override
    public String getDescription() {
        return "applies glowing to metal bendable block with a certain range";
    }

    @Override
    public String getInstructions() {
        return "Hold Sneak";
    }

    @Override
    public String getAuthor() {
        return "Unprankable01";
    }

    @Override
    public String getVersion() {
        return "1.0";
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
        return cooldown;
    }
    @Override
    public Location getLocation() {
        return location;
    }
    @Override
    public void load() {

        // Set default configuration values
        ConfigManager.defaultConfig.get().addDefault(path + "Cooldown", 7000);

        // Save the default configuration
        ConfigManager.defaultConfig.save();

        // Log that the ability has been loaded
        ProjectKorra.log.info(this.getName() + " by " + this.getAuthor() + " " + this.getVersion() + " has been loaded!");
    }

}