package net.tokishu.util.helper;

import net.tokishu.ObsidianGate;
import net.tokishu.util.Base;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;
import java.security.SecureRandom;
import java.util.Base64;

public class ApiKey extends Base {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String[] GREEK_ALPHABET = {
            "alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta",
            "iota", "kappa", "lambda", "mu", "nu", "xi", "omicron", "pi", "rho",
            "sigma", "tau", "upsilon", "phi", "chi", "psi", "omega"
    };


    public static String generateApiKey() {
        byte[] keyBytes = new byte[32]; // 256 бит
        secureRandom.nextBytes(keyBytes);
        String baseKey = Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);

        // Выбираем случайное слово из греческого алфавита
        String greekWord = GREEK_ALPHABET[secureRandom.nextInt(GREEK_ALPHABET.length)].toUpperCase();

        // Формируем финальный ключ
        return "GATE_" + baseKey + "_" + greekWord;
    }

    public static boolean checkApiKey(String key) {
        String configKey = config.getString("api-key");
        return Objects.equals(configKey, key);
    }
}
