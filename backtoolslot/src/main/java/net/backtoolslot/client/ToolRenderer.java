package net.backtoolslot.client;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.backtoolslot.slot.ToolSlot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

/**
 * Desenha a ferramenta nas costas, presa ao torso (body.rotate) para acompanhar agachar/nadar/voar.
 * Referencial do modelo: Y para BAIXO, +Z para TRAS; por isso o item leva uma rotacao de 180 em Z para ficar em pe.
 */
@Environment(EnvType.CLIENT)
public final class ToolRenderer implements TrinketRenderer {

    /** Superficie das costas no referencial do torso (4 pixels de profundidade / 2 / 16). */
    private static final float BACK_SURFACE = 0.125F;

    @Override
    public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {
        if (!(contextModel instanceof BipedEntityModel<?> biped)) {
            return;
        }
        // O renderizador vale por item; so desenha quando o item esta no NOSSO slot.
        if (!ToolSlot.SLOT.equals(slotReference.inventory().getSlotType().getName())) {
            return;
        }
        BackToolConfig.Profile p = BackToolConfig.profile(ProfileSelector.select(entity));
        if (!p.visible) {
            return;
        }
        float side = p.mirror ? -1.0F : 1.0F;

        matrices.push();
        biped.body.rotate(matrices);
        matrices.translate(p.x * side, p.y, BACK_SURFACE + p.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(p.rotY * side));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p.rotX));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F + p.rotZ * side));
        float scale = 1.0F + p.scale;
        matrices.scale(scale, scale, scale);

        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, light,
                OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), entity.getId());
        matrices.pop();
    }
}
