
package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.*;
//////////////////////////////////////////////////////////////////////////////////////////////////////
//                                    master plan


// todo    a list of methods that looks at how good a move is fx. localWin, BlockOpponentLocal, BlockOpponentMacro osv...
// todo   new ideas to methods will only improve the algorithm as long as it is under 1 second in overall time and the points-given can be adjusted
// todo   all methods are not necessary but will all help for a more qualified move

// todo    each method should take a move and check if the current situations is filled and score accordingly
// todo   fx. a move that leads to a local win gives an amount of points and opponent win gives minus points

// todo    we should add each scorePoint-amount as an instance variable, in top of the document
// todo   fx amount of points added to a move when localWin is obtained should be saved in top of our bot class so we can twitch the amount of point easy at a later time

// todo   when all filter/point-giver-methods has ended and all the points are added up on the available moves we should play the one with most points

//todo macro play methods should run before local methods, as local points can depend on macro situation
// todo fx. winning local field should not give any points if the local field cant be used for winning macro play.


/////////////////////////////////////////////////////////////////////////////////////////////


/**
 * todo kenni
 * todo make a filter that looks for which rows are still possible to get 3 in a row local
 * todo should score lower than a local win and block, but still some points, so we know the local field can still be won
 * <p>
 * todo make a filter for what fields are possible to get 3 in  a macro row, (so we dont count local wins that dont matter in macro matter)
 * todo should not take moves that leads to opponent going to a macro field that you need to win.
 * todo should rank moves that sends opponent over to fields that cant be used in macro play anyways higher.
 * <p>
 * <p>
 * todo     last method in our algorithm could (if time) make simple branching out from the top 2-3 nodes,
 * todo     and run all point methods through on the new result
 * todo     these could the be added up to a new best move score we then pick a result from
 * <p>
 * todo     check if opponent gets free mocro turn
 * todo     should give minus points if the opponents next move is free on the macro board
 */


/**
 * todo make a filter for what fields are possible to get 3 in  a macro row, (so we dont count local wins that dont matter in macro matter)
 * todo should not take moves that leads to opponent going to a macro field that you need to win.
 * todo should rank moves that sends opponent over to fields that cant be used in macro play anyways higher.
 *
 */

/**
 * todo     Darling
 * todo     last method in our algorithm could (if time) make simple branching out from the top 2-3 nodes,
 * todo     and run all point methods through on the new result
 * todo     these could the be added up to a new best move score we then pick a result from
 */

/**
 * todo     Steffan
 * todo     check if opponent gets free mocro turn
 * todo     should give minus points if the opponents next move is free on the macro board
 */

/**
 * todo    kenni
 * todo    check if the field that opponent can place in after our move is still Winnable for both us and opponent
 * todo    if the opponent can still use the micro board in macro play the move should get a lov score
 * todo    if we can use the micro field in macro play if so it should score low
 * todo    if high if opponent cant use the micro filed in macro play
 */


public class AA_BOTterThanYourself implements IBot {
    final int moveTimeMs = 1000;
    private String BOT_NAME = "       BOT_NAKED       vs      not as good bot, that is gonna lose  and don't even gets mentioned in title                                                                                                                                                                                                                                                                                               ";


    //todo here the points for whatever situation the method is checking for, so we can optimise via fine tuning

    private int macroWin = 100000000;
    private int macroBlock = macroWin / 3;
    private int localWinPoint = 5000000; //points when the move leads to an local win
    private int localBlockPoint = 10000000; //points when the move leads to an local win
    private int opponentLocalWinChance = -5000000; // points when move leads to enemy getting local win in next round
    private int opponenCanWinMacroInNextMove = - 10000000;

    private int isOwend = 10;
    private int isTheMoveStillLocalWinnable = 5;
    private int localUnwinnable = 0;
    private int localWinnable = 0;
    private int localPresence = 100000;



    /**
     * gets moves that result in a win on mini Board.
     */

    @Override
    public IMove doMove(IGameState state) {

        List<Move> moves = getAvailableScoredMoves(state);
        Move result = lookAhead(moves, state, 40, 6);//getBestMove(scoredMoves);  40-6
        return result;
    }

