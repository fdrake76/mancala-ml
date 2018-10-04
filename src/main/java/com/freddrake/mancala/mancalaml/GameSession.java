package com.freddrake.mancala.mancalaml;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freddrake.mancala.mancalaml.GameBoard.Player;

import static com.freddrake.mancala.mancalaml.GameBoard.Player.PLAYER_ONE;
import static com.freddrake.mancala.mancalaml.GameBoard.Player.PLAYER_TWO;

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
		log.info("Loading engine networks");
		player1Engine.loadNetwork();
		player2Engine.loadNetwork();

		for(long i=0; i<trainingGames; i++) {
			log.info("Playing game {} of {}", i+1, trainingGames);
			gameBoard.resetGameBoard();
			playGame();
		}
		
		log.info("Saving engine networks");
		player1Engine.saveNetwork();
		player2Engine.saveNetwork();
		statistician.dumpBatchesToCsv();
	}
	
	public void playGame() {
		playGame(null);
	}
	public void playGame(Player firstPlayer) {
		log.info("Playing game between player 1 ({}) and player 2 ({})", 
				player1Engine.getClass().getName(), player2Engine.getClass().getName());
		Player currentPlayer = firstPlayer;
		if (currentPlayer != null) {
			log.info("{} selected to go first.", currentPlayer.name());
		} else {
			currentPlayer = new Random().nextBoolean() ? PLAYER_ONE : PLAYER_TWO;
			log.info("{} rondomly selected to go first", currentPlayer.name());
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
