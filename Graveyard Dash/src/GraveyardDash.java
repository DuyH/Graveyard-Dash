/*
 * Duy Huynh
 * June 9, 2014
 * GraveyardDash.java
 * In this game, the player must collect
 * as many pages as possible, while avoiding
 * being hurt by ghosts. The player can only
 * see the map and its obstacles in the beam
 * of light. The player is able to illuminate
 * the entire map by activating the lantern 
 * for a short duration of time.
 * 
 * Sources:
 * All artwork done by me.
 * Sounds:
 * heart.wav, lantern.wav, ghost.wav, ouch.wav were 
 * created using http://www.superflashbros.net/as3sfxr.
 * page.wav free to use from http://www.freesound.org/.
 * scaryBG.wav is “Spooky Loop” (by 8bit Betty)
 * from http://freemusicarchive.org (free to use).
 */

import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import acm.graphics.GImage;
import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GRect;
import acm.graphics.GRectangle;
import acm.io.IODialog;
import acm.program.GraphicsProgram;
import acm.util.RandomGenerator;

@SuppressWarnings("serial")
public class GraveyardDash extends GraphicsProgram {

	// Class constants
	private static final int APPLET_WIDTH = 800;
	private static final int APPLET_HEIGHT = 600;
	private static final int MOVE = 5;// px player movement
	private static final int PLAYER_START_X = 101;
	private static final int PLAYER_START_Y = 151;
	private static final int WAIT = 20;// animation pause (ms)
	private static final int SCAN = 10;// #px to scan ahead for collisions
	private static final int SPRITE_SIZE = 50;// each "tile" is 50x50
	private static final int GRID_COL = APPLET_WIDTH / SPRITE_SIZE;// 50
	private static final int GRID_ROW = APPLET_HEIGHT / SPRITE_SIZE;// 50
	private static final int GRAVES_MAX = 30;
	private static final int SEND_BACK_AMT = 20;// so pickups don't show on top
	private static final int NEXT_PAGE = 3;// Every 3 pages collected = ghost
	private static final int NO_DMG_TIME = 3000;// 3sec invuln after hit
	private static final int PICKUP_SPAWN_TIME = 10000;// 10sec b4 reposition
	private static final int LANTERN_DURATION = 1000;// 1sec for each charge
	private static final int PICKUP_FRAME_WAIT = 360;
	private static final int METER_SIZE = 20;// lantern meter

	// Entities
	private Player player;
	private ArrayList<Ghost> ghost;

	// Pickups
	private GImage heart, lanternOil, page;// pickups
	private int pageCount;// # pages collected
	private int nextGhost;// What page count next ghost will occur

	// Map objects
	private ArrayList<GImage> obstacles;// graves, wall borders
	boolean[][] occupied;// contains x,y of obstacles
	private GImage background;

	// Game mechanics
	private int time;// Keeps track of time elapsed
	private double scanX, scanY;// x,y of "scan ahead" bounding box
	private Light light;// Rotating lighthouse light
	private int lanternStartTime;// records time when player activates lantern
	private int timeHit;// records time when player collides with ghost
	private int animateHeart, animateLantern, animatePage;// for animation

	// Sound effects
	private AudioClip heartSFX = getAudioClip(getCodeBase(), "heart.wav");
	private AudioClip lanternSFX = getAudioClip(getCodeBase(), "lantern.wav");
	private AudioClip ghostSFX = getAudioClip(getCodeBase(), "ghost.wav");
	private AudioClip pageSFX = getAudioClip(getCodeBase(), "page.wav");
	private AudioClip ouchSFX = getAudioClip(getCodeBase(), "ouch.wav");
	private AudioClip music = getAudioClip(getCodeBase(), "scaryBG.wav");

	// HUD
	private Font gameFont = new Font("Comic Sans MS", Font.PLAIN, 36);
	private GImage[] hearts;
	private GRect[] lanternCharges;
	private GLabel pageCountLabel;

	// For tile based movement
	private boolean isMoving;
	private String direction = "";
	private int steps;
	private RandomGenerator random = new RandomGenerator();
	private int moveX, moveY;// player's movement in px

	public void init() {

		// setSize(APPLET_WIDTH, APPLET_HEIGHT);
		setFont(gameFont);

		music.loop();// bg music

	}// init

