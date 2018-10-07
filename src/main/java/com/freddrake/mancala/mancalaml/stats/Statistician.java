package com.freddrake.mancala.mancalaml.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lombok.Singular;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freddrake.mancala.mancalaml.Player;

import lombok.Builder;


public class Statistician {
	private final Logger log = LoggerFactory.getLogger(Statistician.class);
	private long gameBatchSize;
	
	private long sumGames;
	private HashMap<Player, Long> sumWins;
	private HashMap<Player, Long> sumScore;
	private HashMap<Player, List<GameBatch>> gameBatches;
	private HashMap<Player, List<GameBatch>> singleBatch;
	private int announceAfterGames;
	private List<StatsOutputter> outputters;
	
	@Builder
	private Statistician(long gameBatchSize, int announceAfterGames,
			@Singular List<StatsOutputter> outputters) {
		this.gameBatchSize = gameBatchSize == 0 ? 1 : gameBatchSize;
		this.announceAfterGames = announceAfterGames;
		this.sumGames = 0;

        gameBatches = new HashMap<>();
        singleBatch = new HashMap<>();
        sumWins = new HashMap<>();
        sumScore = new HashMap<>();
		Arrays.asList(Player.PLAYER_ONE, Player.PLAYER_TWO).forEach( p -> {
            gameBatches.put(p, new ArrayList<>());
            singleBatch.put(p, new ArrayList<>());
            sumWins.put(p, 0L);
            sumScore.put(p, 0L);
        });
		this.outputters = (outputters == null) ? Collections.emptyList() : outputters;
	}
	
	public void addTieGameResult(int score) {
		addGameResult(Player.NOBODY, score, Player.NOBODY, score);
	}
	
	public void addGameResult(Player winner, int winningScore, Player loser, int losingScore) {
		sumGames++;
		if (winner == Player.NOBODY || loser == Player.NOBODY || winningScore == losingScore) {
			GameBatch tiedBatch = new GameBatch();
			tiedBatch.setTies(1);
			tiedBatch.setScore(winningScore);
			singleBatch.get(Player.PLAYER_ONE).add(tiedBatch);
			singleBatch.get(Player.PLAYER_TWO).add(tiedBatch);
            sumScore.put(winner, sumScore.get(Player.PLAYER_ONE) + winningScore);
            sumScore.put(loser, sumScore.get(Player.PLAYER_TWO) + winningScore);
		} else {
			GameBatch winningBatch = new GameBatch();
			winningBatch.setWins(1);
			winningBatch.setScore(winningScore);
			singleBatch.get(winner).add(winningBatch);
			
			GameBatch losingBatch = new GameBatch();
			losingBatch.setLosses(1);
			losingBatch.setScore(losingScore);
			singleBatch.get(loser).add(losingBatch);

			sumWins.put(winner, sumWins.get(winner) + 1);
            sumScore.put(winner, sumScore.get(winner) + winningScore);
            sumScore.put(loser, sumScore.get(loser) + losingScore);
		}

        bundleBatches();
		
		if (announceAfterGames > 0 && sumGames % announceAfterGames == 0) {
			log.info("Processed {} games", sumGames);
		}
	}

	public void outputResults() {
	    double avgWin = sumWins.get(Player.PLAYER_ONE) / (double) sumGames;
	    double avgScore = sumWins.get(Player.PLAYER_ONE) / (double) sumGames;
        log.info("Player one wins {} (avg score {}) out of {} games", avgWin, avgScore, sumGames);
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
