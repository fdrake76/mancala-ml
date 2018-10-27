package com.freddrake.mancala.mancalaml.engine.reinforcement;

import com.freddrake.mancala.mancalaml.GameBoard;
import com.freddrake.mancala.mancalaml.MancalaException;
import com.freddrake.mancala.mancalaml.Player;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.rl4j.network.dqn.DQN;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.util.ModelSerializer;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class DeepQLearningPlay {
    private GameMDP gameMDP;
    private DQNPolicy<GameObservation> policy;

    @Builder
    private DeepQLearningPlay(@NonNull GameMDP gameMDP, @NonNull InputStream networkInputStream) {
        this.gameMDP = gameMDP;

        IDQN dqn;
        try {
            dqn = new DQN(ModelSerializer.restoreMultiLayerNetwork(networkInputStream));
        } catch (IOException e) {
            throw new MancalaException(e);
        }

        policy = new DQNPolicy<>(dqn);
    }

    public void play() {
        int wins = 0, losses = 0, ties = 0;
        for(int i=0; i<10; i++) {
            policy.play(gameMDP);
            GameBoard board = gameMDP.getGameBoard();
            log.info("Winner {}, engine points {}", board.getPointsLeader(), board.playerPoints(Player.PLAYER_ONE));

            switch (board.getPointsLeader()) {
                case PLAYER_ONE:
                    wins++;
                    break;
                case PLAYER_TWO:
                    losses++;
                    break;
                default:
                    ties++;
                    break;
            }
        }

        log.info("Record: {}-{}-{}", wins, ties, losses);
    }
}
