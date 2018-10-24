package com.freddrake.mancala.mancalaml.stats;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.freddrake.mancala.mancalaml.MancalaException;
import com.freddrake.mancala.mancalaml.Player;

import lombok.Builder;
import lombok.Singular;

@Builder
public class ChartOutputter implements StatsOutputter {
	private OutputStream outputStream;
	@Builder.Default private int batchSize = 1;
	@Builder.Default private String chartTitle = "Player Performance Over Time";
	@Singular private List<Player> players;
	@Singular private Map<Player, String> playerNames;

	@Override
	public void output(HashMap<Player, List<GameBatch>> gameBatches) {
		if (outputStream == null) {
			throw new MancalaException("No output stream defined");
		}
		
		if (players == null || players.isEmpty()) {
			players = Collections.singletonList(Player.PLAYER_ONE);
		}
		
		XYSeriesCollection dataset = new XYSeriesCollection();

		players.forEach(player -> {
			String playerName = playerNames.get(player) == null ? 
					player.name() : playerNames.get(player); 
			XYSeries series = new XYSeries(playerName+" Wins");		
			List<GameBatch> batches = gameBatches.get(player);		
			for(int i=0; i<batches.size(); i++) {
				GameBatch gb = batches.get(i);
				series.add(i+1, gb.getWins());
			}
			dataset.addSeries(series);			
		});

		String xAxisLabel = "Games Played";
		if (batchSize > 1) {
			xAxisLabel = xAxisLabel+" (per "+batchSize+" games)";
		}
		JFreeChart chart = ChartFactory.createScatterPlot(
		        chartTitle, xAxisLabel, "Number of wins", dataset);
		XYPlot plot = chart.getXYPlot();
		NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
		rangeAxis.setRange(0.0, 1.0);

		// Trend line
        for (Player player : players) {
            double[] coeffs = Regression.getOLSRegression(dataset, 0);
            LineFunction2D linefunction2d = new LineFunction2D(coeffs[0], coeffs[1]);
            XYDataset series2 = DatasetUtils.sampleFunction2D(
                    linefunction2d, 0, gameBatches.get(player).size(), 5, "Linear Regression Line");
            plot.setDataset(2, series2);
        }

        XYLineAndShapeRenderer lineDrawer = new XYLineAndShapeRenderer(true, false);
        lineDrawer.setSeriesPaint(0, Color.green);
        plot.setRenderer(2, lineDrawer);
		try {
			ChartUtils.writeChartAsJPEG(outputStream, chart, 800, 600);
		} catch (IOException e) {
			throw new MancalaException(e);
		}
	}
}
