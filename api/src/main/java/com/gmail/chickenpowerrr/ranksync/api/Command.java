package com.gmail.chickenpowerrr.ranksync.api;

import java.util.Collection;
import java.util.List;

/**
 * This interface contains all of the method needed to
 * create a functional command that will get accepted
 * by the {@code CommandFactory}
 *
 * @author Chickenpowerrr
 * @since  1.0.0
 */
public interface Command {

    /**
     * Get the 'name' of the command
     *
     * @return the 'name' of the command
     */
    String getLabel();

    /**
     * Get all of the aliases that, besides the name can be
     * used to invoke this command
     *
     * @return all of the aliases that, besides the name can be
     *         used to invoke this command
     */
    Collection<String> getAliases();

    /**
     * Checks if the given player is allowed to execute this command
     *
     * @param player the player who wants to execute this command
     * @return true if the player is allowed to execute this command,
     *         false if he isn't allowed to do so
     */
    boolean hasPermission(Player player);

    /**
     * Execute the command for a specific player with the given arguments
     *
     * @param invoker   the player that invokes the command
     * @param arguments the arguments that came with the execution of the command
     * @return the message that will be send into the channel where the command was
     *         executed
     */
    String execute(Player invoker, List<String> arguments);
}
