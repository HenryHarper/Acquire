public abstract class MessageHandler extends Thread {
	private Board board;
	protected String message;
	protected int b;
	protected int c;

	MessageHandler(Board board) {
		this.board = board;
	}

	String nextArg() {
		String nextArg = message.substring(b, c);
		b = c + 1;
		c = message.indexOf(MessageSender.ETX, b);
		return nextArg;
	}

	String readString() {
		return nextArg();
	}

	int readInt() {
		return Integer.parseInt(nextArg());
	}

	boolean readBoolean() {
		return Boolean.parseBoolean(nextArg());
	}

	Tile readTile() {
		int row = Integer.parseInt(nextArg());
		int col = Integer.parseInt(nextArg());
		return board.getTile(row, col);
	}

	Corporation readCorp() {
		int corpNum = Integer.parseInt(nextArg());
		return board.getCorporation(corpNum);
	}

	@Override
	public abstract void run();

	public abstract void close();
}
