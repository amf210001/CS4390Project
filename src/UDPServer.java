import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UDPServer implements Runnable {
    private DatagramPacket receivePacket;
    private DatagramSocket serverSocket;
    private Date connectionStartTime;
    private Map<Integer, String> portToClientName = new HashMap<>();
    private String clientName;

    public UDPServer(DatagramPacket receivePacket, DatagramSocket serverSocket) {
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
        this.connectionStartTime = new Date();
    }

    public static void main(String[] args) throws Exception {
        try (DatagramSocket serverSocket = new DatagramSocket(9877)) {
            System.out.println("Server Started");

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
        String sentence = new String(receivePacket.getData(), StandardCharsets.UTF_8);
        System.out.println("Print received packet " + sentence);
        sentence = sentence.replaceAll("\0", "");
        Solver solvedExpression = new Solver(sentence);

        if (!sentence.equalsIgnoreCase("close")) {
            System.out.println("Serving client from port " + receivePacket.getPort());
            byte[] sendData = new byte[1024];
            String returnMsg;
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            if (sentence.trim().startsWith("This is the first msg")) {
                clientName = sentence.substring("This is the first msg".length()).trim();
                portToClientName.put(port, clientName);
                returnMsg = "CONNECTION ACKNOWLEDGED FOR " + clientName + "\nEnter a math equation using numbers and only these characters (+, -, /, *, (, ), ) \nOR 'close' to close the connection";

                // Log connection details and start time to a file in the "logs" folder
                logConnectionDetails(clientName, port);
            } else {
                returnMsg = solvedExpression.evaluate();
            }

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
            // Calculate and log the duration of the connection
            long duration = new Date().getTime() - connectionStartTime.getTime();
            String clientName = portToClientName.get(receivePacket.getPort());
            System.out.println(connectionStartTime);
            System.out.println(duration);
            System.out.println(clientName);
            logConnectionDuration(clientName, duration);

            System.out.println("Client from port " + receivePacket.getPort() + " has closed their connection.");
        }
    }

    private void logConnectionDetails(String clientName, int port) {
        String logFileName = "logs/" + clientName + "_log.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFileName, true))) {
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(now);

            writer.println("Port Number: " + port);
            writer.println("Time of Connection: " + formattedDate);
            writer.println("Connection Duration: Not closed yet"); 
            // Placeholder, actual value logged when closed
            // Add other log details as needed

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logConnectionDuration(String clientName, long duration) {
        String logFileName = "logs/" + clientName + "_log.txt";
        System.out.println(clientName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFileName, true))) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String formattedDuration = timeFormat.format(new Date(duration));
            writer.println("Connection Duration: " + formattedDuration);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
