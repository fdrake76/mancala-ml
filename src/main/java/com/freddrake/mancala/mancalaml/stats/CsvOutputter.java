package com.freddrake.mancala.mancalaml.stats;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freddrake.mancala.mancalaml.MancalaException;
import com.freddrake.mancala.mancalaml.Player;

import lombok.Builder;

@Builder
public class CsvOutputter implements StatsOutputter {
	private final Logger log = LoggerFactory.getLogger(CsvOutputter.class);
	private Writer outputWriter;
	private boolean writeHeaderRow;

	@Override
	public void output(HashMap<Player, List<GameBatch>> gameBatches) {
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
				outputWriter.write(p1B.getWins()+","+p1B.getTies()+","+p1B.getLosses()+","+p1B.getScore()+","+
							p2B.getWins()+","+p2B.getTies()+","+p2B.getLosses()+","+p2B.getScore());
				outputWriter.write(System.lineSeparator());
			} catch (IOException e) {
				throw new MancalaException(e);
			}
		}
	}

}
