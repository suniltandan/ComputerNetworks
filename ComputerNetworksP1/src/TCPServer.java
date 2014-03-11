import java.net.*;

class TCPServer {
	/**
	 * The main method that gets started when this server is started. 
	 * It makes a new serversocket in port 80(default for http clients).
	 * If someone connects then it makes a new handler thread to handle the client. 
	 * This way we can handle multiple clients.
	 * @param argv
	 * @throws Exception
	 */
	public static void main(String argv[]) throws Exception {
		@SuppressWarnings("resource")
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
		}
	}
}