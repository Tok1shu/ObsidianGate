package net.tokishu.command.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.tokishu.util.Base;
import net.tokishu.util.helper.database.repository.User;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Unlink extends Base {
    private static Map<String, String> linkedUsersMap = new HashMap<>();

    public static void updateLinkedNicknames(Connection connection) {
        List<String[]> linkedUsers = User.getAllLinkedUsers(connection);
        linkedUsersMap = fetchMinecraftUsernames(linkedUsers);

    }

    private static Map<String, String> fetchMinecraftUsernames(List<String[]> linkedUsers) {
        Map<String, String> userMap = new HashMap<>();


        for (String[] user : linkedUsers) {
            String uuid = user[0];
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

                    // Парсим JSON-ответ
                    JsonObject profileData = new Gson().fromJson(response.toString(), JsonObject.class);
                    String username = profileData.get("name").getAsString();

                    userMap.put(uuid, username);
                } else {
                    plugin.getLogger().warning("[ Unlink command ] Failed to fetch username for UUID: " + uuid);
                }
                conn.disconnect();
            } catch (Exception e) {
                plugin.getLogger().severe("[ Unlink command ] Error fetching username for UUID " + uuid + ": " + e.getMessage());
            }
        }

        return userMap;
    }

    private static UUID findUuidByNicknameFromMap(String nickname) {
        for (Map.Entry<String, String> entry : linkedUsersMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(nickname)) {
                return UUID.fromString(entry.getKey());
            }
        }
        return null;
    }

    public static void unlink(CommandSender sender, String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            sender.sendMessage("§7[§dObsidianGate§7] §cHow did you end up here?");
            return;
        }

        UUID linkedUuid = findUuidByNicknameFromMap(nickname);

        if (linkedUuid == null) {
            sender.sendMessage("§7[§dObsidianGate§7] §cNo linked account found for nickname: " + nickname);
            return;
        }

        try {
            User.unlinkDiscord(connection, linkedUuid.toString());
            sender.sendMessage("§7[§dObsidianGate§7] §aAccount successfully unlinked for nickname: " + nickname);
        } catch (Exception e) {
            sender.sendMessage("§7[§dObsidianGate§7] §cError unlinking account: " + e.getMessage());
        }
    }

    public static void unlink(CommandSender sender) {
        sender.sendMessage("§7[§dObsidianGate§7] §eTo unlink your account, send 'unlink' to §a" + getBotTag() + "§e in private messages.");
    }

    public static List<String> getLinkedNicknamesCompletion(String partialNickname) {
        if (partialNickname == null || partialNickname.isEmpty()) {
            return new ArrayList<>(linkedUsersMap.values());
        }

        return linkedUsersMap.values().stream()
                .filter(nickname -> nickname.toLowerCase().startsWith(partialNickname.toLowerCase()))
                .collect(Collectors.toList());
    }

    private static class ProfileResponse {
        String id;
        String name;
        Boolean legacy;
        Boolean demo;
    }
}