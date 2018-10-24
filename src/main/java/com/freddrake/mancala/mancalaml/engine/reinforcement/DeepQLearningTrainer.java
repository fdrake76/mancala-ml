package com.freddrake.mancala.mancalaml.engine.reinforcement;

import com.freddrake.mancala.mancalaml.MancalaException;
import com.freddrake.mancala.mancalaml.engine.Trainer;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.rl4j.learning.Learning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.network.dqn.DQN;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.util.DataManager;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.util.ModelSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class DeepQLearningTrainer implements Trainer {
    private QLearning.QLConfiguration learningConfiguration;
    private DQNFactoryStdDense.Configuration netConfiguration;
    private GameMDP gameMDP;
    private InputStream networkInputStream;
    private OutputStream networkOutputStream;
    private UIServer uiServer;

    @Builder
    public DeepQLearningTrainer(@NonNull QLearning.QLConfiguration learningConfiguration,
                                @NonNull DQNFactoryStdDense.Configuration netConfiguration,
                                @NonNull GameMDP gameMDP,
                                InputStream networkInputStream,
                                OutputStream networkOutputStream,
                                UIServer uiServer) {
        this.learningConfiguration = learningConfiguration;
        this.netConfiguration = netConfiguration;
        this.gameMDP = gameMDP;
        this.networkInputStream = networkInputStream;
        this.networkOutputStream = networkOutputStream;
        this.uiServer = uiServer;
    }

    @Override
    public void train() {
        DataManager dataManager;
        try {
            dataManager = new DataManager(true);
        } catch (IOException e) {
            throw new MancalaException(e);
        }

        IDQN dqn;
        if (networkInputStream == null) {
            // Create a fresh new network
            DQNFactoryStdDense dqnFactory = new DQNFactoryStdDense(netConfiguration);
            dqn =
                    dqnFactory.buildDQN(
                            gameMDP.getObservationSpace().getShape(), gameMDP.getActionSpace().getSize());
        } else {
            // Load from input stream
            try {
                dqn = new DQN(ModelSerializer.restoreMultiLayerNetwork(networkInputStream));

            } catch (IOException e) {
                throw new MancalaException(e);
            }
        }

        Learning<GameObservation, Integer, DiscreteSpace, IDQN> dql = new QLearningDiscreteDense(
                gameMDP, dqn, learningConfiguration, dataManager);


        dql.train();
        log.info("Done training");


        if (networkOutputStream != null) {
            try {
                dqn.save(networkOutputStream);

            } catch (IOException e) {
                throw new MancalaException(e);
            }
        }

        gameMDP.close();

        if (uiServer != null) {
            uiServer.stop();
        }
        log.info("Done saving.");
    }
}
