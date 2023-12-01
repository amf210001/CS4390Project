import java.io.*; 
import java.net.*;  
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;


public class UDPClient {
    private static boolean firstMsg = true;

    public static void main(String args[]) throws Exception 
    { 
      BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
      DatagramSocket clientSocket = new DatagramSocket();

      boolean connection = true;
  
      InetAddress IPAddress = InetAddress.getByName("localhost");

      // Gets the date/time of the connection and the start time used to calculate the total connection time
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yyyy");

      long elapsedMinutes, elapsedSeconds;
      long startTime = System.currentTimeMillis();
      LocalDateTime connectionStart = LocalDateTime.now();

      String clientName = "";
      	  
	  while(connection) {
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];

		// Starts connection by entering name
		if (firstMsg == true) {
			System.out.print("Enter a name for the client: ");
		}

		String sentence = inFromUser.readLine();

		// Sends the client's first message as the client's name
		if (firstMsg == true) {
			if (sentence.equalsIgnoreCase(""))
				sentence = "Null";

			try {
				String firstsentence = "This is the first msg " + sentence;
				sendData = firstsentence.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}

			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9877);
			clientSocket.send(sendPacket);
			  
			clientName = sentence;
			firstMsg = false;

			// Receives and prints message from server
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			String modifiedSentence = new String(receivePacket.getData(), StandardCharsets.UTF_8);

			System.out.println("FROM SERVER:" + modifiedSentence);
		} else { // if not the first message

			// if not close
			if (!sentence.toLowerCase().startsWith("close")) {
				sendData = sentence.getBytes("UTF-8");
			  	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9877);
			  	clientSocket.send(sendPacket);
				  // Receives and prints message from server
				  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				  clientSocket.receive(receivePacket);
				  String modifiedSentence = new String(receivePacket.getData(), StandardCharsets.UTF_8);
				  System.out.println("FROM SERVER:" + modifiedSentence);
				  
			} else { //if close
				// Calculate connection time
				elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
				elapsedMinutes = elapsedSeconds / 60;
				// Send close message
				sentence = sentence + " " + clientName + "; Connection Duration: " + elapsedMinutes + " minutes and " + elapsedSeconds + " seconds\n";
				sendData = sentence.getBytes("UTF-8");
			    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9877);
			    clientSocket.send(sendPacket);

				// Print closing connection details
				sentence = "Client " + clientName + " using the client port number " + sendPacket.getPort() + " has disconnected \nConnection began at "
						  + dtf.format(connectionStart) + "\nConnection lasted for " + elapsedMinutes + " minutes and " + elapsedSeconds + " seconds\n";
				sendData = sentence.getBytes("UTF-8");
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9877);
				System.out.println(sentence);

				// Client connection is closed
				clientSocket.close();
				connection = false;
			  }
		  }
	  }
	}
}
