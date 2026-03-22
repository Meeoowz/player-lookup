package tablookup.java;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class LookupCommands {
	private LookupCommands() {
	}

	public static int openConfig(FabricClientCommandSource source) {
		openConfigScreen(source.getClient());
		return 1;
	}

	public static int openPositionEditor(FabricClientCommandSource source) {
		openPositionEditorScreen(source.getClient());
		return 1;
	}

	public static int clearTracked(FabricClientCommandSource source) {
		int clearedCount = LookupOverlayRenderer.clearTrackedPlayers();
		source.sendFeedback(Text.literal("Cleared " + clearedCount + " tracked player(s)."));
		return 1;
	}

	public static CompletableFuture<Suggestions> suggestPlayers(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
		MinecraftClient client = context.getSource().getClient();
		ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

		if (networkHandler == null) {
			return builder.buildFuture();
		}

		List<String> playerNames = new ArrayList<>();

		for (PlayerListEntry entry : networkHandler.getPlayerList()) {
			playerNames.add(entry.getProfile().name());
		}

		return CommandSource.suggestMatching(playerNames, builder);
	}

	public static int sendLookup(FabricClientCommandSource source, String requestedName) {
		MinecraftClient client = source.getClient();
		ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

		if (networkHandler == null) {
			source.sendError(Text.literal("Not connected to a server."));
			return 0;
		}

		PlayerListEntry entry = LookupDataResolver.findPlayerEntry(networkHandler, requestedName);
		if (entry == null) {
			source.sendError(Text.literal("Player not found in tablist: " + requestedName));
			return 0;
		}

		LookupConfig config = LookupConfigManager.getConfig();
		if (!config.hasAnyEnabled()) {
			source.sendError(Text.literal("No fields selected. Use /lookup and enable at least one field."));
			return 0;
		}

		boolean nowTracking = LookupOverlayRenderer.togglePlayer(entry.getProfile().name());
		if (nowTracking) {
			source.sendFeedback(Text.literal("Now tracking " + entry.getProfile().name() + "."));
		} else {
			source.sendFeedback(Text.literal("Stopped tracking " + entry.getProfile().name() + "."));
		}
		return 1;
	}

	public static void openConfigScreen(MinecraftClient client) {
		Screen parent = client.currentScreen;
		client.execute(() -> client.setScreen(new LookupConfigScreen(parent)));
	}

	public static void openPositionEditorScreen(MinecraftClient client) {
		Screen parent = client.currentScreen;
		LookupConfig workingConfig = LookupConfigManager.copyConfig();
		client.execute(() -> client.setScreen(new LookupPositionEditorScreen(parent, workingConfig)));
	}
}
