import java.io.DataOutputStream;
import java.util.List;

public class MessageSender {
	public static final String PLAYER_NAMES_MSG = "00";
	public static final String TURN_MSG = "01";
	public static final String PLACE_TILE_MSG = "02";
	public static final String CHAT_MSG = "03";
	public static final String START_GAME_MSG = "04";
	public static final String DEAL_TILE_MSG = "05";
	public static final String CREATE_CORP_PROMPT_MSG = "06";
	public static final String CREATE_CORP_MSG = "07";
	public static final String KILL_PROMPT_MSG = "08";
	public static final String KILL_MSG = "09";
	public static final String KILLED_MSG = "10";
	public static final String TSK_PROMPT_MSG = "11";
	public static final String TSK_MSG = "12";
	public static final String SB_PROMPT_MSG = "13";
	public static final String SB_MSG = "14";
	public static final String END_TURN_MSG = "15";
	public static final String GAME_OVER_MSG = "16";

	public static final int BUFFERSIZE = 2048;
	public static final char STX = (char) 2;
	public static final char ETX = (char) 3;

	private DataOutputStream out;

	public MessageSender(DataOutputStream out) {
		this.out = out;
	}

	public void passMessage(String message) {
		try {
			out.write(message.getBytes());
			out.flush();
		} catch(Exception e) {
			throw new RuntimeException("MessageSender.passMessage: " + e.toString());
		}
	}

	public void playerNamesMsg(String playerName) {
		passMessage(STX + PLAYER_NAMES_MSG + playerName + ETX);
	}

	public void playerNamesMsg(List<String> playerNames) {
		StringBuilder message = new StringBuilder(128);
		message.append(STX);
		message.append(PLAYER_NAMES_MSG);
		message.append(playerNames.size());
		message.append(ETX);
		for(String playerName: playerNames) {
			message.append(playerName);
			message.append(ETX);
		}
		passMessage(message.toString());
	}

	public void turnMsg(int playerNum) {
		passMessage(STX + TURN_MSG + ETX + playerNum + ETX);
	}

	public void placeTileMsg(int playerNum, Tile tile, Corporation corporation) {
		passMessage(STX + PLACE_TILE_MSG + playerNum + ETX + tile.getRow() + ETX + tile.getCol() + ETX + corporation.getID() + ETX);
	}

	public void chatMsg(String message) {
		passMessage(STX + CHAT_MSG + message + ETX);
	}

	public void startGameMsg(int playerNum) {
		passMessage(STX + START_GAME_MSG + playerNum + ETX);
	}

	public void dealTileMsg(Tile tile) {
		passMessage(STX + DEAL_TILE_MSG + tile.getRow() + ETX + tile.getCol() + ETX);
	}

	public void createCorporationPromptMsg(List<Corporation> createable) {
		StringBuilder message = new StringBuilder(32);
		message.append(STX);
		message.append(CREATE_CORP_PROMPT_MSG);
		message.append(createable.size());
		message.append(ETX);
		for(Corporation corp : createable) {
			message.append(corp.getID());
			message.append(ETX);
		}
		passMessage(message.toString());
	}

	public void createCorporationMsg(int playerNum, Corporation toCreate) {
		passMessage(STX + CREATE_CORP_MSG + playerNum + ETX + toCreate.getID() + ETX);
	}

	public void killPromptMsg(List<Corporation> killable, int firstValidIndex) {
		StringBuilder message = new StringBuilder(32);
		message.append(STX);
		message.append(KILL_PROMPT_MSG);
		message.append(killable.size());
		message.append(ETX);
		for(Corporation corp : killable) {
			message.append(corp.getID());
			message.append(ETX);
		}
		message.append(firstValidIndex);
		message.append(ETX);
		passMessage(message.toString());
	}

	public void killMsg(int killIndex) {
		passMessage(STX + KILL_MSG + killIndex + ETX);
	}

	public void killedMsg(int playerNum, List<Integer> majorityHolders, List<Integer> minorityHolders, int stockPrice) {
		StringBuilder message = new StringBuilder(32);
		message.append(STX);
		message.append(KILLED_MSG);
		message.append(playerNum);
		message.append(ETX);
		message.append(majorityHolders.size());
		message.append(ETX);
		for(Integer player : majorityHolders) {
			message.append(player.intValue());
			message.append(ETX);
		}
		message.append(minorityHolders.size());
		message.append(ETX);
		for(Integer player : minorityHolders) {
			message.append(player.intValue());
			message.append(ETX);
		}
		message.append(stockPrice);
		message.append(ETX);
		passMessage(message.toString());
	}

	public void tskPromptMsg(Corporation corporation) {
		passMessage(STX + TSK_PROMPT_MSG + corporation.getID() + ETX);
	}

	public void tskMsg(int playerNum, Corporation corporation, int traded, int sold, int kept) {
		passMessage(STX + TSK_MSG + playerNum + ETX + corporation.getID() + ETX + traded + ETX + sold + ETX + kept + ETX);
	}

	public void sbPromptMsg() {
		passMessage(STX + SB_PROMPT_MSG + ETX);
	}

	public void sbMsg(int playerNum, List<Integer> sold, List<Integer> bought) {
		StringBuilder message = new StringBuilder(32);
		message.append(STX);
		message.append(SB_MSG);
		message.append(playerNum);
		message.append(ETX);
		message.append(sold.size());
		message.append(ETX);
		for(int quantity : sold) {
			message.append(quantity);
			message.append(ETX);
		}
		message.append(bought.size());
		message.append(ETX);
		for(int quantity : bought) {
			message.append(quantity);
			message.append(ETX);
		}
		passMessage(message.toString());
	}

	public void endTurnMsg(int playerNum, boolean endGame) {
		passMessage(STX + END_TURN_MSG + playerNum + ETX + endGame + ETX);
	}

	public void gameOverMsg(int playerNum) {
		passMessage(STX + GAME_OVER_MSG + playerNum + ETX);
	}
}
