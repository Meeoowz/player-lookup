package tablookup.java;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class LookupPositionEditorScreen extends Screen {
	private static final int CLOSE_SIZE = 10;
	private static final int RESIZE_HANDLE_SIZE = 10;
	private static final int RESIZE_HANDLE_MARGIN = 1;
	private static final int RESIZE_HANDLE_TOP_OFFSET = CLOSE_SIZE + 2;
	private static final int RESIZE_HANDLE_FILL = 0x804DB8FF;
	private static final int RESIZE_HANDLE_BORDER = 0xCCB8E5FF;

	private final Screen parent;
	private final LookupConfig workingConfig;
	private boolean draggingPosition;
	private boolean draggingResize;
	private int dragOffsetX;
	private int dragOffsetY;
	private int resizeStartMouseX;
	private int resizeStartMouseY;
	private int resizeStartWidth;
	private int resizeStartHeight;

	public LookupPositionEditorScreen(Screen parent, LookupConfig workingConfig) {
		super(Text.literal("Move/Resize Lookup Box"));
		this.parent = parent;
		this.workingConfig = workingConfig;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderInGameBackground(context);
		clampToScreen(context.getScaledWindowWidth(), context.getScaledWindowHeight());

		int x = workingConfig.boxX;
		int y = workingConfig.boxY;
		int right = x + workingConfig.boxWidth;
		int bottom = y + workingConfig.boxHeight;

		context.fill(x, y, right, bottom, 0xB0303030);
		context.drawStrokedRectangle(x, y, workingConfig.boxWidth, workingConfig.boxHeight, 0x90FFFFFF);
		context.fill(x, y, right, y + 12, 0x60000000);

		int closeX = getCloseX();
		int closeY = getCloseY();
		context.fill(closeX, closeY, closeX + CLOSE_SIZE, closeY + CLOSE_SIZE, 0xAA771111);
		context.drawStrokedRectangle(closeX, closeY, CLOSE_SIZE, CLOSE_SIZE, 0xCCFFFFFF);

		int resizeX = getResizeHandleX();
		int resizeY = getResizeHandleY();
		context.fill(resizeX, resizeY, resizeX + RESIZE_HANDLE_SIZE, resizeY + RESIZE_HANDLE_SIZE, RESIZE_HANDLE_FILL);
		context.drawStrokedRectangle(resizeX, resizeY, RESIZE_HANDLE_SIZE, RESIZE_HANDLE_SIZE, RESIZE_HANDLE_BORDER);

		context.drawTextWithShadow(this.textRenderer, "Preview Player", x + 6, y + 2, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, "20 \u2764 | 55ms", x + 6, y + 18, 0xFFE6E6E6);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Drag box to move. Drag blue corner to resize."), this.width / 2, 36, 0xFFA0A0A0);
		context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Press ESC or click X to close."), this.width / 2, 48, 0xFFA0A0A0);
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

		if (isInsideResizeHandle(click.x(), click.y())) {
			draggingResize = true;
			resizeStartMouseX = (int) click.x();
			resizeStartMouseY = (int) click.y();
			resizeStartWidth = workingConfig.boxWidth;
			resizeStartHeight = workingConfig.boxHeight;
			return true;
		}

		if (isInsidePreviewBox(click.x(), click.y())) {
			draggingPosition = true;
			dragOffsetX = (int) click.x() - workingConfig.boxX;
			dragOffsetY = (int) click.y() - workingConfig.boxY;
			return true;
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseReleased(Click click) {
		draggingPosition = false;
		draggingResize = false;
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseDragged(Click click, double deltaX, double deltaY) {
		if (click.button() != 0) {
			return super.mouseDragged(click, deltaX, deltaY);
		}

		if (draggingResize) {
			int minWidth = Math.min(LookupOverlayRenderer.MIN_BOX_WIDTH, Math.max(1, this.width));
			int minHeight = Math.min(LookupOverlayRenderer.MIN_BOX_HEIGHT, Math.max(1, this.height));
			int maxWidth = Math.max(minWidth, Math.min(this.width - workingConfig.boxX, LookupOverlayRenderer.MAX_BOX_WIDTH));
			int maxHeight = Math.max(minHeight, Math.min(this.height - workingConfig.boxY, LookupOverlayRenderer.MAX_BOX_HEIGHT));

			int nextWidth = resizeStartWidth + ((int) click.x() - resizeStartMouseX);
			int nextHeight = resizeStartHeight + ((int) click.y() - resizeStartMouseY);
			workingConfig.boxWidth = clamp(nextWidth, minWidth, maxWidth);
			workingConfig.boxHeight = clamp(nextHeight, minHeight, maxHeight);
			return true;
		}

		if (draggingPosition) {
			int maxX = Math.max(0, this.width - workingConfig.boxWidth);
			int maxY = Math.max(0, this.height - workingConfig.boxHeight);
			workingConfig.boxX = clamp((int) click.x() - dragOffsetX, 0, maxX);
			workingConfig.boxY = clamp((int) click.y() - dragOffsetY, 0, maxY);
			return true;
		}

		return super.mouseDragged(click, deltaX, deltaY);
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
			&& mouseX <= workingConfig.boxX + workingConfig.boxWidth
			&& mouseY >= workingConfig.boxY
			&& mouseY <= workingConfig.boxY + workingConfig.boxHeight;
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
		return workingConfig.boxX + workingConfig.boxWidth - CLOSE_SIZE - 3;
	}

	private int getCloseY() {
		return workingConfig.boxY + 1;
	}

	private boolean isInsideResizeHandle(double mouseX, double mouseY) {
		int resizeX = getResizeHandleX();
		int resizeY = getResizeHandleY();
		return mouseX >= resizeX
			&& mouseX <= resizeX + RESIZE_HANDLE_SIZE
			&& mouseY >= resizeY
			&& mouseY <= resizeY + RESIZE_HANDLE_SIZE;
	}

	private int getResizeHandleX() {
		return workingConfig.boxX + workingConfig.boxWidth - RESIZE_HANDLE_SIZE - RESIZE_HANDLE_MARGIN;
	}

	private int getResizeHandleY() {
		return workingConfig.boxY + RESIZE_HANDLE_TOP_OFFSET;
	}

	private void clampToScreen(int screenWidth, int screenHeight) {
		int minWidth = Math.min(LookupOverlayRenderer.MIN_BOX_WIDTH, Math.max(1, screenWidth));
		int minHeight = Math.min(LookupOverlayRenderer.MIN_BOX_HEIGHT, Math.max(1, screenHeight));
		int maxWidth = Math.max(minWidth, Math.min(screenWidth, LookupOverlayRenderer.MAX_BOX_WIDTH));
		int maxHeight = Math.max(minHeight, Math.min(screenHeight, LookupOverlayRenderer.MAX_BOX_HEIGHT));

		workingConfig.boxWidth = clamp(workingConfig.boxWidth, minWidth, maxWidth);
		workingConfig.boxHeight = clamp(workingConfig.boxHeight, minHeight, maxHeight);
		workingConfig.boxX = clamp(workingConfig.boxX, 0, Math.max(0, screenWidth - workingConfig.boxWidth));
		workingConfig.boxY = clamp(workingConfig.boxY, 0, Math.max(0, screenHeight - workingConfig.boxHeight));
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
