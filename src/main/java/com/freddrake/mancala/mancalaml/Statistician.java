package com.freddrake.mancala.mancalaml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freddrake.mancala.mancalaml.GameBoard.Player;

import lombok.Builder;


public class Statistician {
	private final Logger log = LoggerFactory.getLogger(Statistician.class);
	private long gameBatchSize = 1;
	
	private long totalGames;
	private HashMap<Player, List<GameBatch>> gameBatches;
	private HashMap<Player, List<GameBatch>> singleBatch;
	private Writer outputWriter;
	private boolean writeHeaderRow;
	private int announceAfterGames;
	
	@Builder
	private Statistician(long gameBatchSize, Writer outputWriter, Boolean writeHeaderRow, 
			int announceAfterGames) {
		this.gameBatchSize = gameBatchSize == 0 ? 1 : gameBatchSize;
		this.outputWriter = outputWriter;
		this.writeHeaderRow = writeHeaderRow != null ? writeHeaderRow : true;
		this.announceAfterGames = announceAfterGames;
		this.totalGames = 0;
		
		gameBatches = new HashMap<Player, List<GameBatch>>();
		gameBatches.put(Player.PLAYER_ONE, new ArrayList<GameBatch>());
		gameBatches.put(Player.PLAYER_TWO, new ArrayList<GameBatch>());
		singleBatch = new HashMap<Player, List<GameBatch>>();
		singleBatch.put(Player.PLAYER_ONE, new ArrayList<GameBatch>());
		singleBatch.put(Player.PLAYER_TWO, new ArrayList<GameBatch>());
	}
	
	public void addTieGameResult(int score) {
		addGameResult(Player.NOBODY, score, Player.NOBODY, score);
	}
	
	public void addGameResult(Player winner, int winningScore, Player loser, int losingScore) {
		totalGames++;
		if (winner == Player.NOBODY || loser == Player.NOBODY || winningScore == losingScore) {
			GameBatch tiedBatch = new GameBatch();
			tiedBatch.ties = 1;
			tiedBatch.score = winningScore;
			singleBatch.get(Player.PLAYER_ONE).add(tiedBatch);
			singleBatch.get(Player.PLAYER_TWO).add(tiedBatch);
		} else {			
			GameBatch winningBatch = new GameBatch();
			winningBatch.wins = 1;
			winningBatch.score = winningScore;
			singleBatch.get(winner).add(winningBatch);
			
			GameBatch losingBatch = new GameBatch();
			losingBatch.losses = 1;
			losingBatch.score = losingScore;
			singleBatch.get(loser).add(losingBatch);			
		}
		
		bundleBatches();
		
		if (announceAfterGames > 0 && totalGames % announceAfterGames == 0) {
			log.info("Processed {} games", totalGames);
		}
	}
	
	public void dumpBatchesToCsv() {
		if (outputWriter == null) {
			log.warn("No outputwriter defined");
			return;
		}
		
		List<GameBatch> p1Batches = gameBatches.get(Player.PLAYER_ONE);
		List<GameBatch> p2Batches = gameBatches.get(Player.PLAYER_TWO);
		
		if (writeHeaderRow) {
			try {
				outputWriter.write("P1Wins,P1Ties,P1Losses,P1Score,P2Wins,P2Ties,P2Losses,P2Score");
				outputWriter.write(System.lineSeparator());
			} catch (IOException e) {
				throw new MancalaException(e);
			}			
		}
		
		for(int i=0; i<p1Batches.size(); i++) {
			GameBatch p1B = p1Batches.get(i);
			GameBatch p2B = p2Batches.get(i);
			try {
				outputWriter.write(p1B.wins+","+p1B.ties+","+p1B.losses+","+p1B.score+","+
							p2B.wins+","+p2B.ties+","+p2B.losses+","+p2B.score);
				outputWriter.write(System.lineSeparator());
			} catch (IOException e) {
				throw new MancalaException(e);
			}
		}
	}
	
	private void bundleBatches() {
		Arrays.stream(Player.values()).filter(p -> p != Player.NOBODY).forEach(p -> {
			List<GameBatch> batches = singleBatch.get(p);
			if (batches.size() == gameBatchSize) {
				// Average the values into a new batch and push it out
				GameBatch averaged = new GameBatch();
				averaged.wins = batches.stream().mapToDouble(s -> s.wins).sum() / gameBatchSize;
				averaged.ties = batches.stream().mapToDouble(s -> s.ties).sum() / gameBatchSize;
				averaged.losses = batches.stream().mapToDouble(s -> s.losses).sum() / gameBatchSize;
				averaged.score = batches.stream().mapToDouble(s -> s.score).sum() / gameBatchSize;
				gameBatches.get(p).add(averaged);
				batches.clear();
			}
		});
	}
	
	private class GameBatch {
		double wins = 0;
		double ties = 0;
		double losses = 0;
		double score = 0;
	}
}
