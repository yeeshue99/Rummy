/*	
 * 	File:				Deck.java
 * 	Associated Files:	Main.java, Rummy.java, Card.java
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
// * Methods:			dblDigitSort, MakeDeck, DealCards, DrawCard,
 * 							CheckMelds, RemoveCards, RemoveCards, 
 * 							DisplayCards,DisplayMelds, CalculateScore, 
 * 							CardSCore,CalculateScore, CardScore,
 * 							RemoveMatchingCards, RemoveSequenceCards,
 * 							FindHandSize
 */
public class Deck {

	// Different combinations for cards
	static String[] suits = { "Clubs", "Hearts", "Spades", "Diamonds" };
	static String[] values = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A" };
	static HashMap<String, Integer> letterValues = new HashMap<String, Integer>();
	static Random rng =  new Random(420);
	
	// Container to hold all cards
	public static ArrayList<Card> deck = new ArrayList<Card>();
	public static ArrayList<Card> discard = new ArrayList<Card>();
	public static ArrayList<ArrayList<Card>> melds = new ArrayList<ArrayList<Card>>();
	private static ArrayList<String> meldType = new ArrayList<String>();
	
	/*
	 * Function:			dblDigitSort
	 * Params: 				
	 * Purpose:				Sorts based off of Card values: A is low, K is high.
	 * Returns: 			Comparator of two values
	 */
	public static Comparator<Card> dblDigitSort = new Comparator<Card>()
    {
		/*
		 * Function:			compare
		 * Params: 				Two cards to compare(Card, Card)
		 * Purpose:				Calculates and converts the values to be compared
		 * Returns: 			The comparison between c1 and c2
		 */
        @Override
        public int compare(Card c1, Card c2)
        {
        	Integer val1;
        	Integer val2;
        	val1 = c1.GetValue();
        	val2 = c2.GetValue();
            return val1.compareTo(val2);
        }
    };
    
	/*
	 * Function:			suitSort
	 * Params: 				
	 * Purpose:				Sorts based off of Card suits: A is low, K is high.
	 * Returns: 			Comparator of two values
	 */
	public static Comparator<Card> suitSort = new Comparator<Card>()
    {
		/*
		 * Function:			compare
		 * Params: 				Two cards to compare(Card, Card)
		 * Purpose:				Calculates and converts the values to be compared
		 * Returns: 			The comparison between c1 and c2
		 */
        @Override
        public int compare(Card c1, Card c2)
        {
        	char val1;
        	char val2;
        	val1 = c1.GetSuit();
        	val2 = c2.GetSuit();
            return Character.compare(val1, val2);
        }
    };

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
	public static void MakeDeck() {
		for (int i = 0; i < suits.length; i++) {
			for (int j = 0; j < values.length; j++) {
				if (!(values[j] == "Q" && suits[i] == "Hearts")) {
					String label = values[j] + " of " + suits[i];
					Card temp = new Card(label, Card.CardEquivalent(values[j]), Card.SuitToChar(suits[i]));
					deck.add(temp);
				}
			}
		}
		Collections.shuffle(deck);
	}

