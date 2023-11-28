import java.io.*; 
import java.net.*; 
import java.nio.charset.StandardCharsets;  
import java.util.Stack;


public class UDPServer implements Runnable {
    private DatagramPacket receivePacket;
    private DatagramSocket serverSocket;

    public UDPServer(DatagramPacket receivePacket, DatagramSocket serverSocket) {
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
    }

    public static void main(String[] args) throws Exception {
        try (DatagramSocket serverSocket = new DatagramSocket(9877)) {
            System.out.println("Server Started");

            while (true) {
                // Opens a thread for receiving packets from multiple clients
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                UDPServer obj = new UDPServer(receivePacket, serverSocket);
                Thread thread = new Thread(obj);
                thread.start();
            }
        }
    }

    // Runnable interface method
    public void run() {
        // Decodes packet received from client and removes any null characters left behind from decoding 
        String sentence = new String(receivePacket.getData(), StandardCharsets.UTF_8);
        System.out.println("Print received packet " + sentence);
        sentence = sentence.replaceAll("\0", "");
        Solver solvedExpression = new Solver(sentence);

        // if the message is not 'close': intialize variables, else: close connection
        if (!sentence.equalsIgnoreCase("close")) {
            System.out.println("Serving client from port " + receivePacket.getPort());
            byte[] sendData = new byte[1024];
            String returnMsg;
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            //  First message is an acknowledgement, the following messages are calculation results
            if (sentence.trim().startsWith("This is the first msg")) {
                String clientName = sentence.substring("This is the first msg".length()).trim();
                returnMsg = "CONNECTION ACKNOWLEDGED FOR " + clientName + "\nEnter a math equation using numbers and only these characters (+, -, /, *, (, ), ) \nOR 'close' to close the connection";
            } else {
                returnMsg = solvedExpression.evaluate();
                //returnMsg = "Invalid input. Please enter a valid math equation or 'close' to close the connection.";
            }


            // Send the response
            try {
                sendData = returnMsg.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

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

    //Response helper method for an expression
