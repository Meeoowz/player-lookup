package tablookup.java;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LookupDataResolver {
	private static final Pattern TRAILING_HEALTH_PATTERN = Pattern.compile("(\\d+)\\s*[\\u2764\\u2665]?\\s*$");

	private LookupDataResolver() {
	}

	public static PlayerListEntry findPlayerEntry(ClientPlayNetworkHandler networkHandler, String requestedName) {
		for (PlayerListEntry entry : networkHandler.getPlayerList()) {
			if (entry.getProfile().name().equalsIgnoreCase(requestedName)) {
				return entry;
			}
		}

		return null;
	}

	public static Optional<Integer> getTablistHealth(MinecraftClient client, PlayerListEntry entry) {
		if (client.world == null) {
			return parseHealthFromDisplayName(entry);
		}

		Scoreboard scoreboard = client.world.getScoreboard();
		ScoreboardObjective listObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.LIST);

		if (listObjective == null) {
			return parseHealthFromDisplayName(entry);
		}

		ReadableScoreboardScore score = scoreboard.getScore(ScoreHolder.fromName(entry.getProfile().name()), listObjective);
		if (score == null) {
			return parseHealthFromDisplayName(entry);
		}

		return Optional.of(score.getScore());
	}

	private static Optional<Integer> parseHealthFromDisplayName(PlayerListEntry entry) {
		Text displayName = entry.getDisplayName();
		if (displayName == null) {
			return Optional.empty();
		}

		Matcher matcher = TRAILING_HEALTH_PATTERN.matcher(displayName.getString());
		if (!matcher.find()) {
			return Optional.empty();
		}

		try {
			return Optional.of(Integer.parseInt(matcher.group(1)));
		} catch (NumberFormatException exception) {
			return Optional.empty();
		}
	}
}
