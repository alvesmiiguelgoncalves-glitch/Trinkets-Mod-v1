package net.backtoolslot;

import dev.emi.trinkets.api.TrinketsApi;
import net.backtoolslot.slot.ToolSlot;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.util.Identifier;

public class BackToolSlotMain implements ModInitializer {

    public static final String MOD_ID = "backtoolslot";

    /** Id do validador de slot referenciado em data/trinkets/slots/chest/back_tool.json. */
    public static final Identifier TOOL_PREDICATE = id("tool");

    @Override
    public void onInitialize() {
        TrinketsApi.registerTrinketPredicate(TOOL_PREDICATE,
                (stack, slotRef, entity) -> ToolSlot.isTool(stack) ? TriState.TRUE : TriState.FALSE);
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
