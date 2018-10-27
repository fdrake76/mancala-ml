package com.freddrake.mancala.mancalaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Represents the state of a game board.  It is not thread safe.
 */
public class GameBoard {
	;
	
	private static final Logger log = LoggerFactory.getLogger(GameBoard.class);
	private static final int PEBBLE_PODS = 12; // Value should always be even.
	private static final int INITIAL_PEBBLES_PER_POD = 4;
	private int[] points;
	private int[] pebbles;
	private boolean forceQuit;
	
	public GameBoard() {
		resetGameBoard();
	}
	
	/**
	 * Sets up a custom board configuration.  Primarily used for testing purposes.
	 * @param player1Points the number of points for player 1
	 * @param player2Points the number of points for player 2
	 * @param pebbles the number of pebbles in each pod
	 */
	public GameBoard(int player1Points, int player2Points, int... pebbles) {
		resetGameBoard(player1Points, player2Points, pebbles);
	}
	
	public void resetGameBoard() {
		points = new int[] {0, 0};
		pebbles = new int[PEBBLE_PODS];
		Arrays.fill(pebbles, INITIAL_PEBBLES_PER_POD);
		forceQuit = false;
	}
	
	public void resetGameBoard(int player1Points, int player2Points, int... pebbles) {
		if (player1Points < 0) {
			throw new MancalaException("Player 1 points value must be positive.");
		}
		if (player2Points < 0) {
			throw new MancalaException("Player 2 points value must be positive.");
		}
		if (pebbles == null || pebbles.length != PEBBLE_PODS) {
			throw new MancalaException("Pebbles array must contain 12 positive integers.");
		}
		Arrays.stream(pebbles).forEach(p -> {
			if (p < 0) {
				throw new MancalaException("All pebbles must contain positive integers.");
			}
		});
		
		points = new int[] {player1Points, player2Points};
		this.pebbles = pebbles;		
	}
	
	public int playerPoints(Player player) {
		if (player == null || player == Player.NOBODY) {
			throw new MancalaException("Player must be defined");
		}
		
		return player == Player.PLAYER_ONE ? points[0] : points[1];
	}
	
	public int[] playerPebbles(Player player) {
		if (player == null || player == Player.NOBODY) {
			throw new MancalaException("Player must be defined");
		}
		
		int[] pPebbles = new int[PEBBLE_PODS / 2];
		int pebbleIndex = (player == Player.PLAYER_ONE) ? 0 : PEBBLE_PODS / 2;
		for(int i=0; i<pPebbles.length; i++) {
			pPebbles[i] = pebbles[pebbleIndex];
			pebbleIndex++;
		}
		
		return pPebbles;
	}
	
	/**
	 * Outputs the pebble field, from the perspective of the player.  The first elements
	 * will be the pods that are in front of the player, and the remaining will follow
	 * in the same clockwise order that the game is normally played.
	 * @param player the player on which we have perspective
	 * @return the pods with the number of pebbles in each pod
	 */
	public int[] pebbleField(Player player) {
		final int[] playerPebbles = new int[pebbles.length];
		IntStream.range(0, pebbles.length).forEach(index -> {
			int playerIndex = index;
			if (player == Player.PLAYER_TWO) {
				playerIndex = index + pebbles.length / 2;
				if (playerIndex >= pebbles.length) {
					playerIndex -= pebbles.length;
				}					
			}
			
			playerPebbles[playerIndex] = pebbles[index];
		});
		
		return playerPebbles;
	}
	
	/**
	 * Perform a move by a player at a given location.
	 * @param player the player who is moving
	 * @param location their own pod location, a number between 1 and 6
	 * @return true if they ended their turn by dropping a pebble in their points pile, otherwise false
	 */
	public boolean executeMove(Player player, int location) {
		log.debug("Executing move: {} on location {}", player, location);
		if (player == null) {
			throw new MancalaException("Player must be defined");
		}

		log.debug(dumpCurrentState(
				player == Player.PLAYER_ONE ? location - 1 : location + pebbles.length / 2 - 1, false));
		if (location < 1 || location > (PEBBLE_PODS / 2)) {
			throw new MancalaException("Illegal location "+location+
                    ".  Location must be between 1 and "+(PEBBLE_PODS / 2));
		}
		
		// Convert the player's chosen location to the index represented on the board.
		int boardIndex = player == Player.PLAYER_ONE ? location - 1 : location + (PEBBLE_PODS / 2 - 1);
		return executeSubsequentMove(player, boardIndex);
	}
	
	public String dumpCurrentState(int location, boolean endOnPointsPile) {
		return dumpCurrentState(location, endOnPointsPile, false);
	}
	
