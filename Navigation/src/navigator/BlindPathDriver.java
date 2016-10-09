package navigator;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class BlindPathDriver implements Driver {

	public static final int FORWARD_SPEED	= 200;
	public static final int ROTATE_SPEED	= 150;
	
	private static final int MOTOR_LOW		= 75;
	private static final int MOTOR_HIGH		= 250;
	
	private static final double THRESHOLD		= 0.5;
	private static final double THETA_THRESHOLD	= 0.04;
	
	//private static final double THRESHOLD	= 2.0;
	//private static final double THETA_THRESHOLD = 0.4;
	
	private static final boolean[] UPDATE_ALL = {true,true,true};
	
	private static final int BANDWIDTH	= 8;
	private static final int BANDCENTER	= 23;
	
	private static final int BOOST	= 20;
	
	private static int dist;
	
	private static int errorCM;

	private double [] waypoints;
	private double lastTheta;
	private double lastX;
	private double lastY;
	private double width;
	
	private static final int MOTOR_TURN = 60;
	
	private double dangerStart[] = new double[3];
	
	int filter;
	private static final int MAX_FILTER = 0;
	
	private boolean navigating;
	private boolean avoid;
	private boolean danger;
	private boolean hasSeenWall = false;
	
	int counter;

	EV3LargeRegulatedMotor leftMotor;
	EV3LargeRegulatedMotor rightMotor;
	Odometer odometer;

	public BlindPathDriver(double [] waypoints,EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor, Odometer odometer, double width,boolean avoid) {
		this.waypoints = waypoints;
		lastTheta = 0.0;
		lastX = lastY = 0.0;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.width = width;
		this.odometer = odometer;
		navigating = false;
		this.avoid = avoid;
		this.errorCM = 0;
		dist = 0;
		filter = 0;
		danger = false;
		counter = 0;
		dangerStart = new double[3];
	}

	@Override
	public void drive () {

		for(EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
			motor.stop();
			motor.setAcceleration(1000);
		}

		try {
			Thread.sleep(5000);
		}catch(Exception ex) {}
		//System.out.println("\n");

		for(int i = 0; i < waypoints.length; i+= 2) {
			double targetX = waypoints[i];
			double targetY = waypoints[i+1];
			
			travelTo(targetX,targetY);
		}
		leftMotor.stop();
		rightMotor.stop();
	}
	
	public void travelTo(double x, double y) {
		//System.out.println("");
		navigating = true;
		//leftMotor.stop();
		//rightMotor.stop();
		//System.out.println("Theta: " + odometer.getTheta());
		//System.out.println("Target: " + Math.atan2(x-odometer.getY(), y - odometer.getX()));
		while(euclidDistance(x - odometer.getX(),y - odometer.getY()) > THRESHOLD || danger) {
			boolean passed = false;
			if(danger) continue;
			
			double dx = x - odometer.getX();
			double dy = y - odometer.getY();
			double theta = Math.atan2(dx,dy);
			turnTo(theta);
			
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.forward();
			rightMotor.forward();
			
		}
		navigating = false;
	}
	
	public void turnTo(double theta) {
		if(Math.abs(theta - odometer.getTheta()) > THETA_THRESHOLD) {
			if(!(Math.abs(theta) < Math.PI)) {
				if(theta < 0.0) theta = theta + Math.PI;
				else theta = theta - Math.PI;
			} 
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);
			leftMotor.rotate(Util.convertAngle(Lab3.WHEEL_RADIUS,width,(theta - odometer.getTheta()) * 180.0 / Math.PI), true);
			rightMotor.rotate(-Util.convertAngle(Lab3.WHEEL_RADIUS,width,(theta - odometer.getTheta()) * 180.0 / Math.PI), false);
		}
	}
	
	public boolean isNavigating() {
		return navigating;
	}
	
	@Override
	public void processUSData(int distance) {
		dist = distance;
		if(!avoid) return;
		if(distance < BANDCENTER && !danger && !hasSeenWall) {
				danger = true;
				Lab3.sensorMotor.rotate(-MOTOR_TURN);
				leftMotor.setSpeed(ROTATE_SPEED);
				rightMotor.setSpeed(ROTATE_SPEED);
				leftMotor.rotate(Util.convertAngle(Lab3.WHEEL_RADIUS, width, -MOTOR_TURN),true);
				rightMotor.rotate(-Util.convertAngle(Lab3.WHEEL_RADIUS, width, -MOTOR_TURN),false);
				odometer.getPosition(dangerStart, UPDATE_ALL);

		}
		if(danger && !hasSeenWall) {
			double [] position = new double[3];
			odometer.getPosition(position, UPDATE_ALL);
			if(!hasDone180(dangerStart,position)) {
				errorCM = distance - BANDCENTER;
				if(Math.abs(errorCM) < BANDWIDTH) {
					leftMotor.setSpeed(MOTOR_LOW);
					rightMotor.setSpeed(MOTOR_LOW);
					leftMotor.forward();
					rightMotor.forward();
				}
				else if(errorCM < 0) {
					leftMotor.setSpeed(MOTOR_LOW);
					rightMotor.setSpeed(MOTOR_HIGH);
					leftMotor.forward();
					rightMotor.forward();
				}
				else {
					leftMotor.setSpeed(MOTOR_HIGH);
					rightMotor.setSpeed(MOTOR_LOW);
					leftMotor.forward();
					rightMotor.forward();
				}
			}
			else {
				danger = false;
				Lab3.sensorMotor.rotate(MOTOR_TURN);
				//leftMotor.setSpeed(FORWARD_SPEED);
				//rightMotor.setSpeed(FORWARD_SPEED);
				//leftMotor.rotate(Util.convertDistance(Lab3.WHEEL_RADIUS, BOOST));
				//rightMotor.rotate(Util.convertDistance(Lab3.WHEEL_RADIUS, BOOST));
			}
		}
	} 
	
	/*public boolean isOnLine(double[] lineDef,double[] pos) {
		final double LINE_THRESHOLD = 2;
		//Returns true if pos is on line defined by lineDef and pos is not on the point lineDef
		if(euclidDistance(lineDef[0]-pos[0], lineDef[1]-pos[1]) < LINE_THRESHOLD) {
			return false;
		}
		double angle_error = Math.abs(Math.atan2(pos[1] - lineDef[1], pos[0] - lineDef[0]) - lineDef[2] + Math.PI/2);
		//System.out.println(angle_error);
		if(angle_error < Math.PI/10 )
		{
			Sound.beep();
			System.out.println("\n");
			System.out.println("Target Heading: " + (int)(lineDef[2] * 180.0/Math.PI));
			System.out.println("Atan: " + (int)(Math.atan2(pos[1]-lineDef[1], pos[0]-lineDef[0])*180.0/Math.PI));
			return true;
		}
		return false;
	}*/
	
	public boolean hasDone180(double[] initialPos, double[] pos)
	{
		if(Math.abs(Math.abs(initialPos[2] - pos[2]) - Math.PI) < Math.PI/18) //threshold for this
		{
			hasSeenWall = true;
			return true;	
		}
		return false;
	}
	
	@Override
	public int readUSDistance() {
		// TODO Auto-generated method stub
		return dist;
	}
	
	private double euclidDistance(double x, double y) {
		return Math.sqrt(x*x + y*y);
	}

}
