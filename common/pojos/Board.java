package common.pojos;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.List;

import com.google.common.collect.Lists;

public class Board {
	public static final int NUM_ROWS = 9;
	public static final int NUM_COLS = 12;

	private int x;
	private int y;
	private int width;
	private int height;
	private boolean mouseWithin;
	private Color foreground;
	private Color background;

	private Tile[][] tiles;
	private Corporation[] corporations;

	public Board() {
		this(0, 0, 0, 0, 0, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	public Board(int x, int y, int tileWidth, int tileSpacing, int highlightBorderWidth, Font tileFont, FontMetrics tileFontMetrics, Color foreground, Color background, Color yellow, Color red, Color orange, Color green, Color blue, Color purple, Color aqua, Color tile, Color unplayed, Color highlight) {
		this.x = x;
		this.y = y;
		this.foreground = foreground;
		this.background = background;

		corporations = new Corporation[Corporation.NUM_CORPORATIONS];
		corporations[Corporation.ZETA] = new Corporation(Corporation.ZETA, "Zeta", 200, yellow);
		corporations[Corporation.SACKSON] = new Corporation(Corporation.SACKSON, "Sackson", 200, red);
		corporations[Corporation.HYDRA] = new Corporation(Corporation.HYDRA, "Hydra", 300, orange);
		corporations[Corporation.FUSION] = new Corporation(Corporation.FUSION, "Fusion", 300, green);
		corporations[Corporation.AMERICA] = new Corporation(Corporation.AMERICA, "America", 300, blue);
		corporations[Corporation.PHOENIX] = new Corporation(Corporation.PHOENIX, "Phoenix", 400, purple);
		corporations[Corporation.QUANTUM] = new Corporation(Corporation.QUANTUM, "Quantum", 400, aqua);
		corporations[Corporation.DIAGONAL] = new Corporation(Corporation.DIAGONAL, "Diagonal", 0, tile);
		corporations[Corporation.BAG] = new Corporation(Corporation.BAG, "Bag", 0, unplayed);

		tiles = new Tile[NUM_ROWS][NUM_COLS];
		int tempX = 0;
		int tempY = y + tileSpacing;
		for(int r = 0; r < NUM_ROWS; r++) {
			tempX = x + tileSpacing;
			for(int c = 0; c < NUM_COLS; c++) {
				tiles[r][c] = new Tile(r, c, corporations[Corporation.BAG], tempX, tempY, tileWidth, tileWidth, foreground, highlight, highlightBorderWidth, tileFont, tileFontMetrics);
				corporations[Corporation.BAG].addTile(tiles[r][c]);
				tempX += tileWidth + tileSpacing;
			}
			tempY += tileWidth + tileSpacing;
		}
		this.width = tempX - x;
		this.height = tempY - y;

		this.mouseWithin = false;
	}

	public boolean updateHighlights(int mouseX, int mouseY) {
		boolean updated = false;
		boolean wasMouseWithin = mouseWithin;
		mouseWithin = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		if(mouseWithin || wasMouseWithin) {
			for(int r = 0; r < tiles.length; r++) {
				for(int c = 0; c < tiles[r].length; c++) {
					updated = updated || tiles[r][c].updateHighlight(mouseX, mouseY);
				}
			}
		}
		return updated;
	}

	public void paint(Graphics g) {
		g.setColor(background);
		g.fillRect(x, y, width, height);
		
		g.setColor(foreground);
		g.drawRect(x, y, width, height);
		
		for(int r = 0; r < NUM_ROWS; r++) {
			for(int c = 0; c < NUM_COLS; c++) {
				tiles[r][c].paint(g);
			}
		}
	}

	public int getRowCount() {
		return tiles.length;
	}

	public int getColCount(){
		if(tiles.length <= 0) {
			return 0;
		}
		return tiles[0].length;
	}

	public Tile getTile(int row, int col) {
		if(row < 0 || row >= tiles.length) {
			throw new IllegalArgumentException("Board.getTile: invalid row");
		}
		if(col < 0 || col >= tiles[row].length) {
			throw new IllegalArgumentException("Board.getTile: invalid row");
		}
		return tiles[row][col];
	}

	public int getCorporationCount() {
		return corporations.length;
	}

	public Corporation getCorporation(int corporationNum) {
		if(corporationNum < 0 || corporationNum >= corporations.length) {
			throw new IllegalArgumentException("Board.getCorporation: invalid corporation");
		}
		return corporations[corporationNum];
	}

	private static void insertCorporationBySize(List<Corporation> list, Corporation newItem) {
		int i = list.size() - 1;
		int newSize = newItem.getSize();
		while(i >= 0 && newSize > list.get(i).getSize()) {
			i--;
		}
		if(newItem.getID() != list.get(i).getID()) {
			list.add(i + 1, newItem);
		}
	}

	public List<Corporation> getAdjacentCorporations(Tile tile) {
		int row = tile.getRow();
		int col = tile.getCol();
		List<Corporation> adjacentCorps = Lists.<Corporation>newArrayListWithCapacity(4);
		if(row > 0) {
			insertCorporationBySize(adjacentCorps, tiles[row - 1][col].getCorporation());
		}
		if(row < tiles.length - 1) {
			insertCorporationBySize(adjacentCorps, tiles[row + 1][col].getCorporation());
		}
		if(col > 0) {
			insertCorporationBySize(adjacentCorps, tiles[row][col - 1].getCorporation());
		}
		if(col < tiles[0].length - 1) {
			insertCorporationBySize(adjacentCorps, tiles[row][col + 1].getCorporation());
		}
		return adjacentCorps;
	}

	public List<Corporation> getCreateableCorporations() {
		List<Corporation> result = Lists.<Corporation>newArrayListWithCapacity(Corporation.NUM_ACTUAL_CORPORATIONS);
		for(Corporation corporation : corporations) {
			if(corporation.getSize() == 0) {
				result.add(corporation);
			}
		}
		return result;
	}

	public void changeTile(Tile tile, Corporation newCorp) {
		tile.getCorporation().removeTile(tile);
		tile.setCorporation(newCorp);
		newCorp.addTile(tile);
	}

	public void mergeCorporation(Corporation surviving, Corporation dying) {
		for(Tile dyingTile : dying.getTiles()) {
			changeTile(dyingTile, surviving);
		}
		dying.removeAllTiles();
	}
}
