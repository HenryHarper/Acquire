import java.awt.Color;
import java.util.List;

import com.google.common.collect.Lists;

public class Corporation {
	public static final int ZETA = 0;
	public static final int SACKSON = 1;
	public static final int HYDRA = 2;
	public static final int FUSION = 3;
	public static final int AMERICA = 4;
	public static final int PHOENIX = 5;
	public static final int QUANTUM = 6;
	public static final int NUM_ACTUAL_CORPORATIONS = 7;
	public static final int DIAGONAL = 7;
	public static final int BAG = 8;
	public static final int NUM_CORPORATIONS = 9;

	private static final int majorityBonusMultiplier = 10;
	private static final int minorityBonusMultiplier = 5;

	private Color background;

	private int id;
	private String name;
	private List<Tile> tiles;
	private int baseCost;
	private int cost;

	public Corporation(int id, String name, int baseCost, Color foreground) {
		this.id = id;
		this.name = name;
		this.tiles = Lists.<Tile>newLinkedList();
		this.baseCost = baseCost;
		this.cost = -1;
		this.background = foreground;
	}

	private void updateCost() {
		if(tiles.size() <= 2) {
			cost = baseCost;
		} else if(tiles.size() <= 3) {
			cost = baseCost + 100;
		} else if(tiles.size() <= 4) {
			cost = baseCost + 200;
		} else if(tiles.size() <= 5) {
			cost = baseCost + 300;
		} else if(tiles.size() <= 10) {
			cost = baseCost + 400;
		} else if(tiles.size() <= 20) {
			cost = baseCost + 500;
		} else if(tiles.size() <= 30) {
			cost = baseCost + 600;
		} else if(tiles.size() <= 40) {
			cost = baseCost + 700;
		} else {
			cost = baseCost + 800;
		}
	}

	public List<Tile> getTiles() {
		return tiles;
	}

	public void addTile(Tile tile) {
		tiles.add(tile);

		updateCost();
	}

	public void removeTile(Tile tile) {
		tiles.remove(tile);

		updateCost();
	}

	public void removeAllTiles() {
		tiles.clear();
		cost = -1;
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return tiles.size();
	}

	public int getCost() {
		return cost;
	}

	public int getMajorityBonus() {
		return cost * majorityBonusMultiplier;
	}

	public int getMinorityBonus() {
		return cost * minorityBonusMultiplier;
	}

	public Color getColor() {
		return background;
	}

	@Override
	public String toString() {
		return name;
	}
}
