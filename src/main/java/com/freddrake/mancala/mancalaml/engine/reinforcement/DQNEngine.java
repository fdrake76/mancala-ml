package com.freddrake.mancala.mancalaml.engine.reinforcement;

import com.freddrake.mancala.mancalaml.GameBoard;
import com.freddrake.mancala.mancalaml.MancalaException;
import com.freddrake.mancala.mancalaml.Player;
import com.freddrake.mancala.mancalaml.engine.AbstractGamingEngine;
import lombok.Builder;
import lombok.NonNull;
import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.learning.Learning;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.dqn.DQN;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.space.ActionSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.IOException;
import java.io.InputStream;

public class DQNEngine extends AbstractGamingEngine {

    private DQNPolicy<GameObservation> policy;
    private ObservationSpace<GameObservation> observationSpace;

    @Builder
    private DQNEngine(@NonNull InputStream networkInputStream,
                      @NonNull ObservationSpace<GameObservation> observationSpace,
                      @NonNull Player player) {
        this.observationSpace = observationSpace;
        this.player = player;

        IDQN dqn;
        try {
            dqn = new DQN(ModelSerializer.restoreMultiLayerNetwork(networkInputStream));
        } catch (IOException e) {
            throw new MancalaException(e);
        }

        policy = new DQNPolicy<>(dqn);
    }

    @Override
    public int chooseMove(GameBoard gameBoard) {
        GameObservation observation = new GameObservation(gameBoard, player);
        INDArray input = getInput(observation);
        return policy.nextAction(input) + 1;
    }

    @Override
    public boolean isGameOver(GameBoard gameBoard) {
        return gameBoard.isGameOver(player);
    }

    // The native library takes in an MDP, but only uses it for the observation space.  Building a full
    // MDP just to get the observation space is pretty heavy, so this method abstracts it out.
    private INDArray getInput(GameObservation observation) {
        return Learning.getInput(new MDP<GameObservation, Integer, DiscreteSpace>(){
            @Override
            public ObservationSpace<GameObservation> getObservationSpace() {
                return observationSpace;
            }

            @Override
            public DiscreteSpace getActionSpace() {
                return null;
            }

            @Override
            public GameObservation reset() { return null; }

            @Override
            public void close() {}

            @Override
            public StepReply<GameObservation> step(Integer action) { return null; }

            @Override
            public boolean isDone() { return false; }

            @Override
            public MDP<GameObservation, Integer, DiscreteSpace> newInstance() { return null; }
        }, observation);
    }
}
