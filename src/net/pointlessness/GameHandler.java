package net.pointlessness;

import java.util.*;

import android.util.Log;

public class GameHandler {
	/**
	 * A variable declaring whether or not the screen has updated from the numbers being 0, 0, and 0
	 */
	private boolean changedNumbers = false;
	
	/**
	 * The current number being edited
	 */
	private int currentNumber = 0;
	
	/**
	 * A variable exclaiming whether or not the player started the game
	 */
	public boolean isPlaying = false;
	
	/**
	 * The three numbers, used in gameplay heavily
	 */
	public static int[] numbers = null;
	
	/**
	 * The three numbers that are shown on the screen, different from the ones saved
	 */
	public static int[] shownNumbers = null;
	
	/**
	 * The random number generator used to generate numbers for nums 1-3
	 */
	public static Random random = null;
	
	/**
	 * The constructor used when a save file has not yet been created
	 */
	public GameHandler() {
		random = new Random();
		if (numbers == null) {
			setNumbers(0, 0, 0);
			shownNumbers = new int[3];
		}
	}
	
	/**
	 * Change the value of the numbers
	 * @param num1
	 * @param num2
	 * @param num3
	 */
	private void setNumbers(int num1, int num2, int num3) {
		numbers = new int[] {
			num1, num2, num3	
		};
	}
	
	/**
	 * Creates a String displaying the numbers properly
	 * @return String for pointlessness text
	 */
	private String getPointlessnessString() {
		StringBuilder returnString = new StringBuilder();
		
		for (int i = 0; i < 3; i++) {
			if (currentNumber == i) {
				if (i < 2) {
					returnString.append("|" + shownNumbers[i] + ",| ");
				} else {
					returnString.append("|" + shownNumbers[i] + "|");
				}
			} else if (i < 2) {
				returnString.append(shownNumbers[i] + ", ");
			} else {
				returnString.append(shownNumbers[i]);
			}
		}
		
		return returnString.toString();
	}
	
	//When the player drags their finger, call
	public void onDrag() {
		currentNumber++;
			
		if (currentNumber > 2) {
			currentNumber = 0;
		}
		
		updateText(shownNumbers[0] == 0 && shownNumbers[1] == 0 && shownNumbers[2] == 0);
		
		if (shownNumbers[0] == 0 && shownNumbers[1] == 0 && shownNumbers[2] == 0) {
			//If solved showedNumbers, create new numbers and update text
			createNewNumbers();
			
			shownNumbers[0] = numbers[0];
			shownNumbers[1] = numbers[1];
			shownNumbers[2] = numbers[2];
			
			updateText(false);
		}
	}
	
	/**
	 * Sets the showed numbers to the saved numbers
	 */
	public void resetNumbers() {
		shownNumbers[0] = numbers[0];
		shownNumbers[1] = numbers[1];
		shownNumbers[2] = numbers[2];
		updateText(false);
	}
	
	/**
	 * Changes the numbers when the player drags their finger
	 */
	private void changeNumbers() {
		switch(currentNumber) {
			case 0:
				changeNumber(currentNumber + 1, 1, true);
				changeNumber(currentNumber + 2, 1, true);
				break;
				
			case 1:
				changeNumber(currentNumber + 1, 1, true);
				changeNumber(currentNumber - 1, 1, true);
				break;
				
			case 2:
				changeNumber(currentNumber - 1, 1, true);
				changeNumber(currentNumber - 2, 1, true);
				break;
		}
	}
	
	/**
	 * Generates new numbers for the game
	 * @return numbers
	 */
	private void createNewNumbers() {
		//Set the numbers equal to zero
		numbers[0] = 0;
		numbers[1] = 0;
		numbers[2] = 0;
		
		int lastChanged = -1;
		
		//Create a puzzle.  The steps are determined by how many puzzles were solved
		for (int i = 0; i <= (int)((MainActivity.timesEqualInt - 1) / 2); i++) {
			//Generate a variable for the amount the numbers are changed
			int amountChange = random.nextInt(9) + 1;
			
			int oneChanged = MainActivity.timesEqualInt == 1 ? 1 : random.nextInt(3);
			
			while (oneChanged == lastChanged) {
				oneChanged = random.nextInt(3);
			}
			
			Log.d("Pointlessness", "" + i);
			
			switch(oneChanged) {
				case 0:
					//Creates a step in the puzzle.  Also applies for the other cases, but in a different spot.
					changeNumber(1, -amountChange, false);
					changeNumber(2, -amountChange, false);
					break;
					
				case 1:
					changeNumber(0, -amountChange, false);
					changeNumber(2, -amountChange, false);
					break;
					
				case 2:
					changeNumber(0, -amountChange, false);
					changeNumber(1, -amountChange, false);
					break;
			}
			
			lastChanged = oneChanged;
		}
	}
	
	/**
	 * Change a single number so that it stays between 0 and 9.  Used only in changeNumbers()
	 */
	private void changeNumber(int index, int changeValue, boolean shown) {
		if (shown) {
			shownNumbers[index] += changeValue;
			
			if (shownNumbers[index] > 9) {
				shownNumbers[index] -= 10;
			} else if (shownNumbers[index] < 0) {
				shownNumbers[index] += 10;
			}
		} else {
			numbers[index] += changeValue;
			
			if (numbers[index] > 9) {
				numbers[index] -= 10;
			} else if (numbers[index] < 0) {
				numbers[index] += 10;
			}
		}
	}
	
	//When the player taps their finger, call
	public void onTouch() {
		if (!isPlaying) {
			MainActivity.setPointlessnessText(getPointlessnessString());
			MainActivity.showPlayText(false);
			MainActivity.updateTimesNumbersEqual(MainActivity.timesEqualInt != 0);
			isPlaying = true;
		} else if (!changedNumbers) {
			changeNumbers();
			MainActivity.setPointlessnessText(getPointlessnessString());
		}
	}
	
	/**
	 * Updates all the text items as if the tutorial was finished
	 */
	private void updateText(boolean increment) {
		MainActivity.setPointlessnessText(getPointlessnessString());
		MainActivity.updateTimesNumbersEqual(increment);
		if (increment) {
			MainActivity.playEqualSound();
		}
	}
}