package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.util.List;
import java.util.Random;

public class TurboNitroBot implements IBot {
    private int winPoints = 10;
    private int losePoints = -1;
    private int minPoints = -10;
    private int noPoints = -100;

    private GameState currentState;


    @Override
    public IMove doMove(IGameState state) {
        return null;
    }

    public IMove doSimulatedMove(IMove move){
        if(verifyMoveLegality(move)){
            simulateGame(move);
            return move;
        }
        return null;
    }

    private TurboNitroBot.GameSimulator createSimulator(IGameState state) {
        TurboNitroBot.GameSimulator simulator = new TurboNitroBot.GameSimulator(new GameState());
        simulator.setGameOver(GameOverState.Active);
        simulator.setCurrentPlayer(state.getMoveNumber() % 2);
        simulator.getCurrentState().setRoundNumber(state.getRoundNumber());
        simulator.getCurrentState().setMoveNumber(state.getMoveNumber());
        simulator.getCurrentState().getField().setBoard(state.getField().getBoard());
        simulator.getCurrentState().getField().setMacroboard(state.getField().getMacroboard());
        return simulator;
    }

    private IMove calculateWinningMove(IGameState state, int maxTimeMs){
        long time = System.currentTimeMillis();
        Random rand = new Random();
        while(System.currentTimeMillis() < time + maxTimeMs) {

            TurboNitroBot.GameSimulator simulator = createSimulator(state);
            IGameState gs = simulator.getCurrentState();
            List<IMove> moves = gs.getField().getAvailableMoves();
            IMove winnerMove = moves.get(rand.nextInt(moves.size()));
        }
        return null;
    }

    public enum GameOverState {
        Active,
        Win,
        Tie
    }

    public String[][] simulateGame(IMove move){
        String[][] simulatedBoard = getBoard();
        String[][] simulatedMacroBoard = getMacroBoard();
        for(int i = 0; i < simulatedBoard.length; i++){
            for (int k = 0;k < simulatedMacroBoard.length; k++){

            }
        }
        return null;
    }


    public String[][] getBoard(){
        String[][] board = currentState.getField().getBoard();
        return board;
    }

    public String[][] getMacroBoard(){
        String[][] macroBoard = currentState.getField().getMacroboard();
        return macroBoard;
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

    @Override
    public String getBotName() {
        return "GodBot";
    }

    public class GameSimulator {

        private int currentPlayer = 0;
        private volatile TurboNitroBot.GameOverState gameOver = TurboNitroBot.GameOverState.Active;

        private IGameState currentState;
        public GameSimulator(IGameState currentState) {
            this.currentState = currentState;
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

        private void updateBoard(IMove move) {
            String[][] board = currentState.getField().getBoard();
            board[move.getX()][move.getY()] = currentPlayer + "";
            currentState.setMoveNumber(currentState.getMoveNumber() + 1);
            if (currentState.getMoveNumber() % 2 == 0) {
                currentState.setRoundNumber(currentState.getRoundNumber() + 1);
            }
            // checkAndUpdateIfWin(move);
            updateMacroboard(move);
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

        public void setGameOver(TurboNitroBot.GameOverState state) {
            gameOver = state;
        }
        public void setCurrentPlayer(int player) {
            currentPlayer = player;
        }
        public IGameState getCurrentState() {
            return currentState;
        }


    }
    public class SimMove{
        int x=0;
        int y=0;
        int winNum=0;
        public SimMove(int x, int y, int winNum){
            this.x=x;
            this.y=y;
            this.winNum=winNum;
        }
        public void setY(int y){
            this.y=y;
        }

        public void setX(int x){
            this.x=x;
        }
        public void setWinNum(int winNum){this.winNum=winNum;}

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWinNum(){return winNum;}
    }
}

