package net.pointlessness;

import java.io.*;
import java.util.*;

import android.media.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.app.*;
import android.content.*;
import android.content.res.AssetManager;

public class MainActivity extends Activity {
	
	/**
	 * Screen dimensions, x and y respectively
	 */
	private int width = 0, height = 0;

	/**
	 * The GameHandler instance, for calling all the GameHandler methods of course
	 */
	private GameHandler gameHandler = null;
	
	/**
	 * A variable to stop the screen from falling asleep
	 */
	private PowerManager.WakeLock lock = null;
	
	/**
	 * A variable to allow sound to play
	 */
	private SoundPool soundPool = null;
	
	/**
	 * A number representing the sound played whenever the numbers are zero
	 */
	private int equal = 0;
	
	/**
	 * The TextView for accessing the contents of the pointlessness text
	 */
	private static TextView pointlessness = null;
	
	/**
	 * The TextView for accessing the contents of the play text
	 */
	private static TextView play = null;
	
	/**
	 * the TextView for accessing the contents of the timesNumbersEqual text
	 */
	private static TextView timesEqual = null;
	
	/**
	 * The ImageView for the reset image
	 */
	private static ImageView reset = null;
	
	/**
	 * The ImageView for accessing the touch image
	 */
	private static ImageView touch = null;
	
	/**
	 * The ImageView for accessing the swipe image
	 */
	private static ImageView swipe = null;
	
	/**
	 * The number of times the numbers were equal to zero
	 */
	public static int timesEqualInt = 0;
	
	/**
	 * The current MainActivity instance, for accessing non-static methods in a static way
	 */
	public static MainActivity mainActivity = null;
	
	/**
	 * The phrases used in the beginning of the game
	 */
	public static List<String> storyPhrases = null;
	
	/**
	 * The phrases used in the end of the game
	 */
	public static List<String> endPhrases = null;

	//Standard onCreate method.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Call super constructor
		super.onCreate(savedInstanceState);
		
		//Find display dimensions
		Display display = getWindowManager().getDefaultDisplay();
		width = display.getWidth();
		height = display.getHeight();
		
		//Give the GameHandler instance an actual instance
		gameHandler = new GameHandler();
		
		//Give the MainActivity instance an actual instance
		mainActivity = this;
		
		//make game full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		//stop screen from fading
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		lock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Wake Lock");
		lock.acquire(600000);
		
		//Set the contents of the soundPool
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		
		//Set activity_main.xml as the main activity
		setContentView(R.layout.activity_main);
		
		//Set pointlessness TextView
		pointlessness = (TextView) findViewById(R.id.Pointlessness);
		
		//Set play TextView
		play = (TextView) findViewById(R.id.Play);
		
		//Set timesNumbersEqual TextView
		timesEqual = (TextView) findViewById(R.id.TimesNumbersEqual);
		
		//Set reset ImageView
		reset = (ImageView) findViewById(R.id.reset);
		
		//Set touch ImageView
		touch = (ImageView) findViewById(R.id.touch);
		
		//Set swipe ImageView
		swipe = (ImageView) findViewById(R.id.swipe);
		
