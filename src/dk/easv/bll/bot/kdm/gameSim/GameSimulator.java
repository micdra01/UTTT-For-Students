package dk.easv.bll.bot.kdm.gameSim;


import dk.easv.bll.bot.kdm.gameSim.Move;
import  dk.easv.bll.bot.kdm.gameSim.GameSimulator;
import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.bot.kdm.gameSim.Move;
import dk.easv.bll.move.IMove;

public class GameSimulator {
    private final IGameState currentState;
    private int currentPlayer = 0; //player0 == 0 && player1 == 1
    private volatile GameOverState gameOver = GameOverState.Active;

    public void setGameOver(GameOverState state) {
        gameOver = state;
    }

    public GameOverState getGameOver() {
        return gameOver;
    }

    public void setCurrentPlayer(int player) {
        currentPlayer = player;
    }

    public IGameState getCurrentState() {
        return currentState;
    }

    public GameSimulator(IGameState currentState) {
        this.currentState = currentState;
    }

    public Boolean updateGame(IMove move) {
        if (!verifyMoveLegality(move))
            return false;

        updateBoard(move);
        currentPlayer = (currentPlayer + 1) % 2;

        return true;
    }

    private Boolean verifyMoveLegality(IMove move) {
        IField field = currentState.getField();
        boolean isValid = field.isInActiveMicroboard(move.getX(), move.getY());

        if (isValid && (move.getX() < 0 || 9 <= move.getX())) isValid = false;
        if (isValid && (move.getY() < 0 || 9 <= move.getY())) isValid = false;

        if (isValid && !field.getBoard()[move.getX()][move.getY()].equals(IField.EMPTY_FIELD))
            isValid = false;

        return isValid;
    }

    private void updateBoard(IMove move) {
        String[][] board = currentState.getField().getBoard();
        board[move.getX()][move.getY()] = currentPlayer + "";
        currentState.setMoveNumber(currentState.getMoveNumber() + 1);
        if (currentState.getMoveNumber() % 2 == 0) {
            currentState.setRoundNumber(currentState.getRoundNumber() + 1);
        }
        checkAndUpdateIfWin(move);
        updateMacroboard(move);

    }

    private void checkAndUpdateIfWin(IMove move) {
        String[][] macroBoard = currentState.getField().getMacroboard();
        int macroX = move.getX() / 3;
        int macroY = move.getY() / 3;

        if (macroBoard[macroX][macroY].equals(IField.EMPTY_FIELD) ||
                macroBoard[macroX][macroY].equals(IField.AVAILABLE_FIELD)) {

            String[][] board = getCurrentState().getField().getBoard();

            if (isWin(board, move, "" + currentPlayer))
                macroBoard[macroX][macroY] = currentPlayer + "";
            else if (isTie(board, move))
                macroBoard[macroX][macroY] = "TIE";

            //Check macro win
            if (isWin(macroBoard, new Move(macroX, macroY), "" + currentPlayer))
                gameOver = GameOverState.Win;
            else if (isTie(macroBoard, new Move(macroX, macroY)))
                gameOver = GameOverState.Tie;
        }

    }

    private boolean isTie(String[][] board, IMove move) {
        int localX = move.getX() % 3;
        int localY = move.getY() % 3;
        int startX = move.getX() - (localX);
        int startY = move.getY() - (localY);

        for (int i = startX; i < startX + 3; i++) {
            for (int k = startY; k < startY + 3; k++) {
                if (board[i][k].equals(IField.AVAILABLE_FIELD) ||
                        board[i][k].equals(IField.EMPTY_FIELD))
                    return false;
            }
        }
        return true;
    }


    public boolean isWin(String[][] board, IMove move, String currentPlayer) {
        int localX = move.getX() % 3;
        int localY = move.getY() % 3;
        int startX = move.getX() - (localX);
        int startY = move.getY() - (localY);

        //check col
        for (int i = startY; i < startY + 3; i++) {
            if (!board[move.getX()][i].equals(currentPlayer))
                break;
            if (i == startY + 3 - 1) return true;
        }

        //check row
        for (int i = startX; i < startX + 3; i++) {
            if (!board[i][move.getY()].equals(currentPlayer))
                break;
            if (i == startX + 3 - 1) return true;
        }

        //check diagonal
        if (localX == localY) {
            //we're on a diagonal
            int y = startY;
            for (int i = startX; i < startX + 3; i++) {
                if (!board[i][y++].equals(currentPlayer))
                    break;
                if (i == startX + 3 - 1) return true;
            }
        }

        //check anti diagonal
        if (localX + localY == 3 - 1) {
            int less = 0;
            for (int i = startX; i < startX + 3; i++) {
                if (!board[i][(startY + 2) - less++].equals(currentPlayer))
                    break;
                if (i == startX + 3 - 1) return true;
            }
        }
        return false;
    }

    private void updateMacroboard(IMove move) {
        String[][] macroBoard = currentState.getField().getMacroboard();
        for (int i = 0; i < macroBoard.length; i++)
            for (int k = 0; k < macroBoard[i].length; k++) {
                if (macroBoard[i][k].equals(IField.AVAILABLE_FIELD))
                    macroBoard[i][k] = IField.EMPTY_FIELD;
            }

        int xTrans = move.getX() % 3;
        int yTrans = move.getY() % 3;

        if (macroBoard[xTrans][yTrans].equals(IField.EMPTY_FIELD))
            macroBoard[xTrans][yTrans] = IField.AVAILABLE_FIELD;
        else {
            // Field is already won, set all fields not won to avail.
            for (int i = 0; i < macroBoard.length; i++)
                for (int k = 0; k < macroBoard[i].length; k++) {
                    if (macroBoard[i][k].equals(IField.EMPTY_FIELD))
                        macroBoard[i][k] = IField.AVAILABLE_FIELD;
                }
        }
    }
}
