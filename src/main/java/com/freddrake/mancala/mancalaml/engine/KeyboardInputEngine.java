package com.freddrake.mancala.mancalaml.engine;

import com.freddrake.mancala.mancalaml.GameBoard;
import com.freddrake.mancala.mancalaml.Player;
import lombok.Builder;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * This "engine" is the human, manually entering values.
 */
@Builder
public class KeyboardInputEngine extends AbstractGamingEngine {

    @Override
    public int chooseMove(GameBoard gameBoard) {
        outputBoard(gameBoard);

        System.out.print("Make a move (1-6): ");
        Scanner sc = new Scanner(System.in);
        int choice;
        try {
            choice = sc.nextInt();
        } catch (InputMismatchException e) {
            System.err.println("Must be a number between 1-6.");
            return chooseMove(gameBoard);
        }

        if (choice < 1 || choice > 6) {
            System.err.println("Must be a number between 1-6.");
            return chooseMove(gameBoard);
        }

        if (!gameBoard.validMoves(player).contains(choice)) {
            System.err.println("That move is not valid.  Please select another one.");
            return chooseMove(gameBoard);
        }

        return choice;
    }

    @Override
    public boolean isGameOver(GameBoard gameBoard) {
        return gameBoard.isGameOver(player);
    }

    private void outputBoard(GameBoard gameBoard) {
        System.out.println("Player 1: "+gameBoard.playerPoints(Player.PLAYER_ONE)+", Player 2: "+
                gameBoard.playerPoints(Player.PLAYER_TWO));
        int[] pebbles = gameBoard.pebbleField(player);
        System.out.print("Computer: ");
        for(int i=pebbles.length/2; i<pebbles.length; i++) {
            System.out.print("( "+pebbles[i]+" ) ");
        }
        System.out.println();
        System.out.print("YOU     : ");
        for(int i=0; i<pebbles.length/2; i++) {
            System.out.print("( "+pebbles[i]+" ) ");
        }
        System.out.println();
    }
}
