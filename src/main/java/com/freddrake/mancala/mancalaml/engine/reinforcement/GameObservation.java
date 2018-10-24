package com.freddrake.mancala.mancalaml.engine.reinforcement;

import com.freddrake.mancala.mancalaml.GameBoard;
import com.freddrake.mancala.mancalaml.Player;
import lombok.RequiredArgsConstructor;
import org.deeplearning4j.rl4j.space.Encodable;

import java.util.stream.IntStream;

@RequiredArgsConstructor
public class GameObservation implements Encodable {
    private final GameBoard gameBoard;
    private final Player player;

    @Override
    public double[] toArray() {
        int[] pebbles = gameBoard.pebbleField(player);
        return IntStream.of(pebbles).asDoubleStream().toArray();
    }
}
