import java.io.*;
import java.net.*;

class TCPServer {
	public static void main(String argv[]) throws Exception {
		ServerSocket welcomeSocket = new ServerSocket(80);
		System.out.println("TCPServer running");
		while (true) {
		
			Socket connectionSocket = welcomeSocket.accept();
			 if (connectionSocket != null)  
		       {  
		       Handler request = new Handler(connectionSocket);  
		       Thread thread = new Thread(request);  
		       thread.start(); 
		       } 
			
			
			
//			System.out.println("Connection Established");
//			BufferedReader inFromClient = new BufferedReader(
//					new InputStreamReader(connectionSocket.getInputStream()));
//			DataOutputStream outToClient = new DataOutputStream(
//					connectionSocket.getOutputStream());
//			String clientSentence = inFromClient.readLine();
//			System.out.println("Received: " + clientSentence);
//			String capsSentence = clientSentence.toUpperCase() + '\n';
//			outToClient.writeBytes(capsSentence);
		}
	}
}