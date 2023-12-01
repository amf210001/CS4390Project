import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UDPServer implements Runnable {
    private DatagramPacket receivePacket;
    private DatagramSocket serverSocket;
    //private Map<Integer, String> portToClientName = new HashMap<>();
    private String clientName;

    public UDPServer(DatagramPacket receivePacket, DatagramSocket serverSocket) {
        this.receivePacket = receivePacket;
        this.serverSocket = serverSocket;
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
        // Print received packet
        String sentence = new String(receivePacket.getData(), StandardCharsets.UTF_8);
        System.out.println("Packet received: " + sentence);
        sentence = sentence.replaceAll("\0", "");
        Solver solvedExpression = new Solver(sentence);

        // if msg does not begin with 'close'
        if (!sentence.toLowerCase().startsWith("close")) {
            // Initialize msg info
            System.out.println("Serving client from port " + receivePacket.getPort());
            byte[] sendData = new byte[1024];
            String returnMsg;
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            // if first msg
            if (sentence.trim().startsWith("This is the first msg")) {
                //parse client name, respond and log
                clientName = sentence.substring("This is the first msg".length()).trim();
                returnMsg = "CONNECTION ACKNOWLEDGED FOR " + clientName + "\nEnter a math equation using numbers and only these characters (+, -, /, *, (, ), ) \nOR 'close' to close the connection";
                logConnectionDetails(clientName, port);
            } else {//if not the first msg, evaluate the math equation
                returnMsg = solvedExpression.evaluate();
            }

            // send the response
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

        } else {//if msg begins with 'close'
            // Parse client name and duration of the connection for logging
            Pair<String, String> result = parseSentence(sentence);
            // Check if parsing was successful
            if (result != null) {
                //System.out.println("Name: " + result.getFirst());
                //System.out.println("Duration: " + result.getSecond());
                clientName = result.getFirst();
                String duration = result.getSecond();
                //log
                logConnectionDuration(clientName, duration);
            } else {
                System.out.println("Error with logging the following connection close.");
            }

            System.out.println("Client from port " + receivePacket.getPort() + " has closed their connection.");
        }
    }


    // Parsing closing message for logging
    public static Pair<String, String> parseSentence(String inputSentence) {
        Pattern pattern = Pattern.compile("^close\\s+([^;]+);\\s*(.*)$");
        Matcher matcher = pattern.matcher(inputSentence);
        // Check if the pattern matches
        if (matcher.find()) {
            // Extract the name and duration
            String name = matcher.group(1);
            String duration = matcher.group(2).trim();
            // Return the results as a Pair
            return new Pair<>(name, duration);
        } else {
            return null;
        }
    }

    // A simple Pair class to hold two values
    static class Pair<T, U> {
        private final T first;
        private final U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }
        public T getFirst() {
            return first;
        }
        public U getSecond() {
            return second;
        }
    }

    //Find or create a new log file based on client name and log name, port #, and connection time
    private void logConnectionDetails(String clientName, int port) {
        String logFileName = "logs/" + clientName + "_log.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFileName, true))) {
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(now);
            writer.println("Client Name: " + clientName);
            writer.println("Port Number: " + port);
            writer.println("Time of Connection: " + formattedDate);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Find the client's log file and log connection duration
    private void logConnectionDuration(String clientName, String duration) {
        String logFileName = "logs/" + clientName + "_log.txt";
        System.out.println(clientName);
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFileName, true))) {
            writer.println(duration + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
