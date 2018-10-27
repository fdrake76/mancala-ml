package com.freddrake.mancala.mancalaml.spring;


import com.freddrake.mancala.mancalaml.engine.reinforcement.DeepQLearningTrainer;
import com.freddrake.mancala.mancalaml.engine.reinforcement.GameMDP;
import lombok.AllArgsConstructor;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.ui.api.UIServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;

@Component
@Profile("train.network")
@AllArgsConstructor
public class NetworkTrainComponent implements CommandLineRunner {
    private GameMDP dqlGameMDP;
    private QLearning.QLConfiguration learningConfiguration;
    private InputStream networkInputStream;
    private OutputStream networkOutputStream;
    private UIServer uiServer;
    private StatsStorage statsStorage;

    @Override
    public void run(String... args) {
        DeepQLearningTrainer trainer = DeepQLearningTrainer.builder()
                .gameMDP(dqlGameMDP)
                .networkInputStream(networkInputStream)
                .learningConfiguration(learningConfiguration)
                .networkOutputStream(networkOutputStream)
                .uiServer(uiServer)
                .statsStorage(statsStorage)
                .build();

        trainer.train();
    }
}