    private Move rateMove(IGameState state, Move move) {

        move = checkForOpponentMacroWin(state, move);
        move = checkForMacroWin(state, move);
        move = checkForMacroBlock(state, move);
        move = checkForLocalWin(state, move);
        move = checkForLocalBlock(state, move);
        move = checkForOpponentLocalWin(state, move);


        /**
         * move = checkForPresenceInMicro(state, move);
        move = checkForOpponentLocalWin(state, move);
        move = checkForOpponentMacroWin(state, move);
         move = checkForPossibleLocalWin(state, move);
        move = checkForPresenceInMicro(state, move);
         */
        return move;
    }

    private Move checkForPossibleLocalWin(IGameState state, Move move) {
       GameSimulator g = new GameSimulator(state);
       String[][] board = state.getField().getBoard();

        int localX = move.getX() % 3;
        int localY = move.getY() % 3;
        int startX = move.getX() - (localX);
        int startY = move.getY() - (localY);



        //check col
        for (int i = startY; i < startY + 3; i++) {
            if (!board[move.getX()][i].equals("" + g.currentPlayer)){
                if (!board[move.getX()][i].equals(".")) {
                    break;}
            }
            if(board[move.getX()][i].equals("" + g.currentPlayer))move.setScore(move.getScore() + isOwend);
            if (i == startY + 3 - 1) {
                move.setScore(move.getScore() + isTheMoveStillLocalWinnable);
            }
        }

        //check row
        for (int i = startX; i < startX + 3; i++) {
            if (!board[i][move.getY()].equals("" +g.currentPlayer )){
                if (!board[move.getX()][i].equals(".")) {
                    break;}
            }
            if(board[i][move.getY()].equals("" +g.currentPlayer )) move.setScore(move.getScore() + isOwend);
            if (i == startX + 3 - 1) {
                move.setScore(move.getScore() + isTheMoveStillLocalWinnable);
            }
        }

        //check diagonal
        if (localX == localY) {
            //we're on a diagonal
            int y = startY;
            for (int i = startX; i < startX + 3; i++) {
                if (!board[i][y++].equals("" + g.currentPlayer) ){
                    if (!board[move.getX()][i].equals(".")) {
                        break;}
                }
                if (i == startX + 3 - 1){
                    move.setScore(move.getScore() + isTheMoveStillLocalWinnable);
                    //System.out.println("the move can still lead to local win  " + move);
                }
            }
        }

        //check anti diagonal
        if (localX + localY == 3 - 1) {
            int less = 0;
            for (int i = startX; i < startX + 3; i++) {
                if (!board[i][(startY + 2) - less++].equals("" + g.currentPlayer)){
                    if (!board[move.getX()][i].equals(".")) {
                        break;}
                }
                if (i == startX + 3 - 1) {
                    move.setScore(move.getScore() + isTheMoveStillLocalWinnable);
                }
            }
        }
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

        if (g.updateGame(move)) {//checks if move is possible before updating board
            List<IMove> rootMoves = g.getCurrentState().getField().getAvailableMoves();
            List<Move> scoredMoves = new ArrayList<>();
            //makes moves into scoreMoves
            for (IMove moves : rootMoves) {
                Move scoreMove = new Move(moves.getX(), moves.getY());
                scoredMoves.add(scoreMove);
            }
            //runs through all opponent moves to see if they will win
            for (int i = 0; scoredMoves.size() > i; i++) {
                Move opponentMove = scoredMoves.get(i);
                if (g.updateGame(opponentMove)) {
                    if (g.getGameOver() == GameOverState.Win) {
                        move.setScore(move.getScore() + opponenCanWinMacroInNextMove);
                       // System.out.println("watch out the opponent wil win if you play " + scoredMoves + "  score  : " + move.getScore());
                    }
                }
            }
        }
        return move;
    }


    private Move checkForMacroWin(IGameState state, Move move) {
        GameSimulator g = createSimulator(state);
        String currentPlayer = String.valueOf(g.currentPlayer);//gets the current player before we make a simulated move
        if (g.updateGame(move)) {//checks if move is possible before updating board
            if (g.getGameOver() == GameOverState.Win) {
                move.setScore(move.getScore() + macroWin);//adds point to the move if it wins local
            }
        }
        return move;
    }

    private Move checkForMacroBlock(IGameState state, Move move) {
        GameSimulator g = createSimulator(state);
        int currentPlayer = g.currentState.getMoveNumber() % 2 == 0 ? 1 : 0;//gets the current player before we make a simulated move
        g.setCurrentPlayer(currentPlayer);
        if(g.updateGame(move)){//checks if move is possible before updating board
            if(g.getGameOver()== GameOverState.Win){
                move.setScore(move.getScore() + macroBlock);//adds point to the move if it wins local
            }
        }
        return move;
    }

