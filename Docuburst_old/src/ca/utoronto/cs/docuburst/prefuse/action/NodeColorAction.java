package ca.utoronto.cs.docuburst.prefuse.action;

import java.awt.Color;

import ca.utoronto.cs.docuburst.prefuse.ColorScheme;
import ca.utoronto.cs.docuburst.prefuse.DocuBurstActionList;
import prefuse.action.assignment.ColorAction;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

/**
 * Set node fill colors
 */
public class NodeColorAction extends ColorAction {

    private final DocuBurstActionList docuBurstActionList;

    public NodeColorAction(DocuBurstActionList docuBurstActionList, String group, boolean trackSenseIndex) {
		super(group, VisualItem.FILLCOLOR, ColorLib.rgba(100, 100, 100, 0));
        this.docuBurstActionList = docuBurstActionList;
		add("type = 1", new WordCountColorAction(group, VisualItem.FILLCOLOR));
	}

	class SenseColorAction extends ColorAction {

		public SenseColorAction(String group, String field) {
			super(group, field);
		}

		public int getColor(VisualItem item) {
			if (item.getInt("searchDepth") == 1)
				return ColorScheme.searchColor;

			if (item.canGetInt("senseIndex"))
				return ColorScheme.sensesPalette[item.getInt("senseIndex") % ColorScheme.sensesPalette.length];
			else
				return ColorScheme.resultsColor;
		}
	}

	class WordCountColorAction extends ColorAction {
		SenseColorAction sca;

		public WordCountColorAction(String group, String field) {
			super(group, field);
			sca = new SenseColorAction(group, VisualItem.FILLCOLOR);
		}

		public int getColor(VisualItem item) {
			// test 
//			if (item.getBoolean("cut")){
//				return new Color(222,13,107).getRGB();
//			}
			
			int color = sca.getColor(item);
			
			// lemmas and senses in the pathToRoot
			if (item.isInGroup("pathToRoot") && ((item.getInt("type") == 1) || (item.getInt("type") == 4)))
				color = ColorScheme.pathToRootColor;

			// count off -- just return color
			if (NodeColorAction.this.docuBurstActionList.countType.equals(DocuBurstActionList.NOCOUNT))
				return color;

			float total = (float) (item.getFloat(DocuBurstActionList.CACHECOUNT + NodeColorAction.this.docuBurstActionList.countType));
			float maxTotal = NodeColorAction.this.docuBurstActionList.getMaxTotal(NodeColorAction.this.docuBurstActionList.countType);

			if (total < 0) color = ColorScheme.negativeColor;
			
			if (total == 0) {
				if (color != ColorScheme.pathToRootColor)
					// zero senses get grey, zero lemmas and words get clear
					return ColorLib.setAlpha(ColorScheme.zeroOccurrenceSenseColor, ColorScheme.zeroAlpha);
				else
					// modulate path to root color 
					return ColorLib.setAlpha(color, ColorScheme.zeroAlpha);
			}

			// initial alpha for zero and one count
			int alpha = ColorScheme.zeroAlpha;

			alpha += (int) (((float) total / (float) maxTotal) * (float) (255 - alpha));

			if (total < 0)
				System.err.println("total: " + total + " node: " + item.getString("label") + " alpha: " + alpha);

			// this should not occur except for rounding errors
			if (alpha > 255)
				alpha = 255;

			return ColorLib.setAlpha(color, alpha);
		}
	}
} // end of inner class NodeColorAction