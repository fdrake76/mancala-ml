package com.freddrake.mancala.mancalaml.qlearning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freddrake.mancala.mancalaml.AbstractGamingEngine;
import com.freddrake.mancala.mancalaml.GameBoard;
import com.freddrake.mancala.mancalaml.GameBoard.Player;
import com.freddrake.mancala.mancalaml.MancalaException;

import lombok.Builder;

public class QLearningEngine extends AbstractGamingEngine {
	private final Logger log = LoggerFactory.getLogger(QLearningEngine.class);
	
	private final Player player;
	private MultiLayerNetwork network;
	private double epsilon;
	private Random random;
	private final List<ReplayMove> replayMemory;
	private File networkFile;
	private float winReward;
	private float loseReward;
	private float tieReward;
	private boolean trainable;
	
	@Builder
	private QLearningEngine(Player player, MultiLayerNetwork network, Double epsilon, Random random,
			Integer hiddenLayerCount, File networkFile, float winReward, float loseReward, 
			float tieReward, boolean trainable) {
		this.player = player;
		
		int inputLength = 12;
		hiddenLayerCount = (hiddenLayerCount == null) ? 150 : hiddenLayerCount;
		this.epsilon = (epsilon == null) ? .25f : epsilon;
		this.networkFile = networkFile;
		this.winReward = winReward;
		this.loseReward = loseReward;
		this.tieReward = tieReward;
		this.trainable = trainable;
		
		if (network == null) {
	        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
	          		 .seed(123)
	   	             .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
	   	             .list()
	   	             .layer(0, new DenseLayer.Builder().nIn(inputLength).nOut(hiddenLayerCount)
	   	            		 	.weightInit(WeightInit.XAVIER)
	   	            		 		.activation(Activation.RELU)
	   		                        .build())
	   	             .layer(1, new OutputLayer.Builder(LossFunction.MSE)
	   	                        .weightInit(WeightInit.XAVIER)
	   	                        .activation(Activation.IDENTITY)
	   	                        .weightInit(WeightInit.XAVIER)
	   	                        .nIn(hiddenLayerCount).nOut(6).build())
	   	             .pretrain(false).backprop(true).build();
	
			this.network = new MultiLayerNetwork(conf);
			this.network.init();
		} else {
			this.network = network;
		}

		replayMemory = new ArrayList<ReplayMove>();		
		if (random == null) {
			this.random = new Random();
		} else {
			this.random = random;
		}
	}
	
	public void setEpsilon(double e){
		epsilon = e;
	}
	
	public MultiLayerNetwork getNeuralNetwork() {
		return network;
	}
	
	@Override
	public int chooseMove(GameBoard gameBoard) {
		if (gameBoard.isGameOver(player)) {
			throw new MancalaException("Cannot choose move because game is over");
		}
		
		// Get action based on INDArray
		INDArray inputArray = getInputNDArray(gameBoard);
		List<Integer> legalMoves = gameBoard.validMoves(player);
		int chosenMove = -1;
		if (epsilon > random.nextDouble()) {
			// Ignore the engine and just choose a random move.
			log.debug("Going to make a random move");
			chosenMove = legalMoves.get(random.nextInt(legalMoves.size())) - 1;
		} else {
			log.debug("Input: {}", inputArray);
			INDArray outputArray = network.output(inputArray);
			log.debug("Output: {}", outputArray);
			for(int i=0; i<outputArray.length(); i++) {
				if (legalMoves.contains(i+1)) {
					if (chosenMove == -1) {
						chosenMove = i;
					} else {
						chosenMove = outputArray.getFloat(i) > outputArray.getFloat(chosenMove) 
								? i : chosenMove;				
					}				
				}
			}			
		}		
		
		if (chosenMove == -1) {
			throw new MancalaException("No legal move is possible");
		}
		
		if (trainable) {
			replayMemory.add(new ReplayMove(inputArray, chosenMove));						
		}
		
		return chosenMove + 1;
	}	
	
	@Override
	public void onAfterGame(GameBoard gameBoard) {
		if (!trainable) {
			return;
		}
		INDArray inputs = Nd4j.create(replayMemory.size(), gameBoard.pebbleField(player).length);
		IntStream.range(0, replayMemory.size()).forEach(
				index -> inputs.putRow(index, replayMemory.get(index).input));
		INDArray output = network.output(inputs);
		
		float reward;
		if (gameBoard.getPointsLeader().equals(player)) {
			reward = winReward;
		} else if (gameBoard.getPointsLeader().equals(Player.NOBODY)) {
			reward = tieReward;
		} else {
			reward = loseReward;
		}
		
		IntStream.range(0, replayMemory.size()).forEach( index -> {
			int ind[] = { index, replayMemory.get(index).action };
			output.putScalar(ind, reward);			
		});
		
		network.fit(inputs, output);
		
		replayMemory.clear();
	}
	
	/**
	 * Return the DL4J inputs for a given board state.
	 */
	public INDArray getInputNDArray(GameBoard board) {
		int[] pebbles = board.pebbleField(player);
		return Nd4j.create(IntStream.of(pebbles).asDoubleStream().toArray());
	}
	
	public void saveNetwork() {
		if (networkFile == null) {
			log.warn("No network file defined.  Nothing to save.");
			return;
		}
		
		try {
			ModelSerializer.writeModel(network, networkFile, true);
		} catch (IOException e) {
			throw new MancalaException(e);
		}
	}
	
	public void loadNetwork() {
		if (networkFile == null) {
			log.warn("No network file defined.  Nothing to load.");
			return;
		}
		
		try {
			network = ModelSerializer.restoreMultiLayerNetwork(networkFile);
		} catch (IOException e) {
			log.warn("Couldn't restore file.  Using blank network");
		}
	}
	
	private class ReplayMove {
		INDArray input;
		int action;
		
		public ReplayMove(INDArray input, int action) {
			this.input = input;
			this.action = action;
		}
	}
}
