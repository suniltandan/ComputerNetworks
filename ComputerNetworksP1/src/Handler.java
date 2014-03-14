import java.io.*;

import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

class Handler implements Runnable {
	static final String HTML_START = "<html>"
			+ "<title>JAVA HTTP Server</title>" + "<body>";

	static final String HTML_END = "</body>" + "</html>";
	static final String htmlFolder = "./webpage/";
	Socket connectedClient = null;
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;

	/**
	 * Constructor for this handler. Gets the client and saves it.
	 * 
	 * @param client
	 */
	public Handler(Socket client) {
		connectedClient = client;
	}

	private String Protocol;
	private boolean closeConnection = false;

	/**
	 * If the request string has bad format then sends a bad request and closes
	 * connection. Else initiates respond methods to process the other
	 * informations.
	 */
	public void run() {
		try {
			String client = connectedClient.getInetAddress()+":"+connectedClient.getPort();
			System.out.println("The Client " + connectedClient.getInetAddress()
					+ ":" + connectedClient.getPort() + " is connected");

			inFromClient = new BufferedReader(new InputStreamReader(
					connectedClient.getInputStream()));
			outToClient = new DataOutputStream(
					connectedClient.getOutputStream());
			while (!closeConnection) {
//				parseRequests();
				String requestString = inFromClient.readLine();
				
				if (requestString == null)
					break;
				System.out.println("\n\n\n"+requestString);
				String[] s = requestString.split(" ");
				if (s.length != 3) {
					sendResponse(400, "BAD REQUEST", false);
					closeConnection = true;
				} else {
					String httpMethod = s[0];
					String httpQueryString = s[1];
					httpQueryString = URLDecoder.decode(httpQueryString, "UTF-8");
					Protocol = s[2];
					if (Protocol.equals("HTTP/1.0")
							|| Protocol.equals("HTTP/1.1")) {
						respond(httpMethod, httpQueryString);
					}
					// if (Protocol.equals("HTTP/1.0"))
					// respondHTTP10(httpMethod, httpQueryString);
					// else if (Protocol.equals("HTTP/1.1"))
					// respondHTTP11(httpMethod, httpQueryString);
					else {
						sendResponse(400, "BAD REQUEST", false);
						closeConnection = true;
					}
				}
			}
			outToClient.close();
			inFromClient.close();
			connectedClient.close();
			System.out.println("Connection closed with "+client);
		} catch (Exception e) {
		System.out.println("Connection closed");	
		}
	}
	ArrayList<String> Requests= new ArrayList<>();
	private void parseRequests() throws Exception{
		String requestString = inFromClient.readLine();
		if (requestString == null){
			closeConnection=true;
			return;
		}
		System.out.println(requestString);
		String[] s = requestString.split(" ");
		if (s.length != 3) {
			sendResponse(400, "BAD REQUEST", false);
			closeConnection = true;
		} else {
			String httpMethod = s[0];
			String httpQueryString = s[1];
			httpQueryString = URLDecoder.decode(httpQueryString, "UTF-8");
			Protocol = s[2];
			if (Protocol.equals("HTTP/1.0")
					|| Protocol.equals("HTTP/1.1")) {
				respond(httpMethod, httpQueryString);
			}
			// if (Protocol.equals("HTTP/1.0"))
			// respondHTTP10(httpMethod, httpQueryString);
			// else if (Protocol.equals("HTTP/1.1"))
			// respondHTTP11(httpMethod, httpQueryString);
			else {
				sendResponse(400, "BAD REQUEST", false);
				closeConnection = true;
			}
		}
	}

