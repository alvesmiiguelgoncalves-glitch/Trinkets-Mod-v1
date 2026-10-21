package net.backtoolslot.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Posicao da ferramenta nas costas (somente visual, cliente). Arquivo: config/backtoolslot-client.json
 *
 * Perfis INDEPENDENTES (mexer em um nunca altera outro):
 *   normal, chest, backpack, chest_backpack,
 *   elytra, elytra_chest, elytra_backpack, elytra_chest_backpack,
 *   flying, flying_backpack  (voando com elytra, asas abertas)
 *   gliding, gliding_backpack (voando com elytra, asas fechadas)
 * ("chest" = peitoral de armadura equipado; "backpack" = mochila do Backpacked.)
 *
 * Cada perfil: x, y, z, rotX, rotY, rotZ, scale, visible, mirror.
 *   x lateral; y para BAIXO a partir dos ombros; z para TRAS a partir da superficie das costas;
 *   rotacoes em graus (em torno do centro do item, ordem Y, X, Z);
 *   scale = DIFERENCA de tamanho (0 = tamanho de item de moldura, 0.5 = 50% maior);
 *   visible=false esconde a ferramenta naquele perfil; mirror=true troca de lado (x, rotY e rotZ trocam de sinal).
 */
@Environment(EnvType.CLIENT)
public final class BackToolConfig {

    public static final List<String> PROFILES = List.of(
            "normal", "chest", "backpack", "chest_backpack",
            "elytra", "elytra_chest", "elytra_backpack", "elytra_chest_backpack",
            "flying", "flying_backpack", "gliding", "gliding_backpack");

    public static final List<String> FIELDS = List.of("x", "y", "z", "rotX", "rotY", "rotZ", "scale", "visible", "mirror");

    /**
     * Valores iniciais (x, y, z, rotX, rotY, rotZ, scale). Sao um ponto de partida: a ferramenta fica na diagonal
     * nas costas, e com mochila vai para o lado OPOSTO ao do escudo do Back Shield (x negativo) para nao se sobrepor.
     * Ajuste ao vivo com /backtool set.
     */
    private static final Map<String, float[]> DEFAULTS = Map.ofEntries(
            Map.entry("normal", new float[]{0.12F, 0.40F, 0.05F, 0.0F, 0.0F, 0.0F, 0.4F}),
            Map.entry("chest", new float[]{0.12F, 0.40F, 0.07F, 0.0F, 0.0F, 0.0F, 0.4F}),
            Map.entry("backpack", new float[]{-0.26F, 0.45F, 0.30F, 0.0F, -20.0F, 0.0F, 0.3F}),
            Map.entry("chest_backpack", new float[]{-0.26F, 0.45F, 0.36F, 0.0F, -20.0F, 0.0F, 0.3F}),
            Map.entry("elytra", new float[]{0.12F, 0.35F, 0.30F, 15.0F, 0.0F, 0.0F, 0.4F}),
            Map.entry("elytra_chest", new float[]{0.12F, 0.35F, 0.32F, 15.0F, 0.0F, 0.0F, 0.4F}),
            Map.entry("elytra_backpack", new float[]{-0.24F, 0.40F, 0.48F, 5.0F, -15.0F, 0.0F, 0.3F}),
            Map.entry("elytra_chest_backpack", new float[]{-0.24F, 0.40F, 0.50F, 5.0F, -15.0F, 0.0F, 0.3F}),
            Map.entry("flying", new float[]{0.12F, 0.50F, 0.15F, 0.0F, 0.0F, 0.0F, 0.4F}),
            Map.entry("flying_backpack", new float[]{-0.24F, 0.40F, 0.48F, 5.0F, -15.0F, 0.0F, 0.3F}),
            Map.entry("gliding", new float[]{0.12F, 0.50F, 0.15F, 0.0F, 0.0F, 0.0F, 0.4F}),
            Map.entry("gliding_backpack", new float[]{-0.24F, 0.40F, 0.48F, 5.0F, -15.0F, 0.0F, 0.3F}));

