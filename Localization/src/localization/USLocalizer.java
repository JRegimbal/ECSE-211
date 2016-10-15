package localization;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	private static final float NO_WALL = 50.0f;
	private static final double THETA_THRESHOLD = Math.PI / 6.0;
	public static int ROTATION_SPEED = 60;

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	private int filter;
	private double lastTheta;
	int step;
	
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
		//while(filter < 10) getUnfilteredData();
		
		if (locType == LocalizationType.FALLING_EDGE) {
			// rotate the robot until it sees no wall
			try {
				//Thread.sleep(1000);
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
			System.out.println("*1 ("+getFilteredData()+")");

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
			
			angleA = (odo.getTheta() < 0.0) ? odo.getTheta() + 2*Math.PI : odo.getTheta();
			System.out.println("*2. angleA = " + (int)(angleA * 180.0 / Math.PI) + "("+getFilteredData()+")");
			// switch direction and wait until it sees no wall
			
			while(seesWall() || Math.abs(odo.getTheta() - lastTheta) < THETA_THRESHOLD) {
				odo.getMotors()[0].setSpeed(ROTATION_SPEED);
				odo.getMotors()[1].setSpeed(ROTATION_SPEED);
				odo.getMotors()[0].backward();
				odo.getMotors()[1].forward();
				step++;
			}
			//odo.getMotors()[0].stop();
			//odo.getMotors()[1].stop();
			step = 0;
			System.out.println("*3"+" ("+getFilteredData()+")");
			//Sound.beep();
			// keep rotating until the robot sees a wall, then latch the angle 
			while(!seesWall() || step < 30) {
				odo.getMotors()[0].backward();
				odo.getMotors()[1].forward();
				step++;
			}
			step = 0;
			angleB = (odo.getTheta() < 0.0) ? odo.getTheta() + 2*Math.PI : odo.getTheta();
			System.out.println("*4. angleB = " + (int)(angleB*180.0/Math.PI) +"("+getFilteredData()+")");
			Sound.beep();
			Sound.beep();
			odo.getMotors()[0].stop();
			odo.getMotors()[1].stop();
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			double angle;
			if(angleA - angleB < 0) angle = (Math.PI/4) - 0.5 * (angleA + angleB);
			else angle = (5*Math.PI/4) - 0.5 * (angleA + angleB);
			
			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, angle + odo.getTheta()}, new boolean [] {true, true, true});
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
			System.out.println("*1 ("+getFilteredData()+")");

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
			
			angleA = (odo.getTheta() < 0.0) ? odo.getTheta() + 2*Math.PI : odo.getTheta();
			System.out.println("*2. angleA = " + (int)(angleA * 180.0 / Math.PI) + "("+getFilteredData()+")");
			// switch direction and wait until it sees no wall
			
			while(!seesWall() || Math.abs(odo.getTheta() - lastTheta) < THETA_THRESHOLD) {
				odo.getMotors()[0].setSpeed(ROTATION_SPEED);
				odo.getMotors()[1].setSpeed(ROTATION_SPEED);
				odo.getMotors()[0].backward();
				odo.getMotors()[1].forward();
				step++;
			}
			//odo.getMotors()[0].stop();
			//odo.getMotors()[1].stop();
			step = 0;
			System.out.println("*3"+" ("+getFilteredData()+")");
			//Sound.beep();
			// keep rotating until the robot sees a wall, then latch the angle 
			while(seesWall() || step < 30) {
				odo.getMotors()[0].backward();
				odo.getMotors()[1].forward();
				step++;
			}
			step = 0;
			angleB = (odo.getTheta() < 0.0) ? odo.getTheta() + 2*Math.PI : odo.getTheta();
			System.out.println("*4. angleB = " + (int)(angleB*180.0/Math.PI) +"("+getFilteredData()+")");
			Sound.beep();
			Sound.beep();
			odo.getMotors()[0].stop();
			odo.getMotors()[1].stop();
			
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			double angle;
			if(angleA - angleB > 0) angle = (Math.PI/4) - 0.5 * (angleA + angleB);
			else angle = (5*Math.PI/4) - 0.5 * (angleA + angleB);
			
			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, angle + odo.getTheta()}, new boolean [] {true, true, true});
		}
	}
	
	private boolean seesWall() {
		return (getFilteredDataBasic() < NO_WALL);
	}
	
	private float getUnfilteredData() {
		usSensor.fetchSample(usData, 0);
		odo.setData(usData[0] * 100.0f);
		return usData[0] * 100.0f;
	}
	
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0] * 100.0f;
		if(distance >= NO_WALL) {
			filter++;
			if(filter >= 0) {
				distance = NO_WALL;
				odo.setData(distance);
				lastDistance = distance;
				filter = 0;
				return distance;
			}
			odo.setData(666);
			return lastDistance;
		}
		/*
		else {
			filter++;
			if(filter > 10) {
				odo.setData(distance);
				return distance;
			}
			odo.setData(distance);
			return NO_WALL;
		}
		*/
		lastDistance = distance;
		odo.setData(distance);
		return distance;
	}
	
	private float getFilteredDataBasic() {
		usSensor.fetchSample(usData, 0);
		return (usData[0] * 100.0f >= NO_WALL) ? NO_WALL : usData[0] * 100.0f;
	}

}
