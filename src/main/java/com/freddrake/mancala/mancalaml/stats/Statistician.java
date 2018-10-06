package com.freddrake.mancala.mancalaml.stats;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freddrake.mancala.mancalaml.GameBoard;
import com.freddrake.mancala.mancalaml.MancalaException;
import com.freddrake.mancala.mancalaml.GameBoard.Player;

import lombok.Builder;


public class Statistician {
	private final Logger log = LoggerFactory.getLogger(Statistician.class);
	private long gameBatchSize = 1;
	
	private long totalGames;
	private HashMap<Player, List<GameBatch>> gameBatches;
	private HashMap<Player, List<GameBatch>> singleBatch;
	private int announceAfterGames;
	private List<StatsOutputter> outputters;
	
	@Builder
	private Statistician(long gameBatchSize, Boolean writeHeaderRow, int announceAfterGames, 
			List<StatsOutputter> outputters) {
		this.gameBatchSize = gameBatchSize == 0 ? 1 : gameBatchSize;
		this.announceAfterGames = announceAfterGames;
		this.totalGames = 0;
		
		gameBatches = new HashMap<Player, List<GameBatch>>();
		gameBatches.put(Player.PLAYER_ONE, new ArrayList<GameBatch>());
		gameBatches.put(Player.PLAYER_TWO, new ArrayList<GameBatch>());
		singleBatch = new HashMap<Player, List<GameBatch>>();
		singleBatch.put(Player.PLAYER_ONE, new ArrayList<GameBatch>());
		singleBatch.put(Player.PLAYER_TWO, new ArrayList<GameBatch>());
		this.outputters = (outputters == null) ? Collections.emptyList() : outputters;
	}
	
	public void addTieGameResult(int score) {
		addGameResult(Player.NOBODY, score, Player.NOBODY, score);
	}
	
	public void addGameResult(Player winner, int winningScore, Player loser, int losingScore) {
		totalGames++;
		if (winner == Player.NOBODY || loser == Player.NOBODY || winningScore == losingScore) {
			GameBatch tiedBatch = new GameBatch();
			tiedBatch.setTies(1);
			tiedBatch.setScore(winningScore);
			singleBatch.get(Player.PLAYER_ONE).add(tiedBatch);
			singleBatch.get(Player.PLAYER_TWO).add(tiedBatch);
		} else {			
			GameBatch winningBatch = new GameBatch();
			winningBatch.setWins(1);
			winningBatch.setScore(winningScore);
			singleBatch.get(winner).add(winningBatch);
			
			GameBatch losingBatch = new GameBatch();
			losingBatch.setLosses(1);
			losingBatch.setScore(losingScore);
			singleBatch.get(loser).add(losingBatch);			
		}
		
		bundleBatches();
		
		if (announceAfterGames > 0 && totalGames % announceAfterGames == 0) {
			log.info("Processed {} games", totalGames);
		}
	}

	public void outputResults() {
		outputters.forEach(o -> o.output(gameBatches));
	}
	
	private void bundleBatches() {
		Arrays.stream(Player.values()).filter(p -> p != Player.NOBODY).forEach(p -> {
			List<GameBatch> batches = singleBatch.get(p);
			if (batches.size() == gameBatchSize) {
				// Average the values into a new batch and push it out
				GameBatch averaged = new GameBatch();
				averaged.setWins(batches.stream().mapToDouble(s -> s.getWins()).sum() / gameBatchSize);
				averaged.setTies(batches.stream().mapToDouble(s -> s.getTies()).sum() / gameBatchSize);
				averaged.setLosses(batches.stream().mapToDouble(s -> s.getLosses()).sum() / gameBatchSize);
				averaged.setScore(batches.stream().mapToDouble(s -> s.getScore()).sum() / gameBatchSize);
				gameBatches.get(p).add(averaged);
				batches.clear();
			}
		});
	}
}
