import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private int port;

	/**
	 * 
	 * @param server
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TCPClient(String server, Integer port) throws UnknownHostException,
			IOException {
		this.server = server;
		this.port = port;
		
		this.inFromUser = new BufferedReader(new InputStreamReader(System.in));
		
	}
	
	
	
	private void openConnection() throws UnknownHostException,IOException{
		
		this.clientSocket = new Socket(server, port);
		System.out.println("Connection Established");
		this.outToServer = new DataOutputStream(clientSocket.getOutputStream());
		this.inFromServer = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));
	}
	
	private void closeConnection() throws UnknownHostException, IOException{
		this.clientSocket.close();
		this.outToServer.close();
		this.inFromServer.close();
	}
	

	boolean closeConnection = false;

	/**
	 * 
	 * @throws IOException
	 */
	public void startClient() throws IOException {
		openConnection();

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
			System.out.println("Invalid syntax: \n  <METHOD> <URL> <HTMLPROTOCOL>");
			System.out.println(sentence);
			return;
		}

		if (sentence.contains("HEAD")) {
			outToServer.writeBytes(sentence + '\n' + '\n');
			outToServer.flush();
			handleResponse("HEAD", Protocol);
		} else if (sentence.contains("GET")) {
			
			getMethod(s[1], false, Protocol);
			handleResponse("GET", Protocol);

		} else if (sentence.contains("PUT")) {

		} else if (sentence.contains("POST")) {
			System.out.print("geef content");
			String content = inFromUser.readLine();
			int length = content.length();
			outToServer.writeBytes(sentence + '\n' + "HOST: " + server + "\n" + "Content-Type: text/plain"
			+ '\n' + "Content-Length: " + length + '\n'+'\n' + content + '\n'+'\n');

		} else {
			System.out.println("Wrong HTTPCommand");
		}

	}
	private void getMethod(String url, boolean isFile, String Protocol) throws IOException{
		if(Protocol == "HTTP/1.0"){

			

				if(!isFile){
					outToServer.writeBytes("GET"+" "+url+" "+Protocol+ '\n' + '\n');
					outToServer.flush();
				}

				if(isFile){
					//stuur GET request naar server
					outToServer.writeBytes("GET" + " /" +url+ " "+Protocol+ '\n' +"Host:" + server+ '\n' + '\n');
					outToServer.flush();
					// verwerk response

					String response = inFromServer.readLine();
					String fullResponse = "";
					boolean bodystarted =false;
					while (inFromServer.ready()){
						if(response.isEmpty())
							bodystarted=true;
						if(bodystarted)
							fullResponse+=fullResponse;
						response=inFromServer.readLine();
						
					}
					System.out.print(fullResponse);
//					fullResponse = fullResponse.replaceAll("(.*?)(\\n)", "");
					byte[] b = fullResponse.getBytes();


					//slaag image op
					String[] name = url.split("/");
					FileOutputStream fos = new FileOutputStream(name[(name.length-1)]);
					fos.write(b);
					fos.close();
				}
			
		}
		
		//HTTP 1.1 :
		else{
			if(!isFile){

				if(!isFile){
					outToServer.writeBytes("GET" + " " +url+ " "+Protocol+ '\n' +"Host:" + server+ '\n' + '\n');
					outToServer.flush();
				}

				if(isFile){
					//stuur GET request naar server
					outToServer.writeBytes("GET" + " " +url+ " "+Protocol+ '\n' +"Host:" + server+ '\n' + '\n');
					outToServer.flush();
					// verwerk response

					String response = inFromServer.readLine();
					String fullResponse = "";
					while ((response=inFromServer.readLine())!=null){
						fullResponse += "\n"+response;
					}
					System.out.print(fullResponse);
					fullResponse = fullResponse.replaceAll("(.*?)(\\n)", "");
					byte[] b = fullResponse.getBytes();


					//slaag image op
					FileOutputStream fos = new FileOutputStream("C://computernetworks/" +url);
					fos.write(b);
					fos.close();
				}

			}
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
			
		} 
		else if (sentence.contains("GET")) {
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
			
			if(Protocol == "HTTP/1.0"){
				String response = inFromServer.readLine();
				System.out.println(response);
				String fullResponse = "";
				while (inFromServer.ready()) {

					response = inFromServer.readLine();
					fullResponse += "\n" + response;

				}
				System.out.println("OK");
				switch (Method) {
				case "HEAD":

					System.out.println(fullResponse);
					break;

				case "GET":


//					System.out.println(fullResponse);
					ArrayList<String> imgLink = getImgLinks(fullResponse);
					closeConnection();
					for(String s : imgLink){
						openConnection();
						getMethod(s, true, "HTTP/1.0");
						closeConnection();
					}

					break;

				}
			}
			
			if(Protocol == "HTTP/1.1"){
				
				switch (Method) {
				case "HEAD":
					String response = inFromServer.readLine();
					String fullResponse = "";
					while (inFromServer.ready()) {

						response = inFromServer.readLine();
						fullResponse += "\n" + response;

					}

					System.out.println(fullResponse);
					break;

				case "GET":
					String response1 = inFromServer.readLine();
					String fullResponse1 = "";
					while (inFromServer.ready()) {
						response1 = inFromServer.readLine();
						fullResponse1 += "\n" + response1;

					}

					System.out.println(fullResponse1);
					ArrayList<String> imgLink = getImgLinks(fullResponse1);
					for(String s : imgLink){
						
						getMethod(s, true, "1.1");
					}

					break;

				}
				
			}
			}
			
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

	

	
	private ArrayList<String> getImgLinks(String html){
		Pattern p = Pattern.compile("<img src=\"(.*?)\"");
		Matcher m = p.matcher(html);
		Matcher m2 ;
		Pattern p2 = Pattern.compile("(\")(.*)(\")");
		ArrayList<String> imgList = new ArrayList<String>();

		// links naar images worden opgeslagen in arraylist imgList
		while(m.find()) {
			String s = m.group();
			m2 = p2.matcher(s);
			while(m2.find()){
				imgList.add(m2.group().replaceAll("(\")", ""));
			}
		}
		return imgList;
		
	}

}