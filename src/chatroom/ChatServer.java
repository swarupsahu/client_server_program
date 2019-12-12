package chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Swarup Sahu
 */
public class ChatServer extends ChatWindow {

	private ClientHandler handler;
	private ArrayList<Thread> listThread = new ArrayList <Thread> (); 
	private ArrayList<ClientHandler> listClients = new ArrayList <ClientHandler> ();
	static int threadClientCounter = 0; 
	static String message;

	public ChatServer(){
		super();
		this.setTitle("Chat Server");
		this.setLocation(80,80);

		try {
			// Create a listening service for connections
			// at the designated port number.
			ServerSocket srv = new ServerSocket(2050);

			while (true) {
				// The method accept() blocks until a client connects.
				printMsg("Waiting for a connection");
				Socket socket = srv.accept();

				// Create new client handlers after connection is accepted.
				handler = new ClientHandler(socket);
				listClients.add(handler);

				// Create new threads for each ClientHandler				
				listThread.add(new Thread(listClients.get(threadClientCounter))); 

				listThread.get(threadClientCounter).start(); 

				threadClientCounter++;
			}
		} 
		catch (IOException e) {
			System.out.println(e);
		}
		catch(NullPointerException e)
        {
        	System.out.print("NullPointerException caught");
        }
	}

	/** This inner class handles communication to/from one client. */
	class ClientHandler implements Runnable {
		private PrintWriter writer;
		private BufferedReader reader;

		public ClientHandler(Socket socket) {
			try {
				InetAddress serverIP = socket.getInetAddress();
				printMsg("Connection made to " + serverIP);
				writer = new PrintWriter(socket.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			}
			catch (IOException e){
					printMsg("\nERROR:" + e.getLocalizedMessage() + "\n");
			}
			catch(NullPointerException e)
        	{
            	System.out.print("NullPointerException caught");
        	}
		}
		public void run() {
			try {
				while(true) {
					// read a message from the client
					// send the read message back to client					
					message = readMsg();
					if (message.contains("has joined the chatroom.")) {
						sendMsg(message);
					} 
					else if (message != null) { 
						sendMsgAll(message);
					}
				}
			}
			catch (IOException e){
				printMsg("\nERROR:" + e.getLocalizedMessage() + "\n");
			}
			catch(NullPointerException e)
        	{
            	System.out.print("NullPointerException caught");
        	}
		}

		/** Receive and display a message */
		public String readMsg() throws IOException {
			String s = reader.readLine();
			printMsg(s);
			return(s);
		}
		/** Send a string */
		public void sendMsg(String s){
			writer.println(s);
		}

		public void sendMsgAll(String s){
			for (int i = 0; i < threadClientCounter; i++) {
				listClients.get(i).writer.println(s);
			}
		}
	}

	public static void main(String args[]){
		new ChatServer();
	}
}
