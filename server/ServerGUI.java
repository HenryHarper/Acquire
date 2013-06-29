package server;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.ServerSocket;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ServerGUI extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static final int panelLw = 40;
	private static final int panelCw = 160;
	private static final int panelRw = 40;
	private static final int totalWidth = panelLw + panelCw + panelRw;
	private static final int totalHeight = 150;

	private Container content;
	private JButton closeButton;
	private JTextField ipLabel;
	private JPanel panelL;
	private JPanel panelC;
	private JPanel panelR;

	private ServerSocket socket;
	private ServerConnectionManager connectionManager;

	public ServerGUI(ServerSocket socket, String globalip) {
		super("Server");

		this.socket = socket;

		setSize(totalWidth, totalHeight);
		setResizable(false);

		content = getContentPane();
		content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));

		panelL = new JPanel();
		panelL.setPreferredSize(new Dimension(panelLw, totalHeight));

		panelC = new JPanel();
		panelC.setPreferredSize(new Dimension(panelCw, totalHeight));
		panelC.setLayout(new GridLayout(5, 1));

		panelR = new JPanel();
		panelR.setPreferredSize(new Dimension(panelRw, totalHeight));

		ipLabel = new JTextField(globalip);
		ipLabel.setBorder(null);
		ipLabel.setOpaque(false);
		ipLabel.setEditable(false);
		ipLabel.setHorizontalAlignment(SwingConstants.CENTER);

		closeButton = new JButton("Close Server");
		closeButton.setFocusPainted(false);
		closeButton.addActionListener(this);

		panelC.add(new JPanel());
		panelC.add(ipLabel);
		panelC.add(new JPanel());
		panelC.add(closeButton);
		panelC.add(new JPanel());
		content.add(panelL);
		content.add(panelC);
		content.add(panelR);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				close();
			}
		});

		System.out.println("ServerGUI: launching ServerConnectionManager");
		connectionManager = new ServerConnectionManager(socket);
		connectionManager.start();
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == closeButton) {
			close();
		}
	}

	public void close() {
		System.out.println("ServerGUI: closing");
		connectionManager.close();
		try {
			socket.close();
		} catch(Exception e) {
			System.out.println("ServerGUI: unable to close socket");
		}

		this.setVisible(false);
		this.dispose();
	}
}
