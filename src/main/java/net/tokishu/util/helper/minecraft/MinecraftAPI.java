package net.tokishu.util.helper.minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.tokishu.util.Base;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Utility class for interacting with Minecraft-related APIs,
 * primarily focused on fetching player profile information.
 */
public class MinecraftAPI extends Base {

    /**
     * Fetches Minecraft usernames for a list of UUIDs using Mojang's session server API.
     *
     * @param linkedUsers List of user identifiers (typically UUID strings)
     * @return Map of UUID to Minecraft username
     */
    public static Map<String, String> fetchMinecraftUsernames(List<String[]> linkedUsers) {
        Map<String, String> userMap = new HashMap<>();

        for (String[] user : linkedUsers) {
            String uuid = user[0];
            try {
                // Construct Mojang session server profile URL
                URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read response
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse JSON response
                    JsonObject profileData = new Gson().fromJson(response.toString(), JsonObject.class);
                    String username = profileData.get("name").getAsString();

                    userMap.put(uuid, username);
                } else {
                    // Log warning for failed requests
                    plugin.getLogger().warning(String.format("[MinecraftAPI] Failed to fetch username for UUID: %s (Response code: %d)",
                            uuid, responseCode));
                }
                conn.disconnect();
            } catch (Exception e) {
                // Log severe errors
                plugin.getLogger().log(Level.SEVERE,
                        String.format("[MinecraftAPI] Error fetching username for UUID %s", uuid),
                        e);
            }
        }

        return userMap;
    }

    /**
     * Finds a UUID by Minecraft username from a given nickname.
     *
     * @param nickname Minecraft username to search for
     * @return UUID of the matching username, or null if not found
     */
    public static UUID findUuidByNicknameFromMap(String nickname) {
        return findUuidByNicknameFromMap(getLinkedUsersMap(), nickname);
    }

    /**
     * Finds a UUID by Minecraft username from a given map of users.
     *
     * @param linkedUsersMap Map of UUIDs to Minecraft usernames
     * @param nickname Minecraft username to search for
     * @return UUID of the matching username, or null if not found
     */
    public static UUID findUuidByNicknameFromMap(Map<String, String> linkedUsersMap, String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return null;
        }

        for (Map.Entry<String, String> entry : linkedUsersMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(nickname)) {
                return UUID.fromString(entry.getKey());
            }
        }
        return null;
    }

    /**
     * Retrieves a list of linked Minecraft usernames that start with a given prefix.
     *
     * @param partialNickname Prefix to match against usernames
     * @return List of matching usernames
     */
    public static List<String> getLinkedNicknamesCompletion(String partialNickname) {
        return getLinkedNicknamesCompletion(getLinkedUsersMap(), partialNickname);
    }

    /**
     * Retrieves a list of linked Minecraft usernames that start with a given prefix.
     *
     * @param linkedUsersMap Map of UUIDs to Minecraft usernames
     * @param partialNickname Prefix to match against usernames
     * @return List of matching usernames
     */
    public static List<String> getLinkedNicknamesCompletion(Map<String, String> linkedUsersMap, String partialNickname) {
        if (partialNickname == null || partialNickname.isEmpty()) {
            return new ArrayList<>(linkedUsersMap.values());
        }

        return linkedUsersMap.values().stream()
                .filter(nickname -> nickname.toLowerCase().startsWith(partialNickname.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Fetches Minecraft username for a specific UUID using Mojang's session server API.
     *
     * @param uuid The UUID of the Minecraft player
     * @return The username associated with the UUID, or null if not found or an error occurs
     */
    public static String getMinecraftUsername(String uuid) {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JsonObject profileData = new Gson().fromJson(response.toString(), JsonObject.class);
                return profileData.get("name").getAsString();
            } else {
                plugin.getLogger().warning(String.format("[MinecraftAPI] Failed to fetch username for UUID: %s (Response code: %d)",
                        uuid, responseCode));
                return null;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    String.format("[MinecraftAPI] Error fetching username for UUID %s", uuid),
                    e);
            return null;
        }
    }
}