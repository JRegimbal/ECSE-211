package utilities;

import chassis.Lab5;
import chassis.USSensor;
import lejos.hardware.Sound;
import utilities.Odometer.LINEDIR;

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
	private final float[][] corners = new float[][] {{0,0},{0,60.96f},{60.96f,60.96f},{60.96f,0.0f}};
	private int corner;
	private int dir;
	private final static float BLOCK_DISTANCE = 5.0f; //distance to detect block type in cm
	public static double[] blockLocation;
	public static double[] obstacleLocation;
	private final int APPROACH_SPIN_SPEED = 75;
	private Navigation nav;
		
	public static final float[] STYROFOAM_COLOR = new float[] {0.0f,1.0f,1.0f};
	
	public Search(Odometer odo, ColorSensor colorSensor, USSensor usSensor) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.usSensor = usSensor;
		this.corner = 0;
		this.dir = 1;
		nav = new Navigation(odo);
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
			boolean blockFound=false;
			
			while(!blockFound) {
			//while(!blockFound || !obstacleFound) {	
				lastDistanceDetected = FIELD_BOUNDS;
				boolean objectFound = false;
				odo.setMotorSpeeds(Odometer.ROTATE_SPEED, Odometer.ROTATE_SPEED);
				//odo.spin(Odometer.TURNDIR.CCW);
				
				//If last travelTo was interrupted by an obstacle, go back to previous corner and switch directions
				if(Navigation.PathBlocked) {
					dir = -dir;
					corner += dir;
					corner %= 4;
					nav.travelTo(corners[corner][0], corners[corner][1]);		
					int nextCorner = (corner + dir < 0) ? corner + dir + 4 : (corner + dir) % 4;
					nav.turnTo(Math.atan2(corners[nextCorner][1], corners[nextCorner][0]), true);
				}
								
				double targetAngle = odo.getTheta() - dir*fieldToSearch;
				//if(targetAngle < 0.0) targetAngle += 360.0;
				if(targetAngle > 2*Math.PI) targetAngle -= 2*Math.PI;
				else if (targetAngle < 0) targetAngle += 2*Math.PI;
				odo.setMotorSpeed(70);
				odo.spin(dir == 1 ? Odometer.TURNDIR.CW : Odometer.TURNDIR.CCW);
				try{
					Thread.sleep(500);
				} catch(Exception e) {} //we're living dangerously
				while(!(objectFound = isObjectDetected()) && Math.abs(Navigation.minimalAngle(targetAngle, odo.getTheta())) > Math.PI/60);	//check if there is an object at current heading or if area has been scanned
				double angleA = 0, angleB = 0;			
				if(objectFound) {	//go to object, check if it is a styrofoam block
					Sound.beep();
					double tempDistance = usSensor.getMedianSample(US_SAMPLES);
					angleA = odo.getTheta();
					while(Math.abs(usSensor.getMedianSample(US_SAMPLES) - tempDistance) < 10);
					angleB = odo.getTheta();
					
					odo.stopMotors();
					
					double middleAngle = (angleA+angleB)/2;
					nav.turnTo(middleAngle, true);
					
					double distance = usSensor.getMedianSample(US_SAMPLES); //Get distance to detected object
					double heading = odo.getTheta();
					odo.setMotorSpeed(70);
					odo.forwardMotors();
					int notDir = dir;	//because fuck descriptive naming
					while(usSensor.getMedianSample(US_SAMPLES) > 6) {
						while(usSensor.getMedianSample(US_SAMPLES) > distance) {
							if(Math.abs(odo.getTheta() - heading) > Math.PI/4) {
								odo.moveCM(Odometer.LINEDIR.Forward, 0.5, true);
								notDir = -notDir;
							}
							odo.setMotorSpeed(APPROACH_SPIN_SPEED);
							if(notDir == 1) odo.spin(Odometer.TURNDIR.CW);
							else odo.spin(Odometer.TURNDIR.CCW);
						}
						odo.forwardMotors();
					}
					odo.stopMotors();
					Sound.beep();
					odo.stopMotors();
					odo.setMotorSpeeds(60, 60);
					odo.forwardMotors();
					while(usSensor.getMedianSample(US_SAMPLES) > BLOCK_DISTANCE); //wait until close enough to determine if it's a styrofoam block
					odo.setMotorSpeeds(0, 0);
					odo.forwardMotors();
					if(isStyrofoamBlock()) {	//begin capture
						blockFound = true;
						//blockLocation = new double[] {odo.getX(), odo.getY()};
						Sound.beepSequenceUp();
						//Lab5.state = Lab5.RobotState.k_Capture;
						//continue;
					} else {
						//obstacleLocation = new double[] {odo.getX(), odo.getY()};
						//Sound.beepSequence();
					}
					odo.moveCM(LINEDIR.Backward, 13, true); //Move backward, to avoid spinning into obstacle
					if(!blockFound) { //If the obstacle wasn't a styrofoam block, go to next corner
						nav.travelTo(corners[corner][0], corners[corner][1]);
						corner += dir;
						corner %= 4;
						Sound.twoBeeps();
						nav.travelTo(corners[corner][0], corners[corner][1]);
						//int nextCorner = (corner + dir < 0) ? corner + dir + 4 : (corner + dir) % 4;
						nav.turnBy(dir*Math.PI/2);
					}
				} else {	//go to next corner
					odo.stopMotors();
					Sound.twoBeeps();
					Sound.twoBeeps();
					corner += dir;
					corner %= 4;
					nav.travelTo(corners[corner][0], corners[corner][1]);
					//int nextCorner = (corner + dir < 0) ? corner + dir + 4 : (corner + dir) % 4;
					//nav.turnTo(Math.atan2(corners[nextCorner][1], corners[nextCorner][0]), true);
					nav.turnBy(dir*Math.PI/2);
				}
			}
			
			//Styrofoam block found - begin capture
			Sound.twoBeeps();
			Lab5.state = Lab5.RobotState.k_Capture; //If blockFound, switch to Capture state
		} else { //PART 1
			while(true) {
				chassis.LCDInfo.getLCD().clear();
				if(usSensor.getMedianSample(US_SAMPLES) <= BLOCK_DISTANCE) {
					//Sound.beep();
					Lab5.lcd.setLine1("Object detected");
					if(isStyrofoamBlock()) {
						Lab5.lcd.setLine2("Block");
						Sound.beep();
					} else {
						Lab5.lcd.setLine2("Not a block");
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
		float currentDistance = usSensor.getMedianSample(US_SAMPLES);
		boolean isObject = (lastDistanceDetected - currentDistance > DISTANCE_THRESHOLD) && (currentDistance <= 45);
		lastDistanceDetected = currentDistance;
		return isObject;
	}
	
	public static float colorDistance(float[] a, float[] b){
		return (float)Math.sqrt((a[0]-b[0])*(a[0]-b[0]) + (a[1]-b[1])*(a[1]-b[1]) + (a[2]-b[2])*(a[2]-b[2]));
	}
}