	public void run() {
		addKeyListeners();
		setSize(APPLET_WIDTH, APPLET_HEIGHT);
		// Game loop
		while (true) {

			// Setup the game's HUD, background, obstacles, mechanics
			gameSetup();

			// Play the game until player has no health left
			while (player.getHealth() > 0) {

				// Adding elements
				spawnPickups();// 10sec spawn/reposition
				addGhosts();

				// Entity movement
				playerMove();
				ghostMove();// ghost every 3pgs

				// Update things
				ghostCollisions();
				pickupCollisions();
				depleteLanternFuel();

				// Animation things
				animationUpdates();

			}// Exiting loop = player has no health left

			// GameOver
			gameOver();
			removeAll();

		}// Replay the game

	}// run

	// Game setup
	private void gameSetup() {

		// Set the background
		background = new GImage("grassTileBackground.png");
		add(background);
		occupied = new boolean[GRID_COL][GRID_ROW];

		// Create graves/borders
		createObstacles();
		createBorders();

		// Lighthouse light (Any object created BEFORE this, will be hidden
		addLight();

		// Display health, lantern, pages HUD
		createHUD();
		pageCount = 0;
		pageCountLabel.setLabel("" + pageCount);

		// Ghost
		Ghost.resetCount();
		ghost = new ArrayList<Ghost>();
		nextGhost = NEXT_PAGE;

		// Player
		player = new Player();
		add(player, PLAYER_START_X, PLAYER_START_Y);

	}// gameSetup

	// Create the obstacles (graves) in the game
	private void createObstacles() {

		// Put the graves in an array list
		obstacles = new ArrayList<GImage>();
		for (int i = 0; i < GRAVES_MAX; i++) {
			obstacles
					.add((GImage) placeInUnoccupiedTile(new GImage("grave.png")));
		}

	}// createObstacles

	// Create collidable borders around game area
	private void createBorders() {

		// Add border images
		GImage bottomWall = new GImage("fenceTileBottom.png");
		obstacles.add(bottomWall);
		add(bottomWall, 0, (APPLET_HEIGHT - SPRITE_SIZE));

		GImage topWall = new GImage("fenceTileBottom.png");
		obstacles.add(topWall);
		add(topWall, 0, SPRITE_SIZE);

		GImage leftWall = new GImage("brickTileBorder.png");
		obstacles.add(leftWall);
		add(leftWall);

		GImage rightWall = new GImage("brickTileBorder.png");
		obstacles.add(rightWall);
		add(rightWall, APPLET_WIDTH - SPRITE_SIZE, 0);

	}// createBorders

	// Create the lighthouse light
	private void addLight() {

		// Add a new rotating light, center it
		light = new Light();
		add(light, (APPLET_WIDTH - light.getWidth()) / 2,
				(APPLET_HEIGHT - light.getHeight()) / 2);

	}// addLight

	// Create the player's status counters for the top bar
	private void createHUD() {

		// Display HUD
		GImage topBar = new GImage("displayHUD.png");
		add(topBar);

		// Hearts
		heartDisplay();

		// Lantern Charges
		lanternDisplay();

		// Page Count
		pageDisplay();

	}// createHUD

	// Create the heart icons for health
	private void heartDisplay() {

		hearts = new GImage[Player.HEALTH_MAX];
		for (int i = 0; i < hearts.length; i++) {
			hearts[i] = new GImage("heart.png");
			add(hearts[i], 100 + 25 * i, 0);// x,y
		}

	}// heartDisplay

	// Create the lantern charge icons
	private void lanternDisplay() {

		// Create a meter to show lantern charge
		lanternCharges = new GRect[Player.LANTERN_CHARGE_MAX];
		for (int i = 0; i < lanternCharges.length; i++) {
			lanternCharges[i] = new GRect(METER_SIZE, METER_SIZE);
			lanternCharges[i].setFilled(true);
			lanternCharges[i].setFillColor(Color.ORANGE);
			add(lanternCharges[i], 400 + 20 * i, 20);// x,y
		}

	}// lanternDisplay

	// Create the page count label
	private void pageDisplay() {

		pageCountLabel = new GLabel("" + pageCount);
		pageCountLabel.setFont(gameFont);
		pageCountLabel.setColor(Color.WHITE);
		add(pageCountLabel, 750, 40);// x,y

	}// pageDisplay

