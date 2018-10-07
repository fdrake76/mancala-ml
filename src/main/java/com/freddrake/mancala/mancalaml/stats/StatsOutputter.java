package com.freddrake.mancala.mancalaml.stats;

import java.util.HashMap;
import java.util.List;

import com.freddrake.mancala.mancalaml.Player;

public interface StatsOutputter {
	public void output(HashMap<Player, List<GameBatch>> gameBatches);
}
