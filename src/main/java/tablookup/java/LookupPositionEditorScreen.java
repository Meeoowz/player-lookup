package tablookup.java;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class LookupPositionEditorScreen extends Screen {
	private static final int CLOSE_SIZE = 10;

	private final Screen parent;
	private final LookupConfig workingConfig;
	private boolean dragging;
	private int dragOffsetX;
	private int dragOffsetY;

	public LookupPositionEditorScreen(Screen parent, LookupConfig workingConfig) {
		super(Text.literal("Move Lookup Box"));
		this.parent = parent;
		this.workingConfig = workingConfig;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderInGameBackground(context);
		clampToScreen(context.getScaledWindowWidth(), context.getScaledWindowHeight());

		int x = workingConfig.boxX;
		int y = workingConfig.boxY;
		int right = x + LookupOverlayRenderer.BOX_WIDTH;
		int bottom = y + LookupOverlayRenderer.BOX_HEIGHT;

		context.fill(x, y, right, bottom, 0xB0303030);
		context.drawStrokedRectangle(x, y, LookupOverlayRenderer.BOX_WIDTH, LookupOverlayRenderer.BOX_HEIGHT, 0x90FFFFFF);
		context.fill(x, y, right, y + 12, 0x60000000);

		int closeX = getCloseX();
		int closeY = getCloseY();
		context.fill(closeX, closeY, closeX + CLOSE_SIZE, closeY + CLOSE_SIZE, 0xAA771111);
		context.drawStrokedRectangle(closeX, closeY, CLOSE_SIZE, CLOSE_SIZE, 0xCCFFFFFF);

		context.drawTextWithShadow(this.textRenderer, "Preview Player", x + 6, y + 2, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, "20 \u2764 | 55ms", x + 6, y + 18, 0xFFE6E6E6);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Drag the box. Press ESC or click X to close."), this.width / 2, 36, 0xFFA0A0A0);
		context.drawTextWithShadow(this.textRenderer, "X", closeX + 3, closeY + 1, 0xFFFFFFFF);

		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (click.button() != 0) {
			return super.mouseClicked(click, doubled);
		}

		if (isInsideCloseBox(click.x(), click.y())) {
			close();
			return true;
		}

		if (isInsidePreviewBox(click.x(), click.y())) {
			dragging = true;
			dragOffsetX = (int) click.x() - workingConfig.boxX;
			dragOffsetY = (int) click.y() - workingConfig.boxY;
			return true;
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseReleased(Click click) {
		dragging = false;
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseDragged(Click click, double deltaX, double deltaY) {
		if (!dragging || click.button() != 0) {
			return super.mouseDragged(click, deltaX, deltaY);
		}

		int maxX = Math.max(0, this.width - LookupOverlayRenderer.BOX_WIDTH);
		int maxY = Math.max(0, this.height - LookupOverlayRenderer.BOX_HEIGHT);
		workingConfig.boxX = clamp((int) click.x() - dragOffsetX, 0, maxX);
		workingConfig.boxY = clamp((int) click.y() - dragOffsetY, 0, maxY);
		return true;
	}

	@Override
	public void close() {
		LookupConfigManager.updateConfig(workingConfig);
		LookupConfigManager.save();

		if (client != null) {
			client.setScreen(parent);
		}
	}

	private boolean isInsidePreviewBox(double mouseX, double mouseY) {
		return mouseX >= workingConfig.boxX
			&& mouseX <= workingConfig.boxX + LookupOverlayRenderer.BOX_WIDTH
			&& mouseY >= workingConfig.boxY
			&& mouseY <= workingConfig.boxY + LookupOverlayRenderer.BOX_HEIGHT;
	}

	private boolean isInsideCloseBox(double mouseX, double mouseY) {
		int closeX = getCloseX();
		int closeY = getCloseY();
		return mouseX >= closeX
			&& mouseX <= closeX + CLOSE_SIZE
			&& mouseY >= closeY
			&& mouseY <= closeY + CLOSE_SIZE;
	}

	private int getCloseX() {
		return workingConfig.boxX + LookupOverlayRenderer.BOX_WIDTH - CLOSE_SIZE - 3;
	}

	private int getCloseY() {
		return workingConfig.boxY + 1;
	}

	private void clampToScreen(int screenWidth, int screenHeight) {
		workingConfig.boxX = clamp(workingConfig.boxX, 0, Math.max(0, screenWidth - LookupOverlayRenderer.BOX_WIDTH));
		workingConfig.boxY = clamp(workingConfig.boxY, 0, Math.max(0, screenHeight - LookupOverlayRenderer.BOX_HEIGHT));
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
