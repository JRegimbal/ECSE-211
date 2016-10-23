package utilities;

import chassis.Lab5;
import chassis.USSensor;
import lejos.hardware.Sound;
import utilities.Odometer.TURNDIR;

public class USLocalizer extends Thread {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	private static final float NO_WALL = 45.0f;	//Minimum distance for which the US sensor reading should be interpreted as no wall detected
	private static final double THETA_THRESHOLD = Math.PI / 6.0; //Minimum angle between two walls
	public static int ROTATION_SPEED = 60;
	
	private static final double GRID_SIZE = 30.48;

	private final float angleCorrection = 7.42f; //angle correction from data - for falling edge - in degrees
	
	private Odometer odo;
	private USSensor usSensor;
	private LocalizationType locType;
	private double lastTheta;
	int step; //TODO not sure we need this
	private double minimumDistance;
	
	public USLocalizer(Odometer odo,  USSensor usSensor, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.locType = locType;
		step = 0;
		minimumDistance = NO_WALL;
	}
	
	public void doLocalization() {
		Lab5.state = Lab5.RobotState.k_Localization;
		this.start();
	}
	
	private void localize() {
		float minDistance = usSensor.getFilteredDataBasic();
		float distance;
		int count = 0;
		double theta = (float) Math.PI;
		odo.setMotorSpeed(ROTATION_SPEED);
		odo.spin(TURNDIR.CCW);
		while(odo.getTheta() < (11/6)*Math.PI) {
			Sound.beep();
			if((distance = usSensor.getFilteredDataBasic()) < minDistance) {
				minDistance = distance;
				theta = odo.getTheta();
				count++;
			}
		}
		odo.setMotorSpeed(0);
		odo.forwardMotors();
		odo.setX(-GRID_SIZE + minDistance);
		odo.setY(-GRID_SIZE + minDistance);
		Navigator.turnTo(theta);
		if(count == 1) odo.setTheta(-Math.PI/2);
		else odo.setTheta(-Math.PI);
		Navigator.travelTo(0, 0);
		Navigator.turnTo(0);
	}
	
	@Override
	public void run() {
		double [] pos = new double [3];
		double angleA, angleB;
		//localize();
		//Lab5.state = Lab5.RobotState.k_Search;
		if (locType == LocalizationType.FALLING_EDGE) {
			double distance;
			//if(locType == LocalizationType.FALLING_EDGE) return;
			// rotate the robot until it sees no wall
			try {
				Thread.sleep(10);
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

			minimumDistance = NO_WALL;
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
			if(angleA - angleB < 0) angle = (Math.PI/2) - 0.5 * (angleA + angleB); //Calculate heading corretion as seen in tutorial
			else angle = (3*Math.PI/2) - 0.5 * (angleA + angleB);
			
			angle += angleCorrection * Math.PI / 180.0; //Correct heading
			
			//odo.moveCM(Odometer.LINEDIR.Forward, minimumDistance * Math.sin(Math.PI/4), true);
			//Navigator.turnBy(Math.PI/4);
			try {
				Thread.sleep(3000);
			} catch (Exception e) {}
			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {minimumDistance - 30.48, minimumDistance - 30.48, angle + odo.getTheta()}, new boolean [] {true, true, true}); //Update odometer values
			Navigator.travelTo(0, 0);
			Navigator.turnTo(0);
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
			
			double distance;
			
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
				if((distance = usSensor.getFilteredDataBasic()) < minimumDistance) minimumDistance = distance;
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
				if((distance = usSensor.getFilteredDataBasic()) < minimumDistance) minimumDistance = distance;
				step++;
			}
			step = 0;
			angleB = (odo.getTheta() < 0.0) ? odo.getTheta() + 2*Math.PI : odo.getTheta(); //Remove negative angles
			odo.getMotors()[0].stop();
			odo.getMotors()[1].stop();
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			double angle;
			if(angleA - angleB > 0) angle = (Math.PI/4) - 0.5 * (angleA + angleB); //Calculate heading corrections as seen in tutorial
			else angle = (5*Math.PI/4) - 0.5 * (angleA + angleB);
			
			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {minimumDistance - 30.48, minimumDistance - 30.48, angle + odo.getTheta()}, new boolean [] {true, true, true}); //Update odometer values
			Navigation nav = new Navigation(odo);
			nav.travelTo(0,0);
			nav.turnTo(Math.PI/2, true);
			Sound.beepSequenceUp();
			//Navigator.turnTo(0);
		}
		try {
			Thread.sleep(2000);
		} catch (Exception e) {	}
		//slight pause to show localization is complete
		Lab5.state = Lab5.RobotState.k_Search;
	}
	
	private boolean seesWall() {
		float sample = usSensor.getSampleAverage(10);
		if(sample < minimumDistance) minimumDistance = sample;
		return (sample < NO_WALL);
	}
	

}
