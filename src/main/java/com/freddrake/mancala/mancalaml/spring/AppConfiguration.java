package com.freddrake.mancala.mancalaml.spring;

import com.freddrake.mancala.mancalaml.LazyInitializedFileOutputStream;
import com.freddrake.mancala.mancalaml.Player;
import com.freddrake.mancala.mancalaml.engine.RandomEngine;
import com.freddrake.mancala.mancalaml.engine.reinforcement.DQNEngine;
import com.freddrake.mancala.mancalaml.engine.reinforcement.GameMDP;
import com.freddrake.mancala.mancalaml.engine.reinforcement.GameObservation;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.nd4j.linalg.learning.config.Adam;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.nd4j.linalg.learning.config.Adam.*;

@Configuration
public class AppConfiguration {
    private AppProperties appProperties;

    public AppConfiguration(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public QLearning.QLConfiguration learningConfiguration() {
        return QLearning.QLConfiguration.builder()
                .seed(123)
                .maxEpochStep(200)
                .maxStep(500000)
                .expRepMaxSize(500000)
                .batchSize(32)
                .targetDqnUpdateFreq(1000)
                .updateStart(10)
                .rewardFactor(0.1)
                .gamma(0.90)
                .errorClamp(1.0)
                .minEpsilon(0.1f)
                .epsilonNbStep(10)
                .doubleDQN(true)
                .build();
    }

    @Bean
    public DQNFactoryStdDense.Configuration netConfiguration(StatsStorage statsStorage) {
        return DQNFactoryStdDense.Configuration.builder()
                .l2(0.01)
                .numLayer(1)
                .numHiddenNodes(250)
                .updater(Adam.builder()
                        .learningRate(DEFAULT_ADAM_LEARNING_RATE)
                        .beta1(DEFAULT_ADAM_BETA1_MEAN_DECAY)
                        .beta2(DEFAULT_ADAM_BETA2_VAR_DECAY)
                        .epsilon(DEFAULT_ADAM_EPSILON)
                        .build())
                .listeners(new TrainingListener[]{new StatsListener(statsStorage)})
                .build();
    }

    @Bean
    @Lazy
    public UIServer uiServer(StatsStorage statsStorage) {
        UIServer uiServer = UIServer.getInstance();
        uiServer.attach(statsStorage);
        return uiServer;
    }

    @Bean
    public StatsStorage statsStorage() {
        return new InMemoryStatsStorage();
    }

    @Bean
    public GameMDP randomGameMDP(RandomEngine randomEngine, ObservationSpace<GameObservation> observationSpace,
                                 DiscreteSpace discreteSpace) {
        return GameMDP.builder()
                .oppositionEngine(randomEngine)
                .player(Player.PLAYER_ONE)
                .discreteSpace(discreteSpace)
                .observationSpace(observationSpace)
                .illegalMoveReward(appProperties.getIllegalMoveReward())
                .build();
    }

    @Bean
    @Lazy
    public GameMDP dqlGameMDP(DQNEngine dqnEngine, ObservationSpace<GameObservation> observationSpace,
                              DiscreteSpace discreteSpace) {
        return GameMDP.builder()
                .oppositionEngine(dqnEngine)
                .player(Player.PLAYER_ONE)
                .discreteSpace(discreteSpace)
                .observationSpace(observationSpace)
                .illegalMoveReward(appProperties.getIllegalMoveReward())
                .build();
    }

    @Bean
    @Lazy
    public DQNEngine dqnEngine(ObservationSpace<GameObservation> observationSpace, InputStream networkInputStream) {
        return DQNEngine.builder()
                .player(Player.PLAYER_ONE)
                .observationSpace(observationSpace)
                .networkInputStream(networkInputStream)
                .build();
    }

    @Bean
    public RandomEngine randomEngine() {
        return RandomEngine.builder().player(Player.PLAYER_TWO).build();
    }

    @Bean
    @Scope("prototype")
    public DiscreteSpace discreteSpace() {
        return new DiscreteSpace(6);
    }
    @Bean
    public ObservationSpace<GameObservation> observationSpace() {
        return new ArrayObservationSpace<>(new int[]{12});
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
}
