package com.freddrake.mancala.mancalaml.spring;

import com.freddrake.mancala.mancalaml.GameSession;
import com.freddrake.mancala.mancalaml.engine.QLearningEngine;
import com.freddrake.mancala.mancalaml.engine.RandomEngine;
import com.freddrake.mancala.mancalaml.stats.Statistician;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.stream.LongStream;

@Component
@Profile("train.evolve")
@AllArgsConstructor
public class EvolvingTrainComponent implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(EvolvingTrainComponent.class);
    private AppProperties appProperties;
    private ApplicationContext appContext;
    private QLearningEngine evolvingTrainableEngine;
    private QLearningEngine evolvingEngineOpponent;
    private QLearningEngine nonTrainingEngine;
    private RandomEngine randomEngine;
    private Statistician chartStatistician;
    private Statistician announceOnlyStatistician;

    @Override
    public void run(String... args) {
        LongStream.range(0, appProperties.getEvolvingTrainingPasses()).forEach(i -> {
            log.info("Running evolving training pass {} of {} ({} games per pass).", i+1,
                    appProperties.getEvolvingTrainingPasses(), appProperties.getTrainingGamesPerPass());
            GameSession session =  GameSession.builder()
                    .player1Engine(evolvingTrainableEngine)
                    .player2Engine(evolvingEngineOpponent)
                    .trainingGames(appProperties.getTrainingGamesPerPass())
                    .statistician(announceOnlyStatistician)
                    .build();
            session.train();

            log.info("Executing 1000 games against the random opponent to test progress.");
            session = GameSession.builder()
                    .player1Engine(nonTrainingEngine)
                    .player2Engine(randomEngine)
                    .trainingGames(1000)
                    .statistician(chartStatistician)
                    .build();
            session.train();


            // Pull the prototype beans back down before running through this again
            evolvingTrainableEngine = appContext.getBean("evolvingTrainableEngine", QLearningEngine.class);
            evolvingEngineOpponent = appContext.getBean("evolvingEngineOpponent", QLearningEngine.class);
            announceOnlyStatistician = appContext.getBean("announceOnlyStatistician", Statistician.class);
            chartStatistician = appContext.getBean("chartStatistician", Statistician.class);
        });
    }
}
