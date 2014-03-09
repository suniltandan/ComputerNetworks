package test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class HttpTester {
	private static DataOutputStream outToServer;
	private static BufferedReader inFromServer,inFromUser;
	private static Socket clientSocket;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args){
		try {
			inFromUser = new BufferedReader(new InputStreamReader(
				System.in));
	
			clientSocket = new Socket("www.google.com",80);
			System.out.println("Connection Established");
			outToServer = new DataOutputStream(
					clientSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			ReceivedThread rt = new ReceivedThread();
			Thread th = new Thread(rt);
			th.start();
			String toSend=inFromUser.readLine();
			while(!toSend.toLowerCase().contains("exit")){
				System.out.println("Sending: "+toSend);
				String server= "www.google.com";
				outToServer.writeBytes(toSend +'\n'+"HOST:"+server+'\n'+'\n');
				outToServer.flush();
				System.out.println("Message Sent, Connection Status:"+clientSocket.isBound());
				toSend=inFromUser.readLine();
				if(toSend.toLowerCase().contains("connect")){
					clientSocket = new Socket("google.com",80);
					System.out.println("Connection Established");
					outToServer = new DataOutputStream(
							clientSocket.getOutputStream());
					inFromServer = new BufferedReader(new InputStreamReader(
							clientSocket.getInputStream()));
					toSend=inFromUser.readLine();
				}
			}

			
			
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static class ReceivedThread implements Runnable{

		@Override
		public void run() {
			try {
				String received = HttpTester.inFromServer.readLine();
				while(received!=null){
					System.out.println(received);
					received = inFromServer.readLine();
				}
				System.out.println("End of incoming message");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
	}

}
