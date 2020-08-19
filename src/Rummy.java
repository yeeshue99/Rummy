/*	
 * 	File:				War.java
 * 	Associated Files:	Main.java, Deck.java
 * 	Packages Needed:	java.util.ArrayList, java.util.HashMap, java.util.Scanner
 * 	Author:            	Michael Ngo (https://github.com/yeeshue99)
 * 	Date Modified:      8/18/2020 by Michael Ngo
 * 	Modified By:        Michael Ngo
 * 
 * 	Purpose:			Underlying structure for War card game
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/*
 * Class:				War
 * Purpose:				Handles War engine and game
 * Methods:				PlayGame, GetDiscard, GetActionChoice, NextPlayer
 */
public class Rummy {

	int numPlayers = 2;
	int rounds = 0;
	Deck deck;
	ArrayList<ArrayList<String>> allHands;
	String[] actions = { "meld", "discard", "table", "hand" };
	String[] drawLocations = { "discard", "deck" };

	/*
	 * Function:			Initialize
	 * Params: 				Number of players(int)
	 * Purpose:				Initializes Rummy engine
	 * Returns: 			
	 */
	public Rummy(int numPlayers) {
		if (numPlayers <= 1) {
			numPlayers = 2;
			System.out.println("There has to be at least two players. I assume that's what you meant!");
		}
		if (numPlayers > 6) {
			numPlayers = 6;
			System.out.println("There can be at most six players. I assume that's what you meant!");
		}
		this.numPlayers = numPlayers;
		deck = new Deck();
		System.out.println("Dealing the deck evenly to every player...");
		allHands = deck.DealCards(numPlayers);
		for (int i = 0; i < numPlayers; i++) {
			System.out.printf("Player #%d, this is the hand you were dealt:%n", (i + 1));
			deck.DisplayCards(allHands.get(i));
			System.out.printf("Okay, Player #%d, let's see if you had any melds...%n", (i + 1));
			if (!deck.Removecards(allHands.get(i))) {
				System.out.printf("No melds found!%n%n");
			}
		}
	}

	/*
	 * Function:			PlayGame
	 * Params: 				Java command line input(Scanner)
	 * Purpose:				Run the game loop and communicate with user
	 * Returns: 			Player who won and the rounds the game took(int[])
	 */
	public int[] PlayGame(Scanner sc) {
		System.out.println("Welcome to the game of Rummy!");

		int player = 0;
		int chosenCard = 0;

		while (true) {
			System.out.println("======================================");
			System.out.printf("Player #%d, these are your cards:%n", (player + 1));
			deck.DisplayCards(allHands.get(player));

			System.out.printf("Do you want to draw from discard or deck? ");
			System.out.printf("The top of the discard pile is a(n): %s%n", deck.discard.get(0));
			String choice = sc.next();

			while (!(choice.equalsIgnoreCase("discard") || choice.equalsIgnoreCase("deck"))) {
				System.out.println("Invalid location! The loations you can choose are: ");
				System.out.println(Arrays.toString(drawLocations));
				choice = sc.next();
			}
			String cardDrawn = deck.DrawCard(choice);
			allHands.get(player).add(cardDrawn);
			System.out.printf("Player #%d, you drew %s%n", (player + 1), cardDrawn);
			deck.DisplayCards(allHands.get(player));
			System.out.printf("After drawing, let's see if you have any melds...%n", (player + 1));
			if (!deck.Removecards(allHands.get(player))) {
				System.out.printf("No melds found!%n%n");
			}

			String action = "";
			
			while (!action.equalsIgnoreCase("discard")) {
				System.out.println("What do you want to do now? (To see list of actions type \"help\"):");
				action = GetActionChoice(sc);
				if (action.equalsIgnoreCase(actions[0])) {
					if (!deck.CheckMelds(allHands.get(player))) {
						System.out.println("No melds found!");
					}
				}
				else if (action.equalsIgnoreCase(actions[1])) {
					chosenCard = GetDiscardChoice(sc, player);
					System.out.printf("Throwing out your %s...%n", allHands.get(player).get(chosenCard));
					deck.discard.add(0, allHands.get(player).get(chosenCard));
					allHands.get(player).remove(chosenCard);
				}
				else if(action.equalsIgnoreCase(actions[2])){
					deck.DisplayMelds();
				}
				else if(action.equalsIgnoreCase(actions[3])) {
					deck.DisplayCards(allHands.get(player));
				}
				else {
					System.out.println("The actions you can take are: ");
					System.out.println(Arrays.toString(actions));
				}
			}
			if (allHands.get(player).size() <= 0) {
				break;
			}
			player = NextPlayer(player);
		}
		System.out.printf("Someone has no more cards! The game lasted %d rounds!%n", rounds);
		int score = 0;
		for (ArrayList<String> hand : allHands) {
			score += deck.CalculateScore(hand);
		}
		int[] playerAndScore = new int[2];
		playerAndScore[0] = player;
		playerAndScore[1] = score;
		return playerAndScore;
		// return rounds;
	}
	
	/*
	 * Function:			GetDiscardchoice
	 * Params: 				Java command line input(Scanner), Current player(int)
	 * Purpose:				Communicates with user to find what card to discard from hand
	 * Returns: 			Card to discard(int)
	 */
	private int GetDiscardChoice(Scanner sc, int player) {
		System.out.printf("Choose a card to discard (1-%d): ", allHands.get(player).size());
		int chosenCard = -1;

		chosenCard = sc.nextInt();
		// sc.nextLine();
		while (!(chosenCard >= 1 && chosenCard <= allHands.get(player).size())) {
			System.out.println(
					"Invalid card number. Please enter integer between 1 and " + allHands.get(player).size() + ": ");
			chosenCard = sc.nextInt();
		}
		return chosenCard - 1;
	}

	/*
	 * Function:			GetActionChoice
	 * Params: 				Java command line input(Scanner)
	 * Purpose:				Communicates with user to find what action to do
	 * Returns: 			Player's chosen action(String)
	 */
	private String GetActionChoice(Scanner sc) {
		String action;
		action = sc.next();
		boolean reDoChoice = true;
		for (int i = 0; i < actions.length; i++) {
			if (action.equalsIgnoreCase(actions[i])) {
				reDoChoice = false;
			}
		}
		while (reDoChoice) {
			System.out.println("The actions you can take are: ");
			System.out.println(Arrays.toString(actions));
			action = sc.next();
			for (int i = 0; i < actions.length; i++) {
				if (action.equalsIgnoreCase(actions[i])) {
					reDoChoice = false;
				}
			}
		}
		return action;
	}

	/*
	 * Function:			NextPlayer
	 * Params: 				Current player(int)
	 * Purpose:				Calculates the position of the next player
	 * Returns: 			Position of next player(int)
	 */
	private int NextPlayer(int player) {
		player++;
		if (player >= numPlayers) {
			player = 0;
			rounds++;
		}
		return player;
	}
}
