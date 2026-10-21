package net.totemslot.slot;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.Item;
import net.totemslot.TotemSlotMain;

import java.util.Map;
import java.util.Optional;

/** Acesso ao slot Trinkets "legs/totem". Armazenamento, sync e queda ao morrer ficam por conta do Trinkets. */
public final class TotemSlot {

    public static final String GROUP = "legs";
    public static final String SLOT = "totem";

    /**
     * Itens aceitos no slot. O totem vanilla esta na tag; para totens de outros mods basta um datapack
     * com data/totemslot/tags/item/totems.json (veja o README).
     */
    public static final TagKey<Item> TOTEMS = TagKey.of(RegistryKeys.ITEM, TotemSlotMain.id("totems"));

    private TotemSlot() {
    }

    public static boolean isTotem(ItemStack stack) {
        return !stack.isEmpty() && stack.isIn(TOTEMS);
    }

    /** @return o inventario do slot ou null se a entidade nao tem o slot. */
    public static TrinketInventory inventory(LivingEntity entity) {
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
        if (component.isEmpty()) {
            return null;
        }
        Map<String, TrinketInventory> group = component.get().getInventory().get(GROUP);
        if (group == null) {
            return null;
        }
        TrinketInventory inventory = group.get(SLOT);
        return inventory != null && inventory.size() > 0 ? inventory : null;
    }

    /**
     * Gasta UM totem do slot.
     *
     * @return copia do totem gasto, ou ItemStack.EMPTY se nao havia totem no slot
     */
    public static ItemStack consumeOne(LivingEntity entity) {
        TrinketInventory inventory = inventory(entity);
        if (inventory == null) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (isTotem(stack)) {
                ItemStack used = stack.copy();
                used.setCount(1);
                ItemStack remaining = stack.copy();
                remaining.decrement(1);
                // setStack (e nao decrement direto) para o Trinkets marcar o slot como alterado e sincronizar
                inventory.setStack(i, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
                return used;
            }
        }
        return ItemStack.EMPTY;
    }
}