    private Move checkForPresenceInMicro(IGameState state, Move move) {
        GameSimulator g = createSimulator(state);
        String[][] board = g.getCurrentState().getField().getBoard();
        String currentPlayer = "" + g.currentState.getMoveNumber() % 2;//gets the current player before we make a simulated move
        String opponent = g.currentState.getMoveNumber() % 2 == 0 ? "1" : "0";

        int localX = move.getX() % 3;
        int localY = move.getY() % 3;
        int startX = move.getX() - (localX);
        int startY = move.getY() - (localY);

        //check col
        for (int i = startY; i < startY + 3; i++) {
            if (board[move.getX()][i].equals(currentPlayer)) {
                move.setScore(move.getScore() + localPresence);
            }
            if (board[move.getX()][i].equals(opponent)) {
                move.setScore(move.getScore() + localUnwinnable);
                break;
            }

            if (i == startY + 3 - 1) {
                move.setScore(move.getScore() + localWinnable);
            }
        }

        //check row
        for (int i = startX; i < startX + 3; i++) {
            if (board[i][move.getY()].equals(currentPlayer)) {
                move.setScore(move.getScore() + localPresence);
            }
            if (board[i][move.getY()].equals(opponent)) {
                move.setScore(move.getScore() + localUnwinnable);
                break;
            }
            if (i == startX + 3 - 1) {
                move.setScore(move.getScore() + localWinnable);
            }
        }

        //check diagonal
        if (localX == localY) {
            //we're on a diagonal
            int y = startY;
            for (int i = startX; i < startX + 3; i++) {
                if (board[i][y].equals(currentPlayer)) {
                    move.setScore(move.getScore() + localPresence);
                }
                if (board[i][y++].equals(opponent)) {
                    move.setScore(move.getScore() + localUnwinnable);
                    break;
                }
                if (i == startX + 3 - 1) {
                    move.setScore(move.getScore() + localWinnable);
                }
            }
        }

        //check anti diagonal
        if (localX + localY == 3 - 1) {
            int less = 0;
            for (int i = startX; i < startX + 3; i++) {
                if (board[i][(startY + 2) - less].equals(currentPlayer)) {
                    move.setScore(move.getScore() + localPresence);
                }
                if (board[i][(startY + 2) - less++].equals(opponent)) {
                    move.setScore(move.getScore() + localUnwinnable);
                    break;
                }
                if (i == startX + 3 - 1) {
                    move.setScore(move.getScore() + localWinnable);
                }
            }
        }
        return move;
    }

    private IGameState simulateMove(Move move, IGameState state) {
        GameSimulator gs = createSimulator(state);

        rateMove(gs.getCurrentState(), move);

        gs.updateGame(move);

        return gs.getCurrentState();
    }

    private List<Move> simulateGames(List<Move> availableMoves, IGameState state, int expansions, int top, long end) {
        if (expansions > 0 && System.currentTimeMillis() < end) {
            for (Move moves : availableMoves) {
                GameSimulator gs = createSimulator(simulateMove(moves, state));
                if(gs.getGameOver() == GameOverState.Win){
                    moves.setScore(10000000);
                }
                List<Move> counterMoves = getTopCounterMoves(gs.getCurrentState(), top);
                simulateGames(counterMoves, gs.getCurrentState(), expansions - 1, top, end);
            }
        }

        return availableMoves;
    }

    private Move lookAhead(List<Move> availableMoves, IGameState state, int expansions, int top) {
        return getBestMove(simulateGames(availableMoves, state, expansions, top, System.currentTimeMillis() + 1000));
    }

    private List<Move> getAvailableScoredMoves(IGameState state) {
        GameSimulator gs = createSimulator(state);//gets created so we can get available moves
        List<IMove> rootMoves = gs.getCurrentState().getField().getAvailableMoves();//all possible moves from current state
        List<Move> scoredMoves = new ArrayList<>();

        //make each move into a score move that contains a score
        for (IMove moves : rootMoves) {
            Move scoreMove = new Move(moves.getX(), moves.getY());
            scoredMoves.add(scoreMove);
        }

        return scoredMoves;
    }