    private static final Logger LOGGER = LoggerFactory.getLogger("backtoolslot");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, Profile>>() {
    }.getType();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("backtoolslot-client.json");
    private static final Path FILE_TMP = FabricLoader.getInstance().getConfigDir().resolve("backtoolslot-client.json.tmp");

    private static volatile Map<String, Profile> profiles = new LinkedHashMap<>();

    private BackToolConfig() {
    }

    public static final class Profile {
        public float x = 0.0F;
        public float y = 0.0F;
        public float z = 0.0F;
        public float rotX = 0.0F;
        public float rotY = 0.0F;
        public float rotZ = 0.0F;
        public float scale = 0.0F;
        public boolean visible = true;
        public boolean mirror = false;
    }

    private static Profile defaultProfile(String name) {
        Profile p = new Profile();
        float[] d = DEFAULTS.get(name);
        if (d != null) {
            p.x = d[0];
            p.y = d[1];
            p.z = d[2];
            p.rotX = d[3];
            p.rotY = d[4];
            p.rotZ = d[5];
            p.scale = d[6];
        }
        return p;
    }

    /** Nunca retorna null. */
    public static Profile profile(String name) {
        Profile p = profiles.get(name);
        return p != null ? p : defaultProfile(name);
    }

    public static synchronized void load() {
        Map<String, Profile> loaded = null;
        if (Files.exists(FILE)) {
            try {
                loaded = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), MAP_TYPE);
            } catch (Exception e) {
                LOGGER.warn("Could not read {} - keeping a copy as backtoolslot-client.broken.json", FILE, e);
                try {
                    Files.copy(FILE, FILE.resolveSibling("backtoolslot-client.broken.json"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {
                }
            }
        }
        Map<String, Profile> result = new LinkedHashMap<>();
        if (loaded != null) {
            loaded.forEach((k, v) -> {
                if (v != null) {
                    result.put(k, v);
                }
            });
        }
        for (String name : PROFILES) {
            result.putIfAbsent(name, defaultProfile(name));
        }
        profiles = result;
        save();
    }

    public static synchronized void save() {
        try {
            Files.writeString(FILE_TMP, GSON.toJson(profiles, MAP_TYPE), StandardCharsets.UTF_8);
            try {
                Files.move(FILE_TMP, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicUnsupported) {
                Files.move(FILE_TMP, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", FILE, e);
        }
    }

    /** Restaura os valores iniciais de um perfil (ou de todos, se name for null). */
    public static synchronized void reset(String name) {
        for (String profileName : PROFILES) {
            if (name == null || name.equals(profileName)) {
                profiles.put(profileName, defaultProfile(profileName));
            }
        }
        save();
    }

    public static synchronized boolean set(String profileName, String fieldName, float value) {
        if (!PROFILES.contains(profileName) || !FIELDS.contains(fieldName)) {
            return false;
        }
        Profile profile = profiles.computeIfAbsent(profileName, BackToolConfig::defaultProfile);
        try {
            Field field = Profile.class.getField(fieldName);
            if (field.getType() == boolean.class) {
                field.setBoolean(profile, value != 0.0F);
            } else {
                field.setFloat(profile, value);
            }
            save();
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    public static List<String> describe(String profileName) {
        List<String> lines = new ArrayList<>();
        for (String name : PROFILES) {
            if (profileName != null && !profileName.equals(name)) {
                continue;
            }
            Profile p = profile(name);
            lines.add("[" + name + "] x=" + p.x + " y=" + p.y + " z=" + p.z + " rotX=" + p.rotX + " rotY=" + p.rotY
                    + " rotZ=" + p.rotZ + " scale=" + p.scale + " visible=" + p.visible + " mirror=" + p.mirror);
        }
        return lines;
    }
}
