package gmail.fopypvp174.cmloja.listeners;

import gmail.fopypvp174.cmloja.CmLoja;
import gmail.fopypvp174.cmloja.api.Utilidades;
import gmail.fopypvp174.cmloja.enums.LojaEnum;
import gmail.fopypvp174.cmloja.exceptions.InventoryFullException;
import gmail.fopypvp174.cmloja.exceptions.PlayerEqualsTargetException;
import gmail.fopypvp174.cmloja.exceptions.PlayerMoneyException;
import gmail.fopypvp174.cmloja.exceptions.SignUnknowBuy;
import gmail.fopypvp174.cmloja.handlers.LojaBuyServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class EventoComprarSign implements Listener {

    private CmLoja plugin;

    public EventoComprarSign(CmLoja plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onComprar(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (e.getClickedBlock().getType() != Material.SIGN_POST &&
                e.getClickedBlock().getType() != Material.WALL_SIGN) {
            return;
        }

        Sign sign = (Sign) e.getClickedBlock().getState();
        if (!Utilidades.isLojaValid(sign.getLines())) {
            return;
        }

        if (!sign.getLine(0).equals(plugin.getMessageConfig().message("placa.nomeLoja"))) {
            return;
        }

        Block block = e.getClickedBlock().getRelative(((org.bukkit.material.Sign) sign.getData()).getAttachedFace());
        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
            return;
        }

        Player player = e.getPlayer();
        ItemStack item = Utilidades.getItemLoja(sign.getLines());
        try {
            comprarPelaPlaca(player, sign, item);
        } catch (PlayerEqualsTargetException error1) {
            player.sendMessage(plugin.getMessageConfig().message("mensagens.comprar_erro3"));
        } catch (SignUnknowBuy error2) {
            player.sendMessage(plugin.getMessageConfig().message("mensagens.comprar_erro4"));
        } catch (InventoryFullException error3) {
            player.sendMessage(plugin.getMessageConfig().message("mensagens.inventory_full"));
        } catch (PlayerMoneyException erro4) {
            player.sendMessage(plugin.getMessageConfig().message("mensagens.comprar_erro1"));
        }
    }

    private void comprarPelaPlaca(Player player, org.bukkit.block.Sign placa, ItemStack item)
            throws PlayerMoneyException, SignUnknowBuy, InventoryFullException, PlayerEqualsTargetException {
        Double valorCompra = Double.valueOf(Utilidades.getPrices(LojaEnum.COMPRAR, placa));
        if (valorCompra.doubleValue() == 0.0D) {
            throw new SignUnknowBuy("A placa {x=" + placa.getLocation().getX() + ",y=" + placa.getLocation().getY() + ",z=" + placa.getLocation().getZ() + "} não tem opção para comprar.");
        }
        if (placa.getLine(0).equals(player.getDisplayName())) {
            throw new PlayerEqualsTargetException("O jogador '" + player.getName() + "' está tentando comprar dele mesmo.");
        }
        int qntItemPlaca = Short.parseShort(Utilidades.replace(placa.getLine(1)));
        if (!Utilidades.temEspacoInvParaItem(player.getInventory(), item, qntItemPlaca)) {
            throw new InventoryFullException("Inventário do jogador está lotado e não tem como receber os itens.");
        }
        int quantiaDesconto = 0;
        for (int i = 0; i <= 100; i++) {
            if ((player.hasPermission("*")) || (player.isOp())) {
                break;
            }
            if (player.hasPermission("loja.comprar." + i)) {
                valorCompra = Double.valueOf(valorCompra.doubleValue() - valorCompra.doubleValue() * i / 100.0D);
                quantiaDesconto = i;
                break;
            }
        }
        if (this.plugin.getEcon().getBalance(player) < valorCompra.doubleValue()) {
            throw new PlayerMoneyException("O jogador '" + player.getName() + "' não tem dinheiro suficiente para fazer a compra.");
        }
        if (quantiaDesconto > 0) {
            player.sendMessage(this.plugin.getMessageConfig().message("mensagens.comprar_vip_vantagem", Integer.valueOf(quantiaDesconto)));
        }
        String dinheiroFormatado = String.format("%.2f", valorCompra);
        player.sendMessage(this.plugin.getMessageConfig().message("mensagens.comprar_success_sign", Integer.valueOf(qntItemPlaca), dinheiroFormatado));

        this.plugin.getEcon().withdrawPlayer(player, valorCompra.doubleValue());

        item.setAmount(qntItemPlaca);
        player.getInventory().addItem(item);

        LojaBuyServer eventBuy = new LojaBuyServer(player, valorCompra, item, Integer.valueOf(qntItemPlaca));
        Bukkit.getServer().getPluginManager().callEvent(eventBuy);
    }
}
