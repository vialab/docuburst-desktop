package ca.utoronto.cs.docuburst.prefuse.action;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;

import ca.utoronto.cs.docuburst.prefuse.DocuBurstActionList;
import prefuse.action.assignment.FontAction;
import prefuse.render.LabelRenderer;
import prefuse.util.FontLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;

public class StarburstScaleFontAction extends FontAction {
	public StarburstScaleFontAction(String labels) {
		super(labels);
	}

	public double getArcHeight(VisualItem item) {
		// the outer-inner distance between rings minus 2 for borders
		if (item.getDouble("angleExtent") == 360)
			return 2 * (item.getDouble("outerRadius") - item.getDouble("innerRadius") - 4);
		else
			return (item.getDouble("outerRadius") - item.getDouble("innerRadius") - 4);

	}

	public double getArcWidth(VisualItem item) {
		// the chord length between two points at midpoint of circle
		double R = (item.getDouble("outerRadius") + item.getDouble("innerRadius")) / 2;
		if (item.getDouble("innerRadius") == 0)
			return 2 * R; // render across middle of circle
		else
			return R * Math.toRadians(item.getDouble("angleExtent")); // length along arc
	}

	public double getDiagonal(Rectangle2D bounds) {
		// set font based on diagonal not width to make more even around the circle
		return Math.sqrt(bounds.getWidth() * bounds.getHeight());
	}

	public Font getFont(VisualItem item) {
		if (DocuBurstActionList.FONTFROMDIAGONAL)
			return getFontDiagonal(item);
		else
			return getFontPrecise(item);
	}

	public Font getFontDiagonal(VisualItem item) {
		DecoratorItem dItem = (DecoratorItem) item;
		Font currentFont = (Font) item.getSchema().getDefault(VisualItem.FONT);
		FontMetrics fm = LabelRenderer.DEFAULT_GRAPHICS.getFontMetrics(currentFont);
		// too bigDouble("innerRadius")

		while (((fm.stringWidth(item.getString("label")) > getDiagonal(dItem.getDecoratedItem().getBounds())) || (fm.getHeight() > getArcHeight(dItem)))
				&& (currentFont.getSize() > 0)) {
			currentFont = FontLib.getFont(currentFont.getFontName(), currentFont.getStyle(), currentFont.getSize() - 1);
			fm = LabelRenderer.DEFAULT_GRAPHICS.getFontMetrics(currentFont);
		}
		while ((fm.stringWidth(item.getString("label")) < getDiagonal(dItem.getDecoratedItem().getBounds())) && (fm.getHeight() < getArcHeight(dItem))) {
			Font testFont = FontLib.getFont(currentFont.getFontName(), currentFont.getStyle(), currentFont.getSize() + 1);
			FontMetrics fmTest = LabelRenderer.DEFAULT_GRAPHICS.getFontMetrics(testFont);
			if (fmTest.stringWidth(item.getString("label")) < getDiagonal(dItem.getDecoratedItem().getBounds()) * 0.75) {
				currentFont = testFont;
				fm = LabelRenderer.DEFAULT_GRAPHICS.getFontMetrics(currentFont);
			} else {
				break;
			}
		}
		dItem.setFont(currentFont);
		return currentFont;
	}

	public Font getFontPrecise(VisualItem item) {
		DecoratorItem dItem = (DecoratorItem) item;
		Font currentFont = (Font) item.getSchema().getDefault(VisualItem.FONT);
		FontMetrics fm = LabelRenderer.DEFAULT_GRAPHICS.getFontMetrics(currentFont);

		if (item.getDouble("rotation") != 0) {
			// scale based on string width and difference between arc inner and out radii
			double scaleFactor = getArcHeight(dItem.getDecoratedItem()) / fm.stringWidth(dItem.getString("label"));
			// ensure scaled height doesn't exceed median arc width
			if (fm.getHeight() * scaleFactor > getArcWidth(dItem))
				scaleFactor = getArcWidth(dItem) / fm.getHeight();
			currentFont = FontLib.getFont(currentFont.getFontName(), currentFont.getStyle(), Math.min(currentFont.getSize() * scaleFactor, DocuBurstActionList.MAXFONTHEIGHT));
		} else {
			// scale based on string height and difference between arc inner and out radii
			double scaleFactor = getArcHeight(dItem.getDecoratedItem()) / fm.getHeight();
			// ensure scaled height doesn't exceed median arc width
			if (fm.stringWidth(dItem.getString("label")) * scaleFactor > getArcWidth(dItem))
				scaleFactor = getArcWidth(dItem) / fm.stringWidth(dItem.getString("label"));
			// scale is later refined by the renderer
			currentFont = FontLib.getFont(currentFont.getFontName(), currentFont.getStyle(), Math.min(currentFont.getSize() * scaleFactor, DocuBurstActionList.MAXFONTHEIGHT));
		}

		return currentFont;
	}
}