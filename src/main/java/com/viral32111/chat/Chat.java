package com.viral32111.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

// TO-DO: Remember nicknames across joins/leaves and server reboots! (yaml/json???)
// TO-DO: Nickname italics appear in the ': message' bit too!
// TO-DO: Integration with LuckPerms to fetch player prefix! (just colors tho)
// TO-DO: Command for moderators/console to set and/or reset player nicknames!
// TO-DO: Config file entries for all notes above and all code below.

@SuppressWarnings( "unused" )
public class Chat extends JavaPlugin implements Listener {
	private final Pattern usernamePattern = Pattern.compile( "[a-zA-Z0-9_]{3,16}" );

	@Override public void onEnable() {
		//saveDefaultConfig();
		getServer().getPluginManager().registerEvents( this, this );
	}

	@Override public void onDisable() {

	}

	@Override public boolean onCommand( @NotNull CommandSender sender, @NotNull Command command, @NotNull String name, String[] arguments ) {
		if ( ! ( sender instanceof Player player ) ) {
			getLogger().info( "This command is not usable by the console." );
			return true;
		}

		if ( command.getName().equalsIgnoreCase( "nick" ) && player.hasPermission( "chat.nickname" ) ) {
			if ( arguments.length > 0 ) {
				if ( ! usernamePattern.matcher( arguments[ 0 ] ).matches() ) {
					player.sendMessage( Component.text( "That is not a valid nickname.", Style.style( TextDecoration.ITALIC, NamedTextColor.GRAY ) ) );
					getLogger().info( "%s attempted to set their nickname to '%s', but it is invalid!".formatted( player.getName(), arguments[ 0 ] ) );
				} else {
					Component newDisplayname = Component.text( arguments[ 0 ], Style.style( TextDecoration.ITALIC ) );

					if ( player.displayName().equals( newDisplayname ) ) {
						player.sendMessage( Component.text( "Your nickname is already set to that.", Style.style( TextDecoration.ITALIC, NamedTextColor.GRAY ) ) );
						getLogger().info( "%s attempted to set their nickname to '%s', but it is the same as their current one!".formatted( player.getName(), arguments[ 0 ] ) );
					} else {
						player.displayName( newDisplayname );
						player.playerListName( newDisplayname );
						player.sendMessage( Component.text( "Your nickname has been changed.", Style.style( TextDecoration.ITALIC, NamedTextColor.GRAY ) ) );
						getLogger().info( "%s set their nickname to %s.".formatted( player.getName(), arguments[ 0 ] ) );
					}
				}
			} else {
				if ( player.displayName().equals( Component.text( player.getName() ) ) ) {
					player.sendMessage( Component.text( "You have no nickname to clear.", Style.style( TextDecoration.ITALIC, NamedTextColor.GRAY ) ) );
					getLogger().info( "%s attempted to clear their nickname, but they don't have one!".formatted( player.getName() ) );
				} else {
					player.displayName( null );
					player.playerListName( null );
					player.sendMessage( Component.text( "Your nickname has been cleared.", Style.style( TextDecoration.ITALIC, NamedTextColor.GRAY ) ) );
					getLogger().info( "%s cleared their nickname.".formatted( player.getName() ) );
				}
			}

			return true;
		}

		return false;
	}


	@EventHandler public void onAsyncChat( AsyncChatEvent event ) {
		event.renderer( ( player, nickname, message, audience ) -> {
			return Component.text().append( nickname ).append( Component.text( ": " ) ).append( message ).build();
		} );
	}
}
