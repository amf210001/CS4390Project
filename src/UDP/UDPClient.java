import java.io.*; 
import java.net.*; 
  
public class UDPClient {
    public static void main(String args[]) throws Exception 
    { 
      BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
      DatagramSocket clientSocket = new DatagramSocket();

	  boolean connection = true;
  
      InetAddress IPAddress = InetAddress.getByName("localhost");

	  while(connection) {
		  byte[] sendData = new byte[1024];
		  byte[] receiveData = new byte[1024];

		  String sentence = inFromUser.readLine();

		  sendData = sentence.getBytes();

		  DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9877);

		  clientSocket.send(sendPacket);
		  if(!sentence.equalsIgnoreCase("close")) {
			  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			  clientSocket.receive(receivePacket);

			  String modifiedSentence = new String(receivePacket.getData());

			  System.out.println("FROM SERVER:" + modifiedSentence);
		  } else{
			  clientSocket.close();
			  connection = false;
		  }
	  }
	}
}