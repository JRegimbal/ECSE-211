package navigator;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class SmartDriver implements UltrasonicController, Driver{
	
	private static final int FORWARD_SPEED	= 200;
	private static final int ROTATE_SPEED	= 150;
	
	private static final int BAND_CENTER	= 10;
	private static final int BAND_WIDTH		= 4;
	
	private double errorCM;
	private double dist;
	
	private double [] waypoints;
	private double lastTheta;
	private double lastX;
	private double lastY;
	private double width;
	
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private Odometer odometer;
	
	private Object mutex;
	
	public SmartDriver(double [] waypoints, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer, double width) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
		this.width = width;
		this.waypoints = waypoints;
		this.lastTheta = this.lastX = this.lastY = 0.0;
		mutex = new Object();
	}
	
	@Override
	public void drive() {
		//synchronized(mutex) {
			for(EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
				motor.stop();
				motor.setAcceleration(2000);
			}
	
			try {
				Thread.sleep(2000);
			}catch(Exception ex) {}
			//System.out.println("\n");
	
			for(int i = 0; i < waypoints.length; i+= 2) {
				double targetX = waypoints[i];
				double targetY = waypoints[i+1];
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
			}
		//}
	}

	@Override
	public void processUSData(int distance) {
		//synchronized(mutex) {
			errorCM = distance - BAND_CENTER;
			dist = distance;
			if(errorCM < BAND_WIDTH) {
				leftMotor.stop();
				rightMotor.stop();
			}
		//}
	}

	@Override
	public int readUSDistance() {
		// TODO Auto-generated method stub
		return (int) this.dist;
	}

}
