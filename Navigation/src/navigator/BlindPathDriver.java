package navigator;

import lejos.hardware.EV3LargeRegulatedMotor;

public class BlindPathDriver implements Driver {

	public static final int FORWARD_SPEED	= 250;
	public static final int ROTATE_SPEED	= 150;

	private double [] waypoints;
	private double lastTheta;
	private double lastX;
	private double lastY;
	private double width;

	EV3LargeRegulatedMotor leftMotor;
	EV3LargeRegulatedMotor rightMotor;

	public BlindPathDriver(double [] waypoints,EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor, double width) {
		this.waypoints = waypoints;
		lastTheta = 0.0;
		lastX = lastY = 0.0;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.width = width;
	}

	@Override
	public void drive () {

		for(EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
			motor.stop();
			motor.setAcceleration(3000);
		}

		try {
			Thread.sleep(2000);
		}catch(Exception ex) {}

		for(int i = 0; i < waypoints.length/2; i+= 2) {
			double targetX = waypoints[i];
			double targetY = waypoints[i+1];
			double dx = targetX - lastX;
			double dy = targetY - lastY;
			double theta = Math.atan2(dx,dy); //Should return minimal angle.
			double distance = Math.sqrt(dx * dx + dy * dy);
			
			leftMotor.setSpeed(ROTATE_SPEED);
			rightMotor.setSpeed(ROTATE_SPEED);
			leftMotor.rotate(Util.convertAngle(Lab3.WHEEL_RADIUS,width,theta), true);
			rightMotor.rotate(-Util.convertAngle(Lab3.WHEEL_RADIUS,width,theta), false);

			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			leftMotor.rotate(Util.converDistance(Lab3.WHEEL_RADIUS, distance),true);
			rightMotor.rotate(Util.converDistance(Lab3.WHEEL_RADIUS,distance),false);
		}
	}
}
