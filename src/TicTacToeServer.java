import java.io.*;
import java.net.*;
import java.util.*;

public class TicTacToeServer {
    private static final int BOARD_SIZE = 3;
    private char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
    private ClientHandler player1;
    private ClientHandler player2;
    private int currentPlayer;
    private boolean gameStarted = false;

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 12345;
        new TicTacToeServer().startServer(port);
    }

    public void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado na porta " + port);
            initializeBoard();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);

                if (player1 == null) {
                    player1 = clientHandler;
                    setupClient(player1, 0);
                } else if (player2 == null) {
                    player2 = clientHandler;
                    setupClient(player2, 1);
                } else {
                    clientSocket.close();
                }

                if (bothPlayersConnected() && !gameStarted && player1.hasName() && player2.hasName()) {
                    startGame();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupClient(ClientHandler client, int playerNumber) {
        client.setPlayerNumber(playerNumber);
        new Thread(client).start();
    }

    private boolean bothPlayersConnected() {
        return player1 != null && player2 != null;
    }

    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(board[i], ' ');
        }
    }

    private void startGame() {
        currentPlayer = new Random().nextInt(2);
        gameStarted = true;
        broadcast("Jogo comecando entre " + player1.getName() + " (X) e " + player2.getName() + " (O). " +
                getCurrentPlayerName() + " comeca.");
        broadcastBoard();
    }

    private String getCurrentPlayerName() {
        return currentPlayer == 0 ? player1.getName() : player2.getName();
    }

    public synchronized boolean makeMove(int player, int row, int col) {
        if (board[row][col] == ' ') {
            board[row][col] = (player == 0) ? 'X' : 'O';
            currentPlayer = 1 - currentPlayer;
            return true;
        }
        return false;
    }

    public synchronized void broadcast(String message) {
        if (player1 != null) player1.sendMessage(message);
        if (player2 != null) player2.sendMessage(message);
    }

    public synchronized void broadcastBoard() {
        if (player1 != null) player1.sendBoard(board);
        if (player2 != null) player2.sendBoard(board);
    }

    public synchronized boolean isGameOver() {
        return checkWin('X') || checkWin('O') || isBoardFull();
    }

    public synchronized String getResultMessage() {
        if (checkWin('X')) return player1.getName() + " (X) venceu!";
        if (checkWin('O')) return player2.getName() + " (O) venceu!";
        return "Empate!";
    }

    private boolean checkWin(char player) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (checkRow(player, i) || checkColumn(player, i)) return true;
        }
        return checkDiagonals(player);
    }

    private boolean checkRow(char player, int row) {
        return board[row][0] == player && board[row][1] == player && board[row][2] == player;
    }

    private boolean checkColumn(char player, int col) {
        return board[0][col] == player && board[1][col] == player && board[2][col] == player;
    }

    private boolean checkDiagonals(char player) {
        return (board[0][0] == player && board[1][1] == player && board[2][2] == player) ||
                (board[0][2] == player && board[1][1] == player && board[2][0] == player);
    }

    private boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == ' ') return false;
            }
        }
        return true;
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private TicTacToeServer server;
        private PrintWriter out;
        private BufferedReader in;
        private String name;
        private int playerNumber;

        public ClientHandler(Socket socket, TicTacToeServer server) {
            this.socket = socket;
            this.server = server;
        }

        public String getName() {
            return name;
        }

        public boolean hasName() {
            return name != null && !name.trim().isEmpty();
        }

        public void setPlayerNumber(int playerNumber) {
            this.playerNumber = playerNumber;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                requestName();

                server.broadcast(name + " entrou no jogo.");

                while (!server.bothPlayersConnected() || !server.player1.hasName() || !server.player2.hasName()) {
                    Thread.sleep(1000);
                }

                if (!server.gameStarted && server.bothPlayersConnected() && server.player1.hasName() && server.player2.hasName()) {
                    server.startGame();
                }

                while (true) {
                    if (server.isGameOver()) {
                        server.broadcast(server.getResultMessage());
                        server.broadcast("O jogo vai reiniciar.");
                        server.initializeBoard();
                        server.startGame();
                    }

                    if (server.gameStarted && server.currentPlayer == playerNumber) {
                        out.println("Sua vez. Insira sua jogada (linha e coluna):");
                        handleMove();
                    } else {
                        if (!in.ready()) {
                            out.println("Aguarde sua vez...");
                            Thread.sleep(1000);
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void requestName() throws IOException {
            out.println("Digite seu nome:");
            while (true) {
                name = in.readLine();
                if (name == null || name.trim().isEmpty()) {
                    out.println("Nome nao pode estar vazio. Digite seu nome:");
                } else {
                    break;
                }
            }
        }

        private void handleMove() {
            try {
                String[] move = in.readLine().trim().split("\\s+");
                if (move.length != 2) throw new IllegalArgumentException("Entrada deve conter dois numeros separados por espaco.");
                int row = Integer.parseInt(move[0]);
                int col = Integer.parseInt(move[1]);

                if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) throw new IllegalArgumentException("Coordenadas devem estar entre 0 e 2.");

                if (server.makeMove(playerNumber, row, col)) {
                    server.broadcastBoard();
                } else {
                    out.println("Movimento invalido. Posicao ja ocupada. Tente novamente.");
                }
            } catch (Exception e) {
                out.println("Entrada invalida. Certifique-se de inserir dois numeros entre 0 e 2, separados por espaco. Tente novamente. Erro: " + e.getMessage());
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void sendBoard(char[][] board) {
            out.println("Estado atual do tabuleiro:");
            for (int i = 0; i < BOARD_SIZE; i++) {
                out.print(" ");
                for (int j = 0; j < BOARD_SIZE; j++) {
                    out.print(board[i][j]);
                    if (j < BOARD_SIZE - 1) out.print(" | ");
                }
                out.println();
                if (i < BOARD_SIZE - 1) out.println("---+---+---");
            }
        }
    }
}
