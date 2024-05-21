package etherealcraft.unprankable01.etherealaddons;
import com.projectkorra.projectkorra.ProjectKorra;
import etherealcraft.unprankable01.etherealaddons.Earth.Metal.MetalDetector;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.BendingPlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
public class EtherealAddonsListener implements Listener {
    @EventHandler
    public void MetalDetectorListener(PlayerToggleSneakEvent event) {
        Player p = event.getPlayer();
        p.sendMessage("Hello, " + p.getName());
        BendingPlayer bp = BendingPlayer.getBendingPlayer(p);
        if(event.isCancelled() || bp == null || !bp.canUseSubElement(Element.METAL) || !bp.isToggled() || !bp.isElementToggled(Element.EARTH) || p.getGameMode() == GameMode.SPECTATOR || bp.isOnCooldown("MetalDetector")){
            p.sendMessage("conditions were not right");
            return;
        }else if (bp.getBoundAbilityName().equalsIgnoreCase("MetalDetector")) {
            p.sendMessage("Conditions Were Correct");
            new MetalDetector(p);
        }
    }
}