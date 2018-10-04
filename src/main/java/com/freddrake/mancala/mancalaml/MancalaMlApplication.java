package com.freddrake.mancala.mancalaml;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.springframework.boot.Banner;

import com.freddrake.mancala.mancalaml.GameBoard.Player;
import com.freddrake.mancala.mancalaml.qlearning.QLearningEngine;
import com.freddrake.mancala.mancalaml.random.RandomEngine;

@SpringBootApplication
@Profile("!test")
public class MancalaMlApplication implements CommandLineRunner {

	@Override
	public void run(String... args) {
		try(FileWriter fileWriter = new FileWriter("/Users/fdrake/mancala-results.csv");
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);) {
			GameSession session = GameSession.builder()
					.player1Engine(QLearningEngine.builder()
							.epsilon(.25)
							.player(Player.PLAYER_ONE)
							.winReward(5)
							.loseReward(-5)
							.tieReward(0)
							.networkFile(new File("/Users/fdrake/mancala-network"))
							.build())
					.player2Engine(RandomEngine.builder().player(Player.PLAYER_TWO).build())
					.trainingGames(10000)
					.statistician(Statistician.builder()
							.gameBatchSize(100)
							.outputWriter(bufferedWriter)
							.build())
					.build();
			session.train();			
		} catch (Exception e) {
			throw new MancalaException(e);
		}
						
	}
	
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(MancalaMlApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
	}
}
