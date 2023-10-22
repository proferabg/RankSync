package com.gmail.chickenpowerrr.ranksync.velocity;

import com.gmail.chickenpowerrr.ranksync.api.bot.Bot;
import com.gmail.chickenpowerrr.ranksync.api.link.Link;
import com.gmail.chickenpowerrr.ranksync.api.name.NameResource;
import com.gmail.chickenpowerrr.ranksync.api.rank.RankHelper;
import com.gmail.chickenpowerrr.ranksync.api.rank.RankResource;
import com.gmail.chickenpowerrr.ranksync.velocity.bungeeconfig.Configuration;
import com.gmail.chickenpowerrr.ranksync.velocity.bungeeconfig.ConfigurationProvider;
import com.gmail.chickenpowerrr.ranksync.velocity.bungeeconfig.YamlConfiguration;
import com.gmail.chickenpowerrr.ranksync.velocity.command.RankSyncCommand;
import com.gmail.chickenpowerrr.ranksync.velocity.command.RankSyncTabCompleter;
import com.gmail.chickenpowerrr.ranksync.velocity.command.UnSyncCommand;
import com.gmail.chickenpowerrr.ranksync.velocity.listener.PlayerDisconnectEventListener;
import com.gmail.chickenpowerrr.ranksync.velocity.listener.ServerConnectEventListener;
import com.gmail.chickenpowerrr.ranksync.server.link.LinkHelper;
import com.gmail.chickenpowerrr.ranksync.server.plugin.RankSyncServerPlugin;
import com.gmail.chickenpowerrr.ranksync.server.roleresource.LuckPermsRankResource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.Setter;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

/**
 * This class starts all of the parts needed to sync Ranks with the given platforms
 *
 * @author Chickenpowerrr
 * @since 1.3.0
 */

@Plugin(
        id = BuildConstants.ID,
        name = BuildConstants.NAME,
        version = BuildConstants.VERSION,
        description = BuildConstants.DESCRIPTION,
        authors = BuildConstants.DEVELOPERS,
        dependencies = {
                @Dependency(id = "luckperms")
        }
)
public final class RankSyncPlugin implements RankSyncServerPlugin {

  @Getter
  @Setter
  private LinkHelper linkHelper;

  @Getter
  private Map<String, Bot<?, ?>> bots = new HashMap<>();

  @Getter
  @Setter
  private RankHelper rankHelper;

  @Getter
  private RankSyncPlugin plugin;

  @Getter
  private final ProxyServer server;

  @Getter
  private final Logger logger;

  @Getter
  private final Path dataDirectory;

  private final Metrics.Factory metricsFactory;

  private Configuration configuration = null;

