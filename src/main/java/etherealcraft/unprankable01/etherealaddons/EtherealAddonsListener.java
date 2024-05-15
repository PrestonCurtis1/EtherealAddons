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
    public void onSneak(PlayerToggleSneakEvent event) {
        Player p = event.getPlayer();
        p.damage(100);
        ProjectKorra.log.info(p.getName() + "Your dead");
        System.out.println("Hello, world you just snook");
        BendingPlayer bp = BendingPlayer.getBendingPlayer(p);
        if(event.isCancelled() || bp == null || !bp.canUseSubElement(Element.METAL) || !bp.isToggled() || !bp.isElementToggled(Element.EARTH) || p.getGameMode() == GameMode.SPECTATOR || p.isSneaking() || bp.isOnCooldown("MetalDetector")){
            return;
        }else if (bp.getBoundAbilityName().equalsIgnoreCase("MetalDetector")) {
            new MetalDetector(p);
        }
    }
}