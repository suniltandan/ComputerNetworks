import java.io.*;
import java.net.*;

class TCPClient {
	public static void main(String argv[]) throws Exception {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		boolean read=false;
		String server=""; int port=0;
		while(!read){
			try{
			System.out.println("Enter connection server and port: \nFormat: <server> <port>");
			String[] serverPort = input.readLine().split(" ");
			server = serverPort[0];
			port = Integer.parseInt(serverPort[1]);
			read=true;
			}
			catch(Exception exp){
				System.out.println("Wrong Fromat!");
			}
		}
		TCPClient client1 = new TCPClient(server,port);
		client1.startClient();
		
	}
	
	
	private DataOutputStream outToServer;
	private BufferedReader inFromServer,inFromUser;
	private Socket clientSocket;
	/**
	 * 
	 * @param server
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TCPClient(String server, Integer port) throws UnknownHostException, IOException{
		this.inFromUser = new BufferedReader(new InputStreamReader(
				System.in));
		this.clientSocket = new Socket(server,port);
		System.out.println("Connection Established");
		this.outToServer = new DataOutputStream(
				clientSocket.getOutputStream());
		this.inFromServer = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));
	}
	boolean closeConnection=false;
	/**
	 * 
	 * @throws IOException
	 */
	public void startClient() throws IOException {
		
		while(!closeConnection){
			String sentence = inFromUser.readLine();
			if(sentence.contains("HTTP/1.0")){
				handleHTTP10(sentence);
				handleResponse();
				closeConnection=true;
			}
			else if(sentence.contains("HTTP/1.1")){
				handleHTTP11(sentence);
				handleResponse();
				sentence = inFromUser.readLine();
			}
		}
	}
	/**
	 * 
	 * @param sentence
	 * @throws IOException
	 */
	private void handleHTTP10(String sentence) throws IOException {
		outToServer.writeBytes(sentence+'\n' +'\n');
		outToServer.flush();
	
		
	}
	/**
	 * 
	 * @param sentence
	 * @throws IOException
	 */
	private void handleHTTP11(String sentence) throws IOException {
		outToServer.writeBytes(sentence+'\n' +'\n'+"Host: kissmyass.com" +'\n');
		outToServer.flush();
		
		
	}
	/**
	 * 
	 * @throws IOException
	 */
	private void handleResponse() throws IOException {
		String response = "";
		String fullResponse="";
		while ((response=inFromServer.readLine())!=null){
			fullResponse += "\n"+response;
		}
		System.out.println("FROM SERVER: " + fullResponse);
		System.out.println("Connection Status :" + !(clientSocket.isClosed()));
		
	}
	
}