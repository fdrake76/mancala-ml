package com.freddrake.mancala.mancalaml.engine;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.IntStream;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freddrake.mancala.mancalaml.GameBoard;
import com.freddrake.mancala.mancalaml.Player;
import com.freddrake.mancala.mancalaml.MancalaException;

import lombok.Builder;
import lombok.NonNull;

public class QLearningEngine extends AbstractGamingEngine {
	private final Logger log = LoggerFactory.getLogger(QLearningEngine.class);
	
	private final Player player;
	private MultiLayerNetwork network;
	private double epsilon;
	private Random random;
	private final List<ReplayMove> replayMemory;
	private float winReward;
	private float loseReward;
	private float tieReward;
	private boolean trainable;
	private OutputStream persistentOutputStream;
	private int randomSeed;
	private InputStream loadStatObjectsFromStream;
	private OutputStream saveStatObjectsToStream;


	private HashMap<String, Long> gamesMap;
    private HashMap<String, Long> winsMap;
	
	@Builder
	private QLearningEngine(Player player, MultiLayerNetwork network, Double epsilon, Random random,
                            Integer hiddenLayerCount, float winReward, float loseReward, float tieReward,
                            boolean trainable, InputStream loadFromStream, OutputStream persistentOutputStream,
                            Integer randomSeed, InputStream loadStatObjectsFromStream,
                            OutputStream saveStatObjectsToStream) {
		this.player = player;

		int inputLength = 12;
		hiddenLayerCount = Optional.ofNullable(hiddenLayerCount).orElse(150);
		this.epsilon = Optional.ofNullable(epsilon).orElse(.25);
		this.winReward = winReward;
		this.loseReward = loseReward;
		this.tieReward = tieReward;
		this.trainable = trainable;
		this.persistentOutputStream = persistentOutputStream;
		this.randomSeed = Optional.ofNullable(randomSeed).orElse (123);

		this.loadStatObjectsFromStream = loadStatObjectsFromStream;
		this.saveStatObjectsToStream = saveStatObjectsToStream;

		if (network == null) {
            if (loadFromStream != null) {
				this.loadNetwork(loadFromStream);
			} else {
		        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
		          		 .seed(this.randomSeed)
		   	             .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
		   	             .list()
		   	             .layer(0, new DenseLayer.Builder().nIn(inputLength).nOut(hiddenLayerCount)
		   	            		 	.weightInit(WeightInit.XAVIER)
		   	            		 		.activation(Activation.RELU)
		   		                        .build())
                         .layer(1, new DenseLayer.Builder().nIn(hiddenLayerCount)
                                    .nOut(hiddenLayerCount).weightInit(WeightInit.XAVIER)
                                    .activation(Activation.RELU).build())
                        .layer(2, new DenseLayer.Builder().nIn(hiddenLayerCount)
                                .nOut(hiddenLayerCount).weightInit(WeightInit.XAVIER)
                                .activation(Activation.RELU).build())
                        .layer(3, new DenseLayer.Builder().nIn(hiddenLayerCount)
                                .nOut(hiddenLayerCount).weightInit(WeightInit.XAVIER)
                                .activation(Activation.RELU).build())
		   	             .layer(4, new OutputLayer.Builder(LossFunction.MSE)
		   	                        .weightInit(WeightInit.XAVIER)
		   	                        .activation(Activation.IDENTITY)
		   	                        .nIn(hiddenLayerCount).nOut(6).build())
		   	             .pretrain(false).backprop(true).build();
		
				this.network = new MultiLayerNetwork(conf);
				this.network.init();
			}
		} else {
			this.network = network;
		}

		UIServer uiServer = UIServer.getInstance();
		StatsStorage statsStorage = new InMemoryStatsStorage();
		uiServer.attach(statsStorage);
		this.network.setListeners(new StatsListener(statsStorage));

		replayMemory = new ArrayList<>();
		if (random == null) {
			this.random = new Random();
		} else {
			this.random = random;
		}
	}
	
