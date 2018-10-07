package com.freddrake.mancala.mancalaml.engine;

import java.util.List;
import java.util.Random;

import com.freddrake.mancala.mancalaml.GameBoard;
import com.freddrake.mancala.mancalaml.Player;
import com.freddrake.mancala.mancalaml.MancalaException;

import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Builder
public class RandomEngine extends AbstractGamingEngine {
    private static final Logger log = LoggerFactory.getLogger(RandomEngine.class);
	@Builder.Default private Random random = new Random();
	private Player player;
	
	@Override
	public int chooseMove(GameBoard gameBoard) {
		List<Integer> validMoves = gameBoard.validMoves(player);
		if (validMoves.isEmpty()) {
			throw new MancalaException("Cannot play a move");
		}

		int move = validMoves.get(random.nextInt(validMoves.size()));
        log.info("{} chose move {}", player, move);
        return move;
	}

}
