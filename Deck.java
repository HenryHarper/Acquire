import java.util.List;

import com.google.common.collect.Lists;

public class Deck {
	public static final int MAX_STOCK = 25;

	private int[][] stock;
	private int pileIndex;

	public Deck(int numPlayers) {
		stock = new int[numPlayers + 1][Corporation.NUM_ACTUAL_CORPORATIONS];
		for(int i = 0; i < numPlayers; i++) {
			for(int j = 0; j < stock[i].length; j++) {
				stock[i][j] = 0;
			}
		}
		for(int j = 0; j < stock[numPlayers].length; j++) {
			stock[numPlayers][j] = MAX_STOCK;
		}

		pileIndex = numPlayers;
	}

	public int getStock(int playerNum, Corporation corporation) {
		validatePlayer(playerNum);
		return stock[playerNum][corporation.getID()];
	}

	public int[] getHand(int playerNum) {
		validatePlayer(playerNum);
		return stock[playerNum];
	}

	public int getRemaining(Corporation corporation) {
		return stock[pileIndex][corporation.getID()];
	}

	public List<Integer> getSortedStockHolders(Corporation corporation) {
		List<Integer> sortedList = Lists.<Integer>newArrayListWithCapacity(pileIndex);
		for(int i = 0; i < pileIndex; i++) {
			int currStock = stock[i][corporation.getID()];
			if(currStock > 0) {
				int j = 0;
				while(j < sortedList.size() && currStock <= stock[sortedList.get(j).intValue()][corporation.getID()]) {
					j++;
				}
				sortedList.add(j, new Integer(i));
			}
		}
		return sortedList;
	}

	public boolean draw(int playerNum, Corporation corporation, int quantity) {
		validatePlayer(playerNum);
		int corporationNum = corporation.getID();
		if(stock[pileIndex][corporationNum] < quantity) {
			throw new IllegalArgumentException("Deck.draw: not enough stock remaining");
		}
		stock[pileIndex][corporationNum] -= quantity;
		stock[playerNum][corporationNum] += quantity;
		return true;
	}

	private void validatePlayer(int playerNum) {
		if(playerNum < 0 || playerNum >= pileIndex) {
			throw new IllegalArgumentException("Deck: invalid player");
		}
	}
}