	public MultiLayerNetwork getNeuralNetwork() {
		return network;
	}
	
	@Override
	public int chooseMove(@NonNull GameBoard gameBoard) {
		if (gameBoard.isGameOver(player)) {
			throw new MancalaException("Cannot choose move because game is over");
		}

		sendBoardToLog(gameBoard);
		
		// Get action based on INDArray
		INDArray inputArray = getInputNDArray(gameBoard);
		List<Integer> legalMoves = gameBoard.validMoves(player);
		int chosenMove = -1;
		if (epsilon > random.nextDouble()) {
			// Ignore the engine and just choose a random move.
			log.debug("Going to make a random move");
			chosenMove = legalMoves.get(random.nextInt(legalMoves.size())) - 1;
		} else {
			log.trace("Input: {}", inputArray);
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
		    // Note that in the replay memory the chosen move is zero-based, but we output to the user
            // as one-based.
			replayMemory.add(new ReplayMove(inputArray, gameBoard.pebbleField(player), chosenMove));
		}

		log.info("{} chose move {}", player, chosenMove+1);
		
		return chosenMove + 1;
	}

	@Override
    public void onAfterMove(GameBoard gameBoard) {
	    sendBoardToLog(gameBoard);
    }

	@Override
	public void onAfterGame(@NonNull GameBoard gameBoard) {
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

//        replayMemory.forEach(move -> {
//            String key = getDbKey(move.pebbles, move.action);
//            if (gameBoard.getPointsLeader().equals(player)) {
//                long wins = Optional.ofNullable(winsMap.get(key)).orElse(0L);
//                winsMap.put(key, ++wins);
//            }
//            long games = Optional.ofNullable(gamesMap.get(key)).orElse(0L);
//            gamesMap.put(key, ++games);
//
//        });

		replayMemory.clear();
	}

	@Override
    public void onBeforeSession() {
        if (loadStatObjectsFromStream == null) {
            log.warn("Couldn't load statistics from file because input object stream doesn't exist.  " +
                    "Using blank values.");
            return;
        }

        if (winsMap == null && gamesMap == null) {
            log.info("Loading statistics objects from disk.");
            try {
                ObjectInputStream inputStream = new ObjectInputStream(loadStatObjectsFromStream);
                winsMap = (HashMap<String, Long>) inputStream.readObject();
                gamesMap = (HashMap<String, Long>) inputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new MancalaException(e);
            }
        }
    }

