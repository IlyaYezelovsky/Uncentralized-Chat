import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Client {
	
	// UI Components
	private JTextField ipField;
	private JTextField portField;
	private JTextField nameField;
	private JTextField outgoingField;
	private JTextArea incomingArea;
	
	// Network Components
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;

	public static void main(String[] args) {
		Client client = new Client();
		client.start();
	}

	private void start() {
		JFrame frame = new JFrame("Uncentralized Chat Client v0.1.0");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();

		// Create labels
		JLabel ipLabel = new JLabel("Server IP");
		JLabel portLabel = new JLabel("Server port");
		JLabel usernameLabel = new JLabel("Username");

		// Create input fields with default values
		ipField = new JTextField(20);
		ipField.setText("127.0.0.1");
		
		portField = new JTextField(20);
		portField.setText("14134");
		
		nameField = new JTextField(20);
		nameField.setText("IlyaYezelovsky");

		// Create buttons
		JButton joinButton = new JButton("Join");
		JButton exitButton = new JButton("Exit");

		// Add components to panel
		panel.add(ipLabel);
		panel.add(ipField);
		panel.add(Box.createVerticalGlue());
		
		panel.add(portLabel);
		panel.add(portField);
		panel.add(Box.createVerticalGlue());
		
		panel.add(usernameLabel);
		panel.add(nameField);
		panel.add(Box.createVerticalGlue());
		
		panel.add(joinButton);
		joinButton.addActionListener(new JoinListener());
		
		panel.add(exitButton);
		exitButton.addActionListener(new ExitListener());

		frame.getContentPane().add(panel);
		frame.setSize(300, 200);
		frame.setVisible(true);
	}

	private void join(String ip, int port) throws UnknownHostException, IOException {
		socket = new Socket(ip, port);
		InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
		reader = new BufferedReader(streamReader);
		writer = new PrintWriter(socket.getOutputStream());
	}

	private class ExitListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			System.exit(0);
		}
	}

	private class JoinListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			try {
				join(ipField.getText(), Integer.parseInt(portField.getText()));
				openChatRoom();
			} catch (NumberFormatException e) {
				showMessageDialog("Port must be an integer within 0 ~ 65535", "Error");
			} catch (UnknownHostException e) {
				showMessageDialog("Cannot connect to the server", "Error");
			} catch (IOException e) {
				showMessageDialog("Cannot connect to the server", "Error");
			}
		}
	}

	private static void showMessageDialog(String msg, String title) {
		JFrame frame = new JFrame(title);
		JPanel panel = new JPanel();
		JLabel message = new JLabel(msg);
		JButton button = new JButton("OK");
		
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				frame.dispose();
			}
		});
		
		panel.add(message);
		panel.add(button);
		frame.getContentPane().add(panel);
		frame.setSize(300, 100);
		frame.setVisible(true);
	}

	private void openChatRoom() {
		JFrame chatFrame = new JFrame("Chatroom on " + ipField.getText() + ":" + portField.getText());
		JPanel chatPanel = new JPanel();
		
		incomingArea = new JTextArea(30, 50);
		incomingArea.setLineWrap(true);
		incomingArea.setWrapStyleWord(true);
		incomingArea.setEditable(false);
		
		JScrollPane scroller = new JScrollPane(incomingArea);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		outgoingField = new JTextField(20);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendListener());
		
		chatPanel.add(scroller);
		chatPanel.add(outgoingField);
		chatPanel.add(sendButton);
		
		Thread readThread = new Thread(new IncomingReader());
		readThread.start();
		
		chatFrame.getContentPane().add(BorderLayout.CENTER, chatPanel);
		chatFrame.setSize(800, 1000);
		chatFrame.setVisible(true);
	}

	private class SendListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent ev) {
			String msg = "<" + nameField.getText() + "> " + outgoingField.getText();
			send(msg);
			outgoingField.setText("");
			outgoingField.requestFocus();
		}
	}

	private class IncomingReader implements Runnable {
		@Override
		public void run() {
			String msg;
			try {
				while ((msg = reader.readLine()) != null) {
					incomingArea.append(msg + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void send(String msg) {
		try {
			writer.println(msg);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
