import java.io.*;
import java.net.*;

import test.HttpTester;

class TCPClient {
	public static void main(String argv[]) throws Exception {
		BufferedReader input = new BufferedReader(new InputStreamReader(
				System.in));
		boolean read = false;
		String server = "";
		int port = 0;
		while (!read) {
			try {
				System.out
						.println("Enter connection server and port: \nFormat: <server> <port>");
				String[] serverPort = input.readLine().split(" ");
				server = serverPort[0];
				port = Integer.parseInt(serverPort[1]);
				read = true;
			} catch (Exception exp) {
				System.out.println("Wrong Fromat!");
			}
		}
		TCPClient client1 = new TCPClient(server, port);
		client1.startClient();

	}

	private DataOutputStream outToServer;
	public BufferedReader inFromServer, inFromUser;
	private Socket clientSocket;
	private String server;

	/**
	 * 
	 * @param server
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TCPClient(String server, Integer port) throws UnknownHostException,
			IOException {
		this.inFromUser = new BufferedReader(new InputStreamReader(System.in));
		this.clientSocket = new Socket(server, port);
		System.out.println("Connection Established");
		this.server = server;
		this.outToServer = new DataOutputStream(clientSocket.getOutputStream());
		this.inFromServer = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));
	}

	boolean closeConnection = false;

	/**
	 * 
	 * @throws IOException
	 */
	public void startClient() throws IOException {

		while (!closeConnection) {
			String sentence = inFromUser.readLine();
			if (sentence.contains("HTTP/1.0")) {
				handleHTTP10(sentence);
				closeConnection = true;
			} else if (sentence.contains("HTTP/1.1")) {
				handleHTTP11(sentence);
			} else {
				System.out.println("Wrong Format");
			}
		}
	}

	/**
	 * 
	 * @param sentence
	 * @throws IOException
	 */
	private void handleHTTP10(String sentence) throws IOException {
		String Protocol = "HTTP/1.0";
		String[] s = sentence.split(" ");
		if (s.length != 3) {
			System.out
					.println("Invalid syntax: \n  <METHOD> <URL> <HTMLPROTOCOL>");
			System.out.println(sentence);
			return;
		}

		if (sentence.contains("HEAD")) {
			outToServer.writeBytes(sentence + '\n' + '\n');
			outToServer.flush();
			handleResponse("HEAD", Protocol);
		} else if (sentence.contains("GET")) {
			getMethod(s[2], false, Protocol);
			handleResponse("GET", Protocol);

		} else if (sentence.contains("PUT")) {

		} else if (sentence.contains("POST")) {

		} else {
			System.out.println("Wrong HTTPCommand");
		}

	}
	private void getMethod(String url, boolean isFile, String Protocol) throws IOException{
		if(!isFile){
			outToServer.writeBytes("GET"+url+" "+Protocol+ '\n' + '\n');
			outToServer.flush();
		}
	}

	/**
	 * 
	 * @param sentence
	 * @throws IOException
	 */
	private void handleHTTP11(String sentence) throws IOException {
		String[] s = sentence.split(" ");
		if (s.length != 3) {
			System.out
					.println("Invalid syntax: \n <METHOD> <URL> <HTMLPROTOCOL>");
			System.out.println(sentence);
			return;
		}
		if (sentence.contains("HEAD")) {
			outToServer.writeBytes(sentence + '\n' + '\n' + "Host:" + server
					+ '\n');
			System.out.println(sentence);
			outToServer.flush();
			handleResponse("HEAD", "HTTP/1.1");
		} else if (sentence.contains("GET")) {
			outToServer.writeBytes(sentence + '\n' + "HOST:" + server + '\n'
					+ '\n');
			outToServer.flush();
			System.out.println(sentence);
			handleResponse("GET", "HTTP/1.1");
		} else if (sentence.contains("PUT")) {

		} else if (sentence.contains("POST")) {

		}

	}

	/**
	 * 
	 * @param string
	 * @param string2
	 * @throws IOException
	 */
	private void handleResponse(String Method, String Protocol) {
		try {
			String response = inFromServer.readLine();
			String fullResponse = "";
			while (response!= null) {
				fullResponse += "\n" + response;
				response = inFromServer.readLine();
			}
			System.out.println("OK");
			switch (Method) {
			case "HEAD":

				System.out.println(fullResponse);
				break;

			case "GET":

				System.out.println(fullResponse);
				break;

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}