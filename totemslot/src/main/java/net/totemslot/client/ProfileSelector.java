package net.totemslot.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Escolhe o perfil de posicao do totem. Prioridade:
 *   1. voando com elytra: flying / gliding (asas fechadas), com ou sem mochila
 *   2. COM mochila: backpack, chest_backpack, elytra_backpack, elytra_chest_backpack
 *   3. SEM mochila, com lanterna num slot Trinkets: lantern / chest_lantern (a lanterna so vale sem mochila)
 *   4. SEM mochila e sem lanterna: elytra_chest / elytra / chest / normal
 */
@Environment(EnvType.CLIENT)
public final class ProfileSelector {

    /** Abaixo desta abertura das asas (0 = fechadas, 1 = abertas) o perfil vira "gliding". */
    private static final float GLIDE_CLOSED_BELOW = 0.5F;

    private ProfileSelector() {
    }

    public static String select(LivingEntity entity) {
        boolean backpack = BackpackCompat.hasBackpack(entity);

        if (entity.isFallFlying()) {
            boolean closed = wingSpread(entity) < GLIDE_CLOSED_BELOW;
            if (closed) {
                return backpack ? "gliding_backpack" : "gliding";
            }
            return backpack ? "flying_backpack" : "flying";
        }

        boolean chest = hasChestplate(entity);
        boolean elytra = EquipmentCompat.hasElytra(entity);

        if (backpack) {
            if (elytra) {
                return chest ? "elytra_chest_backpack" : "elytra_backpack";
            }
            return chest ? "chest_backpack" : "backpack";
        }
        if (LanternCompat.hasLantern(entity)) {
            return chest ? "chest_lantern" : "lantern";
        }
        if (elytra) {
            return chest ? "elytra_chest" : "elytra";
        }
        return chest ? "chest" : "normal";
    }

    private static boolean hasChestplate(LivingEntity entity) {
        ItemStack chest = entity.getEquippedStack(EquipmentSlot.CHEST);
        return !chest.isEmpty() && !EquipmentCompat.isElytra(chest);
    }

    /** Mesma conta do modelo da elytra do vanilla: 1 = asas abertas, 0 = fechadas (mergulho vertical). */
    static float wingSpread(LivingEntity entity) {
        Vec3d velocity = entity.getVelocity();
        if (velocity.y < 0.0D) {
            return 1.0F - (float) Math.pow(-velocity.normalize().y, 1.5D);
        }
        return 1.0F;
    }

    public static List<String> explain(LivingEntity entity) {
        return List.of(
                "perfil ativo = " + select(entity),
                "voando=" + entity.isFallFlying() + ", asas(1=abertas,0=fechadas)="
                        + String.format("%.2f", wingSpread(entity))
                        + ", elytra=" + EquipmentCompat.hasElytra(entity) + ", peitoral=" + hasChestplate(entity)
                        + ", mochila=" + BackpackCompat.hasBackpack(entity)
                        + ", lanterna=" + LanternCompat.hasLantern(entity));
    }
}
