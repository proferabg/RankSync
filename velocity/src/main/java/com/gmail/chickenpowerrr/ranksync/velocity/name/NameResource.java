package com.gmail.chickenpowerrr.ranksync.velocity.name;

import com.google.gson.JsonParser;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

/**
 * This class looks up the last known name of a user by their UUID
 *
 * @author Chickenpowerrr
 * @since 1.3.0
 */
public class NameResource implements com.gmail.chickenpowerrr.ranksync.api.name.NameResource {
  private final JsonParser jsonParser = new JsonParser();

  private ProxyServer server;

  /**
   * Saves the plugin instance
   *
   * @param server the proxy server instance
   */
  public NameResource(ProxyServer server) {
    this.server = server;
  }

  /**
   * Gets the name of the player if they're online, otherwise asks the Mojang API for the current
   * username
   *
   * @param uuid the UUID on the platform the name should be retrieved from
   */
  @Override
  public String getName(UUID uuid) {
    Optional<Player> proxiedPlayer = server.getPlayer(uuid);
    if (!proxiedPlayer.isPresent()) {
      try {
        URL url = new URL(
            "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString()
                .replace("-", ""));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try (BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(connection.getInputStream()))) {
          return this.jsonParser.parse(bufferedReader).getAsJsonObject().get("name").getAsString();
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      return proxiedPlayer.get().getUsername();
    }
  }
}
