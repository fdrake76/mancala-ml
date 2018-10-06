package com.freddrake.mancala.mancalaml.stats;

import lombok.Data;

@Data
public class GameBatch {
	private double wins = 0;
	private double ties = 0;
	private double losses = 0;
	private double score = 0;
}
