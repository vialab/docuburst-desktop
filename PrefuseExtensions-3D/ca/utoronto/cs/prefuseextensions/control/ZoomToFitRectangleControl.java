package ca.utoronto.cs.prefuseextensions.control;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.PaintListener;
import prefuse.util.ui.UILib;

public class ZoomToFitRectangleControl extends ControlAdapter implements
		PaintListener {
	private int m_button = LEFT_MOUSE_BUTTON;

	private int m_margin = 50;

	private int m_minDistance = 15;

	private long m_duration = 2000;

	private Point start = new Point(0, 0);

	private Point end = new Point(0, 0);

	private boolean buttonPressed = false;

	public ZoomToFitRectangleControl() {
	}

	public ZoomToFitRectangleControl(int button) {
		this.m_button = button;
	}

	public ZoomToFitRectangleControl(int margin, long duration, int button,
			int minDistance) {
		this.m_margin = margin;
		this.m_duration = duration;
		this.m_button = button;
		this.m_minDistance = minDistance;
	}

	public void mousePressed(MouseEvent e) {

		if (UILib.isButtonPressed(e, m_button)) {
			this.start = e.getPoint();
			this.buttonPressed = true;
		}
	}

	public void mouseDragged(MouseEvent e) {
		// paintRectangle
		if (this.buttonPressed) {
			this.end = e.getPoint();
			Display display = (Display) e.getComponent();
			display.repaint();
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			this.end = e.getPoint();

			// to avoid clicks
			if (Math.abs(this.start.x - this.end.x) > m_minDistance
					|| Math.abs(this.start.y - this.end.y) > m_minDistance) {
				Display display = (Display) e.getComponent();
				Point2D absStart = display.getAbsoluteCoordinate(this.start,
						null);
				Point2D absEnd = display.getAbsoluteCoordinate(this.end, null);
				Rectangle rect = createRect(absStart, absEnd);
				GraphicsLib.expand(rect, m_margin / display.getScale());
				DisplayLib.fitViewToBounds(display, rect, m_duration);
				display.repaint();
			}

			this.buttonPressed = false;
			this.end = null;
		}

	}

	public void postPaint(Display d, Graphics2D g) {
		if (this.end != null) {
			Rectangle rect = createRect(this.start, this.end);
			g.drawRect(rect.x, rect.y, rect.width, rect.height);
		}
	}

	public void prePaint(Display d, Graphics2D g) {
	}

	private Rectangle createRect(Point2D start, Point2D end) {
		int x = (int) Math.min(start.getX(), end.getX());
		int y = (int) Math.min(start.getY(), end.getY());
		int x2 = (int) Math.max(start.getX(), end.getX());
		int y2 = (int) Math.max(start.getY(), end.getY());
		int width = (int) Math.abs(x2 - x);
		int height = (int) Math.abs(y2 - y);
		return new Rectangle(x, y, width, height);
	}

}
