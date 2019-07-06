package gmail.fopypvp174.cmloja.listeners;

import gmail.fopypvp174.cmloja.CmLoja;
import gmail.fopypvp174.cmloja.api.Utilidades;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.EnumSet;
import java.util.Set;

public class EventOpenChest implements Listener {

    private CmLoja plugin;

    public EventOpenChest(CmLoja plugin) {
        this.plugin = plugin;
    }

    @Deprecated
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void chestOpen(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (e.getClickedBlock().getType() != Material.CHEST &&
                e.getClickedBlock().getType() != Material.TRAPPED_CHEST) {
            return;
        }

        Set<BlockFace> directions = EnumSet.of(
                BlockFace.WEST, BlockFace.NORTH,
                BlockFace.EAST, BlockFace.SOUTH);

        Block block = e.getClickedBlock();

        directions.stream().map(block::getRelative).forEach(relative -> {
            if (relative.getType() != Material.WALL_SIGN) {
                return;
            }

            Sign sign = (Sign) relative.getState();
            if (!Utilidades.isLojaValid(sign.getLines())) {
                return;
            }

            if (e.getPlayer().getName().equals(sign.getLine(0))) {
                return;
            }

            if (e.getPlayer().hasPermission("loja.abrirbau")) {
                e.setCancelled(false);
                return;
            }
            e.getPlayer().sendMessage(
                    plugin.getMessageConfig()
                            .message("mensagens.open_chest_shop")
                            .replace("%p", sign.getLine(0)));

            e.setCancelled(true);
        });
    }
}
