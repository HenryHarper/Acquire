import java.io.DataInputStream;
import java.net.Socket;
import java.util.List;

import com.google.common.collect.Lists;

public class ServerMessageHandler extends MessageHandler {
	private GameFlowController gameFlowController;
	private Socket clientSocket;
	private DataInputStream in;
	private boolean cont;
	private int clientPlayerNum;

	public ServerMessageHandler(GameFlowController gameFlowController, int playerNum, Socket clientSocket) {
		super(gameFlowController.getBoard());
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

	public void setPlayerNum(int playerNum) {
		this.clientPlayerNum = playerNum;
	}

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
							case MessageSender.PLAYER_NAMES_MSG:
								gameFlowController.receivedPlayerName(readString());
								break;

							case MessageSender.PLACE_TILE_MSG:
								readInt();
								gameFlowController.tilePlayed(clientPlayerNum, readTile());
								break;

							case MessageSender.CHAT_MSG:
								gameFlowController.chatMessage(clientPlayerNum, readString());
								break;

							case MessageSender.START_GAME_MSG:
								gameFlowController.startGame();
								break;

							case MessageSender.CREATE_CORP_MSG:
								gameFlowController.createCorporation(readInt(), readCorp());
								break;

							case MessageSender.KILL_MSG:
								gameFlowController.killCorporation(readInt());
								break;

							case MessageSender.TSK_MSG:
								gameFlowController.playerTSK(readInt(), readCorp(), readInt(), readInt(), readInt());
								break;

							case MessageSender.SB_MSG:
								int playerNum = readInt();
								List<Integer> sold = Lists.newArrayListWithCapacity(readInt());
								for(int i = 0; i < sold.size(); i++) {
									sold.add(readInt());
								}
								List<Integer> bought = Lists.newArrayListWithCapacity(readInt());
								for(int i = 0; i < bought.size(); i++) {
									bought.add(readInt());
								}
								gameFlowController.playerSB(playerNum, sold, bought);
								break;

							case MessageSender.END_TURN_MSG:
								gameFlowController.endTurn(readInt(), readBoolean());
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

	public void close() {
		System.out.println("ServerMessageHandler: closing");
		cont = false;
		try {
			clientSocket.close();
		} catch(Exception e) {}
	}
}