	/**
	 * This is the method that shall respond to protocol HTTP/1.1. It is
	 * possible to combine both protocols but to make it easier this has been
	 * implemented separately.
	 * 
	 * @param httpMethod
	 *            GET/HEAD/POST/PUT
	 * @param httpQueryString
	 *            URL
	 * @throws Exception
	 */
	private void respond(String httpMethod, String httpQueryString)
			throws Exception {
		System.out.print("\nThe HTTP request string is ....");
		String requestString = "";
		String body = "";
		boolean bodystarted = false;
		boolean containsHost = false;
		String connectionHeader = null;
		// Read the complete HTTP Query
		if (!httpMethod.equals("POST") && !httpMethod.equals("PUT")) {
			while (inFromClient.ready()) {
				System.out.println(requestString);
				requestString = inFromClient.readLine();
				// Default connection type for http1.1 is to keep it alive.
				if (requestString.toLowerCase().contains("connection")) {
					connectionHeader = requestString;
				}
				if (requestString.toLowerCase().contains("host"))
					containsHost = true;
				if (bodystarted)
					body += requestString + '\n';
				if (requestString.isEmpty())
					bodystarted = true;

			}
		}
		else{
			String contentLength ="";
			while (inFromClient.ready() && !bodystarted) {
				System.out.println(requestString);
				requestString = inFromClient.readLine();
				// Default connection type for http1.1 is to keep it alive.
				if (requestString.toLowerCase().contains("connection")) {
					connectionHeader = requestString;
				}
				if(requestString.toLowerCase().contains("content-length"))
					contentLength = requestString;
				if (requestString.toLowerCase().contains("host"))
					containsHost = true;
				if (requestString.isEmpty())
					bodystarted = true;

			}
			String[] s= contentLength.split(": ");
			int length = Integer.parseInt(s[1]);
			char[] charArray = new char[length];
			inFromClient.read(charArray);
			while(inFromClient.ready())
				inFromClient.read();
			body = new String(charArray);
			body = URLDecoder.decode(body, "UTF-8");
		
			}
		System.out.println("Body:" + body);
		// If the http1.1 request does not contain HOST header then send a bad
		// request and close the connection.
		if (Protocol.equals("HTTP/1.1") && !containsHost) {
			sendResponse(400, "BAD REQUEST", false);
			closeConnection = true;
			return;
		}
		/**
		 * This looks if the connection has to be closed or not. HTTP/1.1
		 * Default case: keep-alive HTTP/1.0 Default case: close Unless given
		 * otherwise. If it is neither then sends a bad request and closes the
		 * connection.
		 */
		if (Protocol.equals("HTTP/1.1") && connectionHeader != null) {
			if (connectionHeader.toLowerCase().contains("close"))
				closeConnection = true;
			else if (!connectionHeader.toLowerCase().contains("keep-alive")) {
				sendResponse(400, "BAD REQUEST", false);
				closeConnection = true;
				return;
			}
		} else if (Protocol.equals("HTTP/1.0")) {
			closeConnection = true;
			if (connectionHeader != null
					&& connectionHeader.toLowerCase().contains("keep-alive"))
				closeConnection = false;
			else if (connectionHeader != null
					&& !connectionHeader.toLowerCase().contains("close")) {
				sendResponse(400, "BAD REQUEST", false);
				closeConnection = true;
				return;
			}
		}

		/**
		 * This block handles the different methods.
		 */
		if (httpMethod.equals("GET")) {
			if (httpQueryString.equals("/")) {
				String fileName = (htmlFolder + "index.html");
				if (new File(fileName).isFile()) {
					sendResponse(200, fileName, true);
				sendResponse(200, fileName, true);
				}
			} else {
				// This is interpreted as a file name
				String fileName = httpQueryString.replaceFirst("/", "");
				fileName = (htmlFolder + fileName);
				if (new File(fileName).isFile()) {
					sendResponse(200, fileName, true);
				} else {
					sendResponse(404,
							"<b>The Requested resource not found ....</b>",
							false);
				}
			}
		} else if (httpMethod.equals("PUT")) {
			File textFile = new File("PutFrom" + connectedClient.getPort()
					+ ".txt");
			textFile.createNewFile();
			FileOutputStream fout = new FileOutputStream(textFile);
			fout.write(body.getBytes());
			fout.flush();
			fout.close();
			sendResponse(200, textFile.getName(), true);
			System.out.println("Response sent to put method");

		} else if (httpMethod.equals("POST")) {
			File textFile = new File("PostFrom" + connectedClient.getPort()
					+ ".txt");
			textFile.createNewFile();
			FileOutputStream fout = new FileOutputStream(textFile);
			fout.write(body.getBytes());
			fout.flush();
			fout.close();
			sendResponse(200, textFile.getName(), true);
			System.out.println("Response sent to post method");
		}
		// This is for the methods that have not yet been implemented.
		else if (httpMethod.equals("DELETE")||httpMethod.equals("TRACE")||httpMethod.equals("CONNECT")||httpMethod.equals("OPTIONS")) {
			sendResponse(500,
					"Server Error, This kind of request is not implemented.",
					false);
		}

		else {
			sendResponse(400, "BAD REQUEST", false);
			closeConnection = true;
		}

	}

	/**
	 * Sends the response
	 * 
	 * @param statusCode
	 *            Code for the status 200 OK|400|404|500
	 * @param responseString
	 * 
	 * @param isFile
	 *            If the response is a file.
	 * @throws Exception
	 */
	private void sendResponse(int statusCode, String responseString,
			boolean isFile) throws Exception {

		String statusLine = null;
		String serverdetails = "Server: Java Awesome Server\n";
		String contentLengthLine = null;
		String fileName = null;
		String contentTypeLine = "Content-Type: text/html" + "\r\n";
		FileInputStream fin = null;

		if (statusCode == 200)
			statusLine = Protocol + " 200 OK" + "\r\n";
		else if (statusCode == 404)
			statusLine = Protocol + " 404 Not Found" + "\r\n";
		else if (statusCode == 400)
			statusLine = Protocol + " 400 Bad Request" + "\r\n";
		else if (statusCode == 500)
			statusLine = Protocol + " 500 Internal Server Error" + "\r\n";
		if (isFile) {
			fileName = responseString;
			fin = new FileInputStream(fileName);
			contentLengthLine = "Content-Length: "
					+ Integer.toString(fin.available()) + "\r\n";
			if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
				contentTypeLine = "Content-Type: \r\n";

		} else {
			responseString = HTML_START + responseString + HTML_END;
			contentLengthLine = "Content-Length: " + responseString.length()
					+ "\r\n";
		}

		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		if(closeConnection)
			outToClient.writeBytes("Connection: close\r\n");
		else
			outToClient.writeBytes("Connection: keep-alive\r\n");
		outToClient.writeBytes("\r\n");

		if (isFile) {
			sendFile(fin, outToClient);

		} else
			outToClient.writeBytes(responseString +"\r\n");
		outToClient.flush();

	}

	/**
	 * Method for sending file.
	 * 
	 * @param fin
	 * @param out
	 * @throws Exception
	 */
	private void sendFile(FileInputStream fin, DataOutputStream out)
			throws Exception {
		byte[] buffer = new byte[1024];
		int bytesRead;

		while ((bytesRead = fin.read(buffer)) != -1) {
			out.write(buffer, 0, bytesRead);
		}
		fin.close();
	}
}
