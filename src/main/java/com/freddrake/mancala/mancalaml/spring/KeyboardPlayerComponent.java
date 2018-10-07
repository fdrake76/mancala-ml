package com.freddrake.mancala.mancalaml.spring;


import com.freddrake.mancala.mancalaml.GameSession;
import com.freddrake.mancala.mancalaml.engine.KeyboardInputEngine;
import com.freddrake.mancala.mancalaml.engine.QLearningEngine;
import com.freddrake.mancala.mancalaml.engine.RandomEngine;
import com.freddrake.mancala.mancalaml.stats.Statistician;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("human")
@AllArgsConstructor
public class KeyboardPlayerComponent implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(KeyboardPlayerComponent.class);
    private QLearningEngine nonTrainingEngine;
    private KeyboardInputEngine keyboardInputEngine;

    @Override
    public void run(String... args) {
        GameSession session =  GameSession.builder()
                .player1Engine(nonTrainingEngine)
                .player2Engine(keyboardInputEngine)
                .trainingGames(1)
                .build();

        log.info("Playing a game against the machine learning opponent.  You are player two.");

        session.train();
    }
}
