import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class TCPClient {
	public static void main(String argv[]){
		try{
		BufferedReader input = new BufferedReader(new InputStreamReader(
				System.in));
		boolean read = false;
		String uri = "";
		String server="";
		String resourse="";
		String HTTPCommand = "";
		String HTTPversion = "";
		int port = 80;
		if(argv.length == 4){
			uri = argv[1];
			HTTPCommand = argv[0];
			HTTPversion = argv[3];
			port = Integer.parseInt(argv[2]);
			
			
		}
		else if(argv.length == 3){
			uri = argv[1];
			HTTPCommand = argv[0];
			HTTPversion = argv[2];
		}
		else{
			System.out.println("Bad Format");
			return;
		}
		
		String[] serverarray = uri.split("/");
		if(serverarray[0].toLowerCase().equals("http:")){
			server = serverarray[1];
			if(serverarray.length>2){
				for(int i = 2; i<serverarray.length;i++){
					resourse +=("/" + serverarray[i]);
				}
			}
			if(serverarray.length==2){
				resourse ="/";
			}
		}
		else{
			server= serverarray[0];
			if(serverarray.length>1){
				for(int i = 1; i<serverarray.length;i++){
					resourse +=("/" + serverarray[i]);
				}
			}
			if(serverarray.length==1){
				resourse ="/";
			}
		}
		
		
		if(HTTPversion.equals("1.0")|| HTTPversion.equals("HTTP/1.0")){
			HTTPversion = "HTTP/1.0";
		}
		else if (HTTPversion.equals("1.1")|| HTTPversion.equals("HTTP/1.1")){
			HTTPversion = "HTTP/1.1";
		}
		else {
			System.out.println("could not detect HTTP/version");
			return;
		}
		if(HTTPCommand.equals("GET")||HTTPCommand.equals("PUT")||HTTPCommand.equals("POST")||HTTPCommand.equals("HEAD")){
			TCPClient client1 = new TCPClient(server, port);
			client1.startClient(HTTPCommand,resourse, HTTPversion);
		}
		else {
			System.out.println("command not implemented");
		}

		}
		catch(NumberFormatException e){
			System.out.println("Could not parse port;");
		}

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
	public TCPClient(String server, Integer port){
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
	 * @param hTTPversion 
	 * @param hTTPCommand 
	 * @param hTTPversion2 
	 * @throws IOException
	 */
	public void startClient(String hTTPCommand, String resource, String hTTPversion){
		try {
			openConnection();
			String sentence = hTTPCommand + " "+ resource + " " + hTTPversion ;
			while (!closeConnection) {
				
				if (sentence.contains("HTTP/1.0")) {
					handleHTTP10(sentence);
					System.out.println("HTTP/1.0, connection closed.");
					closeConnection = true;
				} else if (sentence.contains("HTTP/1.1")) {
					handleHTTP11(sentence);
					if(!closeConnection){
					System.out.println("HTTP/1.1 connection is still open, type http request");
					sentence = inFromUser.readLine();
					}
					
				} else {
					System.out.println("Wrong Format");
					System.out.println("HTTP/1.1 connection is still open, type http request");
					sentence = inFromUser.readLine();
				}
			}
			closeConnection();
		} catch (UnknownHostException e) {
			System.out.println("could not connect to host");
			return;
		
		} catch (IOException e) {
			System.out.println("connection closed");
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

			System.out.print("Give Body\n");
			String content = inFromUser.readLine();
			int length = content.length();
			outToServer.writeBytes(sentence + '\n' + "HOST: " + server + "\n" + "Content-Type: text/plain"
					+ '\n' + "Content-Length: " + length + '\n'+'\n' + content + '\n'+'\n');
			
			handleResponse("HEAD", Protocol);

		} else if (sentence.contains("POST")) {
			System.out.print("Give Body\n");
			String content = inFromUser.readLine();
			int length = content.length();
			outToServer.writeBytes(sentence + '\n' + "HOST: " + server + "\n" + "Content-Type: text/plain"
					+ '\n' + "Content-Length: " + length + '\n'+'\n' + content + '\n'+'\n');
			
			handleResponse("HEAD", Protocol);

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
				System.out.println("getting image " + url);
				//stuur GET request naar server
				outToServer.writeBytes("GET" + " /" +url+ " "+Protocol+ '\n' +"Host:" + server+ '\n' + '\n');
				outToServer.flush();
				// verwerk response

				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String response = inFromServer.readLine();
				String fullResponse = "";
				boolean bodystarted =false;
				while (!bodystarted){
					response=inFromServer.readLine();
					if(response.isEmpty())
						bodystarted=true;

				}

				//					BufferedImage image = ImageIO.read(this.clientSocket.getInputStream());
				//				G	String[] name = url.split("/");
				//					ImageIO.write(image, "name", new FileOutputStream(name[(name.length-1)]));

				String[] name = url.split("/");
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(name[(name.length-1)])); 
				int i = 0;  
				while ((i = inFromServer.read()) != -1) {  

					out.write(i);  
				}  
				out.flush();  

				out.close(); 

				//					fullResponse = fullResponse.replaceAll("(.*?)(\\n)", "");
				//					byte[] b = fullResponse.getBytes();
				//
				//
				//					//slaag image op
				//					String[] name = url.split("/");
				//					FileOutputStream fos = new FileOutputStream(name[(name.length-1)]);
				//					fos.write(b);
				//					fos.close();
			}

		}

		//HTTP 1.1 :
		else{

				if(!isFile){
					outToServer.writeBytes("GET" + " " +url+ " "+Protocol+ '\n' +"Host:" + server+ '\n' + '\n');
					outToServer.flush();
				}

				if(isFile){
					String[] name = url.split("/");
					System.out.println("getting image " + url);
					//stuur GET request naar server
					outToServer.writeBytes("GET" + " /" +url+ " "+Protocol+ '\n' +"Host:" + server+ '\n' + '\n');
					outToServer.flush();
					// verwerk response

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String response = inFromServer.readLine();
					String fullResponse = "";
					boolean bodystarted =false;
					String contentLength ="";
					String body= "";
					while (inFromServer.ready() && !bodystarted) {
						//System.out.println(response);
						response = inFromServer.readLine();
	
						if(response.toLowerCase().contains("content-length"))
							contentLength = response;
				
						if (response.isEmpty())
							bodystarted = true;

					}
					String[] s= contentLength.split(": ");
					int length = Integer.parseInt(s[1]);
					char[] charArray = new char[length];
					inFromServer.read(charArray);
					body = new String(charArray);
					
					//					BufferedImage image = ImageIO.read(this.clientSocket.getInputStream());
					//					String[] name = url.split("/");
					//					ImageIO.write(image, "name", new FileOutputStream(name[(name.length-1)]));

					
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(name[(name.length-1)])); 
					out.write(body.getBytes());
					out.flush();

					out.close(); 

					//					fullResponse = fullResponse.replaceAll("(.*?)(\\n)", "");
					//					byte[] b = fullResponse.getBytes();
					//
					//
					//					//slaag image op
					//					String[] name = url.split("/");
					//					FileOutputStream fos = new FileOutputStream(name[(name.length-1)]);
					//					fos.write(b);
					//					fos.close();
				}

			
		}

	}


	/**
	 * 
	 * @param sentence
	 * @throws IOException
	 */
	private void handleHTTP11(String sentence) throws IOException {
		String Protocol = "HTTP/1.1";
		String[] s = sentence.split(" ");
		if (s.length != 3) {
			System.out
					.println("Invalid syntax: \n <METHOD> <URL> <HTMLPROTOCOL>");
			System.out.println(sentence);
			return;
		}
		if (sentence.contains("HEAD")) {
			outToServer.writeBytes(sentence + '\n' + "Host:" + server
					+ '\n' + '\n');
			System.out.println(sentence);
			outToServer.flush();
			handleResponse("HEAD", "HTTP/1.1");
			
		} 
		else if (sentence.contains("GET")) {
//			outToServer.writeBytes(sentence + '\n' + "HOST:" + server + '\n'
//					+ '\n');
//			outToServer.flush();
//			System.out.println(sentence);
			getMethod(s[1], false, Protocol);
			handleResponse("GET", "HTTP/1.1");
			
			
		} else if (sentence.contains("PUT")) {
			System.out.print("Give Body" + '\n');
			String content = inFromUser.readLine();
			int length = content.length();
			outToServer.writeBytes(sentence + '\n' + "HOST: " + server + "\n" + "Content-Type: text/plain"
			+ '\n' + "Content-Length: " + length + '\n'+'\n' + content + '\n'+'\n');
			
			handleResponse("HEAD", "HTTP/1.1");

		} else if (sentence.contains("POST")) {
			
			System.out.print("Give Body" + '\n');
			String content = inFromUser.readLine();
			int length = content.length();
			outToServer.writeBytes(sentence + '\n' + "HOST: " + server + "\n" + "Content-Type: text/plain"
			+ '\n' + "Content-Length: " + length + '\n'+'\n' + content + '\n'+'\n');
			
			handleResponse("HEAD", "HTTP/1.1");


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
			Thread.sleep(15);
			if(Protocol == "HTTP/1.0"){
				String response = inFromServer.readLine();
				System.out.println(response);
				String fullResponse = response;
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


				System.out.println(fullResponse);
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
				String response = inFromServer.readLine();
				String fullResponse = response;
				while (inFromServer.ready()){

					response = inFromServer.readLine();
					fullResponse += "\n" + response;

				}
				
				if(fullResponse.toLowerCase().contains("connection: close")){
					this.closeConnection = true;
				}
				
				switch (Method) {
				case "HEAD":
					

					System.out.println(fullResponse);
					break;

				case "GET":

					System.out.println(fullResponse);
					ArrayList<String> imgLink = getImgLinks(fullResponse);
					for(String s : imgLink){
						
						getMethod(s, true, "HTTP/1.1");
					}

					break;

				}
				
			}
			
		}
			
			catch (Exception e) {
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