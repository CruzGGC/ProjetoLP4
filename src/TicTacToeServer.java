import java.io.*;
import java.net.*;
import java.util.*;

public class TicTacToeServer {
    private static final int BOARD_SIZE = 3;  // Tamanho do tabuleiro do jogo da velha
    private final char[][] board = new char[BOARD_SIZE][BOARD_SIZE];  // Representação do tabuleiro
    private ClientHandler player1;  // Jogador 1
    private ClientHandler player2;  // Jogador 2
    private int currentPlayer;  // Jogador atual (0 ou 1)
    private boolean gameStarted = false;  // Indica se o jogo começou

    /**
     * Método principal que inicia o servidor.
     * @param args Argumentos da linha de comando (porta opcional).
     */

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 12345;  // Porta padrão é 12345
        new TicTacToeServer().startServer(port);  // Inicia o servidor na porta especificada
    }

    /**
     * Inicia o servidor na porta especificada.
     * @param port Porta para o servidor escutar.
     */

    public void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado na porta " + port);
            initializeBoard();  // Inicializa o tabuleiro

            while (true) {
                Socket clientSocket = serverSocket.accept();  // Aceita a conexão do cliente
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);

                if (player1 == null) {
                    player1 = clientHandler;  // Define o cliente como jogador 1
                    setupClient(player1, 0);
                } else if (player2 == null) {
                    player2 = clientHandler;  // Define o cliente como jogador 2
                    setupClient(player2, 1);
                } else {
                    clientSocket.close();  // Fecha a conexão se ambos os jogadores já estiverem conectados
                }

                if (bothPlayersConnected() && !gameStarted && player1.hasName() && player2.hasName()) {
                    startGame();  // Inicia o jogo quando ambos os jogadores estão conectados e prontos
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configura o cliente.
     * @param client Cliente a ser configurado.
     * @param playerNumber Número do jogador (0 ou 1).
     */
    private void setupClient(ClientHandler client, int playerNumber) {
        client.setPlayerNumber(playerNumber);
        new Thread(client).start();  // Inicia uma nova thread para o cliente
    }

    /**
     * Verifica se ambos os jogadores estão conectados.
     * @return true se ambos os jogadores estão conectados, false caso contrário.
     */
    private boolean bothPlayersConnected() {
        return player1 != null && player2 != null;
    }

    /**
     * Inicializa o tabuleiro do jogo preenchendo com espaços em branco.
     */
    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            Arrays.fill(board[i], ' ');
        }
    }

    /**
     * Inicia o jogo definindo o jogador inicial e marcando o jogo como iniciado.
     */

    private void startGame() {
        currentPlayer = new Random().nextInt(2);  // Seleciona aleatoriamente o jogador inicial
        gameStarted = true;
        broadcast("Jogo comecando entre " + player1.getName() + " (X) e " + player2.getName() + " (O). " +
                getCurrentPlayerName() + " comeca.");
        broadcastBoard();
        // Inicia o jogo, selecionando aleatoriamente o jogador inicial e marcando o jogo como iniciado.
        // Em seguida, envia uma mensagem de transmissão para informar que o jogo começou e quem é o primeiro a jogar.
        // Finalmente, transmite o estado inicial do tabuleiro.
    }

    // Retorna o nome do jogador atual. Se o jogador atual for 0, retorna o nome do player1, caso contrário, retorna o nome do player2.
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
        // Faz uma jogada no tabuleiro. Se a posição especificada estiver vazia, coloca a marca do jogador (X ou O) na posição e muda a vez do jogador.
        // Retorna verdadeiro se a jogada foi bem sucedida, falso caso contrário.
    }

    public synchronized void broadcast(String message) {
        if (player1 != null) player1.sendMessage(message);
        if (player2 != null) player2.sendMessage(message);
        // Envia uma mensagem para ambos os jogadores, se eles estiverem conectados.
    }

    public synchronized void broadcastBoard() {
        if (player1 != null) player1.sendBoard(board);
        if (player2 != null) player2.sendBoard(board);
        // Envia o estado atual do tabuleiro para ambos os jogadores, se eles estiverem conectados.
    }

    public synchronized boolean isGameOver() {
        return checkWin('X') || checkWin('O') || isBoardFull();
        // Verifica se o jogo terminou. O jogo termina se algum dos jogadores ganhou ou se o tabuleiro está cheio.
    }

    // Este método retorna a mensagem de resultado do jogo.
    // Se o jogador 'X' ganhou, retorna uma mensagem indicando que o jogador 1 venceu.
    // Se o jogador 'O' ganhou, retorna uma mensagem indicando que o jogador 2 venceu.
    // Se nenhum dos jogadores ganhou, retorna uma mensagem indicando um empate.
    public synchronized String getResultMessage() {
        if (checkWin('X')) return player1.getName() + " (X) venceu!";
        if (checkWin('O')) return player2.getName() + " (O) venceu!";
        return "Empate!";
    }

    // Este método verifica se o jogador especificado ganhou o jogo.
    // Verifica todas as linhas, colunas e diagonais para uma vitória.
    private boolean checkWin(char player) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (checkRow(player, i) || checkColumn(player, i)) return true;
        }
        return checkDiagonals(player);
    }

    // Este método verifica se o jogador especificado ganhou em uma linha específica.
    private boolean checkRow(char player, int row) {
        return board[row][0] == player && board[row][1] == player && board[row][2] == player;
    }

    // Este método verifica se o jogador especificado ganhou em uma coluna específica.
    private boolean checkColumn(char player, int col) {
        return board[0][col] == player && board[1][col] == player && board[2][col] == player;
    }

    // Este método verifica se o jogador especificado ganhou em qualquer uma das diagonais.
    private boolean checkDiagonals(char player) {
        return (board[0][0] == player && board[1][1] == player && board[2][2] == player) ||
                (board[0][2] == player && board[1][1] == player && board[2][0] == player);
    }

    // Este método verifica se o tabuleiro está cheio, ou seja, se todas as posições foram preenchidas.
    private boolean isBoardFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == ' ') return false;
            }
        }
        return true;
    }

    // Classe interna para gerir a conexão com o cliente
    private static class ClientHandler implements Runnable {
        private final Socket socket;  // Socket do cliente
        private final TicTacToeServer server;  // Instância do servidor
        private PrintWriter out;  // Escritor para enviar dados ao cliente
        private BufferedReader in;  // Leitor para receber dados do cliente
        private String name;  // Nome do jogador
        private int playerNumber;  // Número do jogador (0 ou 1)

        /**
         * Construtor para ClientHandler.
         * @param socket Socket do cliente.
         * @param server Instância do servidor.
         */

        public ClientHandler(Socket socket, TicTacToeServer server) {
            this.socket = socket;
            this.server = server;
        }

        // Método para obter o nome do jogador
        public String getName() {
            return name;
        }

        // Método para verificar se o jogador tem um nome
        public boolean hasName() {
            return name != null && !name.trim().isEmpty();
        }

        // Método para definir o número do jogador
        public void setPlayerNumber(int playerNumber) {
            this.playerNumber = playerNumber;
        }

        // Método executado quando a thread é iniciada
        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                requestName();  // Solicita o nome do jogador

                server.broadcast(name + " entrou no jogo.");  // Informa que o jogador entrou no jogo

                // Espera até que ambos os jogadores estejam conectados e tenham um nome
                while (!server.bothPlayersConnected() || !server.player1.hasName() || !server.player2.hasName()) {
                    Thread.sleep(1000);
                }

                // Se o jogo ainda não começou e ambos os jogadores estão conectados e têm um nome, inicia o jogo
                if (!server.gameStarted && server.bothPlayersConnected() && server.player1.hasName() && server.player2.hasName()) {
                    server.startGame();
                }

                // Loop principal do jogo
                while (true) {
                    // Se o jogo terminou, informa o resultado e reinicia o jogo
                    if (server.isGameOver()) {
                        server.broadcast(server.getResultMessage());
                        server.broadcast("O jogo vai reiniciar.");
                        server.initializeBoard();
                        server.startGame();
                    }

                    // Se é a vez deste jogador, solicita uma jogada
                    if (server.gameStarted && server.currentPlayer == playerNumber) {
                        out.println("Sua vez. Insira sua jogada (linha e coluna):");
                        handleMove();
                    } else {
                        // Se não é a vez deste jogador, espera
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

        // Método para solicitar o nome do jogador
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

        // Método para tratar uma jogada do jogador
        private void handleMove() {
            try {
                String[] move = in.readLine().trim().split("\\s+");
                if (move.length != 2) throw new IllegalArgumentException("Entrada deve conter dois numeros separados por espaco.");
                int row = Integer.parseInt(move[0]);
                int col = Integer.parseInt(move[1]);

                if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) throw new IllegalArgumentException("Coordenadas devem estar entre 0 e 2.");

                // Se a jogada é válida, atualiza o tabuleiro e informa o estado atual do tabuleiro
                if (server.makeMove(playerNumber, row, col)) {
                    server.broadcastBoard();
                } else {
                    out.println("Movimento invalido. Posicao ja ocupada. Tente novamente.");
                }
            } catch (Exception e) {
                out.println("Entrada invalida. Certifique-se de inserir dois numeros entre 0 e 2, separados por espaco. Tente novamente. Erro: " + e.getMessage());
            }
        }

        // Método para enviar uma mensagem ao jogador
        public void sendMessage(String message) {
            out.println(message);
        }

        // Método para enviar o estado atual do tabuleiro ao jogador
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
