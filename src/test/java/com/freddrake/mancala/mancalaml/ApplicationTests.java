package com.freddrake.mancala.mancalaml;

import com.freddrake.mancala.mancalaml.spring.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static com.freddrake.mancala.mancalaml.Player.PLAYER_ONE;
import static com.freddrake.mancala.mancalaml.Player.PLAYER_TWO;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
public class ApplicationTests {
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

}
