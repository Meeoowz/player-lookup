package tablookup.java;

public class LookupConfig {
	public boolean includeHealth = true;
	public boolean includePing = false;
	public boolean clearTracksOnJoin = true;
	public int boxX = 10;
	public int boxY = 10;

	public LookupConfig copy() {
		LookupConfig copy = new LookupConfig();
		copy.includeHealth = includeHealth;
		copy.includePing = includePing;
		copy.clearTracksOnJoin = clearTracksOnJoin;
		copy.boxX = boxX;
		copy.boxY = boxY;
		return copy;
	}

	public boolean hasAnyEnabled() {
		return includeHealth || includePing;
	}
}
