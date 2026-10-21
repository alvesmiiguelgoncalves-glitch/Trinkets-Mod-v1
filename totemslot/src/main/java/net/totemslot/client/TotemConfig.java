package net.totemslot.client;

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
 * Posicao do totem pendurado (somente visual, cliente). Arquivo: config/totemslot-client.json
 *
 * Perfis INDEPENDENTES (mexer em um nunca altera outro):
 *   normal                    - sem nada de especial
 *   chest                     - peitoral (armadura)
 *   lantern, chest_lantern    - lanterna num slot Trinkets (so vale SEM mochila); sem / com peitoral
 *   backpack, chest_backpack  - mochila do Backpacked; sem / com peitoral
 *   elytra, elytra_chest      - elytra equipada (sem mochila)
 *   elytra_backpack, elytra_chest_backpack - elytra + mochila
 *   flying, flying_backpack   - voando com elytra, asas abertas
 *   gliding, gliding_backpack - voando com elytra, asas fechadas
 *
 * Campos de cada perfil:
 *   x, y, z      ponto onde o totem esta preso, no referencial do torso (blocos; 1 pixel = 0.0625):
 *                x lateral (negativo = direita do jogador); y para BAIXO a partir do pescoco (0.70 = cintura);
 *                z para TRAS (negativo = frente do corpo)
 *   rotX/rotY/rotZ  graus (rotY 90 = de lado)
 *   scale        tamanho (1 = item de moldura; 0.4 = pequeno)
 *   swing        quanto balanca ao andar (graus);  idle = balanco leve parado (graus)
 *   leg          true = o totem fica preso a PERNA e acompanha o movimento dela (use quando ele esta na frente,
 *                onde a perna passaria por cima); false = preso ao corpo. A perna e a direita se x < 0, senao a esquerda.
 *   visible      false esconde o totem neste perfil
 *   mirror       true troca de lado (x e rotY mudam de sinal)
 */
@Environment(EnvType.CLIENT)
public final class TotemConfig {

    public static final List<String> PROFILES = List.of(
            "normal", "chest", "lantern", "chest_lantern",
            "backpack", "chest_backpack",
            "elytra", "elytra_chest", "elytra_backpack", "elytra_chest_backpack",
            "flying", "flying_backpack", "gliding", "gliding_backpack");

    public static final List<String> FIELDS = List.of("x", "y", "z", "rotX", "rotY", "rotZ", "scale", "swing", "idle",
            "leg", "visible", "mirror");

    // ordem: x, y, z, rotX, rotY, rotZ, scale, swing, idle, leg(1/0)
    private static final float[] SIDE = {-0.27F, 0.7F, -0.1F, 0.0F, 80.0F, 0.0F, 0.4F, 15.0F, 2.0F, 0.0F};
    private static final float[] SIDE_CHEST = {-0.3F, 0.7F, -0.1F, 0.0F, 80.0F, 0.0F, 0.4F, 15.0F, 2.0F, 0.0F};
    private static final float[] LANTERN = {0.15F, 0.7F, -0.165F, -10.0F, 0.0F, 0.0F, 0.4F, 10.0F, 4.0F, 1.0F};
    private static final float[] BACKPACK = {-0.35F, 0.5F, 0.3F, 0.0F, 90.0F, 0.0F, 0.4F, 15.0F, 2.0F, 0.0F};
    private static final float[] BACKPACK_CHEST = {-0.35F, 0.5F, 0.35F, 0.0F, 90.0F, 0.0F, 0.4F, 15.0F, 2.0F, 0.0F};
    private static final float[] ELYTRA_BACKPACK = {-0.15F, 0.5F, 0.599F, 0.0F, 180.0F, 0.0F, 0.4F, 15.0F, 2.0F, 0.0F};

    /**
     * Valores iniciais. Os de normal, chest, lantern, backpack, chest_backpack e elytra_backpack sao os que voce
     * ajustou; os outros sao COPIAS deles (chest_lantern = lantern, elytra = normal, elytra_chest = chest,
     * elytra_chest_backpack = elytra_backpack, flying/gliding = normal, flying_backpack/gliding_backpack =
     * elytra_backpack) ate voce ajusta-los.
     */
    private static final Map<String, float[]> DEFAULTS = Map.ofEntries(
            Map.entry("normal", SIDE),
            Map.entry("chest", SIDE_CHEST),
            Map.entry("lantern", LANTERN),
            Map.entry("chest_lantern", LANTERN),
            Map.entry("backpack", BACKPACK),
            Map.entry("chest_backpack", BACKPACK_CHEST),
            Map.entry("elytra", SIDE),
            Map.entry("elytra_chest", SIDE_CHEST),
            Map.entry("elytra_backpack", ELYTRA_BACKPACK),
            Map.entry("elytra_chest_backpack", ELYTRA_BACKPACK),
            Map.entry("flying", SIDE),
            Map.entry("flying_backpack", ELYTRA_BACKPACK),
            Map.entry("gliding", SIDE),
            Map.entry("gliding_backpack", ELYTRA_BACKPACK));

    private static final Logger LOGGER = LoggerFactory.getLogger("totemslot");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<LinkedHashMap<String, Profile>>() {
    }.getType();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("totemslot-client.json");
    private static final Path FILE_TMP = FabricLoader.getInstance().getConfigDir().resolve("totemslot-client.json.tmp");

    private static volatile Map<String, Profile> profiles = new LinkedHashMap<>();

    private TotemConfig() {
    }

    public static final class Profile {
        public float x = 0.0F;
        public float y = 0.0F;
        public float z = 0.0F;
        public float rotX = 0.0F;
        public float rotY = 0.0F;
        public float rotZ = 0.0F;
        public float scale = 0.4F;
        public float swing = 15.0F;
        public float idle = 2.0F;
        public boolean leg = false;
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
            p.swing = d[7];
            p.idle = d[8];
            p.leg = d[9] != 0.0F;
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
                String text = Files.readString(FILE, StandardCharsets.UTF_8);
                loaded = GSON.fromJson(text, MAP_TYPE);
                // Formato antigo (um unico conjunto de campos soltos, sem perfis): guarda copia e recomeca.
                if (loaded != null && !text.contains("\"normal\"")) {
                    backup("old");
                    loaded = null;
                }
            } catch (Exception e) {
                LOGGER.warn("Could not read {} - keeping a copy as totemslot-client.broken.json", FILE, e);
                backup("broken");
                loaded = null;
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

    private static void backup(String suffix) {
        try {
            Files.copy(FILE, FILE.resolveSibling("totemslot-client." + suffix + ".json"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("Could not back up {}", FILE, e);
        }
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
        Profile profile = profiles.computeIfAbsent(profileName, TotemConfig::defaultProfile);
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
                    + " rotZ=" + p.rotZ + " scale=" + p.scale + " swing=" + p.swing + " idle=" + p.idle
                    + " leg=" + p.leg + " visible=" + p.visible + " mirror=" + p.mirror);
        }
        return lines;
    }
}
