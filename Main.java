package ilya.chat;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class Main {
	
	private JFrame frame;
	private JPanel panel;
	private JTextField usernameField;
	private JTextField ipField;
	
	public static void main(String[] args) {
		Main m = new Main();
		m.showGUI();
	}
	
	void showGUI() {
		frame = new JFrame("Uncentralized Chat Client v0.2.0");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JPanel usernamePanel = new JPanel();
		JPanel ipPanel = new JPanel();
		JPanel portPanel = new JPanel();
		
		JLabel usernameLabel = new JLabel("Username");
		JLabel ipLabel = new JLabel("Server IP");
		
		usernameField = new JTextField(20);
		ipField = new JTextField(20);
		
		usernamePanel.add(usernameLabel);
		usernamePanel.add(usernameField);
		
		ipPanel.add(ipLabel);
		ipPanel.add(ipField);
		
		panel.add(usernamePanel);
		panel.add(ipPanel);
		
		JPanel buttonPanel = new JPanel();
		JButton joinButton = new JButton("Join");
		JButton exitButton = new JButton("Exit");
		
		joinButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				join();
			}
		});
		
		exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				System.exit(0);
			}
		});
		
		buttonPanel.add(joinButton);
		buttonPanel.add(exitButton);
		panel.add(buttonPanel);
		
		frame.getContentPane().add(BorderLayout.CENTER, panel);
		frame.setSize(300, 150);
		frame.setVisible(true);
	}
	
	private String getIP() {
		return ipField.getText().split(":")[0];
	}
	
	private int getPort() {
		return Integer.parseInt(ipField.getText().split(":")[1]);
	}
	
	void join() {
		if (!usernameField.getText().isBlank()) {
			Chatroom room = new Chatroom(ipField.getText(), usernameField.getText());
			try {
				room.join(getIP(), getPort());
			} catch (ArrayIndexOutOfBoundsException e) {
				Msgbox.error("Not a valid server IP");
			} catch (NumberFormatException e) {
				Msgbox.error("Not a valid server IP");
			} catch (Exception e) {
				Msgbox.errorWithStackTrace(e);
			}
		} else {
			Msgbox.info("Username cannot be empty");
		}
	}

}