	/*
	 * Function:			DealCards 
	 * Params:				Number of hands(int)
	 * Purpose: 			Evenly split every card from the center deck to each hand 
	 * Returns:				Each hand
	 */
	public static ArrayList<ArrayList<Card>> DealCards(int numPlayers) {
		ArrayList<ArrayList<Card>> hands = new ArrayList<ArrayList<Card>>();
		
		for (int i = 0; i < numPlayers; i++) {
			hands.add(new ArrayList<Card>());
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
				for (ArrayList<Card> temp : hands){
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
	public static Card DrawCard(String location) {
		Card returnCard;
        if(location.equalsIgnoreCase("discard")) {
        	returnCard = discard.remove(0);
        }
        else if (location.equalsIgnoreCase("deck")){
        	if(deck.size() <= 0) {
        		deck = new ArrayList<Card>(discard);
        		discard = new ArrayList<Card>();
        		discard.add(deck.remove(0));
        	}
        	returnCard = deck.remove(0);
        }else {
        	System.out.println("Invalid draw location error! Returning empty string...");
        	returnCard = new Card();
        }
        return returnCard;
	}
	
	/*
	 * Function:			CheckMelds
	 * Params: 				Hand to check(ArrayList<Card>)
	 * Purpose:				Check to see if any cards in the hand can fit in
	 * 							a meld already on the table
	 * Returns: 			Whether or not a match was found(boolean)
	 */
	public static boolean CheckMelds(ArrayList<Card> hand) {
		int cardValue = -1;
		Card card;
		int i = 0;
		boolean removed = false;
		while (i < hand.size()) {
			for (int j = 0; j < melds.size(); j++) {
				card = hand.get(i);
				cardValue = hand.get(i).GetValue();
				if(meldType.get(j).equalsIgnoreCase("Sequence")) {
					if(cardValue == melds.get(j).get(0).GetValue() - 1) {
						melds.get(j).add(0, hand.get(i));
						hand.remove(i);
						removed = true;
						System.out.printf("Meld found for your %s!%n", card);
						break;
					}

					if(cardValue == (melds.get(j).get(melds.get(j).size() - 1).GetValue() + 1)) {
						melds.get(j).add(hand.get(i));
						hand.remove(i);
						removed = true;
						System.out.printf("Meld found for your %s!%n", card);
						break;
					}
				}
				else if(meldType.get(j).equalsIgnoreCase("Match")){
					if(cardValue == melds.get(j).get(0).GetValue()) {
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
	 * Params: 				player's hand(ArrayList<Card>)
	 * Purpose:				Helper function to remove cards based off of meld rules
	 * Returns: 			Whether or not a match was found(boolean)
	 */
	public static Boolean RemoveCards(ArrayList<Card> hand) {
		boolean matching = RemoveMatchingCards(hand);
		boolean sequence = RemoveSequenceCards(hand);
		return (matching || sequence);
	}

	/*
	 * Function:			DisplayCards
	 * Params: 				A certain player's hand(ArrayList<Card>)
	 * Purpose:				Prints out a hand to the console to be viewed
	 * Returns: 			
	 */
	public static void DisplayCards(ArrayList<Card> hand) {
		Collections.sort(hand, dblDigitSort);
		for (int i = 0; i < hand.size(); i++) {
			if (i != 0) {
				System.out.print(", ");
			}
			System.out.print(hand.get(i).GetLabel());
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
	public static void DisplayMelds() {
		for (int i = 0; i < melds.size(); i++) {
			System.out.printf("%s " + melds.get(i) + "%n", meldType.get(i));
		}
		System.out.println();
	}
	
	/*
	 * Function:			CalculateScore
	 * Params: 				A certain player's hand(ArrayList<Card>)
	 * Purpose:				Calculate the worth of a player's hand to be given
	 * 							to the winner
	 * Returns: 			Calculated score
	 */
	public static int CalculateScore(ArrayList<Card> hand) {
		int score = 0;
		for (Card card : hand) {
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
	public static int CardScore(Card card) {
		int num = card.GetValue();
		if(num > 10) {
			num = 10;
		}
		return num;
	}
	
	
	
	/*
	 * Function:			RemoveMatchingCards
	 * Params: 				player's hand(ArrayList<Card>)
	 * Purpose:				Removes every meld from matching cards from the hand
	 * Returns: 			Whether or not a match was found with this method(boolean)
	 */
	private static Boolean RemoveMatchingCards(ArrayList<Card> hand) {
		boolean removed = false;
		if(hand.size() < 3) {
			return removed;
		}

		Collections.sort(hand, dblDigitSort);
		int i = 0;
		int[] cardValues = new int[4];
		while (i < hand.size() - 2) {
			for (int j = 0; j < 3; j++) {
				cardValues[j] = hand.get(i + j).GetValue();
			}
			if (cardValues[0] == cardValues[1] && cardValues[1] == cardValues[2] && cardValues[2] == cardValues[3]) {
				System.out.printf("You had a 4 matching meld of %s\'s!", hand.get(i).GetStringValue());
				ArrayList<Card> newMeld = new ArrayList<Card>();
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
				System.out.printf("You had a 3 matching meld of %s\'s!", hand.get(i).GetStringValue());
				ArrayList<Card> newMeld = new ArrayList<Card>();
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
		System.out.println();
		return removed;
	}
	
	/*
	 * Function:			RemoveSequenceCards
	 * Params: 				player's hand(ArrayList<Card>)
	 * Purpose:				Removes every meld from sequencing cards from the hand
	 * Returns: 			Whether or not a match was found with this method(boolean)
	 */
	private static Boolean RemoveSequenceCards(ArrayList<Card> hand) {
		boolean removed = false;
		if(hand.size() < 1) {
			return removed;
		}
		Collections.sort(hand, suitSort);
		ArrayList<Card> currentSequence = new ArrayList<Card>();
		currentSequence.add(hand.get(0));
		ArrayList<Card> longestSequence = new ArrayList<Card>(currentSequence);
		Card card;
		for (int i = 1; i < hand.size(); i++) {
			card = hand.get(i);
			if (card.GetValue() - 1 == currentSequence.get(currentSequence.size() - 1).GetValue()){
				currentSequence.add(card);
				if(longestSequence.size() < currentSequence.size()) {
					longestSequence = new ArrayList<Card>(currentSequence);
				}
			}
			else if(card.GetValue() > currentSequence.get(currentSequence.size() - 1).GetValue()){
				currentSequence = new ArrayList<Card>();
				currentSequence.add(card);
			}
		}
		if (!(longestSequence.size() < 3)) {
			System.out.println("You had a sequence meld! The sequence was:");
			for (int k = 0; k < longestSequence.size(); k++) {
				if(k != 0) {
					System.out.print(", ");
				}
				System.out.print(longestSequence.get(k).GetLabel());
			}
			hand.removeAll(longestSequence);
			melds.add(longestSequence);
			meldType.add("Sequence");
			System.out.println();
			RemoveSequenceCards(hand);
			removed = true;
		}
		System.out.println();
		return removed;
	}
    
	/*
	 * Function:			FindHandSize
	 * Params: 				Number of players(Scanner)
	 * Purpose:				Calculate the hand size of each player based on formula
	 * Returns: 			Hand size of each player(int)
	 */
	private static int FindHandSize(int numPlayers) {
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
}