import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import client.ClientGUI;

import common.messaging.MessageSender;

import server.ServerGUI;

public class MainGUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static final int PORT = 8888;

	private static final int panelLw = 40;
	private static final int panelCw = 160;
	private static final int panelRw = 40;
	private static final int totalWidth = panelLw + panelCw + panelRw;
	private static final int totalHeight = 150;
	private static final int paddingA = 4;
	private static final int paddingB = 40;

	private Container content;
	private JButton clientButton;
	private JButton serverButton;
	private JPanel panelL;
	private JPanel panelC;
	private JPanel panelR;

	private String globalip;

	public static void main(String[] args) {
		JFrame frame = new MainGUI();
		frame.setVisible(true);
	}

	public MainGUI() {
		super("Acquire");

		setSize(totalWidth, totalHeight);
		setResizable(false);
		setLocation(paddingA, paddingA);

		content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));

		panelL = new JPanel();
		panelL.setPreferredSize(new Dimension(panelLw, totalHeight));

		panelC = new JPanel();
		panelC.setPreferredSize(new Dimension(panelCw, totalHeight));
		panelC.setLayout(new GridLayout(5, 1));

		panelR = new JPanel();
		panelR.setPreferredSize(new Dimension(panelRw, totalHeight));

		clientButton = new JButton("Join");
		clientButton.setFocusPainted(false);
		clientButton.addActionListener(this);

		serverButton = new JButton("Host");
		serverButton.setFocusPainted(false);
		serverButton.addActionListener(this);

		panelC.add(new JPanel());
		panelC.add(clientButton);
		panelC.add(new JPanel());
		panelC.add(serverButton);
		panelC.add(new JPanel());
		content.add(panelL);
		content.add(panelC);
		content.add(panelR);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				if(JOptionPane.showConfirmDialog(content, "Closing this window will close all open games.", "Close", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
					System.exit(0);
				}
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == clientButton) {
			boolean clientError = false;

			globalip = JOptionPane.showInputDialog(this, "IPofHost:");

			if(globalip != null) {
				try {
					System.out.println("MainGUI: launching client");
					Socket csocket = new Socket(globalip, PORT);
					csocket.setReceiveBufferSize(MessageSender.BUFFERSIZE);
					csocket.setSendBufferSize(MessageSender.BUFFERSIZE);
					DataInputStream in = new DataInputStream(csocket.getInputStream());
					MessageSender out = new MessageSender(new DataOutputStream(csocket.getOutputStream()));
	      			String name = getPlayerName();
					if(name.compareTo("") == 0) {
						csocket.close();
					} else {
						out.playerNamesMsg(name);
						ClientGUI clnt = new ClientGUI(csocket, in, out, false);
		      			clnt.setLocation(paddingA + totalWidth + paddingB, paddingA);
						clnt.setVisible(true);
					}
				} catch(Exception e) {
					System.out.println("MainGUI: unable to connect to server");
					clientError = true;
				}
			}

			if(clientError) {
				JOptionPane.showMessageDialog(this, "Unable to connect to server.\n\nPlease try another server or try again later.", "Acquire", JOptionPane.PLAIN_MESSAGE);
			}
		} else if(ae.getSource() == serverButton) {
			boolean clientError = false;
			boolean serverError = false;

			try {
				try {
					BufferedReader urlReader = new BufferedReader(new FileReader("src/IP Lookup URL"));
					URL autoIP = new URL(urlReader.readLine());
					urlReader.close();
					URLConnection connection = autoIP.openConnection();
				    connection.addRequestProperty("Protocol", "Http/1.1");
				    connection.addRequestProperty("Connection", "keep-alive");
				    connection.addRequestProperty("Keep-Alive", "1000");
				    connection.addRequestProperty("User-Agent", "Web-Agent");
					BufferedReader buff = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					globalip = buff.readLine().trim();
					System.out.println("MainGUI: launching server at " + InetAddress.getByName(globalip).getHostName() + "(" + globalip + ")");
				} catch(Exception e) {
					globalip = "<unknown>";
				}
				ServerSocket socket = new ServerSocket(PORT);
				socket.setReceiveBufferSize(MessageSender.BUFFERSIZE);
				ServerGUI server = new ServerGUI(socket, "IP:  " + globalip);
				server.setLocation(paddingA, paddingA + totalHeight + paddingB);
				server.setVisible(true);

				try {
					System.out.println("MainGUI: launching client");
					Socket csocket = new Socket(InetAddress.getLocalHost().getHostAddress(), PORT);
					csocket.setReceiveBufferSize(MessageSender.BUFFERSIZE);
					csocket.setSendBufferSize(MessageSender.BUFFERSIZE);
					DataInputStream in = new DataInputStream(csocket.getInputStream());
					MessageSender out = new MessageSender(new DataOutputStream(csocket.getOutputStream()));
					String name = getPlayerName();
					if(name.compareTo("") == 0) {
						csocket.close();
						server.close();
					} else {
						out.playerNamesMsg(name);
						ClientGUI clnt = new ClientGUI(csocket, in, out, true);
	    	  			clnt.setLocation(paddingA + totalWidth + paddingB, paddingA);
						clnt.setVisible(true);
					}
				} catch(Exception e) {
					System.out.println("MainGUI: unable to connect to server");
					server.close();
					clientError = true;
				}
			} catch(Exception exception) {
				System.out.println("MainGUI: unable to open server socket");
				serverError = true;
			}

			if(serverError) {
				JOptionPane.showMessageDialog(this, "Unable to launch server.\n\nPlease try again later.", "Acquire", JOptionPane.PLAIN_MESSAGE);
			} else if(clientError) {
				JOptionPane.showMessageDialog(this,"Unable to connect to server.\n\nPlease try again later.", "Acquire", JOptionPane.PLAIN_MESSAGE);
			}
		}
	}

	private String getPlayerName() {
		String name = JOptionPane.showInputDialog(this, "Player Name:");
		while(!nameComplies(name)) {
			JOptionPane.showMessageDialog(this, "Player name can only contain letters and numbers");
			name = JOptionPane.showInputDialog(this, "Player Name:");
		}
		return name;
	}

	private boolean nameComplies(String name) {
		if(name.length() == 0) {
			return false;
		}
		for(int i = 0; i < name.length(); i++) {
			if(!Character.isLetterOrDigit(name.codePointAt(i))) {
				return false;
			}
		}
		return true;
	}
}
