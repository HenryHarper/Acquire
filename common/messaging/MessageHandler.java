package common.messaging;
import common.pojos.Board;
import common.pojos.Corporation;
import common.pojos.Tile;

public abstract class MessageHandler extends Thread {
	private Board board;
	protected String message;
	protected int b;
	protected int c;

	protected MessageHandler(Board board) {
		this.board = board;
	}

	protected String nextArg() {
		String nextArg = message.substring(b, c);
		b = c + 1;
		c = message.indexOf(MessageSender.ETX, b);
		return nextArg;
	}

	protected String readString() {
		return nextArg();
	}

	protected int readInt() {
		return Integer.parseInt(nextArg());
	}

	protected boolean readBoolean() {
		return Boolean.parseBoolean(nextArg());
	}

	protected Tile readTile() {
		int row = Integer.parseInt(nextArg());
		int col = Integer.parseInt(nextArg());
		return board.getTile(row, col);
	}

	protected Corporation readCorp() {
		int corpNum = Integer.parseInt(nextArg());
		return board.getCorporation(corpNum);
	}

	@Override
	public abstract void run();

	public abstract void close();
}
