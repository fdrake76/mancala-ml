package com.freddrake.mancala.mancalaml;

import java.util.Random;

import com.freddrake.mancala.mancalaml.engine.GamingEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freddrake.mancala.mancalaml.stats.Statistician;

import static com.freddrake.mancala.mancalaml.Player.PLAYER_ONE;
import static com.freddrake.mancala.mancalaml.Player.PLAYER_TWO;

import lombok.Builder;

/**
 * Represents a specific session in the game
 */
@Builder
public class GameSession {
	private final Logger log = LoggerFactory.getLogger(GameSession.class);
	@Builder.Default private GameBoard gameBoard = new GameBoard();
	private GamingEngine player1Engine;
	private GamingEngine player2Engine;
	private long trainingGames;
	private Statistician statistician;

	public void train() {
	    train(null, true);
    }

    void train(Player player, boolean resetGameBoard) {
	    player1Engine.onBeforeSession();
	    player2Engine.onBeforeSession();

		for(long i=0; i<trainingGames; i++) {
			log.info("Playing game {} of {}", i+1, trainingGames);
			if (resetGameBoard)
			    gameBoard.resetGameBoard();
			playGame(player);
		}
		
		log.info("Saving engine networks");
		player1Engine.saveNetwork();
		player2Engine.saveNetwork();
		player1Engine.onAfterSession();
		player2Engine.onAfterSession();

		if (statistician != null) {
            statistician.outputResults();
        }
	}
	
	void playGame(Player firstPlayer) {
		log.info("Playing game between player 1 ({}) and player 2 ({})", 
				player1Engine.getClass().getName(), player2Engine.getClass().getName());
		Player currentPlayer = firstPlayer;
		if (currentPlayer != null) {
			log.info("{} selected to go first.", currentPlayer.name());
		} else {
			currentPlayer = new Random().nextBoolean() ? PLAYER_ONE : PLAYER_TWO;
			log.info("{} randomly selected to go first", currentPlayer.name());
		}
		while(!gameBoard.isGameOver(currentPlayer)) {
			log.info("It's {}'s turn", currentPlayer.name());
			GamingEngine engine = currentPlayer == PLAYER_ONE ? player1Engine : player2Engine;
			int move = engine.chooseMove(gameBoard);
			log.debug("{} chooses move {}", currentPlayer.name(), move);
			boolean playAgain = gameBoard.executeMove(currentPlayer, move);	
			engine.onAfterMove(gameBoard);
			if (!playAgain) {
				currentPlayer = (currentPlayer == PLAYER_ONE) ? PLAYER_TWO : PLAYER_ONE;
			}
		}
		
		log.info("Game is over.  Winner is {}", gameBoard.getPointsLeader().name());
		log.info("Player 1: {}, Player 2: {}",
                gameBoard.playerPebbles(PLAYER_ONE), gameBoard.playerPebbles(PLAYER_TWO));
		player1Engine.onAfterGame(gameBoard);
		player2Engine.onAfterGame(gameBoard);
		
		if (statistician != null) {
			if (gameBoard.getPointsLeader() == Player.NOBODY) {
				statistician.addTieGameResult(gameBoard.playerPoints(PLAYER_ONE));
			} else {
				Player winner = PLAYER_ONE;
				Player loser = PLAYER_TWO;
				if (gameBoard.getPointsLeader() == PLAYER_TWO) {
					winner = PLAYER_TWO;
					loser = PLAYER_ONE;
				}
				statistician.addGameResult(winner, gameBoard.playerPoints(winner), 
						loser, gameBoard.playerPoints(loser));				
			}
		}
	}
}