		//Load the game from a previous state
		loadGame();
	}
	
	//Whenever the player drags their finger, call the GameHandler class.  Whenever someone touches their finger to the screen, also call the GameHandler class.
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction()) {
			//When the player taps the screen
			case MotionEvent.ACTION_UP:
				if (e.getX() > width / 3) {
					if (e.getX() > (width * 2) / 3) {
						if (gameHandler.isPlaying) {
							gameHandler.onDrag();
							MainActivity.showImages();
						}
					} else if (!gameHandler.isPlaying || timesEqualInt >= 1) {
						gameHandler.onTouch();
						MainActivity.showImages();
					}
				} else {
					if (gameHandler.isPlaying && timesEqualInt >= 1) {
						gameHandler.resetNumbers();
						MainActivity.showImages();
					}
				}
				break;
		}
		return true;
	}
	
	public static void playEqualSound() {
		((Vibrator) mainActivity.getSystemService("vibrator")).vibrate(1000);
		//mainActivity.soundPool.play(mainActivity.equal, 1, 1, 0, 0, 1);
	}
	
	/**
	 * Changes the "Pointlessness" text
	 * @param text
	 */
	public static void setPointlessnessText(String text) {
		pointlessness.setText(text);
	}
	
	/**
	 * Gets the value of pointlessness
	 * @return Pointlessness text
	 */
	public static String getPointlessnessText() {
		return pointlessness.getText().toString();
	}
	
	/**
	 * Changes the "Play" text visibility
	 * @param text
	 */
	public static void showPlayText(boolean show) {
		if (!show) {
			play.setVisibility(View.INVISIBLE);
		} else {
			play.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Changes whether or not the reset text is visible, along with changing the contents of the reset text
	 */
	public static void showResetImage(boolean show) {
		if (!show) {
			reset.setVisibility(View.INVISIBLE);
		} else {
			reset.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Makes the touch and swipe images visible
	 */
	public static void showImages() {
		swipe.setVisibility(View.VISIBLE);
		if (timesEqualInt >= 1) {
			touch.setVisibility(View.VISIBLE);
			reset.setVisibility(View.VISIBLE);
		}
	}
	
	public void onPause() {
		super.onPause();
		//Save the game and close, if the game was being played
		if (gameHandler.isPlaying) {
			saveGame(GameHandler.numbers[0], GameHandler.numbers[1], GameHandler.numbers[2], timesEqualInt);
		}
		finish();
	}
	
	public void onResume() {
		super.onResume();
		//Load the game back up again
		loadGame();
	}
	
	/**
	 * Updates the times the numbers were equal to zero
	 * @param increment
	 */
	public static void updateTimesNumbersEqual(boolean increment) {
		if (increment) {
			timesEqualInt++;
			
			if (timesEqualInt - 1 <= 22 && timesEqualInt - 1 >= 0) {
				timesEqual.setText(storyPhrases.get(timesEqualInt - 1));
			} else {
				timesEqual.setText(endPhrases.get(GameHandler.random.nextInt(12)));
			}
		}
	}
	
	/**
	 * Save the game so that we can go back to where we started
	 * @param num1
	 * @param num2
	 * @param num3
	 * @param timesEqual
	 */
	public void saveGame(int num1, int num2, int num3, int timesEqual) {
		//The string that will be saved on to the file
		String saveText = num1 + ":" + num2 + ":" + num3 + ":" + timesEqual + ":";
		
		//The output stream used to save the file
		FileOutputStream outputStream;
		
		try {
			//Save the save file
			outputStream = this.openFileOutput("saveFile", 0);
			outputStream.write(saveText.getBytes());
			outputStream.close();
		} catch (IOException e) {
			//If something goes wrong
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads the game when the game starts.  If there is no save file yet, nothing will happen
	 */
	public void loadGame() {
		File saveFile = new File("saveFile");
		
		//AssetManager to access phrases
		AssetManager am = this.getAssets();
		
		if (saveFile.exists()) {
			//Get string from file
			String received = null;
			try {
				FileInputStream inputStream;
				inputStream = openFileInput("saveFile");
				byte[] textArray = new byte[inputStream.available()];
				while (inputStream.read(textArray) != -1) {
					received = new String(textArray);
				}
				inputStream.close();
			} catch (IOException e) {
				//If something goes wrong
				e.printStackTrace();
			}
			
			//Get the values from the string
			GameHandler.numbers[0] = Integer.parseInt(received.split(":")[0]);
			GameHandler.numbers[1] = Integer.parseInt(received.split(":")[1]);
			GameHandler.numbers[2] = Integer.parseInt(received.split(":")[2]);
			timesEqualInt = Integer.parseInt(received.split(":")[3]);
		}
		{
			//Get string from file
			String received = null;
			try {
				InputStream inputStream;
				inputStream = am.open("upper.txt");
				byte[] textArray = new byte[inputStream.available()];
				while (inputStream.read(textArray) != -1) {
					received = new String(textArray);
				}
				inputStream.close();
			} catch (IOException e) {
				//If something goes wrong
				e.printStackTrace();
			}
			
			//Get the values from the string
			String[] arrayPhrases = received.split("\n");
			storyPhrases = Arrays.asList(arrayPhrases);
		}
		{
			//Get string from file
			String received = null;
			try {
				InputStream inputStream;
				inputStream = am.open("upper copy.txt");
				byte[] textArray = new byte[inputStream.available()];
				while (inputStream.read(textArray) != -1) {
					received = new String(textArray);
				}
				inputStream.close();
			} catch (IOException e) {
				//If something goes wrong
				e.printStackTrace();
			}
			
			//Get the values from the string
			String[] arrayPhrases = received.split("\n");
			endPhrases = Arrays.asList(arrayPhrases);
		}
	}
}