	// Display Game Over dialog box
	private void gameOver() {

		IODialog gameOver = new IODialog();
		gameOver.println("YOU DIED!");
		isMoving = false;// Prevent further movement
		steps = 0;// ^also avoid glitchy collisions

	}// gameOver

	// Animate pickup items and lighthouse light
	private void animationUpdates() {

		// Animate the pickups to bob up and down in place
		animatePickup(heart, "heart", animateHeart);
		animatePickup(lanternOil, "lanternOil", animateLantern);
		animatePickup(page, "page", animatePage);

		// Animate the rotating lighthouse light
		light.animate();

		// Update the time and pause for animation
		time += WAIT;
		pause(WAIT);

	}// animationUpdates

	// Update lantern charge meter
	private void depleteLanternFuel() {

		// Deplete lantern charges if it is being used
		if (light.isVisible() == false) {

			// Deplete a charge every 1sec
			if (time != lanternStartTime
					&& (time - lanternStartTime) % LANTERN_DURATION == 0) {
				player.setLanternCharge(player.getLanternCharge() - 1);
				remove(lanternCharges[player.getLanternCharge()]);
			}

			// If out of lantern charges, disable it. Show the lighthouse light
			if (player.getLanternCharge() <= 0) {
				light.setVisible(true);
				player.setLanternOn(false);
			}
		}

	}// depleteLanternFuel

	// When player collides with a ghost...
	private void ghostCollisions() {

		// When player was hurt, allow 3 secs of invulnerability
		if (time - timeHit > NO_DMG_TIME) {
			player.setDamageState(false);
		}
		// Under those 3sec of invulnerability, flash player as visual cue
		if (player.getDamageState()) {
			player.setVisible(!player.isVisible());
		}

		// Loop through each of the ghosts:
		for (int i = 0; i < ghost.size(); i++) {
			// If player currently not taking hits then...
			if (player.getDamageState() == false) {
				if (player.getBounds().intersects(ghost.get(i).getBounds())) {
					timeHit = time;
					ouchSFX.play();
					player.setDamageState(true);
					player.setHealth(player.getHealth() - 1);// -1 hp
					remove(hearts[player.getHealth()]);
				}
			}
		}

	}// ghostCollisions

	// How each ghost moves about in the game:
	private void ghostMove() {

		// Make ghost follow the player (only if lantern is OFF)
		if (light.isVisible() == true) {
			for (int i = 0; i < ghost.size(); i++) {
				ghost.get(i).move(player, ghost.get(i));
			}
		}
		ghostlyJitters();

	}// ghostMove

	// Causes each ghost to jitter around in the game
	private void ghostlyJitters() {

		// Spooky jittery ghost movements and avoid bunching up
		for (int i = 0; i < ghost.size(); i++) {
			if (ghost.get(i).getBounds().intersects(ghost.get(i).getBounds())) {
				ghost.get(i).move(random.nextInt(-MOVE, MOVE),
						random.nextInt(-MOVE, MOVE));
			}
		}

	}// ghostlyJitters

	// Spawn each of the pickups
	private void spawnPickups() {

		spawnHealth();
		spawnLanternOil();
		spawnPage();

	}// spawnPickups

	// Spawn a Ghost
	private void addGhosts() {

		// add a ghost every 5 pages
		if (nextGhost == pageCount) {
			ghost.add(Ghost.getCount(), new Ghost());
			placeInUnoccupiedTile(ghost.get(Ghost.getCount() - 1));
			ghostSFX.play();
			nextGhost += NEXT_PAGE;
		}

	}// addGhosts

	// Places an object in an unoccupied tile
	private GObject placeInUnoccupiedTile(GObject thing) {

		boolean placed = false;
		while (!placed) {// keep looking for an empty tile
			int x = random.nextInt(1, GRID_COL - 2);
			int y = random.nextInt(2, GRID_ROW - 2);
			if (occupied[x][y] == false) {// Place object in empty tile
				add(thing, x * SPRITE_SIZE, y * SPRITE_SIZE);
				occupied[x][y] = true;
				placed = true;
				return thing;
			}
		}
		return null;

	}// placeInUnoccupiedTile

