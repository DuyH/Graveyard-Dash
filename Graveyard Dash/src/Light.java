/*
 * Duy Huynh
 * June 9, 2014
 * Light.java
 * This creates the rotating light.
 */

import java.awt.Color;
import acm.graphics.GArc;
import acm.graphics.GCompound;

public class Light extends GCompound {

	// Class constants
	private static final int START_ANGLE = 135;
	private static final int FULL_CIRCLE = 360;// degrees
	private static final int ARC = 60;
	private static final int SIZE = 1000;

	// Instance fields
	private int size;// width&height of circumscribed square
	private int arc;// the arc angle of light
	private double rotationSpeed;// should be +/- 0.0-1.0
	private Color lightColor;

	// Making the object...
	private GArc light;
	private GArc shadow;

	// Default constructor
	public Light() {

		size = SIZE;
		arc = ARC;
		rotationSpeed = 1;
		lightColor = new Color(255, 255, 0, 100);// transparent

		createLight(size, arc);
		createShadow(size, arc);

	}// Light (default)

	// Custom constructor
	public Light(int size, int arc) {

		this.size = size;
		this.arc = arc;
		rotationSpeed = 1.0;
		lightColor = new Color(255, 255, 0, 100);// transparent Yellow

		createLight(size, arc);
		createShadow(size, arc);

	}// Light (custom)

	// GETTERS
	public int getDimensions() {

		// Return circle width/height of light
		return size;

	}// getDimensions

	public int getArc() {

		// Return the arc size of the cone of light
		return arc;

	}// getArc

	public double getSpeed() {

		// from -1,1
		return rotationSpeed;

	}// getSpeed

	public Color getColor() {

		// return the color of the light
		return lightColor;

	}// getColor

	// SETTERS
	public void setDimensions(int size) {

		// /Set the size of the circle
		if (size > 0) {
			this.size = size;
		}

	}// setDimensions

	public void setArc(int arc) {

		// Set the arc of the cone
		if (arc >= 0) {
			this.arc = arc;
		}

	}// setArc

	public void setColor(Color color) {

		// Set cone of light's color
		lightColor = color;

	}// setColor

	public void setSpeed(double rotationSpeed) {

		// Negative reverses the rotation
		if (rotationSpeed >= -1.0 && rotationSpeed <= 1.0) {
			this.rotationSpeed = rotationSpeed;
		}

	}// setSpeed

	// MAKE THE GRAPHICS

	private void createLight(int size, int arc) {

		light = new GArc(size, size, START_ANGLE + arc / 2, -arc);
		light.setFilled(true);
		light.setColor(lightColor);
		add(light);

	}// createLight

	private void createShadow(int size, int arc) {

		shadow = new GArc(size, size, START_ANGLE + arc / 2, FULL_CIRCLE - arc);
		shadow.setFilled(true);
		shadow.setColor(Color.BLACK);
		add(shadow);

	}// createShadow

	// OTHER METHODS
	// Rotates light clockwise for positive rotationSpeed value
	public void animate() {

		light.setStartAngle((light.getStartAngle() + rotationSpeed));
		shadow.setStartAngle((shadow.getStartAngle() + rotationSpeed));

	}// animate

}// Light
