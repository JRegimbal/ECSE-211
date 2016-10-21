package utilities;

import chassis.Lab5;
import chassis.USSensor;
import lejos.hardware.Sound;
import lejos.robotics.Color;
import utilities.Odometer.LINEDIR;

import java.lang.reflect.Array;

import chassis.ColorSensor;

public class Search extends Thread {
	private static final int US_SAMPLES = 10;
	private Odometer odo;
	private USSensor usSensor;
	private ColorSensor colorSensor;
	private final float FIELD_BOUNDS = 65; //cm
	private float lastDistanceDetected;
	private final float DISTANCE_THRESHOLD = 25; //cm
	private final double fieldToSearch = Math.PI/2;
	private final double fieldIncrement = 5*Math.PI/180; //5-degree increments
	private final float[][] scanPoints = new float[][] {{0, 0, 0}, {0, 60, (float)Math.PI/2}};
	private final int SEARCH_SPEED = 200;
	private final float BLOCK_COLOR = Color.BLUE;
	private final static float BLOCK_DISTANCE = 4.0f; //distance to detect block type in cm
	public static double[] blockLocation;
	public static double[] obstacleLocation;
	
	public Search(Odometer odo, ColorSensor colorSensor, USSensor usSensor) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.usSensor = usSensor;
	}
	@Override
	public void run() {
		while(Lab5.state != Lab5.RobotState.k_Search) {
			//wait for localization to finish
			try {
				Thread.sleep(300);
			} catch (Exception e) {}
		}		
		if(Lab5.demo == Lab5.DemoState.k_Part2) {
			//PART 2
			//TODO: Comb through track, check for detection
			//sweep track for obstacles from position (0,0) starting at 0-radians
			int scanPointNumber = 0;
			boolean blockFound=false, obstacleFound=false;
			
			//while(!blockFound && !obstacleFound) {
			while(!blockFound || !obstacleFound) {	
				lastDistanceDetected = FIELD_BOUNDS;
				double thetaScanStart = odo.getTheta();
				boolean objectFound;
				odo.setMotorSpeeds(odo.ROTATE_SPEED, odo.ROTATE_SPEED);
				odo.spin(Odometer.TURNDIR.CCW);
				while(!(objectFound = isObjectDetected()) && odo.getTheta() < thetaScanStart + fieldToSearch) {	//check if there is an object at current heading or if area has been scanned
					Navigator.turnBy(-fieldIncrement);
				}
				if(objectFound) {	//go to object, check if it is a styrofoam block
					odo.setMotorSpeeds(SEARCH_SPEED, SEARCH_SPEED);
					odo.forwardMotors();
					while(usSensor.getSampleAverage(US_SAMPLES) > BLOCK_DISTANCE); //wait until close enough to detect
					odo.setMotorSpeeds(0, 0);
					odo.forwardMotors();
					if(isStyrofoamBlock()) {	//begin capture
						blockFound = true;
						//blockLocation = new double[] {odo.getX(), odo.getY()};
						Sound.beep();
					} else {
						obstacleFound = true;
						//obstacleLocation = new double[] {odo.getX(), odo.getY()};
						Sound.twoBeeps();
					}
					odo.moveCM(LINEDIR.Backward, 8, true);
				} else {	//go to next scan point
					//scanPointNumber = (scanPointNumber+1) % scanPoints.length;
				}
				chassis.LCDInfo.displayMessage("Go to origin.");
				Navigator.travelTo(0, 0);
				Navigator.turnTo(0);
				chassis.LCDInfo.displayMessage("");
				//odo.travelTo(scanPoints[scanPointNumber][0], scanPoints[scanPointNumber][1]);	//travel to scan point
				//odo.turnTo(scanPoints[scanPointNumber][2]);
			}
			
			//Styrofoam block found - begin capture
			Lab5.state = Lab5.RobotState.k_Capture;
		} else { //PART 1
			while(true) {
				chassis.LCDInfo.getLCD().clear();
				if(usSensor.getSampleAverage(US_SAMPLES) <= BLOCK_DISTANCE) {
					chassis.LCDInfo.getLCD().drawString("Object Detected", 0, 0);
					if(isStyrofoamBlock()) {
						chassis.LCDInfo.getLCD().drawString("Block", 0, 1);
					} else {
						chassis.LCDInfo.getLCD().drawString("Not Block", 0, 1);
					}
				}
				try {
					Thread.sleep(500);
				} catch(Exception e) { }
			}
		}
	}
	
	private boolean isStyrofoamBlock() {
		return (colorSensor.getColor() == BLOCK_COLOR);
	}
	
	private boolean isObjectDetected() {
		float currentDistance = usSensor.getSampleAverage(US_SAMPLES);
		boolean isObject = (lastDistanceDetected - currentDistance > DISTANCE_THRESHOLD);
		lastDistanceDetected = currentDistance;
		return isObject;
	}
	
}
