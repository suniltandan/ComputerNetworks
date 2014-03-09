import java.io.*;

import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

class Handler implements Runnable {
	static final String HTML_START = "<html>"
			+ "<title>JAVA HTTP Server</title>" + "<body>";

	static final String HTML_END = "</body>" + "</html>";
	static final String htmlFolder = "./webpage/";
	Socket connectedClient = null;
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;

	public Handler(Socket client) {
		connectedClient = client;
	}

	private boolean closeConnection = false;

	@SuppressWarnings("deprecation")
	public void run() {
		try {
			System.out.println("The Client " + connectedClient.getInetAddress()
					+ ":" + connectedClient.getPort() + " is connected");

			inFromClient = new BufferedReader(new InputStreamReader(
					connectedClient.getInputStream()));
			outToClient = new DataOutputStream(
					connectedClient.getOutputStream());
			while (!closeConnection) {
				String requestString = inFromClient.readLine();
				System.out.println(requestString);
				String[] s = requestString.split(" ");
				if (s.length != 3) {
					sendResponse(400, "BAD REQUEST", false);
					closeConnection = true;
				} else {
					String httpMethod = s[0];
					String httpQueryString = s[1];
					String httpProtocol = s[2];
					if (httpProtocol.equals("HTTP/1.0"))
						respondHTTP10(httpMethod, httpQueryString);
					if (httpProtocol.equals("HTTP/1.1"))
						respondHTTP11(httpMethod, httpQueryString);
				}
			}
			outToClient.close();
			inFromClient.close();
			connectedClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void respondHTTP11(String httpMethod, String httpQueryString)
			throws Exception {
		System.out.println("The HTTP request string is ....");
		String requestString = "";
		String body = "";
		boolean bodystarted = false;
		boolean containsHost = false;
		while (inFromClient.ready()) {
			// Read the HTTP complete HTTP Query
			// responseBuffer.append(requestString + "<BR>");
			System.out.println(requestString);
			requestString = inFromClient.readLine();
			if(requestString.toLowerCase().contains("connection")){
				if(requestString.toLowerCase().contains("close"))
					closeConnection=true;
			}
			if(requestString.toLowerCase().contains("host"))
				containsHost=true;
			if (requestString.isEmpty())
				bodystarted = true;
			if (bodystarted)
				body += requestString + '\n';
		}
		if(!containsHost){
			sendResponse(400, "BAD REQUEST", false);
			closeConnection=true;
			return;
		}

		if (httpMethod.equals("GET")) {
			if (httpQueryString.equals("/")) {
				byte[] bytes = Files.readAllBytes(Paths.get(htmlFolder
						+ "index.html"));
				String text = new String(bytes, StandardCharsets.UTF_8);
				sendResponse(200, text, false);
			} else {
				// This is interpreted as a file name
				String fileName = httpQueryString.replaceFirst("/", "");
				fileName = (htmlFolder + fileName);
				if (new File(fileName).isFile()) {
					sendResponse(200, fileName, true);
				} else {
					sendResponse(
							404,
							"<b>The Requested resource not found ...."
									+ "Usage: http://127.0.0.1:5000 or http://127.0.0.1:5000/</b>",
							false);
				}
			}
		} else if (httpMethod.equals("PUT")) {

		} else if (httpMethod.equals("POST")) {

		}

		else
			sendResponse(
					404,
					"<b>The Requested resource not found ...."
							+ "Usage: http://127.0.0.1:5000 or http://127.0.0.1:5000/</b>",
					false);

	}

	/**
	 * Method to handle the http 1.0 reqest
	 * 
	 * @param httpMethod
	 *            Kind of method GET/PUT/POST
	 * @param httpQueryString
	 *            URL
	 * @throws Exception
	 */
	private void respondHTTP10(String httpMethod, String httpQueryString)
			throws Exception {
		System.out.println("The HTTP request string is ....");
		String requestString = "";
		while (inFromClient.ready()) {
			// Read the HTTP complete HTTP Query
			System.out.println(requestString);
			requestString = inFromClient.readLine();
		}

		if (httpMethod.equals("GET")) {
			if (httpQueryString.equals("/")) {
				byte[] bytes = Files.readAllBytes(Paths.get(htmlFolder
						+ "index.html"));
				String text = new String(bytes, StandardCharsets.UTF_8);
				sendResponse(200, text, false);
			} else {
				// This is interpreted as a file name
				String fileName = httpQueryString.replaceFirst("/", "");
				fileName = (htmlFolder + fileName);
				if (new File(fileName).isFile()) {
					sendResponse(200, fileName, true);
				} else {
					sendResponse(
							404,
							"<b>The Requested resource not found ...."
									+ "Usage: http://127.0.0.1:5000 or http://127.0.0.1:5000/</b>",
							false);
				}
			}
		} else if (httpMethod.equals("POST")) {

		} else if (httpMethod.equals("POST")) {

		}

		else
			sendResponse(
					404,
					"<b>The Requested resource not found ...."
							+ "Usage: http://127.0.0.1:5000 or http://127.0.0.1:5000/</b>",
					false);
		this.closeConnection = true;
	}

	/**
	 * Sends the response
	 * 
	 * @param statusCode
	 * @param responseString
	 * @param isFile
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
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		else if (statusCode == 404)
			statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
		else if (statusCode == 400)
			statusLine = "HTTP/1.1 400 Bad Request" + "\r\n";
		else if (statusCode == 500)
			statusLine = "HTTP/1.1 500 Internal Server Error" + "\r\n";
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
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");

		if (isFile)
			sendFile(fin, outToClient);
		else
			outToClient.writeBytes(responseString);

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
