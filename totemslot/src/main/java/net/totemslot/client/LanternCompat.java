package net.totemslot.client;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.LanternBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.totemslot.slot.TotemSlot;

import java.util.Optional;

/**
 * Detecta uma lanterna pendurada em QUALQUER slot do Trinkets (mods de lanterna na cintura/calca), para o totem
 * sair da frente dela. Conta lanterna vanilla (LanternBlock) e qualquer item cujo id tenha "lantern".
 */
@Environment(EnvType.CLIENT)
final class LanternCompat {

    private LanternCompat() {
    }

    static boolean isLantern(ItemStack stack) {
        if (stack.isEmpty() || TotemSlot.isTotem(stack)) {
            return false;
        }
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof LanternBlock) {
            return true;
        }
        return Registries.ITEM.getId(stack.getItem()).getPath().contains("lantern");
    }

    static boolean hasLantern(LivingEntity entity) {
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
        return component.isPresent() && component.get().isEquipped(LanternCompat::isLantern);
    }
}
