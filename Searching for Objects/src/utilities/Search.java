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
	private final double fieldToSearch = Math.PI;
	private final double fieldIncrement = 5*Math.PI/180; //5-degree increments
	private final float[][] scanPoints = new float[][] {{0, 0, 0}, {0, 60, (float)Math.PI/2}};
	private final float[][] corners = new float[][] {{0,0},{0,60.96f},{60.96f,60.96f},{0,60.96f}};
	private int corner;
	private int dir;
	private final int SEARCH_SPEED = 200;
	private final float BLOCK_COLOR = Color.BLUE;
	private final static float BLOCK_DISTANCE = 4.0f; //distance to detect block type in cm
	public static double[] blockLocation;
	public static double[] obstacleLocation;
	
	private int scanDir;
	
	public static final float[] STYROFOAM_COLOR = new float[] {0.0f,1.0f,1.0f};
	
	public Search(Odometer odo, ColorSensor colorSensor, USSensor usSensor) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.usSensor = usSensor;
		this.corner = 0;
		this.dir = 1;
		scanDir = -1;
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
			
			while(!blockFound) {
			//while(!blockFound || !obstacleFound) {	
				lastDistanceDetected = FIELD_BOUNDS;
				boolean objectFound = false;
				odo.setMotorSpeeds(odo.ROTATE_SPEED, odo.ROTATE_SPEED);
				//odo.spin(Odometer.TURNDIR.CCW);
				if(Navigation.PathBlocked) {
					dir = -dir;
					corner += dir;
					corner %= 4;
					Navigation nav = new Navigation(odo);
					nav.travelTo(corners[corner][0], corners[corner][1]);
				}
				if(corner %2 != 0) scanDir = 1;
				else scanDir = -1;
				double thetaScanStart = odo.getTheta();
				double targetAngle = odo.getTheta() + scanDir*fieldToSearch;
				if(targetAngle < 0.0) targetAngle += 360.0;
				if(targetAngle > 360.0) targetAngle -= 360.0;
				while(!(objectFound = isObjectDetected()) && Math.abs(odo.getTheta() - targetAngle) > Math.PI/30) {	//check if there is an object at current heading or if area has been scanned
					Navigator.turnBy(scanDir * fieldIncrement);
				}
				if(objectFound) {	//go to object, check if it is a styrofoam block
					Sound.twoBeeps();
					double distance = usSensor.getFilteredDataBasic();
					odo.moveCM(Odometer.LINEDIR.Forward, distance - 10, true);
					odo.setMotorSpeed(100);
					odo.spin(Odometer.TURNDIR.CCW);
					while(usSensor.getFilteredDataBasic() > 15);
					Sound.twoBeeps();
					odo.stopMotors();
					odo.setMotorSpeeds(60, 60);
					odo.forwardMotors();
					while(usSensor.getSampleAverage(US_SAMPLES) > BLOCK_DISTANCE); //wait until close enough to detect
					odo.setMotorSpeeds(0, 0);
					odo.forwardMotors();
					if(isStyrofoamBlock()) {	//begin capture
						blockFound = true;
						//blockLocation = new double[] {odo.getX(), odo.getY()};
						Sound.beep();
						//Lab5.state = Lab5.RobotState.k_Capture;
						//continue;
					} else {
						obstacleFound = true;
						//obstacleLocation = new double[] {odo.getX(), odo.getY()};
						Sound.twoBeeps();
					}
					odo.moveCM(LINEDIR.Backward, 13, true);
					if(!blockFound) {
						Navigation nav = new Navigation(odo);
						nav.travelTo(corners[corner][0], corners[corner][1]);
						corner += dir;
						corner %= 4;
						nav.travelTo(corners[corner][0], corners[corner][1]);
					}
				} else {	//go to next scan point
					Sound.twoBeeps();
					Sound.twoBeeps();
					Navigation nav = new Navigation(odo);
					corner += dir;
					corner %= 4;
					nav.travelTo(corners[corner][0], corners[corner][1]);
				}
				chassis.LCDInfo.displayMessage("Go to origin.");
				//Navigator.travelTo(0, 0);
				//Navigator.turnTo(0);
				chassis.LCDInfo.displayMessage("");
				//odo.travelTo(scanPoints[scanPointNumber][0], scanPoints[scanPointNumber][1]);	//travel to scan point
				//odo.turnTo(scanPoints[scanPointNumber][2]);
			}
			
			//Styrofoam block found - begin capture
			Sound.twoBeeps();
			Lab5.state = Lab5.RobotState.k_Capture;
		} else { //PART 1
			while(true) {
				chassis.LCDInfo.getLCD().clear();
				if(usSensor.getSampleAverage(US_SAMPLES) <= BLOCK_DISTANCE) {
					//Sound.beep();
					Lab5.lcd.setLine1("Object detected");
					if(isStyrofoamBlock()) {
						Lab5.lcd.setLine2("Block");
						Sound.beep();
					} else {
						float[] color = colorSensor.getColor();
						Lab5.lcd.setLine2("(" + color[0] + "," + color[1] + "," + color[2]+ ")");
						Sound.twoBeeps();
					}
				}
				try {
					Thread.sleep(500);
				} catch(Exception e) { }
			}
		}
	}
	
	private boolean isStyrofoamBlock() {
		return (colorSensor.getColor()[0] < colorSensor.getColor()[1]);
		//return (colorDistance(colorSensor.getColor(),STYROFOAM_COLOR) < 1.0);
	}
	
	private boolean isObjectDetected() {
		float currentDistance = usSensor.getSampleAverage(US_SAMPLES);
		boolean isObject = (lastDistanceDetected - currentDistance > DISTANCE_THRESHOLD);
		lastDistanceDetected = currentDistance;
		return isObject;
	}
	
	public static float colorDistance(float[] a, float[] b){
		return (float)Math.sqrt((a[0]-b[0])*(a[0]-b[0]) + (a[1]-b[1])*(a[1]-b[1]) + (a[2]-b[2])*(a[2]-b[2]));
	}
}
