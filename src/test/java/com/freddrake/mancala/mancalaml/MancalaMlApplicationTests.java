package com.freddrake.mancala.mancalaml;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Random;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.freddrake.mancala.mancalaml.GameBoard.Player;
import com.freddrake.mancala.mancalaml.qlearning.QLearningEngine;
import com.freddrake.mancala.mancalaml.random.RandomEngine;

import static com.freddrake.mancala.mancalaml.GameBoard.Player.PLAYER_ONE;
import static com.freddrake.mancala.mancalaml.GameBoard.Player.PLAYER_TWO;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class MancalaMlApplicationTests {
	/**
	 * Simple, fast way to build a game board for all of the tests.  The first numbers are
	 * the scores for each player, the rest are the number of pebbles in each pod.
	 */
	private GameBoard buildBoard(int[] boardValues) {
		return new GameBoard(boardValues[0], boardValues[1],
				Arrays.copyOfRange(boardValues, 2, boardValues.length));
	}
	
	@Test
	public void simpleDefaultBoard() {
		GameBoard board = new GameBoard();
		
		int[] startingPebbles = { 4, 4, 4, 4, 4, 4 };
		assertEquals(0, board.playerPoints(PLAYER_ONE));
		assertEquals(0, board.playerPoints(PLAYER_TWO));
		assertArrayEquals(startingPebbles, board.playerPebbles(PLAYER_ONE));
		assertArrayEquals(startingPebbles, board.playerPebbles(PLAYER_TWO));		
	}

	@Test
	public void simpleCustomBoard() {
		int[] boardLayout = {
				3, 4,
				2, 3, 3, 2, 3, 0,
				2, 0, 1, 3, 4, 2								
		};
		GameBoard board = buildBoard(boardLayout);
		
		assertEquals(boardLayout[0], board.playerPoints(PLAYER_ONE));
		assertEquals(boardLayout[1], board.playerPoints(PLAYER_TWO));
		assertArrayEquals(Arrays.copyOfRange(boardLayout, 2,8), board.playerPebbles(PLAYER_ONE));
		assertArrayEquals(Arrays.copyOfRange(boardLayout, 8, 14), board.playerPebbles(PLAYER_TWO));
	}

	@Test
	public void normalNonPointsMove() {
		int[] boardLayout = {
				0, 0,
				3, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0
		};
		GameBoard board = buildBoard(boardLayout);
		boolean retVal = board.executeMove(PLAYER_ONE, 1);
		
		assertFalse(retVal);
		assertArrayEquals(new int[] {0, 1, 1, 1, 0, 0}, board.playerPebbles(PLAYER_ONE));
	}
	
	@Test
	public void normalPointsMoveForPlayers() {
		int[] pebbles = { 
				0, 0,
				0, 0, 0, 4, 0, 0,
				0, 0, 0, 0, 0, 1
		};
		GameBoard board = buildBoard(pebbles);
		boolean retVal = board.executeMove(PLAYER_ONE, 4);
		
		assertFalse(retVal);
		assertArrayEquals(new int[] {0, 0, 0, 0, 1, 1}, board.playerPebbles(PLAYER_ONE));
		assertArrayEquals(new int[] {1, 0, 0, 0, 0, 1}, board.playerPebbles(PLAYER_TWO));
		assertEquals(1, board.playerPoints(PLAYER_ONE));
		assertEquals(0, board.playerPoints(PLAYER_TWO));
		
		retVal = board.executeMove(PLAYER_TWO, 6);
		
		assertTrue(retVal);
		assertArrayEquals(new int[] {0, 0, 0, 0, 1, 1}, board.playerPebbles(PLAYER_ONE));
		assertArrayEquals(new int[] {1, 0, 0, 0, 0, 0}, board.playerPebbles(PLAYER_TWO));
		assertEquals(1, board.playerPoints(PLAYER_ONE));		
		assertEquals(1, board.playerPoints(PLAYER_TWO));		
	}
	
	@Test
	public void gameNotOverYet() {
		int[] pebbles = { 
				0, 0,
				0, 0, 0, 4, 0, 0,
				0, 0, 0, 0, 0, 1
		};
		GameBoard board = buildBoard(pebbles);

		assertFalse(board.isGameOver(PLAYER_ONE));		
		assertFalse(board.isGameOver(PLAYER_TWO));		
	}
	
	@Test
	public void gameIsNowOver() {
		int[] pebbles = { 
				0, 0,
				0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 1
		};
		GameBoard board = buildBoard(pebbles);

		assertTrue(board.isGameOver(PLAYER_ONE));
		assertFalse(board.isGameOver(PLAYER_TWO));
	}
	
	@Test
	public void playerOneWins() {
		int[] pebbles = { 
				4, 3,
				0, 0, 0, 0, 0, 3,
				0, 0, 0, 0, 0, 0
		};
		GameBoard board = buildBoard(pebbles);

		assertEquals(PLAYER_ONE, board.getPointsLeader());
	}
	
	@Test
	public void playerTwoWins() {
		int[] pebbles = { 
				4, 5,
				0, 0, 0, 0, 0, 3,
				0, 0, 0, 0, 0, 0
		};
		GameBoard board = buildBoard(pebbles);

		assertEquals(PLAYER_TWO, board.getPointsLeader());
	}
	
	@Test
	public void playersTied() {
		int[] pebbles = { 
				5, 5,
				0, 0, 0, 0, 0, 3,
				0, 0, 0, 0, 0, 0
		};
		GameBoard board = buildBoard(pebbles);

		assertEquals(Player.NOBODY, board.getPointsLeader());
	}
	
	@Test
	public void testGameSession() {
		int[] pebbles = { 
				0, 0,
				0, 0, 0, 0, 1, 0,
				1, 4, 1, 1, 1, 0
		};
		GameBoard board = buildBoard(pebbles);

		Random random = mock(Random.class);
		when(random.nextInt(Mockito.anyInt())).thenReturn(1);
		GameSession session = GameSession.builder().gameBoard(board)
				.player1Engine(RandomEngine.builder()
						.player(PLAYER_ONE)
						.build()) // Leave real random generator, as there's only one choice
				.player2Engine(RandomEngine.builder()
						.player(PLAYER_TWO)
						.random(random).build())
				.build();
		
		session.playGame(PLAYER_ONE);
		
		assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0}, board.playerPebbles(PLAYER_ONE));
		assertArrayEquals(new int[] {1, 0, 2, 2, 2, 1}, board.playerPebbles(PLAYER_TWO));
		assertEquals(PLAYER_ONE, board.getPointsLeader());
	}
	
	@Test
	public void testSimpleQLearningEngineLosing() {
		QLearningEngine engine = QLearningEngine.builder()
				.player(PLAYER_ONE)
				.winReward(10)
				.loseReward(-10)
				.tieReward(0)
				.build();
		engine.setEpsilon(0f); // No random moves
		
		int[] pebbles = { 
				0, 0,
				0, 0, 0, 1, 0, 0,
				0, 0, 0, 0, 0, 1
		};
		GameBoard board = buildBoard(pebbles);
		GameSession session = GameSession.builder().gameBoard(board)
				.player1Engine(engine)
				.player2Engine(RandomEngine.builder()
						.player(PLAYER_TWO)
						.build())
				.build();
		
		MultiLayerNetwork network = engine.getNeuralNetwork();
		double moveScore = network.output(engine.getInputNDArray(board)).getDouble(3);
		
		// Each play of the game will involve the same moves (which ultimately leads to a loss).
		// Our expectation is that the neural network score for this move, in this scenario 
		// should go down after each game, as it increases confidence that the move will lead 
		// towards a loss.
		for (int i=0; i<3; i++) {
			session.playGame(PLAYER_ONE);			
			board.resetGameBoard(pebbles[0], pebbles[1], Arrays.copyOfRange(pebbles, 2, pebbles.length));
			double nextMoveScore = network.output(engine.getInputNDArray(board)).getDouble(3);
			assertTrue(nextMoveScore < moveScore);
			moveScore = nextMoveScore;
		}
	}
		
	@Test
	public void testSimpleQLearningEngineWinning() {
		QLearningEngine engine = QLearningEngine.builder()
				.player(PLAYER_ONE)
				.winReward(10)
				.loseReward(-10)
				.tieReward(0)
				.build();
		engine.setEpsilon(0f); // No random moves
		
		int[] pebbles = { 
				0, 0,
				0, 0, 0, 0, 0, 1,
				0, 0, 0, 0, 1, 0
		};
		GameBoard board = buildBoard(pebbles);
		GameSession session = GameSession.builder().gameBoard(board)
				.player1Engine(engine)
				.player2Engine(RandomEngine.builder()
						.player(PLAYER_TWO)
						.build())
				.build();
		
		MultiLayerNetwork network = engine.getNeuralNetwork();
		double moveScore = network.output(engine.getInputNDArray(board)).getDouble(5);
		
		// Each play of the game will involve the same moves (which ultimately leads to a win).
		// Our expectation is that the neural network score for this move, in this scenario 
		// should go down after each game, as it increases confidence that the move will lead 
		// towards a win.
		for (int i=0; i<3; i++) {
			session.playGame(PLAYER_ONE);			
			board.resetGameBoard(pebbles[0], pebbles[1], Arrays.copyOfRange(pebbles, 2, pebbles.length));
			double nextMoveScore = network.output(engine.getInputNDArray(board)).getDouble(3);
			assertTrue(nextMoveScore > moveScore);
			moveScore = nextMoveScore;
		}
	}

	@Test
	public void statisticianTest() {
		StringWriter writer = new StringWriter();
		Statistician stats = Statistician.builder().gameBatchSize(5).outputWriter(writer).build();
		stats.addGameResult(PLAYER_ONE, 15, PLAYER_TWO, 5);
		stats.addGameResult(PLAYER_ONE, 25, PLAYER_TWO, 15);
		stats.addGameResult(PLAYER_TWO, 17, PLAYER_ONE, 12);
		stats.addGameResult(PLAYER_TWO, 20, PLAYER_ONE, 19);
		stats.addGameResult(PLAYER_ONE, 18, PLAYER_TWO, 12);

		stats.addGameResult(PLAYER_ONE, 10, PLAYER_TWO, 5);
		stats.addGameResult(PLAYER_ONE, 15, PLAYER_TWO, 10);
		stats.addGameResult(PLAYER_TWO, 15, PLAYER_ONE, 10);
		stats.addGameResult(PLAYER_TWO, 25, PLAYER_ONE, 10);
		stats.addTieGameResult(15);

		stats.dumpBatchesToCsv();
		
		StringBuilder expected = new StringBuilder();
		expected.append("P1Wins,P1Ties,P1Losses,P1Score,P2Wins,P2Ties,P2Losses,P2Score");
		expected.append(System.lineSeparator());
		expected.append("0.6,0.0,0.4,17.8,0.4,0.0,0.6,13.8");
		expected.append(System.lineSeparator());
		expected.append("0.4,0.2,0.4,12.0,0.4,0.2,0.4,14.0");
		expected.append(System.lineSeparator());
		assertEquals(expected.toString(), writer.toString());
		
	}
}
