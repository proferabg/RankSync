package com.gmail.chickenpowerrr.ranksync.velocity.listener;

import com.gmail.chickenpowerrr.ranksync.server.link.LinkHelper;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import lombok.AllArgsConstructor;

/**
 * This class updates a player's ranks when they disconnect from the network
 *
 * @author Chickenpowerrr
 * @since 1.3.0
 */
@AllArgsConstructor
public class PlayerDisconnectEventListener {

  private final LinkHelper linkHelper;

  /**
   * Updates a player's ranks when they disconnect from the network
   *
   * @param event the event that triggered the method
   */
  @Subscribe
  public void onDisconnect(DisconnectEvent event) {
    this.linkHelper.updateRanks(event.getPlayer().getUniqueId());
  }
}
