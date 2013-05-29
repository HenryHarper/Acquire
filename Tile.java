import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class Tile {
	private int x;
	private int y;
	private int width;
	private int height;
	private Color foreground;
	private Color highlight;
	private Font font;
	private FontMetrics fontMetrics;
	private boolean highlighted;
	private int highlightBorderWidth;
	private String name;

	private int row;
	private int col;
	private Corporation corporation;

	public Tile(int row, int col, Corporation corporation, int x, int y, int width, int height, Color foreground, Color highlight, int highlightBorderWidth, Font font, FontMetrics fontMetrics) {
		this.row = row;
		this.col = col;
		this.corporation = corporation;

		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.foreground = foreground;
		this.highlight = highlight;
		this.font = font;
		this.fontMetrics = fontMetrics;
		this.highlighted = false;
		this.highlightBorderWidth = highlightBorderWidth;
		this.name = Integer.toString(col) + ('A' + (row - 1));
	}

	public void paint(Graphics g) {
		paint(g, x, y, corporation.getColor());
	}

	public void paint(Graphics g, int altX, int altY, Color altColor) {
		int centerX = altX + (width / 2);
		int centerY = altY + (height / 2);
		int nameX = centerX - (fontMetrics.stringWidth(name) / 2);
		int nameY = centerY + (fontMetrics.getAscent() / 2);

		g.setColor(altColor);
		g.fillRect(altX, altY, width, height);

		g.setColor(foreground); 
		g.setFont(font);
		g.drawString(name, nameX, nameY);

		if(highlighted) {
			int Ax = altX;
			int Dx = altX + width;
			int Bx = Ax + highlightBorderWidth;
			int Cx = Dx - highlightBorderWidth;
			int Ay = altY;
			int Dy = altY + height;
			int By = Ay + highlightBorderWidth;
			int Cy = Dy - highlightBorderWidth;
			int highlightX[] = {Ax, Dx, Dx, Ax, Ax, Bx, Bx, Cx, Cx, Bx, Ax};
			int highlightY[] = {Ay, Ay, Dy, Dy, Ay, By, Cy, Cy, By, By, Ay};

			g.setColor(highlight);
			g.fillPolygon(highlightX, highlightY, highlightX.length);
		}

		g.setColor(foreground);
		g.drawRect(altX, altY, width, height);
	}

	public void setCorporation(Corporation corporation) {
		this.corporation = corporation;
	}

	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	public Corporation getCorporation() {
		return corporation;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}
}
