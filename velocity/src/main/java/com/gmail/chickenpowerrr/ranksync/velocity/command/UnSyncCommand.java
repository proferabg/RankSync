package com.gmail.chickenpowerrr.ranksync.velocity.command;

import com.gmail.chickenpowerrr.ranksync.api.user.User;
import com.gmail.chickenpowerrr.ranksync.server.plugin.RankSyncServerPlugin;
import com.gmail.chickenpowerrr.ranksync.velocity.user.VelocityUser;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


/**
 * This class handles any incoming /unsync command
 *
 * @author Chickenpowerrr
 * @since 1.3.0
 */
public class UnSyncCommand implements SimpleCommand {

  private final com.gmail.chickenpowerrr.ranksync.server.command.UnSyncCommand unSyncCommand;

  /**
   * @param rankSyncPlugin the ranksync plugin
   */
  public UnSyncCommand(RankSyncServerPlugin rankSyncPlugin) {
    this.unSyncCommand = new com.gmail.chickenpowerrr.ranksync.server.command.UnSyncCommand(rankSyncPlugin) {
      @Override
      protected boolean isValidUser(User user) {
        return ((VelocityUser) user).getCommandSender() instanceof Player;
      }
    };
  }

  /**
   * Unlinks a Minecraft account if the account has been linked
   *
   */

  @Override
  public void execute(Invocation invocation) {
    if(!invocation.source().hasPermission("ranksync.command.unsync")) invocation.source().sendMessage(Component.text("No Permission!").color(NamedTextColor.DARK_RED));
    this.unSyncCommand.execute(new VelocityUser(invocation.source()), "unsync", invocation.arguments());
  }
}
