package tablookup.java;

public class LookupConfig {
	public static final int DEFAULT_BOX_X = 10;
	public static final int DEFAULT_BOX_Y = 10;
	public static final int DEFAULT_BOX_WIDTH = LookupOverlayRenderer.DEFAULT_BOX_WIDTH;
	public static final int DEFAULT_BOX_HEIGHT = LookupOverlayRenderer.DEFAULT_BOX_HEIGHT;

	public boolean includeHealth = true;
	public boolean includePing = false;
	public boolean clearTracksOnJoin = true;
	public int boxX = DEFAULT_BOX_X;
	public int boxY = DEFAULT_BOX_Y;
	public int boxWidth = DEFAULT_BOX_WIDTH;
	public int boxHeight = DEFAULT_BOX_HEIGHT;

	public LookupConfig copy() {
		LookupConfig copy = new LookupConfig();
		copy.includeHealth = includeHealth;
		copy.includePing = includePing;
		copy.clearTracksOnJoin = clearTracksOnJoin;
		copy.boxX = boxX;
		copy.boxY = boxY;
		copy.boxWidth = boxWidth;
		copy.boxHeight = boxHeight;
		return copy;
	}

	public void resetBoxLayout() {
		boxX = DEFAULT_BOX_X;
		boxY = DEFAULT_BOX_Y;
		boxWidth = DEFAULT_BOX_WIDTH;
		boxHeight = DEFAULT_BOX_HEIGHT;
	}

	public boolean hasAnyEnabled() {
		return includeHealth || includePing;
	}
}
