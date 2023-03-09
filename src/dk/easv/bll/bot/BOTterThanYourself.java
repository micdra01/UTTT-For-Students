
package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;
import org.w3c.dom.ls.LSOutput;

import java.util.*;



/**
 * todo make a filter that looks for which rows are still possible to get 3 in a row local
 * todo should score lower than a local win and block, but still some points, so we know the local field can still be won
 */


/**
 * todo make a filter for what fields are possible to get 3 in  a macro row, (so we dont count local wins that dont matter in macro matter)
 * todo should not take moves that leads to opponent going to a macro field that you need to win.
 * todo should rank moves that sends opponent over to fields that cant be used in macro play anyways higher.
 *
 */

/**
 * todo     last method in our algorithm could (if time) make simple branching out from the top 2-3 nodes,
 * todo     and run all point methods through on the new result
 * todo     these could the be added up to a new best move score we then pick a result from
 */

/**
 * todo     check if opponent gets free mocro turn
 * todo     should give minus points if the opponents next move is free on the macro board
 */

/**
 * todo    check if the field that opponent can place in after our move is still Winnable for both us and opponent
 * todo    if the opponent can still use the micro board in macro play the move should get a lov score
 * todo    if we can use the micro field in macro play if so it should score low
 * todo    if high if opponent cant use the micro filed in macro play
 */





public class BOTterThanYourself implements IBot {
    final int moveTimeMs = 1000;
    private String BOT_NAME = getClass().getSimpleName();


    //todo here the points for whatever situation the method is checking for, so we can optimise via fine tuning

    private int macroWin = 100000;
    private int localWinPoint = 50; //points when the move leads to an local win
    private int localBlockPoint = 50; //points when the move leads to an local win
    private int opponentLocalWinChance = -50; // points when move leads to enemy getting local win in next round

    private int opponenCanWinMacroInNextMove = - 1000;




    /**
     * gets moves that result in a win on mini Board.
     */

    @Override
    public IMove doMove(IGameState state) {

        //start of method gets the first available moves and creates a list of scored moves
        GameSimulator gs = createSimulator(state);//gets created so we can get available moves
        List<IMove> rootMoves = gs.getCurrentState().getField().getAvailableMoves();//all possible moves from current state
        List<Move> scoredMoves = new ArrayList<>();

        //make each move into a score move that contains a score
        for (IMove moves : rootMoves) {
            Move scoreMove = new Move(moves.getX(), moves.getY());
            scoredMoves.add(scoreMove);
        }

        //runs through all filters on each available move

        for (Move move : scoredMoves) {

            rateMove(state, move);
        }

        //gets the highest score move and plays it
        Move result = scoredMoves.get(0);
        //Move result = new Move(0, 0, 0);// fake reference move

        for (Move move : scoredMoves) {
            if (move.getScore() >= result.getScore()) {
                result = move;
            }
        }
        //if all node are 0 it plays random
        if (result.getScore() <= -100000) {
            Random r = new Random();
            int i = r.nextInt(scoredMoves.size());
            scoredMoves.sort(Comparator.comparing(Move::getScore));
            System.out.println("random move = " + scoredMoves.get(0) + "ud af = " + scoredMoves.size() +  " moves" + "  score:  " + scoredMoves.get(0).getScore());
            System.out.println("");
            System.out.println("");
            return scoredMoves.get(0);
        }



        System.out.println("kvalificeret bud =  " + result + "  score  : " + result.getScore());
        System.out.println("");
        System.out.println("");

        return result;
    }
    private Move rateMove(IGameState state, Move move) {
        move = checkForLocalWin(state, move);
        move = checkForLocalBlock(state, move);
        move = checkForOpponentLocalWin(state, move);
        move = checkForMacroWin(state, move);
        move = checkForOpponentMacroWin(state, move);
        return move;
    }

    /**
     *
     * @param state
     * @param move
     * @return
     */
    private Move checkForOpponentMacroWin(IGameState state, Move move) {
        GameSimulator g = createSimulator(state);

        if(g.updateGame(move)) {//checks if move is possible before updating board
            List<IMove> rootMoves = g.getCurrentState().getField().getAvailableMoves();
            List<Move> scoredMoves = new ArrayList<>();
            //makes moves into scoreMoves
            for (IMove moves : rootMoves) {
                Move scoreMove = new Move(moves.getX(), moves.getY());
                scoredMoves.add(scoreMove);
            }
            //runs through all opponent moves to see if they will win
            for (int i = 0; scoredMoves.size() > i; i++){
                Move  opponentMove = scoredMoves.get(i);
                if(g.updateGame(opponentMove)){
                    if(g.getGameOver() == GameOverState.Win){
                        move.setScore(move.getScore() + opponenCanWinMacroInNextMove);
                        System.out.println("watch out the opponent wil win if you play " + scoredMoves + "  score  : " + move.getScore());
                    }
                }
            }
        }
        return move;
    }


