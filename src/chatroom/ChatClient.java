package chatroom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.lang.String;
/**
 * Swarup Sahu
 */
public class ChatClient extends ChatWindow {

	// Inner class used for networking
	private Communicator comm;

	// GUI Objects
	private JTextField serverTxt;
	private JTextField nameTxt;
	private JButton connectB;
	private JTextField messageTxt;
	private JButton sendB;

	public ChatClient(){
		super();
		this.setTitle("Chat Client");
		printMsg("Chat Client Started.");

		// GUI elements at top of window
		// Need a Panel to store several buttons/text fields
		serverTxt = new JTextField("localhost");
		serverTxt.setColumns(15);
		nameTxt = new JTextField("Name");
		nameTxt.setColumns(10);
		connectB = new JButton("Connect");
		JPanel topPanel = new JPanel();
		topPanel.add(serverTxt);
		topPanel.add(connectB);
		topPanel.add(nameTxt);
		contentPane.add(topPanel, BorderLayout.NORTH);

		// GUI elements and panel at bottom of window
		messageTxt = new JTextField("");
		messageTxt.setColumns(40);
		sendB = new JButton("Send");
		JPanel botPanel = new JPanel();
		botPanel.add(messageTxt);
		botPanel.add(sendB);
		contentPane.add(botPanel, BorderLayout.SOUTH);

		// Resize window to fit all GUI components
		this.pack();

		// Setup the communicator so it will handle the connect button
		Communicator comm = new Communicator();
		connectB.addActionListener(comm);
		sendB.addActionListener(comm);

	}

	/** This inner class handles communication with the server. */
	class Communicator implements ActionListener {
		private Socket socket;
		private PrintWriter writer;
		private BufferedReader reader;
		private int port = 2050;
		protected String name = nameTxt.getText();
		protected String tempName = name; 

		@Override
		public void actionPerformed(ActionEvent actionEvent) {		

			if(actionEvent.getActionCommand().compareTo("Connect") == 0) {
				connect();
				name = nameTxt.getText();
				printMsg("Hi " + name + ", you have just connected to the ChatServer.");
			}
			else if(actionEvent.getActionCommand().compareTo("Send") == 0) {
				tempName = name; 
				if (messageTxt.getText().length() > 5 && messageTxt.getText().substring(0, 5).equals("/name")) {
					String name = messageTxt.getText().substring(6, messageTxt.getText().length());
					nameTxt.setText(name); 
					sendMsg(" is changing their name to " + name, tempName);
				} else {
					sendMsg(messageTxt.getText(), name);
				}
			} 
		} 

		/** Connect to the remote server and setup input/output streams. */
		public void connect(){
			try {
				socket = new Socket(serverTxt.getText(), port);
				InetAddress serverIP = socket.getInetAddress();
				printMsg("Connection made to " + serverIP);
				writer = new PrintWriter(socket.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				name = nameTxt.getText();
				sendMsg("has joined the chatroom.", name); 

				/** Create a new thread that will stay open to continously 
				read outputStreams from the server. **/				
				ReadHandler r = new ReadHandler(socket, reader);
				Thread t = new Thread(r); 
				t.start();
			}
			catch(IOException e) {
				printMsg("\nERROR:" + e.getLocalizedMessage() + "\n");
			}
		}
		/** Receive and display a message */
		public void readMsg() throws IOException {
			String s = reader.readLine();
			printMsg(s);
		}
		/** Send a string */
		public void sendMsg(String s, String name){
			writer.println(name + ": " + s);
		}
	
		class ReadHandler implements Runnable { 
			private Socket socket;
			private BufferedReader reader; 

			/** Constructor to take in open socket 
			and reader. **/
			public ReadHandler (Socket socket, BufferedReader reader) {
				socket = socket;
				reader = reader;
			}

			public void run() {
				// Keep listening for messages from server.
				while (true) {
					try {
						readMsg();					
					} 
					catch (IOException e) { 
						e.printStackTrace();
					} 
					catch (NullPointerException e) {
						break;
					}
				}				
			}
		}
	}

	public static void main(String args[]){
		new ChatClient();
	}

}
