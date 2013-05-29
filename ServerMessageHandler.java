import java.io.DataInputStream;
import java.net.Socket;

public class ServerMessageHandler extends Thread {
	private GameFlowController gameFlowController;
	private Socket clientSocket;
	private DataInputStream in;
	private boolean cont;
	private int clientPlayerNum;

	private String message;
	private int b;
	private int c;

	public ServerMessageHandler(GameFlowController gameFlowController, int playerNum, Socket clientSocket) {
		this.gameFlowController = gameFlowController;
		this.clientPlayerNum = playerNum;
		this.clientSocket = clientSocket;

		try {
			in = new DataInputStream(clientSocket.getInputStream());
		} catch(Exception e) {
			System.out.println("ServerMessageHandler: unable to open data stream");
			close();
		}

		cont = true;
	}

	private String nextArg() {
		String nextArg = message.substring(b, c);
		b = c + 1;
		c = message.indexOf(MessageSender.ETX, b);
		return nextArg;
	}

	@Override
	public void run() {
		int msgsize;
		byte[] bytes = new byte[MessageSender.BUFFERSIZE];
		while(cont) {
			try {
				Thread.sleep(100);
			} catch(Exception e) {
				System.out.println("ServerMessageHandler: " + e.toString());
			}

			try {
				msgsize = in.read(bytes, 0, in.available());
			} catch(Exception e) {
				System.out.println("ServerMessageHandler: unable to read from input stream");
				msgsize = -1;
			}

			if(msgsize < 0) {
				close();
			} else if(msgsize > 0) {
				message = new String(bytes, 0, msgsize);
				int a = message.indexOf(MessageSender.STX, 0);
				while(a >= 0) {
					try {
						b = a + 3;
						c = message.indexOf(MessageSender.ETX, b);
						String type = message.substring(a + 1, b);
						switch(type) {
							case MessageSender.PLAYER_NAMES_MSG: // playerNamesMsg(String playerName)
								gameFlowController.receivedPlayerName(nextArg());
								break;

							case MessageSender.PLACE_TILES_MSG: // placeTileMsg(int playerNum, int row, int col, int corporation)
								int row = Integer.parseInt(nextArg());
								int col = Integer.parseInt(nextArg());
								gameFlowController.tilePlayed(clientPlayerNum, row, col);
								break;

							case MessageSender.CHAT_MSG: // chatMsg(String message)
								gameFlowController.chatMessage(clientPlayerNum, nextArg());
								break;

							case MessageSender.START_GAME_MSG: // startGameMsg(int playerNum, List<String> playerNames)
								gameFlowController.startGame();
								break;

							case MessageSender.CREATE_CORP_MSG: // createCorporationMsg(int playerNum, Corporation toCreate)
								int playerNum = Integer.parseInt(nextArg());
								int toCreate = Integer.parseInt(nextArg());
								gameFlowController.createCorporation(playerNum, toCreate);
								break;

							case MessageSender.KILL_MSG: // killPromptResponseMsg(int killIndex)
								gameFlowController.killCorporation(Integer.parseInt(nextArg()));
								break;

							case MessageSender.TSK_MSG: // tskMsg(int playerNum, int corporation, int traded, int sold, int kept)
								playerNum = Integer.parseInt(nextArg());
								int corporation = Integer.parseInt(nextArg());
								int traded = Integer.parseInt(nextArg());
								int sold = Integer.parseInt(nextArg());
								int kept = Integer.parseInt(nextArg());
								gameFlowController.playerTSK(playerNum, corporation, traded, sold, kept);
								break;

							case MessageSender.SB_MSG: // sbMsg(int playerNum, int[] sold, int[] bought)
								playerNum = Integer.parseInt(nextArg());
								int[] soldList = new int[Integer.parseInt(nextArg())];
								for(int i = 0; i < soldList.length; i++) {
									soldList[i] = Integer.parseInt(nextArg());
								}
								int[] boughtList = new int[Integer.parseInt(nextArg())];
								for(int i = 0; i < boughtList.length; i++) {
									boughtList[i] = Integer.parseInt(nextArg());
								}
								gameFlowController.playerSB(playerNum, soldList, boughtList);
								break;

							case MessageSender.END_TURN_MSG: // endTurnMsg(int playerNum, boolean endGame, int turnPlayerNum)
								playerNum = Integer.parseInt(nextArg());
								boolean endGame = Boolean.parseBoolean(nextArg());
								gameFlowController.endTurn(playerNum, endGame);
								break;

							default:
								break;
						}
						a = message.indexOf(MessageSender.STX, c);
					} catch(Exception e) {
						System.out.println("ServerMessageHandler: unable to parse message");
					}
				}
			}
		}
	}

	public void setPlayerNum(int playerNum) {
		this.clientPlayerNum = playerNum;
	}

	public void close() {
		System.out.println("ServerMessageHandler: closing");
		cont = false;
		try {
			clientSocket.close();
		} catch(Exception e) {}
	}
}
