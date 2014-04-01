package ca.utoronto.cs.docuburst.prefuse;

import ca.utoronto.cs.prefuseextensions.lib.Colors;
import prefuse.util.ColorLib;

public final class ColorScheme {
	static Colors c = new Colors();
	public static int highlightColor = c.getColor("ORANGE1");
	public static int hoverColor = c.getColor("SEARCHPINK");
	public static int resultsColor = c.getColor("GREEN1");
	public static int searchColor = c.getColor("MEGOLD2"); // GOLD2
	public static int zeroAlpha = 50;
	public static int edgeColor = ColorLib.setAlpha(c.getColor("GRAY3"), zeroAlpha / 2);
	public static int pathToRootColor = c.getColor("ARCTREESBLUE2");
	public static int[] sensesPalette = ColorLib.getCategoryPalette(25, 0.5f, 1.0f, 1.0f, 1.0f);
	public static int wordRootColor = ColorLib.setAlpha(c.getColor("ARCTREESBLUE2"), 150);
	public static int wordSearchColor = ColorLib.setAlpha(c.getColor("GOLD3"), 120);
	public static int zeroOccurrenceSenseColor = c.getColor("GRAY4");
	public static int negativeColor = c.getColor("PURPLE1");
}
	