  @Inject
  public RankSyncPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
    this.server = server;
    this.logger = logger;
    this.dataDirectory = dataDirectory;
    this.metricsFactory = metricsFactory;
    plugin = this;
  }

  @Override
  public File getDataFolder(){
    return dataDirectory.toFile();
  }

  /**
   * Enables the important features in order to synchronize ranks
   */
  @Subscribe
  public void onProxyInitialization(ProxyInitializeEvent event) {
    try {
      enable();
      Metrics metrics = metricsFactory.make(this, 5069);
      metrics.addCustomChart(
          new SimplePie("used_storage", () -> getConfigString("database.type")));
      metrics.addCustomChart(
          new SimplePie("used_language", () -> getConfigString("language")));
    } catch (IllegalStateException e) {
      // Since Spigot now closes the Jar directly after a shutdown, but doesn't
      // immediately stop the main thread, we cannot load any classes
      if (!e.getMessage().equals("zip file closed")) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Shuts all the bots down on disabling.
   */
  @Subscribe
  public void onProxyShutdown(ProxyShutdownEvent event) {
    this.bots.values().forEach(Bot::shutdown);
  }

  /**
   * Returns a Bot by its name
   *
   * @param name the name of the Bot
   * @return the Bot that goes by the given name
   */
  @Override
  public Bot<?, ?> getBot(String name) {
    return this.bots.get(name.toLowerCase());
  }

  /**
   * Runs a timer given its period and delay
   *
   * @param runnable what should be done
   * @param delay when it will start
   * @param period the delay between every action
   */
  @Override
  public void runTaskTimer(Runnable runnable, long delay, long period) {
    server.getScheduler().buildTask(this, runnable).delay(Duration.ofMillis(delay * 50)).repeat(Duration.ofMillis(period * 50));
  }

  /**
   * Calls the onDisable and unregisters all open features
   *
   * @param reason the reason why RankSync should stop
   */
  @Override
  public void shutdown(String... reason) {
    getLogger().error("Disabling the RankSync plugin: ");
    for (String reasonString : reason) {
      getLogger().warn(reasonString);
    }

    //onDisable();

    server.getCommandManager().unregister("ranksync");
    server.getCommandManager().unregister("unsync");
    server.getScheduler().tasksByPlugin(this).forEach(ScheduledTask::cancel);
  }

  /**
   * Returns a string from the config
   *
   * @param key the location of the string
   * @return the requested string
   */
  @Override
  public String getConfigString(String key) {
    if (this.configuration.contains(key)) {
      return this.configuration.getString(key);
    } else if (key.equals("database.type")) {
      return "yaml";
    } else {
      return "";
    }
  }

  /**
   * Returns a long from the config
   *
   * @param key the location of the long
   * @return the requested long
   */
  @Override
  public long getConfigLong(String key) {
    return this.configuration.getLong(key);
  }

  /**
   * Returns a string list from the config
   *
   * @param key the location of the string list
   * @return the requested string list
   */
  @Override
  public List<String> getConfigStringList(String key) {
    return this.configuration.getStringList(key);
  }

  /**
   * Returns a int from the config
   *
   * @param key the location of the int
   * @return the requested int
   */
  @Override
  public int getConfigInt(String key) {
    return this.configuration.getInt(key);
  }

  /**
   * Returns a boolean from the config
   *
   * @param key the location of the boolean
   * @return the requested boolean
   */
  @Override
  public boolean getConfigBoolean(String key) {
    if (this.configuration.contains(key)) {
      return this.configuration.getBoolean(key);
    } else {
      switch (key) {
        case "update_non_synced":
        case "discord.permission-warnings":
          return true;
        case "sync_names":
        default:
          return false;
      }
    }
  }

  /**
   * Registers the commands
   */
  @Override
  public void registerCommands() {
    server.getCommandManager().register("unsync", new UnSyncCommand(this), "unlink", "unranksync", "unsynchronize", "unsyncrank", "unsynchronizerank", "revertrank");
    server.getCommandManager().register("ranksync", new RankSyncCommand(this), "link", "sync", "synchronize", "syncrank", "synchronizerank", "giverank");
  }

  /**
   * Registers the listeners
   */
  @Override
  public void registerListeners() {
    server.getEventManager().register(this, new RankSyncTabCompleter(this.linkHelper));
    server.getEventManager().register(this, new PlayerDisconnectEventListener(this.linkHelper));
    server.getEventManager().register(this, new ServerConnectEventListener(this.linkHelper));
  }

  /**
   * Logs an info message
   *
   * @param message the info
   */
  @Override
  public void logInfo(String message) {
    getLogger().info(message);
  }

  /**
   * Logs a warning message
   *
   * @param message the warning
   */
  @Override
  public void logWarning(String message) {
    getLogger().warn(message);
  }

  /**
   * Validates if it's possible to start with the current dependencies
   */
  @Override
  public RankResource validateDependencies() {
    if (server.getPluginManager().getPlugin("luckperms").isPresent()) {
      return new LuckPermsRankResource(this);
    } else {
      shutdown("You should use LuckPerms to work with RankSync");
      return null;
    }

    //TODO add Vault
  }

  /**
   * Creates a new name resource
   */
  @Override
  public NameResource createNameResource() {
    return new com.gmail.chickenpowerrr.ranksync.velocity.name.NameResource(server);
  }

  /**
   * Executes a given command on the server as the console
   *
   * @param command the command which needs to be executed
   */
  @Override
  public void executeCommand(String command) {
    ConsoleCommandSource consoleCommandSource = server.getConsoleCommandSource();
    server.getCommandManager().executeAsync(consoleCommandSource, command);
  }

  /**
   * Returns all of the links given in the config.yml
   */
  @Override
  public List<Link> getSyncedRanks() {
    List<Link> syncedRanks = new ArrayList<>();

    getBots()
        .forEach((botName, bot) -> this.configuration.getSection("ranks.discord").getKeys().stream()
            .map(section -> this.configuration.getSection("ranks.discord." + section))
            .forEach(section -> {
              String minecraftRank = section.getString("minecraft");
              List<String> platformRanks = section.getStringList(botName);
              if (platformRanks.isEmpty()) {
                platformRanks.add(section.getString(botName));
              }

              syncedRanks.add(new com.gmail.chickenpowerrr.ranksync.server.link.Link(
                  Collections.singletonList(minecraftRank), platformRanks,
                  Optional.ofNullable(section.getString("name-format"))
                      .orElse(this.configuration.getString("discord.name-format")), bot));
            }));

    return syncedRanks;
  }

  /**
   * Sets up the config
   */
  @SuppressWarnings("unchecked")
  @Override
  public void setupConfig() {
    if (!getDataFolder().exists()) {
      getDataFolder().mkdir();
    }

    File file = new File(getDataFolder(), "config.yml");

    if (!file.exists()) {
      try (InputStream in = getResourceAsStream("config.yml")) {
        Files.copy(in, file.toPath());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    try {
      this.configuration = ConfigurationProvider
          .getProvider(YamlConfiguration.class)
          .load(new File(getDataFolder(), "config.yml"),
              ConfigurationProvider.getProvider(YamlConfiguration.class)
                  .load(getResourceAsStream("config.yml")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      Field defaultField = this.configuration.getClass().getDeclaredField("defaults");
      defaultField.setAccessible(true);
      Configuration defaults = (Configuration) defaultField.get(this.configuration);
      Field selfField = this.configuration.getClass().getDeclaredField("self");
      selfField.setAccessible(true);
      Map<String, Object> self = (Map<String, Object>) selfField.get(defaults);
      self.remove("ranks");
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    try {
      ConfigurationProvider.getProvider(YamlConfiguration.class)
          .save(this.configuration, new File(getDataFolder(), "config.yml"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns if the bot is still running
   */
  @Override
  public boolean isRunning() {
    return server.getPluginManager().getPlugin("ranksync").isPresent();
  }

  private InputStream getResourceAsStream(String name){
    return this.getClass().getClassLoader().getResourceAsStream(name);
  }
}