    private List<Move> getTopCounterMoves(IGameState state, int top) {
        List<IMove> rootMoves = state.getField().getAvailableMoves();
        List<Move> scoredMoves = new ArrayList<>();

        for (IMove rootMove : rootMoves) {
            Move newMove = new Move(rootMove.getX(), rootMove.getY());
            scoredMoves.add(newMove);
        }

        addMoveScores(scoredMoves, state);

        return getTopMoves(scoredMoves, top);
    }

    private Move getBestMove(List<Move> scoredMoves) {
        List<Move> bestMoves = new ArrayList<>();

        for (Move move : scoredMoves) {
            if (bestMoves.isEmpty()) {
                bestMoves.add(move);
            } else if (bestMoves.get(0).getScore() < move.getScore()) {
                bestMoves.clear();
                bestMoves.add(move);
            } else if (bestMoves.get(0).getScore() == move.getScore()) {
                bestMoves.add(move);
            }
        }

        if (bestMoves.size() > 1) {
            Random random = new Random();
            int randomIndex = random.nextInt(bestMoves.size());

            return bestMoves.get(randomIndex);
        }

        return bestMoves.get(0);
    }

    private void addMoveScores(List<Move> scoredMoves, IGameState state) {
        for (Move move : scoredMoves) {
            rateMove(state, move);
        }
    }

    private List<Move> getTopMoves(List<Move> scoredMoves, int howMany) {
        List<Move> sortedMoves = new ArrayList<>();
        sortedMoves.addAll(scoredMoves);
        sortedMoves.sort((o1, o2) -> {
            if (o1.getScore() < o2.getScore()) {
                return 1;
            } else if (o1.getScore() == o2.getScore()) {
                return 0;
            } else {
                return -1;
            }
        });

        /*List<Move> topMoves = new ArrayList<>();
        for (int i = 0; i < sortedMoves.size(); i++) {
            if (topMoves.isEmpty()) {
                topMoves.add(sortedMoves.get(i));
            }
            else if (topMoves.get(i - 1).getScore() == sortedMoves.get(i).getScore()) {
                topMoves.add(sortedMoves.get(i));
            }
            else {
                break;
            }
        }

        if (topMoves.size() > howMany) {
            List<Move> randomTopMoves = new ArrayList<>();

            for (int i = 0; i < howMany; i++) {
                Random random = new Random();
                int randomIndex = random.nextInt(topMoves.size());

                randomTopMoves.add(topMoves.get(randomIndex));
                topMoves.remove(topMoves.get(randomIndex));
            }

            return randomTopMoves;
        }*/

        return sortedMoves;
    }


    /**
     * checks if the opponent can win in a local Field after your move
     * @param state
     * @param move
     * @return
     */
    private Move checkForOpponentLocalWin(IGameState state, Move move) {
        GameSimulator g = createSimulator(state);

        if (g.updateGame(move)) {//checks if move is possible before updating board
            List<IMove> rootMoves = g.getCurrentState().getField().getAvailableMoves();
            List<Move> scoredMoves = new ArrayList<>();
            //makes moves into scoreMoves
            for (IMove moves : rootMoves) {
                Move scoreMove = new Move(moves.getX(), moves.getY());
                scoredMoves.add(scoreMove);
            }
            //runs through all opponent moves to see if they will win
            for (int i = 0; scoredMoves.size() > i; i++) {
                Move opponentMove = scoredMoves.get(i);
                opponentMove = checkForLocalWin(g.getCurrentState(), opponentMove);//checks for opponent local win
                if (opponentMove.getScore() > 0) {
                    move.setScore(move.getScore() + opponentLocalWinChance);
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
        if (g.updateGame(move)) {//checks if move is possible before updating board
            g.updateBoard(move);
            if (g.isWin(g.getCurrentState().getField().getBoard(), move, currentPlayer)) {//see if move will win miniBoard
                move.setScore(move.getScore() + localBlockPoint);//adds point to the move if it wins local
            }
        }
        return move;
    }


    private Move checkForLocalWin(IGameState state, Move move) {

        GameSimulator g = createSimulator(state);
        String currentPlayer = String.valueOf(g.currentPlayer);//gets the current player before we make a simulated move
        if (g.updateGame(move)) {//checks if move is possible before updating board
            if (g.isWin(g.getCurrentState().getField().getBoard(), move, currentPlayer)) {//see if move will win miniBoard
                move.setScore(move.getScore() + localWinPoint);//adds point to the move if it wins local
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
