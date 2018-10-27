package com.freddrake.mancala.mancalaml.spring;


import com.freddrake.mancala.mancalaml.engine.reinforcement.DeepQLearningTrainer;
import com.freddrake.mancala.mancalaml.engine.reinforcement.GameMDP;
import lombok.AllArgsConstructor;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.ui.api.UIServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.OutputStream;

@Component
@Profile("train.initial")
@AllArgsConstructor
public class InitialTrainComponent implements CommandLineRunner {
    private GameMDP randomGameMDP;
    private QLearning.QLConfiguration learningConfiguration;
    private DQNFactoryStdDense.Configuration netConfiguration;
    private OutputStream networkOutputStream;
    private UIServer uiServer;

    @Override
    public void run(String... args) {
        DeepQLearningTrainer trainer = DeepQLearningTrainer.builder()
                .gameMDP(randomGameMDP)
                .learningConfiguration(learningConfiguration)
                .netConfiguration(netConfiguration)
                .networkOutputStream(networkOutputStream)
                .uiServer(uiServer)
                .build();

        trainer.train();
    }
}
