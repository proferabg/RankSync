package com.gmail.chickenpowerrr.ranksync.velocity.command;

import com.gmail.chickenpowerrr.ranksync.server.link.LinkHelper;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.TabCompleteEvent;

/**
 * This class is allows autocompletion for the /sync and /unsync command
 *
 * @author Chickenpowerrr
 * @since 1.3.0
 */
public class RankSyncTabCompleter extends com.gmail.chickenpowerrr.ranksync.server.command.RankSyncTabCompleter {

  /**
   * @param linkHelper the link helper
   */
  public RankSyncTabCompleter(LinkHelper linkHelper) {
    super(linkHelper);
  }

  /**
   * Completes all of the possible platforms
   *
   * @param event the event that triggered the function
   */
  @Subscribe
  public void onTabComplete(TabCompleteEvent event) {
    switch (event.getPartialMessage().replaceAll(" .+", "").toLowerCase()) {
      case "ranksync":
      case "link":
      case "sync":
      case "synchronize":
      case "syncrank":
      case "synchronizerank":
      case "giverank":
        // Fall through
      case "unsync":
      case "unlink":
      case "unranksync":
      case "unsynchronize":
      case "unsyncrank":
      case "unsynchronizerank":
      case "revertrank":
        event.getSuggestions()
            .addAll(getOptions(event.getPartialMessage().replaceFirst(".+? ", "").split(" ")));
        break;
      default:
        break;
    }
  }
}
