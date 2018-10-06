package com.freddrake.mancala.mancalaml.stats;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freddrake.mancala.mancalaml.MancalaException;
import com.freddrake.mancala.mancalaml.GameBoard.Player;

import lombok.Builder;

@Builder
public class ChartOutputter implements StatsOutputter {
	private final Logger log = LoggerFactory.getLogger(ChartOutputter.class);
	private OutputStream outputStream;
	private int batchSize;

	@Override
	public void output(HashMap<Player, List<GameBatch>> gameBatches) {
		if (outputStream == null) {
			throw new MancalaException("No output stream defined");
		}
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries p1Series = new XYSeries("Player 1 Wins");		
		List<GameBatch> p1Batches = gameBatches.get(Player.PLAYER_ONE);		
		for(int i=0; i<p1Batches.size(); i++) {
			GameBatch p1B = p1Batches.get(i);
			p1Series.add(i+1, p1B.getWins());
		}
		dataset.addSeries(p1Series);
		
		JFreeChart chart = ChartFactory.createScatterPlot(
		        "Player Performance Over Time", 
		        "Games Played (per "+batchSize+" games)", "Number of wins", dataset);
		
		try {
			ChartUtils.writeChartAsJPEG(outputStream, chart, 800, 600);
		} catch (IOException e) {
			throw new MancalaException(e);
		}
	}
}
