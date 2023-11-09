import java.io.*; 
import java.net.*; 
  
public class UDPServer implements Runnable {
    private DatagramPacket receivePacket;
    private DatagramSocket serverSocket;
    public UDPServer(DatagramPacket receivePacket, DatagramSocket serverSocket){
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
    }
    public static void main(String[] args) throws Exception {
        try (DatagramSocket serverSocket = new DatagramSocket(9877)) {

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                UDPServer obj = new UDPServer(receivePacket, serverSocket);
                Thread thread = new Thread(obj);
                thread.start();
                }
            }
      }
    public void run() {
        String sentence = new String(receivePacket.getData());
        //System.out.println(sentence);
        if (!sentence.equalsIgnoreCase("close")) {
            System.out.println("Serving client from port " + receivePacket.getPort());
            byte[] sendData = new byte[1024];

            InetAddress IPAddress = receivePacket.getAddress();

            int port = receivePacket.getPort();

            String capitalizedSentence = sentence.toUpperCase();

            sendData = capitalizedSentence.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);

            try {
                serverSocket.send(sendPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Client from port " + receivePacket.getPort() + " has closed their connection.");
        }
    }
}
