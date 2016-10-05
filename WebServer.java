/**
* Project1 - WebServer simulator by Nadya Pena
*/

import java.io.*;
import java.net.*;
import java.util.*;

public final class WebServer{

	public static void main(String[] stringargs) throws Exception{

		//Set the port number
		int port_number = 5559;

		//Establish the listening (server) socket
		ServerSocket serverSocket = new ServerSocket(port_number);
                                                                                                                       
		//Process HTTP service requests in an infinite loop
		while (true){
			//Listen for a TCP connection request
			Socket connectionSocket = serverSocket.accept();	

			//Construct object to process the HTTP request message
			HttpRequest request = new HttpRequest(connectionSocket);

			//Create new thread to process the request
			Thread thread = new Thread(request);

			//Start the thread
			thread.start();
		}
	}
}

