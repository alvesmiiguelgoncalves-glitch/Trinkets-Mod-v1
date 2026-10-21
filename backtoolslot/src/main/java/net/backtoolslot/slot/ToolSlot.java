package net.backtoolslot.slot;

import net.backtoolslot.BackToolSlotMain;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class ToolSlot {

    public static final String GROUP = "chest";
    public static final String SLOT = "back_tool";

    /**
     * Itens aceitos no slot. A tag inclui espadas, picaretas, machados, pas, enxadas, tesoura e vara de pesca;
     * qualquer item que estenda ToolItem (ferramentas de outros mods) tambem e aceito. Para liberar outros itens,
     * use um datapack com data/backtoolslot/tags/item/tools.json.
     */
    public static final TagKey<Item> TOOLS = TagKey.of(RegistryKeys.ITEM, BackToolSlotMain.id("tools"));

    private ToolSlot() {
    }

    public static boolean isTool(ItemStack stack) {
        return !stack.isEmpty() && (stack.isIn(TOOLS) || stack.getItem() instanceof ToolItem);
    }
}