    @Override
	public void onAfterSession() {
	    if (saveStatObjectsToStream == null) {
	        log.warn("Couldn't save statistics to file because output object stream doesn't exist.");
	        return;
        }

        log.info("Saving statistic objects to disk.");
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(saveStatObjectsToStream);
            outputStream.writeObject(winsMap);
            outputStream.writeObject(gamesMap);
        } catch (IOException e) {
            throw new MancalaException(e);
        }

    }
	
	/**
	 * Return the DL4J inputs for a given board state.
	 */
	public INDArray getInputNDArray(@NonNull GameBoard board) {
		int[] pebbles = board.pebbleField(player);
		return Nd4j.create(IntStream.of(pebbles).asDoubleStream().toArray());
	}
	
	public void saveNetwork() {
		if (persistentOutputStream == null) {
			log.warn("No output stream defined.  Nothing to save.");
			return;
		}
		
		try {
			ModelSerializer.writeModel(network, persistentOutputStream, true);
		} catch (IOException e) {
			throw new MancalaException(e);
		}
	}
	
	private void loadNetwork(@NonNull InputStream inputStream) {
		try {
			Nd4j.getRandom().setSeed(this.randomSeed);
			network = ModelSerializer.restoreMultiLayerNetwork(inputStream);
		} catch (IOException e) {
			throw new MancalaException(e);
		}
	}

	private void sendBoardToLog(GameBoard gameBoard) {
        if (!log.isInfoEnabled()) {
            return;
        }

        int[] pebbles = gameBoard.pebbleField(player);
        double[] pctWinByMove = new double[pebbles.length / 2];
        long[] gamesByMove = new long[pebbles.length / 2];
        if (player == Player.PLAYER_ONE) {
            StringBuilder sb = new StringBuilder();
            sb.append("Opponent ").append(String.format("%1$2s", gameBoard.playerPoints(Player.PLAYER_TWO)))
                    .append(": ## ");
            for(int i=pebbles.length-1; i>=pebbles.length/2; i--) {
                sb.append("(").append(String.format("%1$2s", pebbles[i])).append(" ) ");
            }
            sb.append("##");
            log.info(sb.toString());

            sb = new StringBuilder();
            sb.append("Engine   ").append(String.format("%1$2s", gameBoard.playerPoints(Player.PLAYER_ONE)))
                    .append(": ## ");
            for(int i=0; i<pebbles.length/2; i++) {
                sb.append("(").append(String.format("%1$2s", pebbles[i])).append(" ) ");
                pctWinByMove[i] = calcPctWinByMove(pebbles, i);
                gamesByMove[i] = getGamesByMove(pebbles, i);
            }
            sb.append("##");
            log.info(sb.toString());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Opponent ").append(String.format("%1$2s", gameBoard.playerPoints(Player.PLAYER_ONE)))
                    .append(": ## ");
            for(int i = pebbles.length/2 - 1; i>=0; i--) {
                sb.append("(").append(String.format("%1$2s", pebbles[i])).append(" ) ");
            }
            sb.append("##");
            log.info(sb.toString());

            sb = new StringBuilder();
            sb.append("Engine   ").append(String.format("%1$2s", gameBoard.playerPoints(Player.PLAYER_TWO)))
                    .append(": ## ");
            for(int i=0; i<pebbles.length/2; i++) {
                sb.append("(").append(String.format("%1$2s", pebbles[i + pebbles.length/2])).append(" ) ");
                pctWinByMove[i] = calcPctWinByMove(pebbles, i);
                gamesByMove[i] = getGamesByMove(pebbles, i);
            }
            sb.append("##");
            log.info(sb.toString());
        }

        if (winsMap != null && gamesMap != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Win %: ");
            DecimalFormat df = new DecimalFormat("#.##%");
            IntStream.range(0, pctWinByMove.length).forEach( i -> {
                if (pctWinByMove[i] < 0) {
                    sb.append("---");
                } else {
                    sb.append(i+1).append(": ").append(df.format(pctWinByMove[i]))
                            .append(" (").append(gamesByMove[i]).append(")");
                }
                if (i < pctWinByMove.length - 1) {
                    sb.append(", ");
                }
            });
            log.info(sb.toString());
        }
    }

    // move is zero-based choice.  Return -1 if no DB or never any record of scenario.
    private double calcPctWinByMove(int[] pebbles, int move) {
	    if (winsMap == null) return 0;
        String key = getDbKey(pebbles, move);
        long wins = Optional.ofNullable(winsMap.get(key)).orElse(0L);
        long games = getGamesByMove(pebbles, move);

        if (games == 0) {
            return -1;
        }

        return wins / (double)games;
    }

    private long getGamesByMove(int[] pebbles, int move) {
	    if (gamesMap == null) return 0;
        String key = getDbKey(pebbles, move);
        return Optional.ofNullable(gamesMap.get(key)).orElse(0L);
    }

    private String getDbKey(int[] pebbles, int move) {
	    StringBuilder sb = new StringBuilder();
        Arrays.stream(pebbles).forEach( p -> {
               sb.append(p).append('|');
        });
        sb.append(move);
        return sb.toString();
//	    List<Integer> key = Arrays.stream(pebbles).boxed().collect(Collectors.toList());
//	    key.add(move);
//        return key;
    }

	private class ReplayMove {
		INDArray input;
		int[] pebbles;
		int action;
		
		ReplayMove(INDArray input, int[] pebbles, int action) {
			this.input = input;
			this.pebbles = pebbles;
			this.action = action;
		}
	}
}
