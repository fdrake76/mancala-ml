package com.freddrake.mancala.mancalaml.spring;

import com.freddrake.mancala.mancalaml.LazyInitializedFileOutputStream;
import com.freddrake.mancala.mancalaml.Player;
import com.freddrake.mancala.mancalaml.engine.KeyboardInputEngine;
import com.freddrake.mancala.mancalaml.engine.QLearningEngine;
import com.freddrake.mancala.mancalaml.engine.RandomEngine;
import com.freddrake.mancala.mancalaml.stats.ChartOutputter;
import com.freddrake.mancala.mancalaml.stats.Statistician;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.*;

@Configuration
public class AppConfiguration {
    private AppProperties appProperties;

    public AppConfiguration(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    @Scope("prototype")
    public QLearningEngine initialTrainableEngine(OutputStream networkOutputStream,
                                                  OutputStream statsObjectOutputStream) {
        return QLearningEngine.builder()
                .epsilon(appProperties.getTrainingEpsilon())
                .player(Player.PLAYER_ONE)
                .winReward(appProperties.getWinReward())
                .loseReward(appProperties.getLoseReward())
                .tieReward(appProperties.getTieReward())
                .trainable(true)
                .saveStatObjectsToStream(statsObjectOutputStream)
                .persistentOutputStream(networkOutputStream)
                .build();
    }

    @Bean
    @Scope("prototype")
    public QLearningEngine evolvingTrainableEngine(InputStream networkInputStream,
                                                   OutputStream networkOutputStream,
                                                   InputStream statsObjectInputStream,
                                                   OutputStream statsObjectOutputStream) {
        return QLearningEngine.builder()
                .epsilon(appProperties.getTrainingEpsilon())
                .player(Player.PLAYER_ONE)
                .winReward(appProperties.getWinReward())
                .loseReward(appProperties.getLoseReward())
                .tieReward(appProperties.getTieReward())
                .trainable(true)
                .loadStatObjectsFromStream(statsObjectInputStream)
                .saveStatObjectsToStream(statsObjectOutputStream)
                .loadFromStream(networkInputStream)
                .persistentOutputStream(networkOutputStream)
                .build();
    }

    @Bean
    @Scope("prototype")
    public QLearningEngine evolvingEngineOpponent(InputStream networkInputStream) {
        return QLearningEngine.builder()
                .epsilon(0.0)
                .player(Player.PLAYER_TWO)
                .winReward(appProperties.getWinReward())
                .loseReward(appProperties.getLoseReward())
                .tieReward(appProperties.getTieReward())
                .trainable(false)
                .loadFromStream(networkInputStream)
                .build();
    }

    @Bean
    @Scope("prototype")
    public QLearningEngine nonTrainingEngine(InputStream networkInputStream,
                                             InputStream statsObjectInputStream) {
        return QLearningEngine.builder()
                .epsilon(0.0)
                .player(Player.PLAYER_ONE)
                .trainable(false)
                .loadFromStream(networkInputStream)
                .loadStatObjectsFromStream(statsObjectInputStream)
                .build();
    }

    @Bean
    @Scope("prototype")
    public Statistician chartStatistician() {
        String fileName = appProperties.getStatistics().getOutputFile().replace(
                "${timestamp}", DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").format(ZonedDateTime.now()));
        LazyInitializedFileOutputStream outputStream = new LazyInitializedFileOutputStream(new File(fileName));
        return Statistician.builder()
                .gameBatchSize(appProperties.getStatistics().getGamesPerBatch())
                .announceAfterGames(appProperties.getStatistics().getAnnounceAfterGames())
                .outputter(ChartOutputter.builder()
                        .batchSize(appProperties.getStatistics().getGamesPerBatch())
                        .outputStream(outputStream)
                        .player(Player.PLAYER_ONE)
                        .playerName(Player.PLAYER_ONE, "Player 1")
                        .build())
                .build();
    }

    @Bean
    @Scope("prototype")
    public Statistician announceOnlyStatistician() {
        return Statistician.builder()
                .gameBatchSize(appProperties.getStatistics().getGamesPerBatch())
                .announceAfterGames(appProperties.getStatistics().getAnnounceAfterGames())
                .build();
    }

    @Bean
    public RandomEngine randomEngine() {
        return RandomEngine.builder().player(Player.PLAYER_TWO).build();
    }

    @Bean
    public KeyboardInputEngine keyboardInputEngine() {
        return KeyboardInputEngine.builder().player(Player.PLAYER_TWO).build();
    }

    @Bean
    @Scope("prototype")
    public InputStream networkInputStream() throws Exception {
        return new FileInputStream(new File(appProperties.getNetworkLocation()));
    }

    @Bean
    @Scope("prototype")
    public OutputStream networkOutputStream() {
        return new LazyInitializedFileOutputStream(new File(appProperties.getNetworkLocation()));
    }

    @Bean
    @Scope("prototype")
    public InputStream statsObjectInputStream() throws Exception {
        return new FileInputStream(new File(appProperties.getStatsDatabaseLocation()));
    }

    @Bean
    @Scope("prototype")
    public OutputStream statsObjectOutputStream() {
        return new LazyInitializedFileOutputStream(new File(appProperties.getStatsDatabaseLocation()));
    }
}
