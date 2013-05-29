import java.io.DataOutputStream;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import com.google.common.collect.Lists;

public class ServerConnectionManager extends Thread {
	private GameFlowController gameFlowController;
	private ServerSocket socket;
	private List<ServerMessageHandler> messageHandlers;
	private boolean cont;

	public ServerConnectionManager(ServerSocket socket) {
		gameFlowController = new GameFlowController(this);
		this.socket = socket;
		messageHandlers = Lists.<ServerMessageHandler>newArrayListWithCapacity(GameFlowController.MAX_PLAYERS);
		cont = true;
	}

	@Override
	public void run() {
		while(cont) {
			try {
				Thread.sleep(200);
			} catch(Exception e) {}

			try {
				Socket csocket = socket.accept();
				MessageSender out = new MessageSender(new DataOutputStream(csocket.getOutputStream()));
				int index = gameFlowController.playerJoined(out);
				ServerMessageHandler messageHandler = new ServerMessageHandler(gameFlowController, index, csocket);
				messageHandler.start();
				messageHandlers.add(messageHandler);
				cont = (messageHandlers.size() < GameFlowController.MAX_PLAYERS);
				System.out.println("ServerConnectionManager: handling client at " + csocket.getInetAddress().getHostAddress());
			} catch(Exception e) {
				System.out.println("ServerConnectionManager: " + e.toString());
			}
		}
	}

	public void setAccept(boolean accept) {
		if(!accept) {
			System.out.println("ServerConnectionManager: no longer accepting connections");
		}
		this.cont = accept;
	}

	public int getNumClients() {
		return messageHandlers.size();
	}

	public void close() {
		System.out.println("ServerConnectionManager: closing");
		cont = false;
		for(ServerMessageHandler messageHandler : messageHandlers) {
			try {
				messageHandler.close();
			} catch(Exception e) {}
		}
	}
}
