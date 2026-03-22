package tablookup.java;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class PlayerLookup implements ClientModInitializer {
	public static final String MOD_ID = "player-lookup";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final String KEY_OPEN_CONFIG = "key.player_lookup.open_config";
	private static final KeyBinding.Category KEYBIND_CATEGORY = KeyBinding.Category.create(Identifier.of("player_lookup", "general"));
	private static KeyBinding openConfigKeybind;

	@Override
	public void onInitializeClient() {
		LookupConfigManager.load();
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of(MOD_ID, "lookup_boxes"), LookupOverlayRenderer::render);
		registerKeybind();

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (LookupConfigManager.getConfig().clearTracksOnJoin) {
				LookupOverlayRenderer.clearTrackedPlayers();
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
				ClientCommandManager.literal("lookup")
					.then(ClientCommandManager.literal("pos")
						.executes(context -> LookupCommands.openPositionEditor(context.getSource())))
					.then(ClientCommandManager.literal("clear")
						.executes(context -> LookupCommands.clearTracked(context.getSource())))
					.executes(context -> LookupCommands.openConfig(context.getSource()))
			);

			dispatcher.register(
				ClientCommandManager.literal("s")
					.then(ClientCommandManager.argument("player", word())
						.suggests(LookupCommands::suggestPlayers)
						.executes(context -> LookupCommands.sendLookup(context.getSource(), getString(context, "player"))))
			);
		});

		LOGGER.info("Player Lookup initialized.");
	}

	private static void registerKeybind() {
		openConfigKeybind = KeyBindingHelper.registerKeyBinding(
			new KeyBinding(KEY_OPEN_CONFIG, InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L, KEYBIND_CATEGORY)
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfigKeybind.wasPressed()) {
				if (client.currentScreen == null && isShiftHeld(client)) {
					LookupCommands.openConfigScreen(client);
				}
			}
		});
	}

	private static boolean isShiftHeld(MinecraftClient client) {
		var window = client.getWindow();
		return InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)
			|| InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
	}
}