	public String dumpCurrentState(int location, boolean endOnPointsPile, boolean indent) {
		StringBuilder b = new StringBuilder();
		if (indent) b.append("    ");
		
		b.append("Player1 ("+playerPoints(Player.PLAYER_ONE)+"): ");
		int[] p1Pebbles = playerPebbles(Player.PLAYER_ONE);
		if (location >= 0 && location < pebbles.length / 2) {
			// Make sure the location is highlighted
			b.append("[");
			for(int i=0; i<p1Pebbles.length; i++) {
				if (i > 0) b.append(", ");
				if (i == location) {
					b.append("{"+p1Pebbles[i]+"}");
				} else {
					b.append(p1Pebbles[i]);
				}
			}
			b.append("]");
		} else {
			b.append(Arrays.toString(p1Pebbles));
		}
		
		b.append(", Player2 ("+playerPoints(Player.PLAYER_TWO)+"): ");
		int[] p2Pebbles = playerPebbles(Player.PLAYER_TWO);
		if (location >= pebbles.length / 2) {
			b.append("[");
			for(int i=0; i<p2Pebbles.length; i++) {
				if (i > 0) b.append(", ");
				if (i == location) {
					b.append("{"+p2Pebbles[i]+"}");
				} else {
					b.append(p2Pebbles[i]);
				}
			}
			b.append("]");
		} else {
			b.append(Arrays.toString(p2Pebbles));
		}
		
		if (endOnPointsPile) {
			b.append(" POINT");
		}

		return b.toString();
	}
	
	public List<Integer> validMoves(Player player) {
		ArrayList<Integer> validMoves = new ArrayList<>();
		int[] playerPebbles = playerPebbles(player);
		for (int i=0; i<playerPebbles.length; i++) {
			if (playerPebbles[i] > 0) {
				validMoves.add(i+1);
			}
		}
		
		return validMoves;
	}
	
	/**
	 * The game is over when the given player's pods are all empty.
	 * @return true if the game is over, false otherwise
	 */
	public boolean isGameOver(Player player) {
		return forceQuit || IntStream.of(playerPebbles(player)).sum() == 0;
	}
	
	public Player getPointsLeader() {
		if (points[0] > points[1]) {
			return Player.PLAYER_ONE;
		}
		if (points[1] > points[0]) {
			return Player.PLAYER_TWO;
		}
		
		return Player.NOBODY;
	}
	
	/**
	 * Perform a move by a player at a given location.  The difference between this call
	 * and executeMove(player,location) is that this is intended to be the internal to the
	 * class; it allows a selection for any pod on the board (zero based, so 0 through 11)
	 * and it will recursively call itself each time it moves again.
	 * @param player the player who is moving
	 * @param location a pod location on the board, a number between 0 and 11
	 * @return true if they ended their turn by dropping a pebble in their points pile, otherwise false
	 */
	private boolean executeSubsequentMove(Player player, int location) {
		// Update each pebble pod until you run out of pebbles in your hand.
		// Then as long as the final pod doesn't contain one, recursively call this method.
		if (pebbles[location] == 0) {
			// Illegal move.  Demonstrate this by ending the game early, and give the
            // player a score of -1.
            points[player == Player.PLAYER_ONE ? 0 : 1] = -1;
            forceQuit = true;
            return false;
		}

		if (player == Player.NOBODY) {
			throw new MancalaException("Player cannot be nobody.");
		}
		
		// Place pebbles from the given location to the player's hand.
		int pebblesInHand = pebbles[location];
		pebbles[location] = 0;
		log.trace("Pebbles in hand: {}", pebblesInHand);

		int nextLocation = location;
		while (pebblesInHand > 0) {
			nextLocation = (nextLocation == PEBBLE_PODS - 1) ? 0 : nextLocation + 1;
			// Is the next location going to be that player's points pile?
			if ((player == Player.PLAYER_ONE && nextLocation == PEBBLE_PODS / 2) ||
					(player == Player.PLAYER_TWO && nextLocation == 0)) {
				// A pebble goes into the points pile
				if (player == Player.PLAYER_ONE) {
					log.trace("Point for player 1");
					points[0]++;
				} else {
					log.trace("Point for player 2");
					points[1]++;
				}
				pebblesInHand--;
				
				if (pebblesInHand == 0) { // That was the last pebble in their hand
					log.debug(dumpCurrentState(nextLocation, true, true));
					return true;
				}				
			}
			
			// Add a pebble to this next pod			
			pebblesInHand--;
			pebbles[nextLocation]++;
			log.trace("Pebbles now in location {}: {}", nextLocation, pebbles[nextLocation]);
		}
		log.debug(dumpCurrentState(nextLocation, false, true));
		
		// If that pod's location wasn't empty before the player dropped their last stone,
		// pick up that pile of stones and move again.
		if (pebbles[nextLocation] > 1) {
			return executeSubsequentMove(player, nextLocation);
		}
		
		// The pod they dropped their last stone into was empty.  Their turn ends.
		return false;
	}
}
