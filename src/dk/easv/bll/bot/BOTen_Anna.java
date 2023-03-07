package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.util.*;

public class BOTen_Anna implements IBot {

    private final String bot_name = "BOTen Anna";



    @Override
    public IMove doMove(IGameState state) {
        MonteCarloTreeSearch mcts = new MonteCarloTreeSearch();
        MonteState monteState = new MonteState(state);
        Node naisuNode = mcts.findNextMove(monteState, monteState.getMoveNumber());
        IMove move = null;
        for (int i = 0; i < naisuNode.getState().getField().getBoard().length; i++) {
            for (int j = 0; j < naisuNode.getState().getField().getBoard()[i].length; j++) {
                String naisuMove = naisuNode.getState().getField().getBoard()[i][j];
                String parentMove = naisuNode.getParent().getState().getField().getBoard()[i][j];

                if (!parentMove.equals(naisuMove)) {
                    move = new Move(i, j);
                }
            }
        }

        /*long time = System.currentTimeMillis();
        Random rand = new Random();
        int count = 0;
        long maxTimeMs = 1000;
        while (System.currentTimeMillis() < time + maxTimeMs) { // check how much time has passed, stop if over maxTimeMs
            GameSimulator simulator = createSimulator(state);
            IGameState gs = simulator.getCurrentState();
            List<IMove> moves = gs.getField().getAvailableMoves();
            IMove randomMovePlayer = moves.get(rand.nextInt(moves.size()));
            IMove winnerMove = randomMovePlayer;

            while (simulator.getGameOver()== GameOverState.Active){ // Game not ended
                simulator.updateGame(randomMovePlayer);

                // Opponent plays randomly
                //todo here we should filter through how our opponent plays
                //todo the opponent should play as optimal as possible so we need to make less calculations before finding lose or win
                if (simulator.getGameOver()== GameOverState.Active){ // game still going
                    moves = gs.getField().getAvailableMoves();
                    IMove randomMoveOpponent = moves.get(rand.nextInt(moves.size()));
                    simulator.updateGame(randomMoveOpponent);
                }

                //todo here we play our move, it should also be as good a move as possible so we minimize possible outcomes
                //players move
                if (simulator.getGameOver()== GameOverState.Active){ // game still going
                    moves = gs.getField().getAvailableMoves();
                    randomMovePlayer = moves.get(rand.nextInt(moves.size()));
                }
            }

            //todo here we should score winning moves vs losing so we can se what the best outcome is.
            if (simulator.getGameOver()== GameOverState.Win){
                //System.out.println("Found a win, :)");
                return winnerMove; // Hint you could maybe save multiple games and pick the best? Now it just returns at a possible victory
            }
            count++;
        }
        */

        /**
         * picks a random move
         */
        //System.out.println("Did not win, just doing random :¨(");
        /*List<IMove> moves = state.getField().getAvailableMoves();
        IMove randomMovePlayer = moves.get(rand.nextInt(moves.size()));
        return randomMovePlayer; // just play randomly if solution not found
         */
        return move;
    }

    @Override
    public String getBotName() {
        return bot_name;
    }

    public class MonteState extends GameState { // Board and playerNo are not needed, as they are implemented in GameState.
        int visitCount; // Total number of games finished from this Node.
        double winScore; // Total number of games won, from this Node.
        IMove lastMove;

        public MonteState(IGameState state) {
            super(state);
            visitCount = 0;
            winScore = 0;
        }

        public MonteState(IGameState state, IMove lastMove) {
            super(state);
            visitCount = 0;
            winScore = 0;
            this.lastMove = lastMove;
        }

        // TODO look into why this doesn't return anything.
        public List<MonteState> getAllPossibleStates() {
            List<MonteState> possibleStates = new ArrayList<>();
            List<IMove> availableMoves = getField().getAvailableMoves();
            GameSimulator gameSimulator;

            for (IMove move : availableMoves) {
                gameSimulator = new GameSimulator(new MonteState(this));
                gameSimulator.updateBoard(move);
                possibleStates.add(new MonteState(gameSimulator.getCurrentState(), move));
            }

            return possibleStates;
        }

        public void randomMove() {
            Random random = new Random();
            List<MonteState> possibleStates = getAllPossibleStates();
            System.out.println(possibleStates.size());
            int randomIndex = random.nextInt(possibleStates.size());


            getField().setBoard(possibleStates.get(randomIndex).getField().getBoard());
            setLastMove(possibleStates.get(randomIndex).getLastMove());
        }

        public void setLastMove(IMove lastMove) {
            this.lastMove = lastMove;
        }

        public IMove getLastMove() {
            return lastMove;
        }

        public int getVisitCount() {
            return visitCount;
        }

        public double getWinScore() {
            return winScore;
        }

        public int getOpponent() {
            return getMoveNumber() == 0 ? 1 : 0;
        }

        public void incrementVisitCount() {
            visitCount++;
        }

        public void addWinScore(double score) {
            winScore += score;
        }

        public void setWinScore(double score) {
            this.winScore = score;
        }

        public void togglePlayer() {
            setMoveNumber(getMoveNumber() == 0 ? 1 : 0);
        }
    }

    public class Node {
        MonteState gameState;
        Node parent; // Parent of the current Node.
        List<Node> children; // List of all possible moves, from the current Node.

