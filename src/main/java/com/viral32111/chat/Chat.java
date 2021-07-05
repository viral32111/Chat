package com.viral32111.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

// Chat format would be something like '%groupcolor%%nickname%: %message%'

// TO-DO: Integration with LuckPerms to fetch player prefix! (just colors tho)
// TO-DO: Command for moderators/console to set and/or reset player nicknames!
// TO-DO: Config file entries for all notes above and all code below.
// TO-DO: More information in the chat player name hover event (time joined, their nickname, etc)
// TO-DO: Use nickname in advancement/challenge achieved message.
// TO-DO: Use nickname in death/kill messages.

@SuppressWarnings( "unused" )
public class Chat extends JavaPlugin implements Listener {
	private final Pattern usernamePattern = Pattern.compile( "[a-zA-Z0-9_]{3,16}" );
	private final String playerDataDirectory = "display-names";

	// Gets a player's display name persistent data path
	private Path getPlayerDataPath( Player player ) {
		return Paths.get( getDataFolder().getAbsolutePath(), "%s/%s.json".formatted( playerDataDirectory, player.getUniqueId() ) );
	}

	// Sets a player's display name
	private void setPlayerDisplayName( Player player, Component displayName, boolean shouldSave ) {
		// TO-DO: Get color from LuckPerms user prefix!

		player.displayName( displayName );
		player.playerListName( displayName );

		if ( shouldSave ) {
			Path playerDataPath = getPlayerDataPath( player );
			String displayNameJSON = GsonComponentSerializer.gson().serialize( displayName );

			try {
				Files.writeString( playerDataPath, displayNameJSON );
			} catch ( Exception exception ) {
				exception.printStackTrace();
			}
		}
	}

	// Gets a player's display name
	private Component getPlayerDisplayName( Player player ) {
		Path playerDataPath = getPlayerDataPath( player );

		if ( Files.exists( playerDataPath ) ) {
			try {
				String displayNameJSON = Files.readString( playerDataPath );
				return GsonComponentSerializer.gson().deserialize( displayNameJSON );
			} catch ( Exception exception ) {
				exception.printStackTrace();
			}
		}

		return null;
	}

	// Clears the player's display name
	private void clearPlayerDisplayName( Player player ) {
		// TO-DO: Get color from LuckPerms user prefix!

		player.displayName( null );
		player.playerListName( null );

		try {
			Files.delete( getPlayerDataPath( player ) );
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}

	@Override public void onEnable() {
		saveDefaultConfig();

		try {
			Path playerDataDirectoryPath = Paths.get( getDataFolder().getAbsolutePath(), playerDataDirectory );
			if ( Files.notExists( playerDataDirectoryPath ) ) {
				Files.createDirectory( playerDataDirectoryPath );
				getLogger().info( "Created directory for storing player display names." );
			}
		} catch ( Exception exception ) {
			exception.printStackTrace();
		}

		getServer().getPluginManager().registerEvents( this, this );
	}

	@Override public void onDisable() {

	}

	@Override public boolean onCommand( @NotNull CommandSender sender, @NotNull Command command, @NotNull String name, String[] arguments ) {
		if ( ! ( sender instanceof Player player ) ) {
			getLogger().info( "This command is not useable by the console!" );
			return true;
		}

		if ( command.getName().equalsIgnoreCase( "nick" ) && player.hasPermission( "chat.nick" ) ) {
			String responseMessage;

			if ( arguments.length > 0 ) {
				String attemptedDisplayName = arguments[ 0 ];

				if ( usernamePattern.matcher( attemptedDisplayName ).matches() ) {
					Component newDisplayName = Component.text( attemptedDisplayName, Style.style( TextDecoration.ITALIC ) );

					if ( player.getName().equals( attemptedDisplayName ) ) {
						responseMessage = "That is the same as your account name.";
						getLogger().info( "%s attempted to set their nickname to '%s' but failed because it is the same as their account name!".formatted( player.getName(), attemptedDisplayName ) );
					} else if ( player.displayName().equals( newDisplayName ) ) {
						responseMessage = "That is the same as your current nickname.";
						getLogger().info( "%s attempted to set their nickname to '%s' but failed because it is the same as their current one!".formatted( player.getName(), attemptedDisplayName ) );
					} else {
						setPlayerDisplayName( player, newDisplayName, true );

						responseMessage = "Your nickname has been changed!";
						getLogger().info( "%s set their nickname to '%s'.".formatted( player.getName(), attemptedDisplayName ) );
					}
				} else {
					responseMessage = "That is not a valid nickname.";
					getLogger().info( "%s attempted to set their nickname to '%s' but failed because it is not valid!".formatted( player.getName(), attemptedDisplayName ) );
				}
			} else {
				if ( getPlayerDisplayName( player ) != null ) {
					String plainDisplayName = PlainTextComponentSerializer.plainText().serialize( player.displayName() );

					clearPlayerDisplayName( player );

					responseMessage = "Your nickname has been cleared!";
					getLogger().info( "%s cleared their nickname '%s'.".formatted( player.getName(), plainDisplayName ) );
				} else {
					responseMessage = "You have no nickname to clear!";
					getLogger().info( "%s attempted to clear their nickname but failed because they do not have one!".formatted( player.getName() ) );
				}
			}

			player.sendMessage( Component.text( responseMessage, Style.style( TextDecoration.ITALIC, NamedTextColor.GRAY ) ) );

			return true;
		} else if ( command.getName().equalsIgnoreCase( "setnick" ) && player.hasPermission( "chat.setnick" ) ) {
			player.sendMessage( Component.text( "Sowwy, I don't work yet! :c", Style.style( TextDecoration.ITALIC, NamedTextColor.GRAY ) ) );
			return true;
		}

		return false;
	}

	@EventHandler public void onPlayerJoin( PlayerJoinEvent event ) {
		Component savedDisplayName = getPlayerDisplayName( event.getPlayer() );

		if ( savedDisplayName != null ) {
			setPlayerDisplayName( event.getPlayer(), savedDisplayName, false );

			String plainDisplayName = PlainTextComponentSerializer.plainText().serialize( event.getPlayer().displayName() );
			getLogger().info( "Loaded nickname '%s' for %s.".formatted( plainDisplayName, event.getPlayer().getName() ) );
		}

		event.joinMessage( Component.text().append( event.getPlayer().displayName().hoverEvent( HoverEvent.showText( Component.text( event.getPlayer().getName() ).append( Component.newline() ).append( Component.text( event.getPlayer().getUniqueId().toString(), NamedTextColor.DARK_GRAY ) ) ) ) ).append( Component.text( " joined!", Style.style( NamedTextColor.YELLOW ) ) ).build() );
	}

	@EventHandler public void onPlayerQuit( PlayerQuitEvent event ) {
		event.quitMessage( Component.text().append( event.getPlayer().displayName().hoverEvent( HoverEvent.showText( Component.text( event.getPlayer().getName() ).append( Component.newline() ).append( Component.text( event.getPlayer().getUniqueId().toString(), NamedTextColor.DARK_GRAY ) ) ) ) ).append( Component.text( " left.", Style.style( NamedTextColor.YELLOW ) ) ).build() );
	}

	@EventHandler public void onAsyncChat( AsyncChatEvent event ) {
		event.renderer( ( player, displayName, message, audience ) -> Component.text().append( player.displayName().hoverEvent( HoverEvent.showText( Component.text( player.getName() ).append( Component.newline() ).append( Component.text( player.getUniqueId().toString(), NamedTextColor.DARK_GRAY ) ) ) ) ).append( Component.text( ": ", Style.empty() ) ).append( message ).build() );
	}
}
