package net.backshield.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** Botao "Configurar" do Back Shield no Mod Menu. So e carregado quando o Mod Menu esta instalado. */
@Environment(EnvType.CLIENT)
public final class BackShieldModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return BackShieldConfigScreen::new;
    }
}
