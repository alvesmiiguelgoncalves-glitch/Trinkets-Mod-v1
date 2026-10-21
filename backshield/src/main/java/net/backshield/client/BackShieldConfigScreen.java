package net.backshield.client;

import net.backshield.config.BackShieldConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Tela de configuracao (Mod Menu): escolhe o perfil com < >, edita x/y/z/rotacoes/escala, liga/desliga
 * "visivel" e "espelhar" e restaura o padrao do perfil. Cada alteracao ja e salva no arquivo de config.
 */
@Environment(EnvType.CLIENT)
public final class BackShieldConfigScreen extends Screen {

    private static final List<String> LEFT = List.of("x", "y", "z", "scale");
    private static final List<String> RIGHT = List.of("rotX", "rotY", "rotZ");

    private final Screen parent;
    private int index;

    public BackShieldConfigScreen(Screen parent) {
        super(Text.translatable("screen.backshield.config"));
        this.parent = parent;
    }

    private String profileName() {
        return BackShieldConfig.PROFILES.get(index);
    }

    private BackShieldConfig.Profile profile() {
        return BackShieldConfig.profile(profileName());
    }

    private static float read(BackShieldConfig.Profile p, String field) {
        return switch (field) {
            case "x" -> p.x;
            case "y" -> p.y;
            case "z" -> p.z;
            case "rotX" -> p.rotX;
            case "rotY" -> p.rotY;
            case "rotZ" -> p.rotZ;
            default -> p.scale;
        };
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> change(-1))
                .dimensions(cx - 150, 36, 20, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> change(1))
                .dimensions(cx + 130, 36, 20, 20).build());

        addNumberFields(LEFT, cx - 150, 70);
        addNumberFields(RIGHT, cx + 10, 70);

        BackShieldConfig.Profile p = profile();
        this.addDrawableChild(ButtonWidget.builder(toggleText("options.backshield.visible", p.visible), b -> {
            BackShieldConfig.set(profileName(), "visible", profile().visible ? 0.0F : 1.0F);
            b.setMessage(toggleText("options.backshield.visible", profile().visible));
        }).dimensions(cx - 150, 168, 140, 20).build());
        this.addDrawableChild(ButtonWidget.builder(toggleText("options.backshield.mirror", p.mirror), b -> {
            BackShieldConfig.set(profileName(), "mirror", profile().mirror ? 0.0F : 1.0F);
            b.setMessage(toggleText("options.backshield.mirror", profile().mirror));
        }).dimensions(cx + 10, 168, 140, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("button.backshield.reset"), b -> {
            BackShieldConfig.reset(profileName());
            this.clearAndInit();
        }).dimensions(cx - 150, 198, 140, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> this.close())
                .dimensions(cx + 10, 198, 140, 20).build());
    }

    private void addNumberFields(List<String> names, int x, int y0) {
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            TextFieldWidget field = new TextFieldWidget(this.textRenderer, x + 50, y0 + i * 24, 90, 18, Text.literal(name));
            field.setMaxLength(12);
            field.setText(String.valueOf(read(profile(), name)));
            field.setChangedListener(text -> {
                try {
                    BackShieldConfig.set(profileName(), name, Float.parseFloat(text.trim().replace(',', '.')));
                } catch (NumberFormatException ignored) {
                    // texto incompleto (ex.: "-"): espera o proximo caractere
                }
            });
            this.addDrawableChild(field);
        }
    }

    private static Text toggleText(String key, boolean value) {
        return Text.translatable(key).append(": ").append(ScreenTexts.onOrOff(value));
    }

    private void change(int delta) {
        int size = BackShieldConfig.PROFILES.size();
        index = (index + delta + size) % size;
        this.clearAndInit();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int cx = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, 16, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(profileName()), cx, 42, 0xFFFFAA);
        for (int i = 0; i < LEFT.size(); i++) {
            context.drawTextWithShadow(this.textRenderer, LEFT.get(i), cx - 150, 75 + i * 24, 0xAAAAAA);
        }
        for (int i = 0; i < RIGHT.size(); i++) {
            context.drawTextWithShadow(this.textRenderer, RIGHT.get(i), cx + 10, 75 + i * 24, 0xAAAAAA);
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
