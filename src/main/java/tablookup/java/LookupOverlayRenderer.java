package tablookup.java;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class LookupOverlayRenderer {
	public static final int DEFAULT_BOX_WIDTH = 170;
	public static final int DEFAULT_BOX_HEIGHT = 34;
	public static final int MIN_BOX_WIDTH = 120;
	public static final int MIN_BOX_HEIGHT = 28;
	public static final int MAX_BOX_WIDTH = 420;
	public static final int MAX_BOX_HEIGHT = 140;
	public static final int BOX_SPACING = 4;

	private static final String HEART_SYMBOL = "\u2764";
	private static final List<String> TRACKED_PLAYERS = new ArrayList<>();

	private LookupOverlayRenderer() {
	}

	public static boolean togglePlayer(String playerName) {
		int existingIndex = findTrackedIndex(playerName);

		if (existingIndex >= 0) {
			TRACKED_PLAYERS.remove(existingIndex);
			return false;
		}

		TRACKED_PLAYERS.add(playerName);
		return true;
	}

	public static int clearTrackedPlayers() {
		int clearedCount = TRACKED_PLAYERS.size();
		TRACKED_PLAYERS.clear();
		return clearedCount;
	}

	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		if (TRACKED_PLAYERS.isEmpty()) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		LookupConfig config = LookupConfigManager.getConfig();
		int boxWidth = clamp(config.boxWidth, MIN_BOX_WIDTH, MAX_BOX_WIDTH);
		int boxHeight = clamp(config.boxHeight, MIN_BOX_HEIGHT, MAX_BOX_HEIGHT);
		int originX = clamp(config.boxX, 0, Math.max(0, context.getScaledWindowWidth() - boxWidth));
		int originY = clamp(config.boxY, 0, Math.max(0, context.getScaledWindowHeight() - boxHeight));

		for (int i = 0; i < TRACKED_PLAYERS.size(); i++) {
			int boxY = originY + i * (boxHeight + BOX_SPACING);
			if (boxY > context.getScaledWindowHeight()) {
				break;
			}

			String playerName = TRACKED_PLAYERS.get(i);
			renderBox(context, client, config, originX, boxY, boxWidth, boxHeight, playerName);
		}
	}

	private static void renderBox(DrawContext context, MinecraftClient client, LookupConfig config, int x, int y, int boxWidth, int boxHeight, String playerName) {
		int right = x + boxWidth;
		int bottom = y + boxHeight;
		context.fill(x, y, right, bottom, 0xB0000000);
		context.drawStrokedRectangle(x, y, boxWidth, boxHeight, 0x90FFFFFF);
		context.fill(x, y, right, y + 12, 0x40000000);

		context.drawTextWithShadow(client.textRenderer, playerName, x + 6, y + 2, 0xFFFFFFFF);
		context.drawTextWithShadow(client.textRenderer, buildValuesText(client, config, playerName), x + 6, y + 18, 0xFFE6E6E6);
	}

	private static String buildValuesText(MinecraftClient client, LookupConfig config, String playerName) {
		ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
		if (networkHandler == null) {
			return "Not connected";
		}

		PlayerListEntry entry = LookupDataResolver.findPlayerEntry(networkHandler, playerName);
		if (entry == null) {
			return "Player not in tablist";
		}

		List<String> values = new ArrayList<>();
		if (config.includeHealth) {
			Optional<Integer> health = LookupDataResolver.getTablistHealth(client, entry);
			values.add(health.map(value -> "health " + value + " " + HEART_SYMBOL).orElse("health unavailable"));
		}

		if (config.includePing) {
			values.add("ping " + entry.getLatency() + "ms");
		}

		if (values.isEmpty()) {
			return "No variables selected";
		}

		return String.join(" | ", values);
	}

	private static int findTrackedIndex(String playerName) {
		for (int i = 0; i < TRACKED_PLAYERS.size(); i++) {
			if (TRACKED_PLAYERS.get(i).equalsIgnoreCase(playerName)) {
				return i;
			}
		}

		return -1;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
