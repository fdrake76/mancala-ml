package com.freddrake.mancala.mancalaml;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import org.springframework.boot.Banner;

import com.freddrake.mancala.mancalaml.GameBoard.Player;
import com.freddrake.mancala.mancalaml.qlearning.QLearningEngine;
import com.freddrake.mancala.mancalaml.random.RandomEngine;
import com.freddrake.mancala.mancalaml.stats.CsvOutputter;
import com.freddrake.mancala.mancalaml.stats.Statistician;

@SpringBootApplication
@Profile("!test")
public class MancalaMlApplication implements CommandLineRunner {

	@Override
	public void run(String... args) {
		try(FileWriter fileWriter = new FileWriter("/Users/fdrake/mancala-results.csv");
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);) {
			GameSession session = GameSession.builder()
					.player1Engine(QLearningEngine.builder()
							.epsilon(0.25)
							.player(Player.PLAYER_ONE)
							.winReward(10)
							.loseReward(-10)
							.tieReward(0)
							.networkFile(new File("/Users/fdrake/mancala-network.zip"))
							.trainable(true)
							.build())
//					.player2Engine(RandomEngine.builder().player(Player.PLAYER_TWO).build())
					.player2Engine(QLearningEngine.builder()
							.epsilon(0d)
							.player(Player.PLAYER_TWO)
							.networkFile(new File("/Users/fdrake/mancala-network-2018-10-04.zip"))
							.trainable(false)
							.build())
					.trainingGames(40000)
					.statistician(Statistician.builder()
							.gameBatchSize(100)
							.writeHeaderRow(false)
							.announceAfterGames(1000)
							.outputters(Arrays.asList(
									CsvOutputter.builder()
										.outputWriter(bufferedWriter)
										.build()
									))
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
