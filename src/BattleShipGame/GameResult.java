package BattleShipGame;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILE_NAME = "data/gameResults.ser";

    private String winner;
    private Date date;

    public GameResult(String winner) {
        this.winner = winner;
        this.date = new Date();
    }

    public String getWinner() {
        return winner;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Winner: " + winner + ", Date: " + date;
    }

    public static void saveResult(GameResult result) {
        List<GameResult> results = loadResults();
        results.add(result);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<GameResult> loadResults() {
        List<GameResult> results = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            results = (List<GameResult>) ois.readObject();
        } catch (FileNotFoundException e) {
            // No previous results, return empty list
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return results;
    }
}
