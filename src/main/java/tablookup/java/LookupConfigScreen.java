package tablookup.java;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class LookupConfigScreen extends Screen {
	private final Screen parent;
	private final LookupConfig workingConfig;

	public LookupConfigScreen(Screen parent) {
		super(Text.literal("Player Lookup Config"));
		this.parent = parent;
		this.workingConfig = LookupConfigManager.copyConfig();
	}

	@Override
	protected void init() {
		int left = this.width / 2 - 100;
		int y = this.height / 4 + 32;

		addDrawableChild(
			ButtonWidget.builder(buildHealthText(), button -> {
				workingConfig.includeHealth = !workingConfig.includeHealth;
				button.setMessage(buildHealthText());
			}).dimensions(left, y, 200, 20).build()
		);

		y += 24;
		addDrawableChild(
			ButtonWidget.builder(buildPingText(), button -> {
				workingConfig.includePing = !workingConfig.includePing;
				button.setMessage(buildPingText());
			}).dimensions(left, y, 200, 20).build()
		);

		y += 24;
		addDrawableChild(
			ButtonWidget.builder(buildClearOnJoinText(), button -> {
				workingConfig.clearTracksOnJoin = !workingConfig.clearTracksOnJoin;
				button.setMessage(buildClearOnJoinText());
			}).dimensions(left, y, 200, 20).build()
		);

		y += 24;
		addDrawableChild(
			ButtonWidget.builder(Text.literal("Edit box position"), button -> {
				if (client != null) {
					client.setScreen(new LookupPositionEditorScreen(this, workingConfig));
				}
			}).dimensions(left, y, 200, 20).build()
		);

		y += 32;
		addDrawableChild(
			ButtonWidget.builder(Text.literal("Close"), button -> close())
				.dimensions(left, y, 200, 20)
				.build()
		);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		// Avoid duplicate per-frame blur calls on 1.21.11.
		renderInGameBackground(context);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Choose fields used by /s <player>"), this.width / 2, 42, 0xA0A0A0);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Box position: " + workingConfig.boxX + ", " + workingConfig.boxY), this.width / 2, 56, 0xA0A0A0);
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void close() {
		LookupConfigManager.updateConfig(workingConfig);
		LookupConfigManager.save();

		if (client != null) {
			client.setScreen(parent);
		}
	}

	private Text buildHealthText() {
		return Text.literal((workingConfig.includeHealth ? "[x] " : "[ ] ") + "Health");
	}

	private Text buildPingText() {
		return Text.literal((workingConfig.includePing ? "[x] " : "[ ] ") + "Ping");
	}

	private Text buildClearOnJoinText() {
		return Text.literal((workingConfig.clearTracksOnJoin ? "[x] " : "[ ] ") + "Clear tracks on join");
	}

}
