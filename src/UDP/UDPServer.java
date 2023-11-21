import java.io.*; 
import java.net.*; 
import java.nio.charset.StandardCharsets;  
import java.lang.Math;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.Arrays; 

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
    public void run() {
        // Decodes packet received from client and removes any null characters left behind from decoding 
	String sentence = new String(receivePacket.getData(), StandardCharsets.UTF_8);
	//System.out.println(sentence);	
	sentence = sentence.replaceAll("\0", "");

	if (!sentence.equalsIgnoreCase("close")) {
            System.out.println("Serving client from port " + receivePacket.getPort());
            byte[] sendData = new byte[1024];
	    String returnMsg; 

            InetAddress IPAddress = receivePacket.getAddress();

            int port = receivePacket.getPort();
	    
	    // First message is an acknowledgement, the following messages are calculation results
	    if (!sentence.equalsIgnoreCase("this is the first msg")) {
	    	returnMsg = DijkstraTwoStack(sentence);
	    }
	    else{
	    	returnMsg = "CONNECTION ACKNOWLEDGED";
	    }
	    
	    // Sends the result after the first message
	    if (!sentence.equalsIgnoreCase("this is the first msg")) {
	   	 try{
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
	    }

        } else {
            System.out.println("Client from port " + receivePacket.getPort() + " has closed their connection.");
        }
    }

    private String DijkstraTwoStack(String equation) {
    	// Ignores string values that contain letters
	// Server will send an empty response for inputs with letters
	  if(!equation.matches("[A-Za-z]+")) {
		// Splits the string based on spaces and adds each element to a queue
		String [] str = equation.split("\\s+");
		Queue<String> queue = new LinkedList<>();
		queue.addAll(Arrays.asList(str));
 	
		Stack<String> operators = new Stack<>();
		Stack<Double> operands = new Stack<>();
		
		// Goes through the queue and pushes operands/operators to each stack
		while(!queue.isEmpty()) {
			String token = queue.poll();
			switch (token) {
				case "(":
					break;
				case "+":	case "-":	case "/":	case "*":	case "^":
					operators.push(token);
					break;
				case ")":
					operands.push(evaluate(operators, operands));
					break;
				default:
					operands.push(Double.parseDouble(token));
					break;
			}

		}
		String returnVal = evaluate(operators,operands) + "";
		return returnVal;
    	}
	return "";
    }

    private Double evaluate(Stack<String> operators, Stack<Double> operands) { 
    	double val = operands.pop();
	// Performs the operations on two operands and returns the result
	if(!operators.empty()){
		String op = operators.pop();
		switch (op) {
			case "+":
				val = operands.pop() + val;
				break;
			case "-":
				val = operands.pop() - val;
				break;
			case "/":
				val = operands.pop() / val;
				break;
			case "*":
				val = operands.pop() * val;
				break;
			case "^":
				val = Math.pow(operands.pop(), val);	
				break;
			default:
				break;
		}
	}
    	return val;
    }


}
