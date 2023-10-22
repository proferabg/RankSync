package com.gmail.chickenpowerrr.ranksync.velocity.user;

import com.gmail.chickenpowerrr.ranksync.api.user.User;
import java.util.UUID;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * This class wraps a bungee player
 *
 * @author Chickenpowerrr
 * @since 1.3.0
 */
@Getter
@AllArgsConstructor
public class VelocityUser implements User {

  private final CommandSource commandSender;

  /**
   * Sends the given message to the bungee user
   *
   * @param message the message the bungee user should receive
   */
  @Override
  public void sendMessage(String message) {
    this.commandSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
  }

  /**
   * Returns the UUID of the represented bungee user
   */
  @Override
  public UUID getUuid() {
    return ((Player) this.commandSender).getUniqueId();
  }
}
