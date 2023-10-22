package com.gmail.chickenpowerrr.ranksync.velocity.listener;

import com.gmail.chickenpowerrr.ranksync.server.link.LinkHelper;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import lombok.AllArgsConstructor;

/**
 * This class updates a player's ranks when they switch servers
 *
 * @author Chickenpowerrr
 * @since 1.3.0
 */
@AllArgsConstructor
public class ServerConnectEventListener {

  private final LinkHelper linkHelper;

  /**
   * Updates a player's ranks when they switch servers
   *
   * @param event the event that triggered the method
   */
  @Subscribe
  public void onServerConnect(PostLoginEvent event) {
    this.linkHelper.updateRanks(event.getPlayer().getUniqueId());
  }
}
