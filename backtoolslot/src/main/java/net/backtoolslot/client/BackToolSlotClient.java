package net.backtoolslot.client;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.backtoolslot.slot.ToolSlot;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public class BackToolSlotClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BackToolConfig.load();

        // O Trinkets so desenha itens que tem renderizador. Tags so existem depois de entrar no mundo, entao
        // registramos aqui, quando as tags chegam: so para os itens da tag #backtoolslot:tools. (Ferramentas de
        // outros mods que apenas estendem ToolItem, sem estar na tag, ficam no slot mas nao sao desenhadas:
        // adicione-as na tag por datapack.)
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (!client) {
                return;
            }
            ToolRenderer renderer = new ToolRenderer();
            for (RegistryEntry<Item> entry : Registries.ITEM.iterateEntries(ToolSlot.TOOLS)) {
                TrinketRendererRegistry.registerRenderer(entry.value(), renderer);
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("backtool")
                        .then(ClientCommandManager.literal("status").executes(ctx -> {
                            var player = ctx.getSource().getPlayer();
                            ProfileSelector.explain(player).forEach(l -> ctx.getSource().sendFeedback(Text.literal(l)));
                            BackpackCompat.describe(player).forEach(l -> ctx.getSource().sendFeedback(Text.literal(l)));
                            EquipmentCompat.describeTrinkets(player).forEach(l -> ctx.getSource().sendFeedback(Text.literal(l)));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("show")
                                .executes(ctx -> {
                                    BackToolConfig.describe(null).forEach(l -> ctx.getSource().sendFeedback(Text.literal(l)));
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("profile", StringArgumentType.word())
                                        .suggests((ctx, b) -> CommandSource.suggestMatching(BackToolConfig.PROFILES, b))
                                        .executes(ctx -> {
                                            BackToolConfig.describe(StringArgumentType.getString(ctx, "profile"))
                                                    .forEach(l -> ctx.getSource().sendFeedback(Text.literal(l)));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("reload").executes(ctx -> {
                            BackToolConfig.load();
                            ctx.getSource().sendFeedback(Text.literal("Back Tool Slot: config recarregada"));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("reset")
                                .executes(ctx -> {
                                    BackToolConfig.reset(null);
                                    ctx.getSource().sendFeedback(Text.literal("Todos os perfis restaurados ao padrao"));
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("profile", StringArgumentType.word())
                                        .suggests((ctx, b) -> CommandSource.suggestMatching(BackToolConfig.PROFILES, b))
                                        .executes(ctx -> {
                                            String profile = StringArgumentType.getString(ctx, "profile");
                                            if (!BackToolConfig.PROFILES.contains(profile)) {
                                                ctx.getSource().sendFeedback(Text.literal("Perfil desconhecido: " + profile));
                                                return 0;
                                            }
                                            BackToolConfig.reset(profile);
                                            ctx.getSource().sendFeedback(Text.literal("Perfil " + profile + " restaurado ao padrao"));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("set")
                                .then(ClientCommandManager.argument("profile", StringArgumentType.word())
                                        .suggests((ctx, b) -> CommandSource.suggestMatching(BackToolConfig.PROFILES, b))
                                        .then(ClientCommandManager.argument("field", StringArgumentType.word())
                                                .suggests((ctx, b) -> CommandSource.suggestMatching(BackToolConfig.FIELDS, b))
                                                .then(ClientCommandManager.argument("value", FloatArgumentType.floatArg())
                                                        .executes(ctx -> {
                                                            String profile = StringArgumentType.getString(ctx, "profile");
                                                            String field = StringArgumentType.getString(ctx, "field");
                                                            float value = FloatArgumentType.getFloat(ctx, "value");
                                                            boolean ok = BackToolConfig.set(profile, field, value);
                                                            ctx.getSource().sendFeedback(Text.literal(ok
                                                                    ? profile + "." + field + " = " + value
                                                                    : "Perfil ou campo desconhecido: " + profile + " " + field));
                                                            return ok ? 1 : 0;
                                                        })))))));
    }
}
