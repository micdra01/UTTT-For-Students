package dk.easv.bll.bot.kdm;

import dk.easv.bll.bot.kdm.gameSim.GameOverState;
import dk.easv.bll.bot.kdm.gameSim.GameSimulator;
import dk.easv.bll.field.Field;
import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;


import java.util.List;

public class State implements IGameState {
    //create the game at current state
    GameSimulator board;
    private IGameState gameState;


    int playerNo = 0;
    int visitCount;
    double winScore;
    int timePerMove = 1000;
    int roundNumber;
    int moveNumber;
    Field field;

    public State(IGameState gameState){
        field = new Field();
        field.setMacroboard(gameState.getField().getMacroboard());
        field.setBoard(gameState.getField().getBoard());
        playerNo = gameState.getRoundNumber()%0;

        moveNumber = gameState.getMoveNumber();
        roundNumber = gameState.getRoundNumber();
    }

    public State(){
        field = new Field();
        moveNumber=0;
        roundNumber=0;
    }

    // copy constructor, getters, and setters

    public List<IMove> getAllPossibleStates() {
        GameSimulator gs= createSimulator(gameState);
       return gs.getCurrentState().getField().getAvailableMoves();

    }
    public void randomPlay() {

    }

    public GameSimulator createSimulator(IGameState gameState) {
        GameSimulator simulator = new GameSimulator(gameState);
        simulator.setGameOver(GameOverState.Active);
        simulator.setCurrentPlayer(playerNo);
        simulator.getCurrentState().setRoundNumber(gameState.getRoundNumber());
        simulator.getCurrentState().setMoveNumber(gameState.getMoveNumber());
        simulator.getCurrentState().getField().setBoard(gameState.getField().getBoard());
        simulator.getCurrentState().getField().setMacroboard(gameState.getField().getMacroboard());
        return simulator;
    }



    @Override
    public IField getField() {
        return field;
    }

    @Override
    public int getMoveNumber() {
        return moveNumber;
    }

    @Override
    public void setMoveNumber(int moveNumber) {
        this.moveNumber=moveNumber;
    }

    @Override
    public int getRoundNumber() {
        return roundNumber;
    }

    @Override
    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    @Override
    public int getTimePerMove()
    {
        return this.timePerMove;
    }

    @Override
    public void setTimePerMove(int milliSeconds)
    {
        this.timePerMove = milliSeconds;
    }
}