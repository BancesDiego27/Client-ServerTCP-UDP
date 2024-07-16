import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {
    private static String protocolo;
    private static int puerto;

    public static void main(String[] args) {
        parseArguments(args);

        try {
            if (protocolo.equalsIgnoreCase("TCP")) {
                inciarTCPServer();
            } else if(protocolo.equalsIgnoreCase("UDP")) {
                iniciarUDPServer();
            }
            else{
                System.err.println("Error: Ingrese un Protocolo Valido TCP / UDP");
            }

            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void parseArguments(String[] args) {
        protocolo= "TCP"; // Si no trae nada en los argumentos se establecen valores por defecto no se especifican asi que asumire TCP y el puerto 3001
        puerto = 3001;
        for (int i = 0; i < args.length; i += 2) {

            String key = args[i]; // Agarrando el valor de protocol / port
            String value = args[i + 1]; // Cual es el valor de la llave  
            switch (key.toLowerCase()) {
                case "protocol":
                    protocolo = value;
                    break;
                case "port":
                    puerto = Integer.parseInt(value) ;
                    break;
            }
        }
    }

    private static void inciarTCPServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(puerto);
        System.out.println("TCP server iniciado en el puerto " + puerto);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // Leer lo que escribe el cliente
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); // Manda mi resultado al cliente
            //System.out.println(clientSocket.getInetAddress().getHostAddress());
            String request = in.readLine(); // La peticion del usuario
            String clientIP = clientSocket.getInetAddress().getHostAddress();
            imprimirMensaje(">", "client", "TCP", "request", request,clientIP);

            if (request.equalsIgnoreCase("EXIT")) { // Solo se deberia de cerrar conexion con el cliente si ponen EXIT
                imprimirMensaje("<", "server", "TCP", "response", "EXIT",clientIP);
                clientSocket.close();
                break;
            }

            String response = hacerOperacion(request); // la funcion analiza la operacion y devuelve un resultado
            out.println(response);
            imprimirMensaje("<", "server", "TCP", "response", response,clientIP);

           // clientSocket.close();
        }
        serverSocket.close();
    }

    private static void iniciarUDPServer() throws IOException {
        DatagramSocket socket = new DatagramSocket(puerto);
        System.out.println("UDP server iniciado en el puerto  " + puerto);

        byte[] receiveBuffer = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length); // Recibe la info del cliente 
            socket.receive(receivePacket);
            String clientIP = receivePacket.getAddress().getHostAddress();

            String request = new String(receivePacket.getData(), 0, receivePacket.getLength()); // La peticion del usuario
          
            
            imprimirMensaje(">", "client", "UDP", "request", request, clientIP);

            if (request.equalsIgnoreCase("EXIT")) {
                  // Solo se deberia de cerrar conexion con el cliente si ponen EXIT
                  imprimirMensaje("<", "server", "UDP", "response", "EXIT",clientIP);
                break;
            }

            String response = hacerOperacion(request); // la funcion analiza la operacion y devuelve un resultado
            byte[] sendBuffer = response.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, receivePacket.getAddress(), receivePacket.getPort());
            socket.send(sendPacket); // manda al cliente el resultado de la operacion 
            imprimirMensaje("<", "server", "UDP", "response", response,clientIP);
        }
        socket.close();
    }

    private static String hacerOperacion(String operation) {
        try {
            String[] parts = operation.split(" "); // Deberian de tener un formato Digito Signo Digito  ej 1 + 2 , 5 - 4, 9 * 8, 7 / 3 , 8 % 2
            if (parts.length != 3) throw new IllegalArgumentException("Operación inválida");

            int a = Integer.parseInt(parts[0]);
            int b = Integer.parseInt(parts[2]);
            String operator = parts[1];

            int result;
            switch (operator) {
                case "+":
                    result = a + b;
                    break;
                case "-":
                    result = a - b;
                    break;
                case "*":
                    result = a * b;
                    break;
                case "/":
                    result = a / b;
                    break;
                case "%":
                    result = a % b;
                    break;
                case "EXIT":
                    result = -500;
                    break;
                default:
                    throw new IllegalArgumentException("Operador inválido");
            }
            return String.valueOf(result);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static void imprimirMensaje(String direccion, String hostType, String protocolo, String descripcion, String mensaje , String clientIP) {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String localIP = inetAddress.getHostAddress();
            String host = hostType.equals("server") ? localIP : clientIP;
            String dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            System.out.println(String.format("[%s] %s %s [%s] %s: %s", direccion, host, hostType, dateTime, protocolo, descripcion + ": " + mensaje));
            
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
