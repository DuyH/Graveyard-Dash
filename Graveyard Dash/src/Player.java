/*
 * Duy Huynh
 * June 9, 2014
 * Player.java
 * This creates a player. Health, 
 * Lantern charge/state, and dmg
 * state are gotten/set from here.
 */

import java.awt.Color;
import acm.graphics.GCompound;
import acm.graphics.GImage;
import acm.graphics.GOval;

public class Player extends GCompound {

	// Class constants
	public static final int HEALTH_MAX = 5;// player's max health
	public static final int LANTERN_CHARGE_MAX = 10;// in secs
	private static final int SPEED = 5;// movement in px
	private static final int LIGHT_LOC = 46;// x,y placement of lantern light

	// Instance fields
	private int health;
	private int lanternCharge;
	private int playerSpeed;
	private boolean lanternState;
	private boolean takingDamage;
	private GImage player;

	// Default constructor for creating a player
	public Player() {

		health = HEALTH_MAX;
		lanternCharge = LANTERN_CHARGE_MAX;
		playerSpeed = SPEED;
		lanternState = false;
		takingDamage = false;
		spawnPlayer();

	}// Player (default)

	// "Custom" constructor for creating a player
	public Player(int health, int lanternCharge, int playerSpeed) {

		this.health = health;// Player HP
		this.lanternCharge = lanternCharge;// Max lantern charge
		this.playerSpeed = playerSpeed;// player's movement speed

	}// Player (custom)

	private void spawnPlayer() {

		player = new GImage("girlRight.png");
		add(player);

		// Create a transparent light around character
		GOval light = new GOval(LIGHT_LOC, LIGHT_LOC);
		light.setFilled(true);
		light.setColor(new Color(255, 255, 0, 100));// transparent)
		add(light, 1, 1);

	}// spawnPlayer

	// Getters
	public int getHealth() {

		// Return the health of the player
		return health;

	}// getHealth

	public int getLanternCharge() {

		// Return the # of lantern charges
		return lanternCharge;

	}// getLanternCharge

	public boolean getLanternState() {

		// Return if lantern is on/off
		return lanternState;

	}// getLanternState

	public int getSpeed() {

		// Return movement speed of player
		return playerSpeed;

	}// getSpeed

	public boolean getDamageState() {

		// Return if the player is currently taking dmg
		return takingDamage;

	}// takingDamage

	// Setters
	public void setHealth(int health) {

		// Set the health of the player
		if (health <= HEALTH_MAX) {
			this.health = health;
		}

	}// setHealth

	public void setLanternCharge(int lanternCharge) {

		// Set the number of lantern charges
		if (lanternCharge <= LANTERN_CHARGE_MAX) {
			this.lanternCharge = lanternCharge;
		}

	}// setLanternCharge

	public void setLanternOn(boolean lanternState) {

		// Set if lantern is on or off
		this.lanternState = lanternState;

	}// setLanternOn

	public void setDamageState(boolean takingDamage) {

		// Set if player is currently taking dmg
		this.takingDamage = takingDamage;

	}// setDamageState

	public void setImage(String file) {

		// Set the image of the player for direction changes
		player.setImage(file);

	}// setImage

	// Other methods
	public void activateLantern(int gameTime) {

		// can only turn on if fuel available and lantern is off
		if (lanternState == false && lanternCharge > 0) {
			lanternState = true;
			System.out.println("Lantern on!");// debug
		} else if (lanternState == true && lanternCharge > 0) {
			lanternState = false;
			System.out.println("Lantern off!");// debug
		} else {
			System.out.println("Not enough lantern fuel!");// debug
		}

	}// activateLantern

	public String toString() {

		// Return status of the player
		return "Player has: \n" + "Health: " + health + "\nLantern Fuel: "
				+ lanternCharge + "\nIs lantern on?: " + lanternState;

	}// toString

}// Player