	// Animate pickups, making them bob up and down in place
	private void animatePickup(GImage pickup, String file, int frameNum) {

		if (pickup != null) {// ensures the pickup has been initialized
			// Change the image every 0.5s to create bobbing animation
			if (time % PICKUP_FRAME_WAIT == 0) {
				if (frameNum == 0) {
					pickup.setImage(file + ".png");
				} else if (frameNum == 1) {
					pickup.setImage(file + "2.png");
				} else if (frameNum == 2) {
					pickup.setImage(file + "3.png");
				} else if (frameNum == 3) {
					pickup.setImage(file + "2.png");
					frameNum = -1;// reset the loop
				}
				frameNum++;
				if (file.equals("heart")) {
					animateHeart = frameNum;
				} else if (file.equals("lanternOil")) {
					animateLantern = frameNum;
				} else if (file.equals("page")) {
					animatePage = frameNum;

				}
			}
		}

	}// animatePickup

	// Relocate pickup after some time has passed
	private void relocatePickup(GImage pickup) {

		// Relocating pickup prevents "trapped" pickups
		if (time % PICKUP_SPAWN_TIME == 0 && pickup != null) {
			placeInUnoccupiedTile(pickup);

			// Send the pickup backwards; prevent it from being visible on top
			for (int i = 1; i <= SEND_BACK_AMT + Ghost.getCount(); i++) {
				pickup.sendBackward();
			}
		}

	}// relocatePickup

	// Pickup collisions
	private void pickupCollisions() {

		pickupHeart();
		pickupLanternOil();
		pickupPage();

	}// pickupCollisions

	// Spawn a heart pickup
	private void spawnHealth() {

		// Relocate the pickup after time has passed
		relocatePickup(heart);

		// Spawn item on a random empty tile
		if (time % PICKUP_SPAWN_TIME == 0 && heart == null) {
			if (random.nextInt(0, 2) == 0) {
				heart = new GImage("heart.png");
				placeInUnoccupiedTile(heart);
				for (int i = 1; i <= SEND_BACK_AMT + Ghost.getCount(); i++) {
					heart.sendBackward();
				}
			}
		}

	}// spawnHealth

	// Spawn a lantern charge
	private void spawnLanternOil() {

		// Relocate the pickup after time has passed
		relocatePickup(lanternOil);

		// Spawn item on a random empty tile
		if (time % PICKUP_SPAWN_TIME == 0 && lanternOil == null) {
			if (random.nextInt(0, 1) == 0) {
				lanternOil = new GImage("lanternOil.png");
				placeInUnoccupiedTile(lanternOil);
				for (int i = 1; i <= SEND_BACK_AMT + Ghost.getCount(); i++) {
					lanternOil.sendBackward();
				}
			}
		}

	}// spawnLanternOil

	// Spawn a page charge
	private void spawnPage() {

		// Relocate the pickup after time has passed
		relocatePickup(page);

		// Spawn item on a random empty tile
		if (time % PICKUP_SPAWN_TIME == 0 && page == null) {
			page = new GImage("page.png");
			placeInUnoccupiedTile(page);
			for (int i = 1; i <= SEND_BACK_AMT + Ghost.getCount(); i++) {
				page.sendBackward();
			}
		}

	}// spawnPage

	// Heart pickup collision
	private void pickupHeart() {

		// Increment health +1 on collision
		if (heart != null) {// as long as a heart can be picked up
			if (player.getBounds().intersects(heart.getBounds())) {
				player.setHealth(player.getHealth() + 1);
				add(hearts[player.getHealth() - 1]);
				heartSFX.play();
				remove(heart);
				heart = null;
			}
		}

	}// pickupHeart

	// Lantern Oil pickup collision
	private void pickupLanternOil() {

		// Increment lantern charge +1 on collision
		if (lanternOil != null) {// as long as there is a lantern on the map
			if (player.getBounds().intersects(lanternOil.getBounds())) {
				player.setLanternCharge(player.getLanternCharge() + 1);
				add(lanternCharges[player.getLanternCharge() - 1]);
				lanternSFX.play();
				remove(lanternOil);
				lanternOil = null;
			}
		}

	}// pickupLanternOil

	// Page pickup collision
	private void pickupPage() {

		// Increment page count +1 on collision
		if (page != null) {// as long as there is a page available
			if (player.getBounds().intersects(page.getBounds())) {
				pageCountLabel.setLabel("" + ++pageCount);
				pageSFX.play();
				remove(page);
				page = null;
			}
		}

	}// pickupPage

