package ilya.chat;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Chatroom {
	
//	UI components
	private JFrame frame;
	private JPanel panel;
	private JTextArea msgArea;
	private JTextField inputField;
	private JTextArea onlineArea;
	
//	Network components
	private String serverIP;
	private Socket sock;
	private BufferedReader reader;
	private PrintWriter writer;
	private InputStreamReader streamReader;
	
//	Client arguments
	private String username;
	
	Chatroom(String ip, String name) {
		serverIP = ip;
		username = name;
	}
	
//	For test
	public static void main(String[] args) {
		Chatroom room = new Chatroom("127.0.0.1:48700", "IlyaYezelovsky");
		room.showGUI();
	}
	
	void showGUI() {
		frame = new JFrame("Uncentralized Chatroom on " + serverIP);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		msgArea = new JTextArea(30, 100);
		msgArea.setLineWrap(true);
		msgArea.setWrapStyleWord(true);
		msgArea.setEditable(false);
		
		JScrollPane scroller = new JScrollPane(msgArea);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		inputField = new JTextField(25);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				send();
			}
		});
		
		JButton exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				quit();
				frame.dispose();
			}
		});
		
		JPanel sendPanel = new JPanel();
		sendPanel.add(inputField);
		sendPanel.add(sendButton);
		sendPanel.add(exitButton);
		
		panel.add(scroller);
		panel.add(sendPanel);
		
//		TODO add online player display
//		onlineArea = new JTextArea(5, 30);
//		onlineArea.setLineWrap(false);
//		onlineArea.setWrapStyleWord(false);
//		onlineArea.setEditable(false);
//		
//		JScrollPane onlineScroller = new JScrollPane(onlineArea);
//		onlineScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
//		onlineScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		
//		JPanel onlinePanel = new JPanel();
//		onlinePanel.add(onlineScroller);
		
		frame.getContentPane().add(BorderLayout.CENTER, panel);
//		frame.getContentPane().add(BorderLayout.EAST, onlinePanel);
		frame.setSize(600, 580);
		frame.setVisible(true);
	}
	
	void join(String ip, int port) {
		try {
			sock = new Socket(ip, port);
			streamReader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(streamReader);
			writer = new PrintWriter(sock.getOutputStream());
			showGUI();
			Thread readThread = new Thread(new IncomingReader());
			readThread.start();
		} catch (UnknownHostException e) {
			Msgbox.error("Unknown host.");
		} catch (IOException e) {
			Msgbox.error("Failed establishing connection.");
		} catch (NumberFormatException e) {
			Msgbox.error("Not a valid server IP.");
		} catch (Exception e) {
			Msgbox.errorWithStackTrace(e);
		}
	}
	
	void quit() {
	    try {
	        if (writer != null) writer.close();
	        if (reader != null) reader.close();
	        if (sock != null && !sock.isClosed()) sock.close();
	    } catch (IOException e) {
	        Msgbox.errorWithStackTrace(e);
	    }
	}
	
	void send() {
		if (!inputField.getText().isBlank()) {
			String msg = "<" + username + "> " + inputField.getText();
			try {
				writer.println(msg);
				writer.flush();
			} catch (Exception e) {
				Msgbox.errorWithStackTrace(e);
			} finally {
				inputField.setText("");
				inputField.requestFocus();
			} 
		} else {
			Msgbox.info("Message cannot be empty");
		}
	}
	
	private class IncomingReader implements Runnable {
		@Override
		public void run() {
			String msg;
			try {
				while ((msg = reader.readLine()) != null) {
					msgArea.append(msg + "\n");
				}
			} catch (IOException e) {
				Msgbox.errorWithStackTrace(e);
			}
		}
	}

}
