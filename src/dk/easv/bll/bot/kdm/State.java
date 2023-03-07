package dk.easv.bll.bot.kdm;

import dk.easv.bll.bot.kdm.gameSim.GameOverState;
import dk.easv.bll.bot.kdm.gameSim.GameSimulator;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;


import java.util.List;

public class State{
    //create the game at current state
    GameSimulator board;
    private IGameState gameState;


    int playerNo;
    int visitCount;
    double winScore;

    public State(IGameState gameState){
        this.gameState = gameState;
    }

    // copy constructor, getters, and setters

    public List<IMove> getAllPossibleStates() {
        GameSimulator gs= createSimulator(gameState);
       return gs.getCurrentState().getField().getAvailableMoves();

    }
    public void randomPlay() {
        /* get a list of all possible positions on the board and
           play a random move */
    }

    private GameSimulator createSimulator(IGameState gameState) {
        GameSimulator simulator = new GameSimulator(gameState);
        simulator.setGameOver(GameOverState.Active);
        simulator.setCurrentPlayer(playerNo);
        simulator.getCurrentState().setRoundNumber(gameState.getRoundNumber());
        simulator.getCurrentState().setMoveNumber(gameState.getMoveNumber());
        simulator.getCurrentState().getField().setBoard(gameState.getField().getBoard());
        simulator.getCurrentState().getField().setMacroboard(gameState.getField().getMacroboard());
        return simulator;
    }
}

