# Tic Tac Toe

Desenvolver uma aplicação em Java que permita a dois utilizadores jogar o jogo do galo através de uma rede. A aplicação deve utilizar sockets para comunicação em rede e a consola para a interface com o utilizador.

* O desenvolvimento da aplicação deve contemplar os seguintes pontos:
  * Criar uma representação do tabuleiro do jogo do galo numa grelha 3x3 utilizando a consola;
  * Adicionar instruções na consola para os jogadores introduzirem os seus movimentos;
  * Incluir mensagens na consola para mostrar o estado atual do jogo (ex: "Jogador 1 (X) é a sua vez", "Jogador 2 (O) é a sua vez", "Jogador 1 venceu!", "Empate");
  * Implementar um servidor que gere o jogo e mantenha o estado do tabuleiro;
  * Implementar clientes que se conectem ao servidor para jogar;
  * Os clientes devem enviar movimentos ao servidor, que validará e atualizará o estado do jogo;
  * O servidor deve enviar atualizações aos clientes após cada movimento válido;
  * Validar movimentos para garantir que os jogadores só podem jogar em células vazias;
  * Detetar vitória ou empate e atualizar do estado do jogo;
  * Reiniciar o jogo sem fechar a aplicação;
 
* Servidor:
  * Criar uma classe TicTacToeServer que inicia um servidor socket na porta especificada;
  * A classe deve manter o estado do jogo (tabuleiro) e gerir a comunicação com os clientes;
  * A classe deve enviar o estado do tabuleiro atualizado aos clientes após cada movimento;

* Cliente:
  * Criar uma classe TicTacToeClient que se conecta ao servidor;
  * A interface de consola deve permitir que o jogador faça movimentos e envie esses movimentos ao servidor;
  * A classe deve receber atualizações do servidor e atualizar a interface de consola;