	// Player movement
	private void playerMove() {

		// Ignores keyboard commands until next tile is reached
		scanAheadCollisions(player, direction, obstacles);
		if (isMoving) {
			if (steps < SPRITE_SIZE / MOVE) {
				player.move(moveX, moveY);
				steps++;
			}
			// Reset steps, allow player to move again
			if (steps == SPRITE_SIZE / MOVE) {
				isMoving = false;
				steps = 0;
			}
		}

	}// playerMove

	// Scans one tile ahead in keypress direction, checking for obstacles
	private void scanAheadCollisions(GObject actor, String direction,
			ArrayList<GImage> collidables) {

		// Arrow keys determine location of the "look-ahead" box
		switch (direction) {
		case "up":
			scanX = actor.getX() + SCAN;
			scanY = actor.getY() - SCAN;
			break;
		case "down":
			scanX = actor.getX() + SCAN;
			scanY = actor.getY() + actor.getHeight() + SCAN;
			break;

		case "left":
			scanX = actor.getX() - SCAN;
			scanY = actor.getY() + SCAN;
			break;

		case "right":
			scanX = actor.getX() + actor.getWidth() + SCAN;
			scanY = actor.getY() + SCAN;
			break;
		}

		// Check the destination tile beforehand for possible collision
		for (int i = 0; i < collidables.size(); i++) {// check against array
			if (new GRectangle(scanX, scanY, 1, 1).intersects((collidables
					.get(i)).getBounds())) {
				isMoving = false;// Collision detected so stop moving
			}
		}
		this.direction = "";// empty current input for next keyboard press

	}// scanAheadCollisions

	// Key presses
	public void keyPressed(KeyEvent k) {

		int key = k.getKeyCode();

		// "&& !isMoving" disables kb ctrl until player done moving to next tile
		if (key == KeyEvent.VK_UP && !isMoving) {
			moveY = -MOVE;
			moveX = 0;
			isMoving = true;
			direction = "up";
			player.setImage("girlBack.png");
		}

		if (key == KeyEvent.VK_DOWN && !isMoving) {
			moveY = MOVE;
			moveX = 0;
			isMoving = true;
			direction = "down";
			player.setImage("girlLeft.png");
		}

		if (key == KeyEvent.VK_LEFT && !isMoving) {
			moveX = -MOVE;
			moveY = 0;
			isMoving = true;
			direction = "left";
			player.setImage("girlLeft.png");
		}

		if (key == KeyEvent.VK_RIGHT && !isMoving) {
			moveX = MOVE;
			moveY = 0;
			isMoving = true;
			direction = "right";
			player.setImage("girlRight.png");
		}

		// Activate lantern
		if (key == KeyEvent.VK_SPACE) {
			// If Lighthouse beam is visible and lantern charge to spare:
			if (light.isVisible() && player.getLanternCharge() > 0) {
				lanternStartTime = time;// Keep track of time lantern activated
				light.setVisible(false);// Deactivate Lighthouse beam
				// Force at least one charge used before deactivating lantern
			} else if (time - lanternStartTime > LANTERN_DURATION) {
				light.setVisible(true);// Lighthouse beam becomes visible
			}
		}

		// Exit out of the game
		if (key == KeyEvent.VK_ESCAPE) {
			System.exit(-1);
		}

		// Print out the player's status in the console
		if (key == KeyEvent.VK_S) {
			System.out.println(player.toString());
		}

		// Misc key presses for debugging purposes
		if (key == KeyEvent.VK_MINUS) {
			player.setHealth((player.getHealth() - 1));
			System.out.println("CHEAT!: Health was decreased by 1");
		}
		if (key == KeyEvent.VK_EQUALS) {
			player.setHealth((player.getHealth() + 1));
			System.out.println("CHEAT!: Health was increased by 1");
		}
		if (key == KeyEvent.VK_9) {
			player.setLanternCharge((player.getLanternCharge() - 1));
			System.out.println("CHEAT!: Lantern was decreased by 1");
		}
		if (key == KeyEvent.VK_0) {
			player.setLanternCharge((player.getLanternCharge() + 1));
			System.out.println("CHEAT!: Lantern was increased by 1");
		}
		if (key == KeyEvent.VK_R) {// Force game over
			player.setHealth(0);// set health to 0, restart the game
		}
		if (key == KeyEvent.VK_P) {
			pageCount++;// increases page count +1
		}
	}// keyPressed

	public static void main(String[] args) {
		GraveyardDash game = new GraveyardDash();
		game.init();
		game.start(args);

	}

}// GraveyardDash
