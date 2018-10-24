package com.freddrake.mancala.mancalaml.spring;


import com.freddrake.mancala.mancalaml.engine.reinforcement.DeepQLearningPlay;
import com.freddrake.mancala.mancalaml.engine.reinforcement.DeepQLearningTrainer;
import com.freddrake.mancala.mancalaml.engine.reinforcement.GameMDP;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Profile("play.engine-vs-random")
@AllArgsConstructor
public class NetworkPlaysRandomComponent implements CommandLineRunner {
    private GameMDP gameMDP;
    private InputStream networkInputStream;

    @Override
    public void run(String... args) {
        DeepQLearningPlay player = DeepQLearningPlay.builder()
                .gameMDP(gameMDP)
                .networkInputStream(networkInputStream)
                .build();

        player.play();
    }
}
