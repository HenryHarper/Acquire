import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

public class GameFlowController {
	public static final int MAX_PLAYERS = 6;
	public static final int HAND_SIZE = 6;

	private ServerConnectionManager connectionManager;
	private Board board;
	private Bag bag;
	private Deck deck;
	private List<MessageSender> outs;
	private List<String> names;
	private int turnPlayerNum;

	private int savedRow;
	private int savedCol;
	private int savedKiller;
	private List<Corporation> savedMergeList;
	private List<Integer> savedTSKOrder;

	public GameFlowController(ServerConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		this.board = new Board();
		this.bag = new Bag(board);
		this.deck = null;
		this.outs = Lists.<MessageSender>newArrayListWithCapacity(MAX_PLAYERS);
		this.names = Lists.<String>newArrayListWithCapacity(MAX_PLAYERS);
		this.turnPlayerNum = 0;

		this.savedRow = -1;
		this.savedCol = -1;
		this.savedKiller = -1;
		this.savedMergeList = Lists.<Corporation>newArrayListWithCapacity(Corporation.NUM_ACTUAL_CORPORATIONS);
		this.savedTSKOrder = Lists.<Integer>newLinkedList();
	}

	public int playerJoined(MessageSender out) {
		outs.add(out);

		return outs.size() - 1;
	}

	public void receivedPlayerName(String playerName) {
		names.add(playerName);

		for(MessageSender out : outs) {
			out.playerNamesMsg(names);
		}
	}

	public void playerTSK(int playerNum, int corporation, int traded, int sold, int kept) {
		for(int i = 0; i < outs.size(); i++) {
			if(i != playerNum) {
				outs.get(i).tskMsg(playerNum, corporation, traded, sold, kept);
			}
		}

		if(savedTSKOrder.size() == 0) {
			Corporation dyingCorp = board.getCorporation(corporation);

			board.mergeCorporation(savedMergeList.get(0), dyingCorp);

			savedMergeList.remove(dyingCorp);
			mergeCorporations(savedKiller, savedMergeList);
		} else {
			outs.get(savedTSKOrder.remove(0).intValue()).tskMsg(-1, corporation, -1, -1, -1);
		}
	}

	public void killCorporation(int toKillIndex) {
		if(toKillIndex < 0 || toKillIndex >= savedMergeList.size()) {
			throw new IllegalArgumentException("GameFlowController.killCorporation: invalid corporation to kill");
		}

		Corporation toKill = savedMergeList.get(toKillIndex);

		List<Integer> sortedHolders = deck.getSortedStockHolders(toKill.getID());

		int numHolders = sortedHolders.size();

		savedTSKOrder.clear();

		List<List<Integer>> tieredHolders = Lists.<List<Integer>>newArrayListWithCapacity(numHolders);

		int i = 0;
		while(i < numHolders) {
			int currStock = deck.getStock(sortedHolders.get(i).intValue(), toKill.getID());
			List<Integer> tempTier = Lists.<Integer>newLinkedList();
			int j = i;
			while(j < numHolders && deck.getStock(sortedHolders.get(j).intValue(), toKill.getID()) == currStock) {
				tempTier.add(sortedHolders.get(j));
				j++;
			}
			Collections.shuffle(tempTier);
			for(Integer tier : tempTier) {
				if(tier.intValue() != savedKiller) {
					savedTSKOrder.add(tier);
				}
			}
			tieredHolders.add(tempTier);
			i = j;
		}

		if(tieredHolders.size() == 0) {
			board.mergeCorporation(savedMergeList.get(0), toKill);

			savedMergeList.remove(toKill);
			mergeCorporations(savedKiller, savedMergeList);
		} else {
			List<Integer> majorityHolders = tieredHolders.get(0);
			List<Integer> minorityHolders;
			if(majorityHolders.size() > 1) {
				minorityHolders = Lists.<Integer>newLinkedList();
			} else {
				if(tieredHolders.size() == 1) {
					minorityHolders = majorityHolders;
				} else {
					minorityHolders = tieredHolders.get(1);
				}
			}

			for(MessageSender out : outs) {
				out.killedMsg(majorityHolders, minorityHolders, toKill.getCost());
			}

			outs.get(savedKiller).tskMsg(-1, toKill.getID(), -1, -1, -1);
		}
	}

	private void mergeCorporations(int playerNum, List<Corporation> toMerge) {
		if(toMerge.size() == 0) {
			throw new IllegalArgumentException("GameFlowController.mergeCorporations: empty list merge list");
		} else if(toMerge.size() == 1) {
			board.changeTile(board.getTile(savedRow, savedCol), toMerge.get(0));

			for(MessageSender out : outs) {
				out.placeTileMsg(-1, savedRow, savedCol, toMerge.get(0).getID());
			}
		} else {
			int canDieIndex = toMerge.size() - 1;
			int minSize = toMerge.get(canDieIndex).getSize();
			for(int i = 0; i < toMerge.size(); i++) {
				if(toMerge.get(i).getSize() == minSize) {
					canDieIndex = i;
					break;
				}
			}

			savedKiller = playerNum;
			savedMergeList = toMerge;
			if(canDieIndex < toMerge.size() -  1) {
				outs.get(playerNum).killPromptMsg(toMerge, canDieIndex);
			}
			else {
				killCorporation(canDieIndex);
			}
		}
	}

