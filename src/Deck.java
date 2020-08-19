/*	
 * 	File:				Deck.java
 * 	Associated Files:	Main.java, OldMaid.java
 * 	Packages Needed:	java.util.ArrayList, java.util.Collections, java.util.Comparator;
 * 	Author:            	Michael Ngo (https://github.com/yeeshue99)
 * 	Date Modified:      8/17/2020 by Michael Ngo
 * 	Modified By:        Michael Ngo
 * 
 * 	Purpose:			Setup card structure to be used in card games
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

/*
 * Class:				Deck
 * Purpose:				Handle all card operations
 * Methods:				MakeDeck, FindHandSize, DealCards, DrawCard,
 * 							CheckMelds, RemoveCards, RemoveMatchingCards,
 * 							RemoveSequenceCards, DisplayCards, DisplayMelds,
 * 							CalculateScore, CardScore, CardToValue, dblDigitSort,
 * 							isNumeric
 */
public class Deck {

	// Different combinations for cards
	String[] suits = { "Clubs", "Hearts", "Spades", "Diamonds" };
	String[] values = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A" };
	HashMap<String, Integer> letterValues = new HashMap<String, Integer>();
	Random rng =  new Random(420);
	
	// Container to hold all cards
	public ArrayList<String> deck = new ArrayList<String>();
	public ArrayList<String> discard = new ArrayList<String>();
	public ArrayList<ArrayList<String>> melds = new ArrayList<ArrayList<String>>();
	private ArrayList<String> meldType = new ArrayList<String>();

	/*
	 * Function:			Initialize
	 * Params: 				
	 * Purpose:				Initializes Rummy engine
	 * Returns: 			
	 */
	public Deck() {
		MakeDeck();
		letterValues.put("J", 11);
		letterValues.put("Q", 12);
		letterValues.put("K", 13);
		letterValues.put("A", 1);
	}

	/*
	 * Function:			MakeDeck 
	 * Params: 
	 * Purpose:				Initialize central deck to hold all cards but the Queen of Hearts
	 * Returns: 			
	 */
	public void MakeDeck() {
		for (int i = 0; i < suits.length; i++) {
			for (int j = 0; j < values.length; j++) {
				deck.add(values[j] + " of " + suits[i]);
			}
		}

		Collections.shuffle(deck, rng);
	}
	
	/*
	 * Function:			FindHandSize
	 * Params: 				Number of players(Scanner)
	 * Purpose:				Calculate the hand size of each player based on formula
	 * Returns: 			Hand size of each player(int)
	 */
	private int FindHandSize(int numPlayers) {
		if(numPlayers <= 2) {
			return 10;
		}
		else if(numPlayers >= 3 && numPlayers <= 4) {
			return 7;
		}
		else {
			return 6;
		}
	}

	/*
	 * Function:			DealCards 
	 * Params:				Number of hands(int)
	 * Purpose: 			Evenly split every card from the center deck to each hand 
	 * Returns:				Each hand
	 */
	public ArrayList<ArrayList<String>> DealCards(int numPlayers) {
		ArrayList<ArrayList<String>> hands = new ArrayList<ArrayList<String>>();
		
		for (int i = 0; i < numPlayers; i++) {
			hands.add(new ArrayList<String>());
		}

		int player = 0;
		int handSize = FindHandSize(numPlayers);
		outer:
		while (!deck.isEmpty()) {
			if(hands.get(player).size() < handSize) {
				hands.get(player).add(deck.get(0));
				deck.remove(0);
			}
			else {
				int playersDone = 0;
				for (ArrayList<String> temp : hands){
					if (temp.size() >= handSize) {
						Collections.sort(temp, dblDigitSort);
						playersDone++;
					}
				}
				if (playersDone >= numPlayers) {
					break outer;
				}
			}
			player++;
			if (player >= numPlayers) {
				player = 0;
			}

		}
		discard.add(deck.get(0));
		deck.remove(0);
		System.out.println();
		return hands;
	}
	
	/*
	 * Function:			DrawCard
	 * Params: 				Where to draw a card from(String)
	 * Purpose:				Draw a card from either the deck or discard pile
	 * Returns: 			Drawn card(String)
	 */
	public String DrawCard(String location) {
		String returnStr;
        if(location.equalsIgnoreCase("discard")) {
        	returnStr = discard.get(0);
        	discard.remove(0);
        }
        else if (location.equalsIgnoreCase("deck")){
        	returnStr = deck.get(0);
        	deck.remove(0);
        }else {
        	System.out.println("Invalid draw location error! Returning empty string...");
        	returnStr = "";
        }
        return returnStr;
	}
	
