import java.util.List;

import com.google.common.collect.Lists;

public class Bag {
	private List<Tile> tiles;

	public Bag(Board board) {
		int numRows = board.getRowCount();
		int numCols = board.getColCount();
		tiles = Lists.<Tile>newArrayListWithCapacity(numRows * numCols);
		for(int r = 0; r < numRows; r++) {
			for(int c = 0; c < numCols; c++) {
				tiles.add(board.getTile(r, c));
			}
		}
	}

	public int numTiles() {
		return tiles.size();
	}

	public Tile drawTile() {
		if(tiles.size() <= 0) {
			throw new IndexOutOfBoundsException("Bag.drawTile: no tiles left in bag");
		}
		return tiles.remove((int) (Math.random() * tiles.size()));
	}
}
