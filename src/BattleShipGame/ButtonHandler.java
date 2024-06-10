package BattleShipGame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class ButtonHandler implements ActionListener {
    private final int x;
    private final int y;
    private final boolean isPlayer;
    private final BattleShipGame game;
    private final Random random = new Random();

    public ButtonHandler(int x, int y, boolean isPlayer, BattleShipGame game) {
        this.x = x;
        this.y = y;
        this.isPlayer = isPlayer;
        this.game = game;
    }

    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        if (isPlayer && game.isGameStarted()) {
            return; // Ignorar cliques no tabuleiro do jogador após o início do jogo
        }
        if (isPlayer && !game.isGameStarted()) {
            try {
                game.placePlayerShip(x, y);
                button.setText("..."); // Mostrar navio do jogador
                System.out.println("Seu navio foi posicionado em: (" + x + ", " + y + ")");
                game.setShipsToPlace(game.getShipsToPlace() - 1);
                game.getShipsRemainingLabel().setText("Navios restantes: " + game.getShipsToPlace());
                if (game.getShipsToPlace() == 0) {
                    game.setGameStarted(true);
                    game.getStatusLabel().setText("Todos os navios foram posicionados. Ataque os navios da máquina!");
                    game.clearPlayerBoard(); // Chamada ao método
                }
            } catch (InvalidShipPlacementException ex) {
                JOptionPane.showMessageDialog(game, ex.getMessage());
            }
        } else if (!isPlayer && game.isGameStarted() && game.isPlayerTurn()) {
            try {
                game.handleAttack(x, y, game.getMachineButtons(), game.getMachineShips(), true);
                game.setPlayerTurn(false);
                SwingUtilities.invokeLater(this::makeMachineMove);
            } catch (InvalidAttackException ex) {
                JOptionPane.showMessageDialog(game, ex.getMessage());
            }
        }
    }

    private void makeMachineMove() {
        int x, y;
        Point move;
        if(game.isValidAttack()){
    
            // faz o ataque que não foi tentado ainda
            do {
                x = random.nextInt(game.getSIZE());
                y = random.nextInt(game.getSIZE());
                move = new Point(x, y);
            } while (game.getMachineHits().contains(move) || game.getMachineMisses().contains(move));
        
            System.out.println("Ataque da máquina em: (" + x + ", " + y + ")");
            try {
                // chama o metodo pra lidar com as jogadas 
                game.handleAttack(x, y, game.getPlayerButtons(), game.getPlayerShips(), false);
            } catch (InvalidAttackException e) {
                e.printStackTrace();
            }
        
            // vez do jogador e salva o status pra caso a maquina tenha vencido nessa jogada
            game.setPlayerTurn(true);
            game.updateStatus();
        }
        else{
            game.setPlayerTurn(true);
            game.setValidAttack(true);
            return;
        }
}
}
