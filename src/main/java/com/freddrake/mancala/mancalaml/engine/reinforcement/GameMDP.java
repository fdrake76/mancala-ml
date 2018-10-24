package com.freddrake.mancala.mancalaml.engine.reinforcement;

import com.freddrake.mancala.mancalaml.GameBoard;
import com.freddrake.mancala.mancalaml.Player;
import com.freddrake.mancala.mancalaml.engine.GamingEngine;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Optional;

@Slf4j
public class GameMDP implements MDP<GameObservation, Integer, DiscreteSpace> {
    private final GameBoard gameBoard;
    private final Player player;
    private final GameObservation observation;
    private final ObservationSpace<GameObservation> observationSpace;
    private final GamingEngine oppositionEngine;
    private final DiscreteSpace discreteSpace;

    @Builder
    private GameMDP(GameBoard gameBoard, @NonNull Player player, @NonNull GamingEngine oppositionEngine,
                    @NonNull DiscreteSpace discreteSpace) {
        this.gameBoard = Optional.ofNullable(gameBoard).orElse(new GameBoard());
        this.player = player;
        this.oppositionEngine = oppositionEngine;
        this.discreteSpace = discreteSpace;
        observation = new GameObservation(this.gameBoard, player);
        observationSpace = new ArrayObservationSpace<>(new int[]{this.gameBoard.pebbleField(player).length});
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    @Override
    public ObservationSpace<GameObservation> getObservationSpace() {
        return observationSpace;
    }

    @Override
    public DiscreteSpace getActionSpace() {
        return discreteSpace;
    }

    @Override
    public GameObservation reset() {
        gameBoard.resetGameBoard();
        return observation;
    }

    @Override
    public void close() {

    }

    @Override
    public StepReply<GameObservation> step(@NonNull Integer action) {
        int playerScore = gameBoard.playerPoints(player);
        boolean playAgain = gameBoard.executeMove(player, action + 1);
        int nextPlayerScore = gameBoard.playerPoints(player);

        int reward;
        if (nextPlayerScore == -1) {
            // We lost due to an illegal move.
            // TODO consider lowering this reward or make it parameterized
            reward = -1;
        } else {
            reward = nextPlayerScore - playerScore;
        }

        // TODO: Consider adding to reward if player gets to move again

        if (playAgain) {
            // Skip the opponent's move and let's play again
            return new StepReply<>(observation, reward, gameBoard.isGameOver(player), null);
        }

        // Opponent's turn to move
        if (oppositionEngine.isGameOver(gameBoard)) {
            // Opponent can't move, game is over.
            return new StepReply<>(observation, reward, true, null);
        }

        // Execute a move by the opponent, and let us know if they couldn't play because
        // the game was over.
        boolean gameOverForOpponent = oppositionEngine.executeMove(gameBoard);

        return new StepReply<>(observation, reward, gameBoard.isGameOver(player) || gameOverForOpponent, null);
    }

    @Override
    public boolean isDone() {
        return gameBoard.isGameOver(player);
    }

    @Override
    public MDP<GameObservation, Integer, DiscreteSpace> newInstance() {
        return GameMDP.builder()
                .gameBoard(gameBoard)
                .oppositionEngine(oppositionEngine)
                .player(player)
                .build();
    }

//    @Value
//    public class GameObservationSpace implements ObservationSpace {
//        private String name;
//        private int[] shape;
//        private INDArray low;
//        private INDArray high;
//
//        public GameObservationSpace() {
//            name = "Mancala";
//            shape = new int[]{12};
//            low = Nd4j.create(shape);
//            low.putScalar(0, 0);
//            high = Nd4j.create(shape);
//            high.putScalar(0, 48);
//        }
//    }
}
