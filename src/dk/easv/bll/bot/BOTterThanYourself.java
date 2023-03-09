
package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.*;


public class BOTterThanYourself implements IBot {
    final int moveTimeMs = 1000;
    private String BOT_NAME = getClass().getSimpleName();

    private int localWinPoint = 50;

    /**
     * gets moves that result in a win on mini Board.
     */
    private Move checkForLocalWin(IGameState state, Move move){

            GameSimulator g = createSimulator(state);
            String currentPlayer = String.valueOf(g.currentPlayer);//gets the current player before we make a simulated move
            if(g.updateGame(move)){//checks if move is possible before updating board
                 if(g.isWin(g.getCurrentState().getField().getBoard(), move, currentPlayer)){//see if move will win miniBoard
                     move.setScore(localWinPoint);//adds point to the move if it wins local
                     System.out.println("vi fandt en ");
                 }
            }
        return move;
    }

    @Override
    public IMove doMove(IGameState state) {

        //start of method gets the first available moves and creates a list of scored moves
        GameSimulator gs = createSimulator(state);//gets created so we can get available moves
        List<IMove> rootMoves = gs.getCurrentState().getField().getAvailableMoves();//all possible moves from current state
        List<Move> scoredMoves = new ArrayList<>();


        //make each move into a score move that contains a score
        for(IMove moves: rootMoves) {
            Move scoreMove = new Move(moves.getX(), moves.getY());
            scoredMoves.add(scoreMove);
        }


        //runs through all filters on each available move
        //todo all filter methods should be placed here
        for(Move move: scoredMoves ) {

            //cheks for local win in this round
            move = checkForLocalWin(state, move);


        }




        //gets the highest score move and plays it
        scoredMoves.sort(Comparator.comparing(Move::getScore));
        Move result = new Move(0,0);// fake reference move
        for(Move move: scoredMoves ){;
            if(move.getScore() >= result.getScore()){
                result = move;
            }
        }
        //if all node are 0 it plays random
        if (result.getScore() == 0){
            Random r = new Random();
            System.out.println("dont have a good move");
            return scoredMoves.get(r.nextInt(scoredMoves.size()));
        }
        return result;
    }

          //////////////////////////////////////////////////////////////////////////////////////////////////////
          //                                    master plan

          // todo    a long list of methods that looks at how good a move is fx. localWin, BlockPlayer MacroWin..
          // todo    each method should take a list of moves check if the current situations is filled and score accordingly
          // todo   fx. a move that leads to a local win an amount of points and opponent win gives minus points
          // todo    we should save each score point a an instance variable,
          // todo   fx amount of points added to a move when localWin is obtained should be saved in top of our bot class so we can twitch the amount of point easy at a later time
          // todo   when all filter/point giver methods have ended and all the points are added up on the available moves we should play the one with most points



          // todo   all methods are not necessary but will all help for a more qualified move,
          /////////////////////////////////////////////////////////////////////////////////////////////


          /**
           * //todo filter through a list of moves and score them points if they block opponent
           * //todo if the move blocks it gains some points
           */


          /**
           * todo a filter that sees if the opponent can get a micro win in next move
           * todo if the opponent can win a miniBoard the move should be reduced in points
           */

          /**
           * todo a filter that checks if the opponent can get a macroWin
           * todo should only run if 2 macro fields are won already
           * todo if the move leads to opponent macro win it should get lowest score
           */

          /**
           * todo a filter that checks if player can get a macro win in 2 turns
           * todo should only be run late late game, when 2 fields are taken
           * todo if we can win the move should be given a high score
           */


          /**
           * todo make a filter that looks for which rows are still possible to get 3 in a row
           * todo should score lower than a local win and block, but still some points, so we know the local filed can still be won
           */

          /**
           * todo
           */


        //todo sort list of  after points and return the move with most points






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
