import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Client {
    private static String protocolo;
    private static String serverIp;
    private static int puerto;

    public static void main(String[] args) {
        parseArguments(args);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Ingrese operación matemática (o EXIT para salir): ");
            String operation = scanner.nextLine();

            if (operation.equalsIgnoreCase("EXIT")) {
                sendExitMessage();
                break;
            }

            try {

                if (protocolo.equalsIgnoreCase("TCP")) {
                    conectarTCP(operation);
                } else if (protocolo.equalsIgnoreCase("UDP")) {
                    conectarUDP(operation);
                } else {
                    System.err.println("Error: Ingrese un Protocolo Valido TCP / UDP");
                }

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void parseArguments(String[] args) {
        protocolo = "TCP"; // Si no trae nada en los argumentos se establecen valores por defecto no se
                           // especifican asi que asumire TCP y el puerto 3001
        puerto = 3001;
        for (int i = 0; i < args.length; i += 2) {
            String key = args[i];
            String value = args[i + 1];
            switch (key.toLowerCase()) {
                case "protocol":
                    protocolo = value;
                    break;
                case "server":
                    serverIp = value;
                    break;
                case "port":
                    puerto = Integer.parseInt(value);
                    break;
            }
        }
    }

    private static String conectarTCP(String operation) throws IOException {
        Socket socket = new Socket(serverIp, puerto);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        out.println(operation); // Lo que le mando al server

        imprimirMensaje("<", "client", "TCP", "request", operation);
        if (operation == "EXIT") {
            imprimirMensaje(">", "server", "TCP", "response", "EXIT");
            socket.close();
            return "EXIT";
        }
        String response = in.readLine();
        imprimirMensaje(">", "server", "TCP", "response", response);

        socket.close();
        return response;
    }

    private static String conectarUDP(String operation) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(serverIp);
        byte[] sendBuffer = operation.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, puerto);
        socket.send(sendPacket);
        imprimirMensaje("<", "client", "UDP", "request", operation);

        if (operation == "EXIT") {
            imprimirMensaje(">", "server", "UDP", "response", "EXIT");
            socket.close();
            return "EXIT";
        }

        byte[] receiveBuffer = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
        socket.receive(receivePacket);
        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
        imprimirMensaje(">", "server", "UDP", "response", response);

        socket.close();
        return response;
    }

    private static void sendExitMessage() {
        try {
            if (protocolo.equalsIgnoreCase("TCP")) {
                conectarTCP("EXIT");
            } else {
                conectarUDP("EXIT");
            }
        } catch (IOException e) {
            System.err.println("Error sending EXIT message: " + e.getMessage());
        }
    }

    private static void imprimirMensaje(String direccion, String hostType, String protocolo, String descripcion,
            String mensaje) {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String localIP = inetAddress.getHostAddress();
            String host = hostType.equals("client") ? localIP : serverIp;
            String dateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            System.out.println(String.format("[%s] %s %s [%s] %s: %s", direccion, host, hostType, dateTime, protocolo,
                    descripcion + ": " + mensaje));

        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
