package com.gmail.chickenpowerrr.ranksync.velocity.command;

import com.gmail.chickenpowerrr.ranksync.api.user.User;
import com.gmail.chickenpowerrr.ranksync.server.plugin.RankSyncServerPlugin;
import com.gmail.chickenpowerrr.ranksync.velocity.user.VelocityUser;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * This class handles any incoming /sync command
 *
 * @author Chickenpowerrr
 * @since 1.3.0
 */
public class RankSyncCommand implements SimpleCommand {

  private final com.gmail.chickenpowerrr.ranksync.server.command.RankSyncCommand rankSyncCommand;

  /**
   * @param rankSyncPlugin the ranksync plugin
   */
  public RankSyncCommand(RankSyncServerPlugin rankSyncPlugin) {
    this.rankSyncCommand = new com.gmail.chickenpowerrr.ranksync.server.command.RankSyncCommand(rankSyncPlugin) {
      @Override
      protected boolean isValidUser(User user) {
        return ((VelocityUser) user).getCommandSender() instanceof Player;
      }
    };
  }

  /**
   * Links a Minecraft account if the player has a valid code
   */
  @Override
  public void execute(Invocation invocation) {
    if(!invocation.source().hasPermission("ranksync.command.ranksync")) invocation.source().sendMessage(Component.text("No Permission!").color(NamedTextColor.DARK_RED));
    this.rankSyncCommand.execute(new VelocityUser(invocation.source()), "ranksync", invocation.arguments());
  }
}
