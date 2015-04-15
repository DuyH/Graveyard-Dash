/*
 * Duy Huynh
 * June 9, 2014
 * Ghost.java
 * This class creates a ghost
 * to chase the player in the game.
 */

import acm.graphics.GCompound;
import acm.graphics.GImage;

public class Ghost extends GCompound {

	// Class constants
	private static final int HP_MAX = 1;
	private static final int SPEED = 2;

	// Instance fields
	private int health;
	private int speed;
	private boolean isVisible;
	private static int count;

	// Default Ghost
	public Ghost() {

		health = HP_MAX;
		speed = SPEED;
		isVisible = false;
		count++;// When a ghost created, increment ghost count
		spawnGhost();

	}// Ghost

	// Custom Ghost
	public Ghost(int health) {

		count++;// When a ghost created, increment ghost count
		spawnGhost();

	}// Ghost

	// Create a ghost
	private void spawnGhost() {

		GImage ghost = new GImage("ghost.png");
		add(ghost);

	}// spawnGhost

	// Getters
	public int getHealth() {

		return health;

	}// getHealth

	public int getSpeed() {

		return speed;

	}// getSpeed

	public boolean getVisibility() {

		return isVisible;

	}// getVisibility

	public static int getCount() {

		return count;

	}// getCount

	// Setters
	public void setHealth(int health) {

		if (health >= 0 && health <= HP_MAX) {
			this.health = health;
		}

	}// setHealth

	public void setSpeed(int speed) {

		if (speed >= 0) {
			this.speed = speed;
		}

	}// setSpeed

	public void setVisibility(boolean visibility) {

		isVisible = visibility;

	}// setVisibility

	public static void resetCount() {

		count = 0;

	}// resetCount

	// Other methods

	// Move the ghosts
	public void move(Player player, Ghost ghost) {

		double moveX, moveY;
		moveX = ghost.getX() - player.getX();
		moveY = ghost.getY() - player.getY();
		move(-moveX * 0.01, -moveY * 0.01);
	}

}// Ghost
