package BattleShipGame;

public class InvalidShipPlacementException extends Exception {
    public InvalidShipPlacementException(String message) {
        super(message);
    }
}