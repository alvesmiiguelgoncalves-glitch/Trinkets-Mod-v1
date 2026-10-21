package net.totemslot;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.totemslot.slot.TotemSlot;

/**
 * Faz o totem do slot salvar o jogador, com o MESMO efeito do totem vanilla na mao:
 * vida 1, efeitos removidos, Regeneracao II (45s), Absorcao II (5s), Resistencia ao Fogo (40s), animacao/som
 * do totem, estatistica e gatilho de conquista.
 *
 * Usa o evento ALLOW_DEATH da Fabric API (sem mixin): so age se o jogador ia morrer e nao ha totem nas maos
 * (totem na mao continua sendo tratado pelo vanilla, entao nunca gasta dois).
 */
public final class TotemHandler {

    private TotemHandler() {
    }

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DEATH.register(TotemHandler::allowDeath);
    }

    /** @return false para cancelar a morte. */
    private static boolean allowDeath(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return true;
        }
        // Como no vanilla, o totem nao salva de /kill nem do void.
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        // Totem na mao: deixa o vanilla tratar.
        for (Hand hand : Hand.values()) {
            if (player.getStackInHand(hand).isOf(Items.TOTEM_OF_UNDYING)) {
                return true;
            }
        }

        ItemStack used = TotemSlot.consumeOne(player);
        if (used.isEmpty()) {
            return true;
        }

        player.incrementStat(Stats.USED.getOrCreateStat(Items.TOTEM_OF_UNDYING));
        Criteria.USED_TOTEM.trigger(player, used);
        player.setHealth(1.0F);
        player.clearStatusEffects();
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
        player.getWorld().sendEntityStatus(player, (byte) 35);
        return false;
    }
}
