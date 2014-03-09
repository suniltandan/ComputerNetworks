import java.net.*;

class TCPServer {
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