	public void tilePlayed(int playerNum, int row, int col) {
		List<Corporation> adjacentCorps = board.getAdjacentCorporations(row, col);
		if(adjacentCorps.size() == 0) {
			board.changeTile(board.getTile(row, col), board.getCorporation(Corporation.DIAGONAL));

			for(MessageSender out : outs) {
				out.placeTileMsg(playerNum, row, col, Corporation.DIAGONAL);
			}
		} else if(adjacentCorps.size() == 1) {
			int adjacentCorpID = adjacentCorps.get(0).getID();

			if(adjacentCorpID == Corporation.DIAGONAL) {
				outs.get(playerNum).createCorporationPromptMsg(board.getCreateableCorporations());
			}
			else {
				board.changeTile(board.getTile(row, col), adjacentCorps.get(0));

				for(MessageSender out : outs) {
					out.placeTileMsg(playerNum, row, col, adjacentCorpID);
				}
			}
		} else {
			for(MessageSender out : outs) {
				out.placeTileMsg(playerNum, row, col, Corporation.DIAGONAL);
			}

			savedRow = row;
			savedCol = col;
			mergeCorporations(playerNum, adjacentCorps);
		}
	}

	public void chatMessage(int playerNum, String message) {
		if(playerNum < 0 || playerNum >= names.size()) {
			throw new IllegalArgumentException("GameFlowController.chatMessage: invalid player");
		}

		String finalMessage = names.get(playerNum) + ": " + message;
		for(MessageSender out : outs) {
			out.chatMsg(finalMessage);
		}
	}

	public void startGame() {
		if(outs.size() == names.size()) {
			throw new RuntimeException("GameFlowController.startGame: unequal number of outs and names");
		}

		connectionManager.setAccept(false);

		this.deck = new Deck(outs.size());

		for(MessageSender out : outs) {
			Tile tile = bag.drawTile();
			for(MessageSender o : outs) {
				o.placeTileMsg(-1, tile.getRow(), tile.getCol(), Corporation.DIAGONAL);
			}

			for(int j = 0; j < GameFlowController.HAND_SIZE; j++) {
				out.dealTileMsg(bag.drawTile());
			}
		}

		List<MessageSender> outsCopy = Lists.<MessageSender>newArrayList(outs);
		List<String> namesCopy = Lists.<String>newArrayList(names);
		outs.clear();
		names.clear();
		while(outsCopy.size() > 0) {
			int index = (int) (Math.random() * outsCopy.size());
			outs.add(outsCopy.remove(index));
			names.add(namesCopy.remove(index));
		}

		for(int i = 0; i < outs.size(); i++) {
			outs.get(i).playerNamesMsg(names);
			outs.get(i).startGameMsg(i);
		}
	}

	public void createCorporation(int playerNum, int corporationID) {
		Corporation corporation = board.getCorporation(corporationID);
		LinkedList<Tile> toChange = Lists.<Tile>newLinkedList();
		toChange.addLast(board.getTile(savedRow, savedCol));
		while(toChange.size() > 0) {
			Tile currTile = toChange.removeFirst();
			int currRow = currTile.getRow();
			int currCol = currTile.getCol();
			if(currTile.getCorporation().getID() == Corporation.DIAGONAL) {
				for(MessageSender out : outs) {
					out.placeTileMsg(-1, currRow, currCol, corporationID);
				}

				if(currRow > 0) {
					toChange.add(board.getTile(currRow - 1, currCol));
				}
				if(currRow < board.getRowCount() - 1) {
					toChange.add(board.getTile(currRow + 1, currCol));
				}
				if(currCol > 0) {
					toChange.add(board.getTile(currRow, currCol - 1));
				}
				if(currCol < board.getColCount() - 1) {
					toChange.add(board.getTile(currRow, currCol + 1));
				}
			}
		}

		deck.draw(playerNum, corporationID, 1);
		for(MessageSender out : outs) {
			out.createCorporationMsg(playerNum, corporation);
		}
	}

	public void playerSB(int playerNum, int[] sold, int[] bought) {
		for(int i = 0; i < sold.length; i++) {
			deck.draw(playerNum, i, -sold[i]);
		}

		for(int i = 0; i < bought.length; i++) {
			deck.draw(playerNum, i, bought[i]);
		}

		for(MessageSender out : outs) {
			out.sbMsg(playerNum, sold, bought);
		}
	}

	public void endTurn(int playerNum, boolean endGame) {
		if(endGame) {
			for(MessageSender out : outs) {
				out.gameOverMsg(turnPlayerNum);
			}
		}
		else {
			turnPlayerNum++;
			if(turnPlayerNum == outs.size()) {
				turnPlayerNum = 0;
			}

			for(MessageSender out : outs) {
				out.endTurnMsg(playerNum, endGame, turnPlayerNum);
			}
		}
	}
}
