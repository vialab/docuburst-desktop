package ca.utoronto.cs.docuburst.prefuse.action;

import ca.utoronto.cs.docuburst.prefuse.ColorScheme;
import ca.utoronto.cs.docuburst.prefuse.DocuBurstActionList;
import prefuse.action.assignment.ColorAction;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

/**
 * Set node fill colors
 */
public class NodeStrokeColorAction extends ColorAction {

    final DocuBurstActionList docuBurstActionList;

    public NodeStrokeColorAction(DocuBurstActionList docuBurstActionList, String group) {
		// default is invisible stroke (for word and lemma nodes)
		super(group, VisualItem.STROKECOLOR, ColorLib.gray(0, 0));
        this.docuBurstActionList = docuBurstActionList;
		add("ingroup('highlight')", ColorScheme.highlightColor);
		add("_hover", ColorScheme.hoverColor);
		add("type = 1 or ingroup('_search_')", new WordCountColorAction(group, VisualItem.STROKECOLOR));
		// if it's a sense node, run the count-based action
	}

	class SenseColorAction extends ColorAction {

		public SenseColorAction(String group, String field) {
			super(group, field);
		}

		public int getColor(VisualItem item) {
			// search results always pink, alpha changes based on count
			if (item.getInt("searchDepth") == 1)
				return ColorScheme.searchColor;

			if (item.canGetInt("senseIndex"))
				return ColorScheme.sensesPalette[item.getInt("senseIndex") % ColorScheme.sensesPalette.length];
			else
				return ColorScheme.zeroOccurrenceSenseColor;
		}
	}

	class WordCountColorAction extends ColorAction {
		SenseColorAction sca;

		public WordCountColorAction(String group, String field) {
			super(group, field);
			sca = new SenseColorAction(group, VisualItem.FILLCOLOR);
		}

		public int getColor(VisualItem item) {
			int color = ColorLib.darker(item.getFillColor());//ColorLib.darker(sca.getColor(item));
			int alpha = ColorScheme.zeroAlpha + 10;

			// return search color if match; don't modulate based on occurrence or omit due to size
			if (item.getInt("searchDepth") == 1)
				return ColorScheme.searchColor;

			// for small nodes, omit border
			if (item.getDouble("angleExtent") < 2)
				alpha = 0;

			// if we aren't counting, just return border colour
			if (NodeStrokeColorAction.this.docuBurstActionList.countType.equals(DocuBurstActionList.NOCOUNT))
				return color;

			float maxTotal = NodeStrokeColorAction.this.docuBurstActionList.getMaxTotal(NodeStrokeColorAction.this.docuBurstActionList.countType);
			//float total = (float) Math.log(item.getFloat(CACHECOUNT + countType));
			float total = (float) (item.getFloat(DocuBurstActionList.CACHECOUNT + NodeStrokeColorAction.this.docuBurstActionList.countType));

			// no search results, return senseColor, with 0 alpha if < 2 degrees
			if (total == 0)
				return ColorLib.setAlpha(ColorLib.darker(sca.getColor(item)), alpha);

			// if we are have a single sense and count is non-zero, set color to results colour
			// otherwise keep it as fill color 
			if (color == ColorScheme.zeroOccurrenceSenseColor)
				color = ColorScheme.resultsColor;

			// base border alpha on count; alpha starts darker than for fill
			alpha = 80;
			alpha += (int) ((total / maxTotal) * (float) (255 - alpha));

			if (alpha > 255)
				alpha = 255;

			if (item.getDouble("angleExtent") < 2)
				alpha = 0;
			return ColorLib.setAlpha(color, alpha);
		}

	}
} // end of inner class NodeColorAction