        public Node() {
            children = new ArrayList<>();
        }

        public Node(MonteState gameState) {
            this.gameState = gameState;
            children = new ArrayList<>();
        }

        public Node(Node node) {
            this.gameState = node.getState();
            this.parent = node.getParent();
            this.children = node.getChildren();
        }

        public Node getChildWithMaxScore() {
            Node bestNode = this;

            for (Node child : children) {
                if (bestNode.getState().getWinScore() < child.getState().getWinScore()) {
                    bestNode = child;
                }
            }

            return bestNode;
        }

        public List<Node> getChildren() {
            return children;
        }

        public MonteState getState() {
            return gameState;
        }

        public Node getRandomChild() {
            Random random = new Random();
            int randomIndex = random.nextInt(children.size());

            return children.get(randomIndex);
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public Node getParent() {
            return parent;
        }
    }

    public class Tree {
        Node root;

        public Tree(MonteState state) {
            root = new Node(state);
        }

        public void setRoot(Node node) {
            this.root = node;
        }

        public Node getRoot() {
            return root;
        }
    }

    public class UCT {
        public static double uctValue(int totalVisit, double nodeWinScore, int nodeVisit) {
            if (nodeVisit == 0) {
                return Integer.MAX_VALUE;
            }

            double bestNode = nodeWinScore / (double) nodeVisit;
            double exploration = 1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);

            return bestNode + exploration;
        }

        public static Node findBestNodeWithUCT(Node node) {
            int parentVisit = node.getState().getVisitCount();

            return Collections.max(node.getChildren(), Comparator.comparing((c -> uctValue(parentVisit, c.getState().getWinScore(), c.getState().getVisitCount()))));
        }
    }

    public class MonteCarloTreeSearch {
        public static int WIN_SCORE = 10;
        private int oponent;

        public Node findNextMove(MonteState state, int playerNo) {
            long maxTime = System.currentTimeMillis() + 1000;

            oponent = state.getOpponent();
            Tree tree = new Tree(state);
            Node rootNode = tree.getRoot();
            rootNode.getState().setMoveNumber(oponent);

            while (System.currentTimeMillis() < maxTime) {
                Node promisingNode = selectPromisingNode(rootNode);
                GameSimulator gameSimulator = new GameSimulator(promisingNode.getState());
                if (gameSimulator.gameOver == GameOverState.Active) {
                    expandNode(promisingNode);
                }

                Node nodeToExplore = promisingNode;
                if (promisingNode.getChildren().size() > 0) {
                    nodeToExplore = promisingNode.getRandomChild();
                }

                int playoutResult = simulateRandomPlayout(nodeToExplore);
                backPropogation(nodeToExplore, playoutResult);
            }

            Node winnerNode = rootNode.getChildWithMaxScore();
            tree.setRoot(winnerNode);
            return winnerNode;
        }

        private Node selectPromisingNode(Node rootNode) {
            Node node = rootNode;
            while (node.getChildren().size() != 0) {
                node = UCT.findBestNodeWithUCT(node);
            }

            return node;
        }

        private void expandNode(Node node) {
            List<MonteState> possibleStates = node.getState().getAllPossibleStates();

            possibleStates.forEach(state -> {
                Node newNode = new Node(state);
                newNode.setParent(node);
                newNode.getState().setMoveNumber(node.getState().getOpponent());
                node.getChildren().add(newNode);
            });
        }

        private void backPropogation(Node nodeToExplore, int playerNo) {
            Node tempNode = nodeToExplore;
            while (tempNode != null) {
                tempNode.getState().incrementVisitCount();
                if (tempNode.getState().getMoveNumber() == playerNo) {
                    tempNode.getState().addWinScore(WIN_SCORE);
                }
                tempNode = tempNode.getParent();
            }
        }

        private int simulateRandomPlayout(Node node) {
            Node tempNode = new Node(node);
            MonteState tempState = tempNode.getState();
            GameSimulator gameSimulator = new GameSimulator(tempState);
            GameOverState boardStatus = gameSimulator.getGameOver();

            // Checks to see if the opponent has won
            if (boardStatus == GameOverState.Win && tempState.getMoveNumber() == oponent) {
                tempNode.getParent().getState().setWinScore(Integer.MIN_VALUE);
                return oponent;
            }

            // Simulates moves until the game is decided
            while (boardStatus == GameOverState.Active) {
                tempState.togglePlayer();
                tempState.randomMove();
                gameSimulator.updateGame(tempState.getLastMove());
                boardStatus = gameSimulator.getGameOver();
            }

            if (boardStatus == GameOverState.Tie) {
                return -1;
            }

            return tempState.getMoveNumber();
        }
    }


























    public enum GameOverState {
        Active,
        Win,
        Tie
    }


    /*
        The code below is a simulator for simulation of gameplay. This is needed for AI.

        It is put here to make the Bot independent of the GameManager and its subclasses/enums

        Now this class is only dependent on a few interfaces: IMove, IField, and IGameState

        You could say it is self-contained. The drawback is that if the game rules change, the simulator must be
        changed accordingly, making the code redundant.

     */


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

    public class Move implements IMove {
        int x = 0;
        int y = 0;

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
}