	/*
	 * Function:			CheckMelds
	 * Params: 				Hand to check(ArrayList<String>)
	 * Purpose:				Check to see if any cards in the hand can fit in
	 * 							a meld already on the table
	 * Returns: 			Whether or not a match was found(boolean)
	 */
	public boolean CheckMelds(ArrayList<String> hand) {
		int cardValue = -1;
		String card;
		int i = 0;
		boolean removed = false;
		while (i < hand.size()) {
			for (int j = 0; j < melds.size(); j++) {
				card = hand.get(i);
				cardValue = CardToValue(hand.get(i));
				if(meldType.get(j).equalsIgnoreCase("Sequence")) {
					if(cardValue == CardToValue(melds.get(j).get(0)) - 1) {
						melds.get(j).add(0, hand.get(i));
						hand.remove(i);
						removed = true;
						System.out.printf("Meld found for your %s!%n", card);
						break;
					}

					if(cardValue == (CardToValue(melds.get(j).get(melds.get(j).size() - 1)) + 1)) {
						melds.get(j).add(hand.get(i));
						hand.remove(i);
						removed = true;
						System.out.printf("Meld found for your %s!%n", card);
						break;
					}
				}
				else if(meldType.get(j).equalsIgnoreCase("Match")){
					if(cardValue == CardToValue(melds.get(j).get(0))) {
						melds.get(j).add(hand.get(i));
						hand.remove(i);
						removed = true;
						System.out.printf("Meld found for your %s!%n", card);
						break;
					}
				}

			}
			i++;
		}
		return removed;
	}
	
	/*
	 * Function:			RemoveCards
	 * Params: 				player's hand(ArrayList<String>)
	 * Purpose:				Helper function to remove cards based off of meld rules
	 * Returns: 			Whether or not a match was found(boolean)
	 */
	public Boolean Removecards(ArrayList<String> hand) {
		boolean matching = RemoveMatchingCards(hand);
		boolean sequence = RemoveSequenceCards(hand);
		return (matching || sequence);
	}

	/*
	 * Function:			RemoveMatchingCards
	 * Params: 				player's hand(ArrayList<String>)
	 * Purpose:				Removes every meld from matching cards from the hand
	 * Returns: 			Whether or not a match was found with this method(boolean)
	 */
	public Boolean RemoveMatchingCards(ArrayList<String> hand) {
		boolean removed = false;
		if(hand.size() < 3) {
			return removed;
		}

		Collections.sort(hand, dblDigitSort);
		int i = 0;
		int[] cardValues = new int[4];
		while (i < hand.size() - 2) {
			for (int j = 0; j < 3; j++) {
				cardValues[j] = CardToValue(hand.get(i + j));
			}
			if (cardValues[0] == cardValues[1] && cardValues[1] == cardValues[2] && cardValues[2] == cardValues[3]) {
				System.out.printf("You had a 4 matching meld of %s\'s!", hand.get(i).charAt(0));
				ArrayList<String> newMeld = new ArrayList<String>();
				meldType.add("Match");
				for (int __ = 0; __ < 4; __++) {
					newMeld.add(hand.get(i));
					hand.remove(i);
				}
				i = Math.max(0, (i - 3));
				melds.add(newMeld);
				removed = true;
			} 
			else if (cardValues[0] == cardValues[1] && cardValues[1] == cardValues[2]) {
				System.out.printf("You had a 3 matching meld of %s\'s!", hand.get(i).charAt(0));				System.out.println("You had a meld of " + hand.get(i).charAt(0) + hand.get(i).charAt(1) + "\'s!");
				ArrayList<String> newMeld = new ArrayList<String>();
				meldType.add("Match");
				for (int __ = 0; __ < 3; __++) {
					newMeld.add(hand.get(i));
					hand.remove(i);
				}
				i = Math.max(0, (i - 2));
				melds.add(newMeld);
				removed = true;
			}

			else {
				i++;
			}
		}
		//Collections.shuffle(hand, rng);
		System.out.println();
		return removed;
	}
	
