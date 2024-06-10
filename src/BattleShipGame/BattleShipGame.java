package BattleShipGame;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Random;

public class BattleShipGame extends JFrame implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int SIZE = 10; // Tamanho do tabuleiro (10x10)
    private final JButton[][] playerButtons = new JButton[SIZE][SIZE];
    private final JButton[][] machineButtons = new JButton[SIZE][SIZE];
    private final JPanel playerPanel = new JPanel(new GridLayout(SIZE, SIZE, 5, 5));
    private final JPanel machinePanel = new JPanel(new GridLayout(SIZE, SIZE, 5, 5));
    private final JLabel statusLabel = new JLabel("Prepare seus navios", SwingConstants.CENTER);
    private final JLabel shipsRemainingLabel = new JLabel("Navios restantes: 6", SwingConstants.CENTER);
    private final Set<Point> playerShips = new HashSet<>();
    private final Set<Point> machineShips = new HashSet<>();
    private Set<Point> playerHits = new HashSet<>();
    private Set<Point> playerMisses = new HashSet<>();
    private Set<Point> machineHits = new HashSet<>();
    private Set<Point> machineMisses = new HashSet<>();
    private boolean validAttack = true;
    private final Random random = new Random();
    private boolean playerTurn = true; // Jogador começa o jogo
    private boolean gameStarted = false;
    private int shipsToPlace = 6;

    public BattleShipGame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Batalha Naval");
        setLayout(new BorderLayout());
        setSize(800, 450);
        setupBoards();
        setupStatusBar();

        int option = JOptionPane.showOptionDialog(this,
                "Deseja carregar um jogo salvo ou iniciar um novo jogo?",
                "Carregar Jogo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Carregar Jogo", "Novo Jogo"},
                "Novo Jogo");

        if (option == JOptionPane.YES_OPTION) {
            loadGameState(); // Carregar estado do jogo salvo
        } else {
            try {
                placeRandomMachineShips(6); // Posicionar navios da máquina aleatoriamente
                placeMachineShips(); // Posicionar navios da máquina a partir do CSV
            } catch (InvalidShipPlacementException | IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao carregar navios: " + e.getMessage());
            }
        }

        setVisible(true);
    }

    private void setupBoards() {
        playerPanel.setBorder(BorderFactory.createTitledBorder("Campo do Jogador"));
        machinePanel.setBorder(BorderFactory.createTitledBorder("Campo da Máquina"));

        playerPanel.setPreferredSize(new Dimension(400, 400));
        machinePanel.setPreferredSize(new Dimension(400, 400));

        setupBoard(playerButtons, playerPanel, true);
        setupBoard(machineButtons, machinePanel, false);

        add(playerPanel, BorderLayout.WEST);
        add(machinePanel, BorderLayout.EAST);
    }

    private void setupStatusBar() {
        JPanel statusPanel = new JPanel(new GridLayout(1, 3));
        statusLabel.setFont(new Font("Serif", Font.BOLD, 16));
        shipsRemainingLabel.setFont(new Font("Serif", Font.BOLD, 16));

        JButton saveButton = new JButton("Salvar e Terminar Jogo");
        saveButton.addActionListener(e -> saveAndEndGame());

        statusPanel.add(statusLabel);
        statusPanel.add(shipsRemainingLabel);
        statusPanel.add(saveButton);
        add(statusPanel, BorderLayout.NORTH);
    }

    private void placeMachineShips() throws InvalidShipPlacementException, IOException {
        List<String> lines = Files.readAllLines(Paths.get("data/machineShips.csv"));
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 2) {
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                Point p = new Point(x, y);
                if (!machineShips.add(p)) {
                    throw new InvalidShipPlacementException("Erro ao posicionar navio da máquina em: (" + x + ", " + y + ")");
                }
            } else {
                throw new InvalidShipPlacementException("Formato inválido no arquivo: " + line);
            }
        }
        saveGameState();
    }

    public void placePlayerShip(int x, int y) throws InvalidShipPlacementException {
        if (playerShips.size() >= 6) {
            throw new InvalidShipPlacementException("Já foram posicionados 6 navios.");
        }
        Point point = new Point(x, y);
        if (!playerShips.add(point)) {
            throw new InvalidShipPlacementException("Erro ao posicionar navio do jogador em: (" + x + ", " + y + ")");
        }
        saveGameState();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public int getShipsToPlace() {
        return shipsToPlace;
    }

    public void setShipsToPlace(int shipsToPlace) {
        this.shipsToPlace = shipsToPlace;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(boolean playerTurn) {
        this.playerTurn = playerTurn;
    }

    public JLabel getShipsRemainingLabel() {
        return shipsRemainingLabel;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public JButton[][] getMachineButtons() {
        return machineButtons;
    }

    public Set<Point> getMachineShips() {
        return machineShips;
    }

    public Set<Point> getPlayerHits() {
        return playerHits;
    }

    public Set<Point> getPlayerMisses() {
        return playerMisses;
    }

    public Set<Point> getMachineHits() {
        return machineHits;
    }

    public Set<Point> getMachineMisses() {
        return machineMisses;
    }
   
    public int getSIZE() {
        return SIZE;
    }

    public JButton[][] getPlayerButtons() {
        return playerButtons;
    }

    public Set<Point> getPlayerShips() {
        return playerShips;
    }
    public void setValidAttack(boolean validAttack) {
        this.validAttack = validAttack;
    }

    public boolean isValidAttack() {
        return validAttack;
    }

    public void handleAttack(int x, int y, JButton[][] board, Set<Point> ships, boolean isPlayerAttacking) throws InvalidAttackException {
        
        JButton button = board[x][y];
        Point point = new Point(x, y);

        if (playerHits.contains(point) || playerMisses.contains(point) && isPlayerAttacking){
            System.out.println("This position has already been attacked.");
            setValidAttack(false);
            return; // Ignore the attack
        }
        
        if (ships.contains(point)) {
            
            button.setText("...");
            button.setForeground(Color.BLUE);
            ships.remove(point);
            if (isPlayerAttacking) {
                playerHits.add(point);
                saveAttack("PAH", point);
            } else {
                machineHits.add(point);
                saveAttack("MAH", point);
            }
            System.out.println("Navio acertado em: (" + x + ", " + y + ")");
        } else {
            button.setText("...");
            button.setForeground(Color.RED);
            if (isPlayerAttacking) {
                playerMisses.add(point);
                saveAttack("PAM", point);
            } else {
                machineMisses.add(point);
                saveAttack("MAM", point);
            }
            System.out.println("Ataque errado em: (" + x + ", " + y + ")");
        }
        //saveGameState();
        updateStatus();
    }

    private void saveGameState() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/gameState.csv"))) {
            // Save player ships
            for (Point p : playerShips) {
                writer.write("P," + p.getX() + "," + p.getY() + "\n");
            }
            // Save machine ships
            for (Point p : machineShips) {
                writer.write("M," + p.getX() + "," + p.getY() + "\n");
            }
            
           
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar o estado do jogo: " + e.getMessage());
        }
    }

    private void saveAttack(String attackType, Point point) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/gameState.csv", true))) {
            System.out.println(attackType + " salvo pontos (" + point.getX() + ", " + point.getY() + ")");
            writer.write(attackType + "," + point.getX() + "," + point.getY() + "\n");
            
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao salvar ataque: " + e.getMessage());
        }
    }

    public void updateStatus() {
        int playerScore = playerHits.size();
        int machineScore = machineHits.size();
    
        if (machineScore == 6) {
            JOptionPane.showMessageDialog(this, "Máquina ganhou");
            GameResult result = new GameResult("Machine");
            GameResult.saveResult(result);
            saveAndEndGame();;
        } else if (playerScore == 6) {
            JOptionPane.showMessageDialog(this, "Player ganhou");
            GameResult result = new GameResult("Player");
            GameResult.saveResult(result);
            saveAndEndGame();
        }
    
        statusLabel.setText("Navios destruídos - Jogador: " + playerScore + ", Máquina: " + machineScore);
    }
    public static void main(String[] args) {
        new BattleShipGame();
    }

    private void setupBoard(JButton[][] board, JPanel panel, boolean isPlayer) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(30, 30));
                button.addActionListener(new ButtonHandler(i, j, isPlayer, this));
                board[i][j] = button;
                panel.add(button);
            }
        }
    }

    public void clearPlayerBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (playerButtons[i][j].getText().equals("...")) {
                    playerButtons[i][j].setText("");
                }
            }
        }
    }

    public void loadGameState() {
        try (BufferedReader reader = new BufferedReader(new FileReader("data/gameState.csv"))) {
            playerShips.clear();
            machineShips.clear();
            playerHits.clear();
            playerMisses.clear();
            machineHits.clear();
            machineMisses.clear();
            String line;
            boolean hasValidData = false; // Flag to check if valid data is found
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    hasValidData = true; // Found at least one valid line
                    String type = parts[0].trim();
                    int x = Integer.parseInt(parts[1].trim());
                    int y = Integer.parseInt(parts[2].trim());
                    Point p = new Point(x, y);
                    switch (type) {
                        case "P":
                            playerShips.add(p);
                            break;
                        case "M":
                            machineShips.add(p);
                            break;
                        case "MAH":
                            machineHits.add(p);
                            break;
                        case "MAM":
                            machineMisses.add(p);
                            break;
                        case "PAH":
                            playerHits.add(p);
                            break;
                        case "PAM":
                            playerMisses.add(p);
                            break;
                    }
                }
            }

            if (!hasValidData) {
                throw new NoValidGameDataException("Nenhum jogo salvo");
            }

            // Update the player board with loaded attacks from the machine
            for (Point p : machineHits) {
                JButton button = playerButtons[p.getX()][p.getY()];
                button.setText("...");
                button.setForeground(Color.BLUE);
            }
            for (Point p : machineMisses) {
                JButton button = playerButtons[p.getX()][p.getY()];
                button.setText("...");
                button.setForeground(Color.RED);
            }

            // Update the machine board with loaded attacks from the player
            for (Point p : playerHits) {
                JButton button = machineButtons[p.getX()][p.getY()];
                button.setText("...");
                button.setForeground(Color.BLUE);
            }
            for (Point p : playerMisses) {
                JButton button = machineButtons[p.getX()][p.getY()];
                button.setText("...");
                button.setForeground(Color.RED);
            }

            gameStarted = true;
            playerTurn = true; // Assume it's the player's turn after loading the game
            updateStatus();
            JOptionPane.showMessageDialog(this, "Estado do jogo carregado com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao carregar o estado do jogo: " + e.getMessage());
        } catch (NoValidGameDataException e) {
            // Handle the case where no valid game data was found
            JOptionPane.showMessageDialog(this, e.getMessage());
            System.exit(0);
        }
    }

    public void printGameResults() {
        List<GameResult> results = GameResult.loadResults();
        System.out.println("Game Results:");
        for (GameResult result : results) {
            System.out.println(result);
        }
    }

    public void saveAndEndGame() {
        //saveGameState();
        printGameResults();
        System.exit(0);

    }

    public void placeRandomMachineShips(int numberOfShips) throws IOException {
        Set<String> machineShipPositions = new HashSet<>();

        // Generate unique random positions for the machine ships
        while (machineShipPositions.size() < numberOfShips) {
            int x = random.nextInt(SIZE);
            int y = random.nextInt(SIZE);
            String position = x + "," + y;

            // Add the position to the set if it's not already present
            if (!machineShipPositions.contains(position)) {
                machineShipPositions.add(position);
            }
        }

        // Write the positions to the machineShips.csv file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/machineShips.csv"))) {
            for (String position : machineShipPositions) {
                writer.write(position + "\n");
            }
        }
    }

}

