package net.totemslot.client;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.totemslot.slot.TotemSlot;

/**
 * Desenha o totem pendurado, preso ao torso OU a uma perna (campo "leg" do perfil), balancando ao andar.
 *
 * Preso a perna, o totem acompanha o movimento dela (nao e mais atravessado pela perna quando esta na frente) e
 * recebe uma contra-rotacao parcial para continuar mais "pendurado" do que rigido.
 *
 * Referencial do modelo: Y para BAIXO, +Z para TRAS; por isso o item leva uma rotacao de 180 em Z para ficar em pe,
 * e depois e descido para ficar pendurado pelo topo.
 */
@Environment(EnvType.CLIENT)
public final class TotemRenderer implements TrinketRenderer {

    /** Metade da altura de um item plano no modo FIXED (escala 0.5 do vanilla). */
    private static final float HALF_ITEM_HEIGHT = 0.25F;
    /** Quanto da rotacao da perna e cancelada no totem preso a perna (0 = rigido, 1 = sempre na vertical). */
    private static final float LEG_COUNTER = 0.6F;

    @Override
    public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {
        if (!(contextModel instanceof BipedEntityModel<?> biped)) {
            return;
        }
        // O renderizador vale por item; so desenha quando o item esta no NOSSO slot.
        if (!TotemSlot.SLOT.equals(slotReference.inventory().getSlotType().getName())) {
            return;
        }
        TotemConfig.Profile p = TotemConfig.profile(ProfileSelector.select(entity));
        if (!p.visible) {
            return;
        }
        float side = p.mirror ? -1.0F : 1.0F;
        float px = p.x * side;

        matrices.push();
        float counter = 0.0F;
        if (p.leg) {
            // x, y, z do perfil estao no referencial do torso; converte para o da perna (pivo atual do modelo).
            ModelPart leg = px < 0.0F ? biped.rightLeg : biped.leftLeg;
            leg.rotate(matrices);
            matrices.translate(px - leg.pivotX / 16.0F, p.y - leg.pivotY / 16.0F, p.z - leg.pivotZ / 16.0F);
            counter = -(float) Math.toDegrees(leg.pitch) * LEG_COUNTER;
        } else {
            biped.body.rotate(matrices);
            matrices.translate(px, p.y, p.z);
        }

        float sway = (float) Math.sin(limbAngle * 0.6662F) * limbDistance * p.swing
                + (float) Math.sin(animationProgress * 0.08F) * p.idle;

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(p.rotX + sway + counter));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(p.rotY * side));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F + p.rotZ));
        matrices.scale(p.scale, p.scale, p.scale);
        // Depois do giro de 180 em Z, "para baixo" e -Y: o centro do item desce meia altura, e o topo fica no pivo.
        matrices.translate(0.0F, -HALF_ITEM_HEIGHT, 0.0F);

        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, light,
                OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), entity.getId());
        matrices.pop();
    }
}