	/*
	 * Function:			RemoveSequenceCards
	 * Params: 				player's hand(ArrayList<String>)
	 * Purpose:				Removes every meld from sequencing cards from the hand
	 * Returns: 			Whether or not a match was found with this method(boolean)
	 */
	public Boolean RemoveSequenceCards(ArrayList<String> hand) {
		boolean removed = false;
		if(hand.size() < 1) {
			return removed;
		}
		Collections.sort(hand, dblDigitSort);
		ArrayList<String> currentSequence = new ArrayList<String>();
		currentSequence.add(hand.get(0));
		ArrayList<String> longestSequence = new ArrayList<String>(currentSequence);
		String card;
		for (int i = 1; i < hand.size(); i++) {
			/*
			System.out.println("===");
			System.out.println("Longest: ");
			System.out.println(longestSequence);
			System.out.println("Current: ");
			System.out.println(currentSequence);
			*/
			card = hand.get(i);
			if (CardToValue(card) - 1 == CardToValue(currentSequence.get(currentSequence.size() - 1))){
				currentSequence.add(card);
				if(longestSequence.size() < currentSequence.size()) {
					longestSequence = new ArrayList<String>(currentSequence);
				}
			}
			else if(CardToValue(card) > CardToValue(currentSequence.get(currentSequence.size() - 1))){
				currentSequence = new ArrayList<String>();
				currentSequence.add(card);
			}
		}
		if (!(longestSequence.size() < 3)) {
			System.out.println("You had a sequence meld! The sequence was:");
			for (int k = 0; k < longestSequence.size(); k++) {
				if(k != 0) {
					System.out.print(", ");
				}
				System.out.print(longestSequence.get(k));
			}
			hand.removeAll(longestSequence);
			melds.add(longestSequence);
			meldType.add("Sequence");
			System.out.println();
			RemoveSequenceCards(hand);
			removed = true;
		}
		//Collections.shuffle(hand, rng);
		System.out.println();
		return removed;
	}

	/*
	 * Function:			DisplayCards
	 * Params: 				A certain player's hand(ArrayList<String>)
	 * Purpose:				Prints out a hand to the console to be viewed
	 * Returns: 			
	 */
	public void DisplayCards(ArrayList<String> hand) {
		Collections.sort(hand, dblDigitSort);
		for (int i = 0; i < hand.size(); i++) {
			if (i != 0) {
				System.out.print(", ");
			}
			System.out.print(hand.get(i));
		}
		System.out.println();
	}
	
	/*
	 * Function:			DisplayMelds
	 * Params: 				
	 * Purpose:				Prints out every meld on the table to the console
	 * 							to be viewed
	 * Returns: 			
	 */
	public void DisplayMelds() {
		for (int i = 0; i < melds.size(); i++) {
			System.out.printf("%s " + melds.get(i) + "%n", meldType.get(i));
		}
		System.out.println();
	}
	
	/*
	 * Function:			CalculateScore
	 * Params: 				A certain player's hand(ArrayList<String>)
	 * Purpose:				Calculate the worth of a player's hand to be given
	 * 							to the winner
	 * Returns: 			Calculated score
	 */
	public int CalculateScore(ArrayList<String> hand) {
		int score = 0;
		for (String card : hand) {
			score += CardScore(card);
		}
		return score;
	}
	
	/*
	 * Function:			CardScore
	 * Params: 				Specific card(String)
	 * Purpose:				Prints out a hand to the console to be viewed
	 * Returns: 			
	 */
	public int CardScore(String card) {
		String[] splitString = card.split(" ");
		int num = 0;
		if(isNumeric(splitString[0])) {
            num = Integer.parseInt(splitString[0]);
    	}
    	else {
    		num = letterValues.get(splitString[0]);
    	}
		if(num > 10) {
			num = 10;
		}
		return num;
	}
	
	private int CardToValue(String str) {
		String[] splitString = str.split(" ");
		if(isNumeric(splitString[0])) {
            return (Integer.parseInt(splitString[0]));
    	}
    	else {
    		return(letterValues.get(splitString[0]));
    	}
	}
	
	/*
	 * Function:			dblDigitSort
	 * Params: 				
	 * Purpose:				Sorts based off of Card values: A is low, K is high.
	 * Returns: 			Comparator of two values
	 */
	public Comparator<String> dblDigitSort = new Comparator<String>()
    {
        @Override
        public int compare(String s1, String s2)
        {
        	Integer val1;
        	Integer val2;
        	val1 = CardToValue(s1);
        	val2 = CardToValue(s2);
            return val1.compareTo(val2);
        }
    };
    
	/*
	 * Function:			isNumeric
	 * Params: 				Card Value(String)
	 * Purpose:				Determines whether or not a string is wholly a number
	 * Returns: 			Whether or not the given string can be parsed as a number
	 */
	private static boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        Integer.parseInt(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}


}


