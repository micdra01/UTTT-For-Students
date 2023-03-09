
package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

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
 * todo check if opponent can get a micro win in next move if you make an move
 * todo should take move in parameter
 * todo simulate the players move
 * todo check if opponent has an local win move
 * todo if the opponent can win a miniBoard the move should be reduced in points
 *
 */

/**
 * todo check if the opponent can get a macroWin in next turn
 * todo should only run if opponent can get local win on local board  (as it cant happen else... so waste of time)
 * todo should only run if 2 macro fields are won already
 * todo if the move leads to opponent macro win it should get lowest score
 */

/**
 * todo checks if our bot player can get a macro win in its next turns
 * todo should only be run late late game, when 2 fields are taken
 * todo if we can win the move should be given a high score
 */


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





public class BOTterThanYourself implements IBot {
    final int moveTimeMs = 1000;
    private String BOT_NAME = getClass().getSimpleName();


    //todo here the points for whatever situation the method is checking for, so we can optimise via fine tuning
    private int localWinPoint = 50; //points when the move leads to an local win
    private int opponentLocalWinChance = -51; // points when move leads to enemy getting local win in next round




    /**
     * gets moves that result in a win on mini Board.
     */

    @Override
    public IMove doMove(IGameState state) {
        //start of method gets the first available moves and creates a list of scored moves
        /*GameSimulator gs = createSimulator(state);//gets created so we can get available moves
        List<IMove> rootMoves = gs.getCurrentState().getField().getAvailableMoves();//all possible moves from current state
        List<Move> scoredMoves = new ArrayList<>();

        //make each move into a score move that contains a score
        for (IMove moves : rootMoves) {
            Move scoreMove = new Move(moves.getX(), moves.getY());
            scoredMoves.add(scoreMove);
        }*/

        List<Move> scoredMoves = getAvailableScoredMoves(state);

        List<Move> topMoves = getTopMoves(scoredMoves, 3);

        lookAhead(topMoves, state);
        /*for (Move topMove : topMoves) {
            GameSimulator gs = createSimulator(state);
            gs.updateGame(topMove);

            List<Move> topCounterMoves = getTopCounterMoves(gs);

            for (Move topCounterMove : topCounterMoves) {
                GameSimulator gs2 = createSimulator(gs.getCurrentState());
                gs2.updateGame(topCounterMove);

                List<Move> topCountersToCounters = getTopCounterMoves(gs2);

                topCounterMove.setScore(topCounterMove.getScore() + getBestMove(topCountersToCounters).getScore());
            }

            topMove.setScore(topMove.getScore() + getBestMove(topCounterMoves).getScore());
        }*/


        //runs through all filters on each available move


        /*for (Move move : scoredMoves) {

            //todo all filter/ point methods methods should be placed here
            move = checkForLocalWin(state, move);  //checks for local win in this round

            move = checkForLocalBlock(state, move);  //checks for possible block moves

            move = checkForOpponentLocalWin(state, move);//checks for opponent local wins in next move


        }*/


        //gets the highest score move and plays it

       // Move result = new Move(0, 0, 0);// fake reference move
        Move result = getBestMove(topMoves);
        /*for (Move move : scoredMoves) {
            if (move.getScore() >= result.getScore()) {
                result = move;
            }
        }*/
        //if all node are 0 it plays random
        if (result.getScore() <= -2) {
            Random r = new Random();
            int i = r.nextInt(scoredMoves.size());
            scoredMoves.sort(Comparator.comparing(Move::getScore));
            System.out.println("random move = " + scoredMoves.get(0) + "ud af = " + scoredMoves.size() +  " moves" + "  score:  " + scoredMoves.get(0).getScore());
            return scoredMoves.get(0);
        }

        System.out.println("kvalificeret bud =  " + result + "  score  : " + result.getScore());
        return result;
    }

    private void lookAhead(List<Move> topMoves, IGameState state) {
        for (Move topMove : topMoves) {
            GameSimulator gs = createSimulator(state);
            gs.updateGame(topMove);

            addMoveScore(topMove, state);

            List<Move> topCounterMoves = getTopCounterMoves(gs);

            for (Move topCounterMove : topCounterMoves) {
                GameSimulator gs2 = createSimulator(gs.getCurrentState());
                gs2.updateGame(topCounterMove);

                addMoveScore(topCounterMove, gs.getCurrentState());

                List<Move> topCountersToCounters = getTopCounterMoves(gs2);

                for (Move topCounterToCounters : topCountersToCounters) {
                    GameSimulator gs3 = createSimulator(gs2.getCurrentState());
                    gs3.updateGame(topCounterToCounters);

                    addMoveScore(topCounterToCounters, gs2.getCurrentState());
                }

                topCounterMove.setScore(topCounterMove.getScore() - getBestMove(topCountersToCounters).getScore());
            }

            topMove.setScore(topMove.getScore() - getBestMove(topCounterMoves).getScore());
        }
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

    private List<Move> getTopCounterMoves(GameSimulator gs) {
        List<IMove> rootMoves = gs.getCurrentState().getField().getAvailableMoves();
        List<Move> scoredMoves = new ArrayList<>();

        for (IMove rootMove : rootMoves) {
            Move newMove = new Move(rootMove.getX(), rootMove.getY());
            scoredMoves.add(newMove);
        }

        addMoveScores(scoredMoves, gs.getCurrentState());

        return getTopMoves(scoredMoves, 3);
    }

    private Move getBestMove(List<Move> scoredMoves) {
        List<Move> bestMoves = new ArrayList<>();

        for (Move move : scoredMoves) {
            if (bestMoves.isEmpty()) {
                bestMoves.add(move);
            }
            else if (bestMoves.get(0).getScore() < move.getScore()) {
                bestMoves.clear();
                bestMoves.add(move);
            }
            else if (bestMoves.get(0).getScore() == move.getScore()) {
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
            addMoveScore(move, state);
        }
    }

    private void addMoveScore(Move move, IGameState state) {
        //todo all filter/ point methods methods should be placed here
        move = checkForLocalWin(state, move);  //checks for local win in this round

        move = checkForLocalBlock(state, move);  //checks for possible block moves

        move = checkForOpponentLocalWin(state, move);//checks for opponent local wins in next move
    }

    private List<Move> getTopMoves(List<Move> scoredMoves, int howMany) {
        List<Move> sortedMoves = new ArrayList<>();
        sortedMoves.addAll(scoredMoves);
        sortedMoves.sort((o1, o2) -> {
            if (o1.getScore() < o2.getScore()) {
                return 1;
            }
            else if (o1.getScore() == o2.getScore()) {
                return 0;
            }
            else {
                return -1;
            }
        });

        List<Move> topMoves = new ArrayList<>();
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
        }

        return sortedMoves.subList(0, howMany);
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
