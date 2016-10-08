package navigator;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class BlindPathDriver implements Driver {

	public static final int FORWARD_SPEED	= 200;
	public static final int ROTATE_SPEED	= 150;
	
	//private static final double THRESHOLD		= 0.5;
	//private static final double THETA_THRESHOLD	= 0.04;
	
	private static final double THRESHOLD	= 2.0;
	private static final double THETA_THRESHOLD = 0.4;
	
	
	private static final int BANDWIDTH	= 5;
	private static final int BANDCENTER	= 15;
	
	private static final int BOOST	= 20;
	
	private static int dist;
	
	private static int errorCM;

	private double [] waypoints;
	private double lastTheta;
	private double lastX;
	private double lastY;
	private double width;
	
	int filter;
	private static final int MAX_FILTER = 0;
	
	private boolean navigating;
	private boolean avoid;
	private boolean danger;

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
	}

	@Override
	public void drive () {

		for(EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
			motor.stop();
			motor.setAcceleration(1000);
		}

		try {
			Thread.sleep(2000);
		}catch(Exception ex) {}
		//System.out.println("\n");

		for(int i = 0; i < waypoints.length; i+= 2) {
			double targetX = waypoints[i];
			double targetY = waypoints[i+1];
			/**
			double dx = targetX - lastX;
			double dy = targetY - lastY;
			double theta = (Math.atan2(dy,dx)) - lastTheta; //Should return minimal angle.
			if(!(Math.abs(theta) < 180.0)) {
				if(theta < 0.0) theta = theta + 360.0;
				else theta = theta - 360.0;
			}
			double distance = Math.sqrt(dx * dx + dy * dy);
			
			//System.out.println("Angle:    (" + theta * 180.0 / Math.PI);
			
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);
			leftMotor.rotate(Util.convertAngle(Lab3.WHEEL_RADIUS,width,theta * 180.0 / Math.PI), true);
			rightMotor.rotate(-Util.convertAngle(Lab3.WHEEL_RADIUS,width,theta * 180.0 / Math.PI), false);

			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.rotate(Util.convertDistance(Lab3.WHEEL_RADIUS, distance),true);
			rightMotor.rotate(Util.convertDistance(Lab3.WHEEL_RADIUS,distance),false);
			lastX = targetX;
			lastY = targetY;
			lastTheta = Math.atan2(dy,dx);
			*/

			travelTo(targetX,targetY);
			/*
			leftMotor.stop();
			rightMotor.stop();
			break;
			*/
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
		while(euclidDistance(x - odometer.getX(),y - odometer.getY()) > THRESHOLD) {
		//while()
			double ox,oy,ot;
			ox = odometer.getX();
			oy = odometer.getY();
			ot = odometer.getTheta();
			
			/*
			if(!navigating) {
				leftMotor.stop();
				rightMotor.stop();
				break;
			}
			*/
			//System.out.println(odometer.getX());
			boolean passed = false;
			if(danger) {
				passed = true;
				int counter = 0;
				leftMotor.setSpeed(0);
				rightMotor.setSpeed(0);
				leftMotor.rotate(0,true);
				rightMotor.rotate(0,false);
				leftMotor.setSpeed(ROTATE_SPEED);
				rightMotor.setSpeed(ROTATE_SPEED);
				leftMotor.rotate(Util.convertAngle(Lab3.WHEEL_RADIUS, width, -90),true);
				rightMotor.rotate(-Util.convertAngle(Lab3.WHEEL_RADIUS, width, -90),false);
				Lab3.sensorMotor.rotate(-90);
				while(counter < 3) {
					while(!passed) {
						leftMotor.setSpeed(FORWARD_SPEED);
						rightMotor.setSpeed(FORWARD_SPEED);
						leftMotor.forward();
						rightMotor.forward();
						if(dist < BANDCENTER) passed = true;
					}
					while(dist <= BANDCENTER + 3) {
						System.out.println("Passed");
						leftMotor.setSpeed(FORWARD_SPEED);
						rightMotor.setSpeed(FORWARD_SPEED);
						leftMotor.forward();
						rightMotor.forward();
					}
					//Lab3.sensorMotor.rotate(90);
					leftMotor.rotate(Util.convertDistance(Lab3.WHEEL_RADIUS, (int)width),true);
					rightMotor.rotate(Util.convertDistance(Lab3.WHEEL_RADIUS, (int)width),false);
					leftMotor.setSpeed(ROTATE_SPEED);
					rightMotor.setSpeed(ROTATE_SPEED);
					
					leftMotor.rotate(Util.convertAngle(Lab3.WHEEL_RADIUS,width,90),true);
					rightMotor.rotate(-Util.convertAngle(Lab3.WHEEL_RADIUS, width, 90),false);
					/*
					leftMotor.setSpeed(FORWARD_SPEED);
					rightMotor.setSpeed(FORWARD_SPEED);
					leftMotor.rotate(Util.convertDistance(Lab3.WHEEL_RADIUS, BOOST),true);
					rightMotor.rotate(Util.convertDistance(Lab3.WHEEL_RADIUS, BOOST),false);
					*/
					counter++;
				}

				danger = false;
			}
			double dx = x - odometer.getX();
			double dy = y - odometer.getY();
			double theta = Math.atan2(dx,dy);
			turnTo(theta);
			//break;
			
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.forward();
			rightMotor.forward();
			
		}
		//System.out.println("Next Waypoint");
		navigating = false;
	}
	
	public void turnTo(double theta) {
		//System.out.println("(" + theta + ", " + odometer.getTheta() + ")");
		//System.out.println("Theta: " + (int)(theta * 180 / Math.PI));
		//System.out.println("Odo: " + (int)(odometer.getTheta() * 180 / Math.PI));
		//System.out.println("=========");
		Sound.beep();
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
		if(distance < BANDCENTER) {
			filter++;
			if(filter >= MAX_FILTER) {
				System.out.println("DANGER");
				danger = true;
				/*
				leftMotor.stop();
				rightMotor.stop();
				leftMotor.setSpeed(ROTATE_SPEED);
				rightMotor.setSpeed(ROTATE_SPEED);
				leftMotor.rotate(Util.convertAngle(Lab3.WHEEL_RADIUS, width, 90),true);
				rightMotor.rotate(-Util.convertAngle(Lab3.WHEEL_RADIUS, width, 90),false);
				Lab3.sensorMotor.rotate(-90);
				filter = 0;
				System.exit(0);
				*/
			}
		}
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
