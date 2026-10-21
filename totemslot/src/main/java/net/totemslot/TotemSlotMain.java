package net.totemslot;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.util.Identifier;
import net.totemslot.slot.TotemSlot;

public class TotemSlotMain implements ModInitializer {

    public static final String MOD_ID = "totemslot";

    /** Id do validador de slot referenciado em data/trinkets/slots/legs/totem.json. */
    public static final Identifier TOTEM_PREDICATE = id("totem");

    @Override
    public void onInitialize() {
        // O slot so aceita itens da tag #totemslot:totems (o totem vanilla ja esta nela).
        TrinketsApi.registerTrinketPredicate(TOTEM_PREDICATE,
                (stack, slotRef, entity) -> TotemSlot.isTotem(stack) ? TriState.TRUE : TriState.FALSE);

        TotemHandler.init();
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
