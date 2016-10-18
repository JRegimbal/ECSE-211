package localization;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	private static final float NO_WALL = 45.0f;	//Minimum distance for which the US sensor reading should be interpreted as no wall detected
	private static final double THETA_THRESHOLD = Math.PI / 6.0; //Minimum angle between two walls
	public static int ROTATION_SPEED = 60;

	private final float angleCorrection = 7.42f; //angle correction from data - for falling edge - in degrees
	
	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private double lastTheta;
	int step; //TODO not sure we need this
	
	private float lastDistance;
	
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		filter = 0;
		step = 0;
		lastDistance = 0;
	}
	
	public void doLocalization() {
		double [] pos = new double [3];
		double angleA, angleB;
		if (locType == LocalizationType.FALLING_EDGE) {
			// rotate the robot until it sees no wall
			try {
				Thread.sleep(1000);
			}catch (Exception ex) {}
			
			odo.getMotors()[0].setSpeed(ROTATION_SPEED);
			odo.getMotors()[1].setSpeed(ROTATION_SPEED);
			
			while(seesWall()) {
				odo.getMotors()[0].forward();
				odo.getMotors()[1].backward();
			}
			odo.getMotors()[0].setSpeed(0);
			odo.getMotors()[1].setSpeed(0);
			lastTheta = odo.getTheta();

			// keep rotating until the robot sees a wall, then latch the angle
			while(!seesWall() || Math.abs(odo.getTheta() - lastTheta) < THETA_THRESHOLD) {
				odo.getMotors()[0].setSpeed(ROTATION_SPEED);
				odo.getMotors()[1].setSpeed(ROTATION_SPEED);
				odo.getMotors()[0].forward();
				odo.getMotors()[1].backward();
				step++;
			}
			odo.getMotors()[0].setSpeed(0);
			odo.getMotors()[1].setSpeed(0);
			lastTheta = odo.getTheta();
			
			angleA = (odo.getTheta() < 0.0) ? odo.getTheta() + 2*Math.PI : odo.getTheta(); //Remove negative angles

			// switch direction and wait until it sees no wall
			while(seesWall() || Math.abs(odo.getTheta() - lastTheta) < THETA_THRESHOLD) {
				odo.getMotors()[0].setSpeed(ROTATION_SPEED);
				odo.getMotors()[1].setSpeed(ROTATION_SPEED);
				odo.getMotors()[0].backward();
				odo.getMotors()[1].forward();
				step++;
			}
			step = 0;

			// keep rotating until the robot sees a wall, then latch the angle 
			while(!seesWall() || step < 30) {
				odo.getMotors()[0].backward();
				odo.getMotors()[1].forward();
				step++;
			}
			step = 0;
			angleB = (odo.getTheta() < 0.0) ? odo.getTheta() + 2*Math.PI : odo.getTheta(); //Remove negative angles
			Sound.beep();
			Sound.beep();
			odo.setMotorSpeed(0);
			odo.forwardMotors();
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			double angle;
			if(angleA - angleB < 0) angle = (Math.PI/4) - 0.5 * (angleA + angleB); //Calculate heading corretion as seen in tutorial
			else angle = (5*Math.PI/4) - 0.5 * (angleA + angleB);
			
			angle += angleCorrection * Math.PI / 180.0; //Correct heading
			
			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, angle + odo.getTheta()}, new boolean [] {true, true, true}); //Update odometer values
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			//
			// FILLTHIS IN
			//
			odo.getMotors()[0].setSpeed(ROTATION_SPEED);
			odo.getMotors()[1].setSpeed(ROTATION_SPEED);
			
			while(!seesWall()) {
				odo.getMotors()[0].forward();
				odo.getMotors()[1].backward();
			}
			odo.getMotors()[0].setSpeed(0);
			odo.getMotors()[1].setSpeed(0);
			lastTheta = odo.getTheta();

			// keep rotating until the robot sees a wall, then latch the angle
			while(seesWall() || Math.abs(odo.getTheta() - lastTheta) < THETA_THRESHOLD) {
				odo.getMotors()[0].setSpeed(ROTATION_SPEED);
				odo.getMotors()[1].setSpeed(ROTATION_SPEED);
				odo.getMotors()[0].forward();
				odo.getMotors()[1].backward();
				step++;
			}
			odo.getMotors()[0].setSpeed(0);
			odo.getMotors()[1].setSpeed(0);
			lastTheta = odo.getTheta();
			
			angleA = (odo.getTheta() < 0.0) ? odo.getTheta() + 2*Math.PI : odo.getTheta(); //Remove negative angles

			// switch direction and wait until it sees no wall
			while(!seesWall() || Math.abs(odo.getTheta() - lastTheta) < THETA_THRESHOLD) {
				odo.getMotors()[0].setSpeed(ROTATION_SPEED);
				odo.getMotors()[1].setSpeed(ROTATION_SPEED);
				odo.getMotors()[0].backward();
				odo.getMotors()[1].forward();
				step++;
			}
			step = 0;

			// keep rotating until the robot sees a wall, then latch the angle 
			while(seesWall() || step < 30) {
				odo.getMotors()[0].backward();
				odo.getMotors()[1].forward();
				step++;
			}
			step = 0;
			angleB = (odo.getTheta() < 0.0) ? odo.getTheta() + 2*Math.PI : odo.getTheta(); //Remove negative angles
			Sound.beep();
			odo.getMotors()[0].stop();
			odo.getMotors()[1].stop();
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			double angle;
			if(angleA - angleB > 0) angle = (Math.PI/4) - 0.5 * (angleA + angleB); //Calculate heading corrections as seen in tutorial
			else angle = (5*Math.PI/4) - 0.5 * (angleA + angleB);
			
			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, angle + odo.getTheta()}, new boolean [] {true, true, true}); //Update odometer values
		}
	}
	
	private boolean seesWall() {
		return (getFilteredDataBasic() < NO_WALL);
	}

	//TODO I don't know what this is
/*	private float detectUSMinimum() {
		//find closest distance between robot and wall
		float minimum, lastValue;
		odo.setMotorSpeeds(ROTATION_SPEED, ROTATION_SPEED);
		lastValue = 255.0f; //dummy value to start
		odo.getMotors()[0].forward();
		odo.getMotors()[1].backward();
		for(float current = getFilteredDataBasic(); lastValue > current;) { lastValue = current; current = getFilteredDataBasic(); } //in loop to avoid warnings in eclipse
		minimum = lastValue;
		odo.getMotors()[0].backward();	//rotate other way
		odo.getMotors()[1].forward();
		for(float current = getFilteredDataBasic(); lastValue > current;) { lastValue = current; current = getFilteredDataBasic(); }
		minimum = (minimum > lastValue ? minimum : lastValue);
		return minimum;
	}*/
	
	private float getFilteredDataBasic() {
		usSensor.fetchSample(usData, 0); //Store distance in usData
		return (usData[0] * 100.0f >= NO_WALL) ? NO_WALL : usData[0] * 100.0f; //Cap data at NO_WALL, scale data by 100.
	}

}
