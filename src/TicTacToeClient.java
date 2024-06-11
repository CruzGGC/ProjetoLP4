import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TicTacToeClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Solicita o IP do servidor
        System.out.print("Digite o IP do servidor (deixe em branco para 'localhost'): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) {
            host = "localhost"; // Usa 'localhost' se o IP não for fornecido
        }

        // Solicita a porta do servidor
        System.out.print("Digite a porta do servidor (deixe em branco para '12345'): ");
        String portInput = scanner.nextLine().trim();
        int port;
        if (portInput.isEmpty()) {
            port = 12345; // Usa a porta padrão 12345 se não for fornecida
        } else {
            try {
                port = Integer.parseInt(portInput); // Converte a entrada para um número inteiro
            } catch (NumberFormatException e) {
                System.out.println("Porta inválida. Usando a porta padrão '12345'.");
                port = 12345; // Usa a porta padrão em caso de erro
            }
        }

        // Conecta ao servidor usando o IP e a porta fornecidos
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Solicita o nome do jogador
            System.out.println(in.readLine());
            String name;
            while (true) {
                name = scanner.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("Nome não pode estar vazio. Digite seu nome:");
                } else {
                    break; // Continua se um nome válido for fornecido
                }
            }
            out.println(name); // Envia o nome ao servidor

            // Thread para lidar com mensagens do servidor
            Thread serverMessages = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage); // Imprime mensagens do servidor
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // Trata exceções de entrada/saída
                }
            });
            serverMessages.start(); // Inicia a thread para receber mensagens do servidor

            // Lida com a entrada do usuário
            while (true) {
                String userInput = scanner.nextLine();
                out.println(userInput); // Envia a entrada do usuário ao servidor
            }
        } catch (IOException e) {
            System.out.println("Não foi possível conectar ao servidor. Por favor, tente novamente mais tarde.");
        }
    }
}
