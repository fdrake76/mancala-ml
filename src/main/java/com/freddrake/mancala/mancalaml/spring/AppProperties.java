package com.freddrake.mancala.mancalaml.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app")
public class AppProperties {
    @Getter @Setter private String networkLocation;
    @Getter @Setter private double trainingEpsilon;
    @Getter @Setter private int illegalMoveReward;
    @Getter @Setter private float winReward;
    @Getter @Setter private float loseReward;
    @Getter @Setter private float tieReward;
    @Getter @Setter private int trainingGamesPerPass;
    @Getter @Setter private long evolvingTrainingPasses;
}
