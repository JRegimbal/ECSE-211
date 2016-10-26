package utilities;

import chassis.ColorSensor;
import chassis.Lab5;
import chassis.USSensor;
import lejos.hardware.Sound;
import utilities.Odometer.LINEDIR;

public class Search extends Thread {
	private static final int US_SAMPLES = 6;
	private Odometer odo;
	private USSensor usSensor;
	private ColorSensor colorSensor;
	private final float DISTANCE_THRESHOLD = 60; //cm
	private final double fieldToSearch = Math.PI/2;
	private final float[][] corners = new float[][] {{0,0},{0,60.96f},{60.96f,60.96f},{60.96f,0.0f}};
	private int corner;
	private int dir;
	private final static float BLOCK_DISTANCE = 4.0f; //distance to detect block type in cm
	public static double[] blockLocation;
	public static double[] obstacleLocation;
	private final int APPROACH_SPIN_SPEED = 90;
	private Navigation nav;
	private boolean sweepMethod = true;
	private static double SHIFT_DISTANCE = 3.00;
		
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
		
		boolean found = false;
		
		if(Lab5.demo == Lab5.DemoState.k_Part2 && !sweepMethod) {
			int dir = 1;
			isStyrofoamBlock(); //Initialize rgb mode
			
			while(!found) {
				odo.setMotorSpeed(APPROACH_SPIN_SPEED);
				odo.forwardMotors();
				while(usSensor.getMedianSample(US_SAMPLES) > BLOCK_DISTANCE && (odo.getX() < 58 && odo.getY() < 58));
				odo.stopMotors();
				
				if(odo.getX() >= 55 || odo.getY() >= 55) { //If approaching boundary...
					odo.moveCM(LINEDIR.Backward, 13, true); //Back up
					nav.turnBy(Math.pow(-1, dir) * Math.PI/2); //Turn in alternating directions compared to previous boundary condition
					dir++; //Increment to alternate spin direction for next time
					Sound.beepSequence();
				}
				else if(usSensor.getMedianSample(US_SAMPLES) <= BLOCK_DISTANCE && isStyrofoamBlock()) { //If found styrofoam block...
					//Capture
					found = true;
					Sound.beep();
					odo.moveCM(LINEDIR.Backward, 13, true); //Back up to avoid spinning collisions.
					Lab5.state = Lab5.RobotState.k_Capture; //Switch to capture mode.
				}
				else {
					Sound.twoBeeps(); //No block found.
					odo.moveCM(LINEDIR.Backward, 13, true); //Back up and rotate.
					nav.turnBy(Math.PI/2);
				}
			}
		}

	/*
	 * Sweep	
	 */
		