    private Move checkForMacroWin(IGameState state, Move move) {
        GameSimulator g = createSimulator(state);
        String currentPlayer = String.valueOf(g.currentPlayer);//gets the current player before we make a simulated move
        if(g.updateGame(move)){//checks if move is possible before updating board
            if(g.getGameOver()== GameOverState.Win){
                move.setScore(move.getScore() + macroWin);//adds point to the move if it wins local
            }
        }
        return move;
    }


    /**
     * checks if the opponent can win in a local Field after your move
     * @param state
     * @param move
     * @return
     */
    private Move checkForOpponentLocalWin(IGameState state, Move move) {
        GameSimulator g = createSimulator(state);

        if(g.updateGame(move)) {//checks if move is possible before updating board
            List<IMove> rootMoves = g.getCurrentState().getField().getAvailableMoves();
            List<Move> scoredMoves = new ArrayList<>();
            //makes moves into scoreMoves
            for (IMove moves : rootMoves) {
                Move scoreMove = new Move(moves.getX(), moves.getY());
                scoredMoves.add(scoreMove);
            }
            //runs through all opponent moves to see if they will win
            for (int i = 0; scoredMoves.size() > i; i++){
                Move  opponentMove = scoredMoves.get(i);
                opponentMove = checkForLocalWin(g.getCurrentState(), opponentMove);//checks for opponent local win
                if(opponentMove.getScore() > 0){
                    move.setScore(move.getScore() +  opponentLocalWinChance);
                    System.out.println("we missed a opponenet local win" + move + "  score  : " + move.getScore());
                }
            }
        }
        return move;
    }


    /**
     * //todo check if we can block for opponent local-win with our move
     * //todo if the move blocks it gains some points
     */
    private Move checkForLocalBlock(IGameState state, Move move) {
        GameSimulator g = createSimulator(state);
        String currentPlayer = g.currentState.getMoveNumber() % 2 == 0 ? "1" : "0";//gets the current player before we make a simulated move
        System.out.println(currentPlayer);
        if(g.updateGame(move)){//checks if move is possible before updating board
            g.updateBoard(move);
            if(g.isWin(g.getCurrentState().getField().getBoard(), move, currentPlayer)){//see if move will win miniBoard
                move.setScore(move.getScore() + localBlockPoint);//adds point to the move if it wins local
                System.out.println("we found a local block " + move + "  score  : " + move.getScore());
            }
        }
        return move;
    }



    private Move checkForLocalWin(IGameState state, Move move){

        GameSimulator g = createSimulator(state);
        String currentPlayer = String.valueOf(g.currentPlayer);//gets the current player before we make a simulated move
        if(g.updateGame(move)){//checks if move is possible before updating board
            if(g.isWin(g.getCurrentState().getField().getBoard(), move, currentPlayer)){//see if move will win miniBoard
                move.setScore(move.getScore() + localWinPoint);//adds point to the move if it wins local
                System.out.println("we found a local win " + move + "  score  : " + move.getScore());
            }
        }
        return move;
    }

    private GameSimulator createSimulator(IGameState state) {
        GameSimulator simulator = new GameSimulator(new GameState());
        simulator.setGameOver(GameOverState.Active);
        simulator.setCurrentPlayer(state.getMoveNumber() % 2);
        simulator.getCurrentState().setRoundNumber(state.getRoundNumber());
        simulator.getCurrentState().setMoveNumber(state.getMoveNumber());
        simulator.getCurrentState().getField().setBoard(state.getField().getBoard());
        simulator.getCurrentState().getField().setMacroboard(state.getField().getMacroboard());
        return simulator;
    }




    /*
        The code below is a simulator for simulation of gameplay. This is needed for AI.

        It is put here to make the Bot independent of the GameManager and its subclasses/enums

        Now this class is only dependent on a few interfaces: IMove, IField, and IGameState

        You could say it is self-contained. The drawback is that if the game rules change, the simulator must be
        changed accordingly, making the code redundant.

     */


    @Override
    public String getBotName() {
        return BOT_NAME;
    }

    public enum GameOverState {
        Active,
        Win,
        Tie
    }

    public class Move implements IMove {
        int x = 0;
        int y = 0;

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public int score = 0;

        public Move(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Move(int x, int y, int score) {
            this.x = x;
            this.y = y;
            this.score = score;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Move move = (Move) o;
            return x == move.x && y == move.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    class GameSimulator {
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

        public Boolean updateGame(Move move) {
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

}
