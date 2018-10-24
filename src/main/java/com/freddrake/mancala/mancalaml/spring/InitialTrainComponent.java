package com.freddrake.mancala.mancalaml.spring;


import com.freddrake.mancala.mancalaml.GameSession;
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
@Profile("train.initial")
@AllArgsConstructor
public class InitialTrainComponent implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(InitialTrainComponent.class);
    private AppProperties appProperties;
    private QLearningEngine initialTrainableEngine;
    private RandomEngine randomEngine;
    private Statistician chartStatistician;

    @Override
    public void run(String... args) {
        GameSession session =  GameSession.builder()
                .player1Engine(initialTrainableEngine)
                .player2Engine(randomEngine)
                .trainingGames(appProperties.getTrainingGamesPerPass())
                .statistician(chartStatistician)
                .build();

        log.info("Starting neural network by running {} games against an opponent who makes random moves.",
                appProperties.getTrainingGamesPerPass());
        session.train();
    }
}