		else if(Lab5.demo == Lab5.DemoState.k_Part2 && sweepMethod) { //Temporary, in case we want to switch back to this method
			//PART 2
			//Comb through track, check for detection
			//sweep track for obstacles from position (0,0) starting at 0-radians
			boolean blockFound=false;
			
			isStyrofoamBlock(); //Initialize rgb mode
			
			while(!blockFound) {
				boolean objectFound = false;
				odo.setMotorSpeeds(Odometer.ROTATE_SPEED, Odometer.ROTATE_SPEED);
				
				//If last travelTo was interrupted by an obstacle, go back to previous corner and switch directions
				if(Navigation.PathBlocked) {
					odo.setMotorSpeed(60);
					odo.forwardMotors();
					for(double distance = usSensor.getFilteredDataBasic(); distance > BLOCK_DISTANCE; distance = usSensor.getFilteredDataBasic()); //Approach block slowly
					odo.stopMotors();
					if(isStyrofoamBlock()) {
						//If styrofoam...
						odo.moveCM(LINEDIR.Backward, 13, true); //Back up
						blockFound = true;
						break; //Break out of loop
					}
					//Obstacle is not styrofoam...
					odo.moveCM(LINEDIR.Backward, 13, true); //Back up
					dir = -dir; //Switch overall contour direction
					corner += dir;
					corner %= 4;
					nav.travelTo(corners[corner][0], corners[corner][1]);		
					int nextCorner = (corner + dir < 0) ? corner + dir + 4 : (corner + dir) % 4; //Make sure modulo isn't negative
					nav.turnTo(Math.atan2(corners[nextCorner][1], corners[nextCorner][0]), true); //Turn to next corner
					corner = nextCorner;
				}
								
				double targetAngle = odo.getTheta() - dir*fieldToSearch; //Get bound on angle to search through
				if(targetAngle > 2*Math.PI) targetAngle -= 2*Math.PI; //Wrap around
				else if (targetAngle < 0) targetAngle += 2*Math.PI;
				odo.setMotorSpeed(70);
				odo.spin(dir == 1 ? Odometer.TURNDIR.CW : Odometer.TURNDIR.CCW); //Start spinning and scanning
				try{
					Thread.sleep(500);
				} catch(Exception e) {} //we're living dangerously
				while(!(objectFound = isObjectDetected()) && Math.abs(Navigation.minimalAngle(targetAngle, odo.getTheta())) > Math.PI/60);	//check if there is an object at current heading or if area has been scanned
				if(objectFound) {	//go to object, check if it is a styrofoam block
//					Sound.beep();
					odo.stopMotors();
					
					double distance = usSensor.getMedianSample(US_SAMPLES); //Get distance to detected object
					double [] initialPosition = new double[3];
					boolean somethingFound = true;
					
					odo.getPosition(initialPosition);
					
					double headingShift = Math.atan(SHIFT_DISTANCE/(distance + 5)); //Point toward middle of object
					distance = usSensor.getMedianSample(US_SAMPLES); //Get updated distance to object
					nav.turnBy(dir*headingShift);
					double heading = odo.getTheta();
					
					odo.setMotorSpeed(70);
					odo.forwardMotors();
					int notDir = dir; //Direction of object detection refining
					while(usSensor.getMedianSample(US_SAMPLES) > 6 && somethingFound) { //While not close enough to the object
						while(usSensor.getMedianSample(US_SAMPLES) > distance) { //While detected distance to object is greater than originally, rotate
							if(Math.abs(odo.getTheta() - heading) > Math.PI/4) {
								//If the rotation overshot the object, move forward a little and switch scan direction
								odo.moveCM(Odometer.LINEDIR.Forward, 0.5, true);
								notDir = -notDir;
							}
							odo.setMotorSpeed(APPROACH_SPIN_SPEED);
							if(notDir == 1) odo.spin(Odometer.TURNDIR.CW);
							else odo.spin(Odometer.TURNDIR.CCW);
						}
						if(odo.euclideanDistance(initialPosition, new double[] {odo.getX(), odo.getY(), odo.getTheta()}) > distance)
							somethingFound = false;
						odo.forwardMotors();
					}
					odo.stopMotors();
					if(somethingFound) {	
						Sound.beep();
						odo.stopMotors();
						odo.setMotorSpeeds(60, 60);
						odo.forwardMotors();
						while(usSensor.getMedianSample(US_SAMPLES) > BLOCK_DISTANCE); //wait until close enough to determine if it's a styrofoam block
						odo.setMotorSpeeds(0, 0);
						odo.forwardMotors();
						if(isStyrofoamBlock()) {	//begin capture
							blockFound = true;
							Sound.beep();
						} else {
							Sound.twoBeeps();
						}
					}
					odo.moveCM(LINEDIR.Backward, 13, true); //Move backward, to avoid spinning into obstacle
					if(!blockFound) { //If the obstacle wasn't a styrofoam block, go to next corner
						nav.travelTo(corners[corner][0], corners[corner][1]);
						corner += dir;
						corner %= 4;
						Sound.twoBeeps();
						int nextCorner = (corner < 0) ? corner + 4 : (corner) % 4; //Make sure modulo isn't negative
						nav.travelTo(corners[nextCorner][0], corners[nextCorner][1]);
						corner = nextCorner;
					}
				} else {	//go to next corner
					odo.stopMotors();
					Sound.twoBeeps();
					Sound.twoBeeps();
					corner += dir;
					corner %= 4;
					int nextCorner = (corner < 0) ? corner + 4 : (corner) % 4; //Make sure modulo isn't negative
					nav.travelTo(corners[nextCorner][0], corners[nextCorner][1]);
					corner = nextCorner;
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
		boolean isObject = (/*lastDistanceDetected - currentDistance > DISTANCE_THRESHOLD) && (*/ currentDistance <= DISTANCE_THRESHOLD);
		return isObject;
	}
	
	public static float colorDistance(float[] a, float[] b){
		return (float)Math.sqrt((a[0]-b[0])*(a[0]-b[0]) + (a[1]-b[1])*(a[1]-b[1]) + (a[2]-b[2])*(a[2]-b[2]));
	}
}
