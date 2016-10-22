package utilities;

import lejos.hardware.Sound;

public class Navigator {
	private static Odometer odo;
	private static final double NAVIGATION_THRESHOLD = 0.5;
	
	public Navigator() {
	}
	
	public static void setOdometer(Odometer odo) {
		Navigator.odo = odo;
	}
	
	public static void moveTo(double x, double y) {
		double minAng = 0;
		turnBy(Math.atan2(y-odo.getY(), x-odo.getX()));
		while(Math.abs(x - odo.getX()) > 0.5 || Math.abs(odo.getY() - y) > 0.5) {
			minAng = (Math.atan2(y - odo.getY(), x - odo.getX())) * (180.0 / Math.PI);
			if(minAng < 0) minAng += 360.0;
			odo.setMotorSpeed(200);
			odo.forwardMotors();
		}
	}
	
	public static void spinTo(double theta) {
		double error = theta - odo.getTheta();
		
		if(Math.abs(error) > Math.PI) {
			if(error < 0) error += 2*Math.PI;
			else error -= 2 * Math.PI;
		}
		
		while(error > Math.PI/144) {
			if(error < -Math.PI) {
				odo.spin(Odometer.TURNDIR.CCW);
			}
			else if (error < 0.0) {
				odo.spin(Odometer.TURNDIR.CW);
			}
			else if (error > Math.PI) {
				odo.spin(Odometer.TURNDIR.CW);
			}
			else odo.spin(Odometer.TURNDIR.CCW);
		}
	}
	
	public static void travelTo(double x, double y) {
		double [] currentPosition = new double[] {0, 0, 0};
		odo.getPosition(currentPosition);
		while(odo.euclideanDistance(new double [] {odo.getX(),odo.getY()}, new double[] {x, y}) > NAVIGATION_THRESHOLD) {
			double dx = x-odo.getX();
			double dy = y-odo.getY();
			double newTheta = Math.atan2(dy, dx);
			turnTo(newTheta);
			Sound.beep();
			odo.setMotorSpeed(Odometer.NAVIGATE_SPEED);
			odo.forwardMotors();
			odo.getPosition(currentPosition);
			
			//while(odo.euclideanDistance(new double[] {odo.getX(), odo.getY()}, new double[] {x, y}) > NAVIGATION_THRESHOLD);
		}
		odo.setMotorSpeed(0);
		odo.forwardMotors();
	}
	
	public static void turnTo(double theta) {
		double currentTheta = odo.getTheta();
		if(Math.abs(theta - currentTheta) < Math.PI/288) return;
		double dTheta = (theta - currentTheta > Math.PI) ? theta - currentTheta - Math.PI*2 : theta - currentTheta;
		turnBy(dTheta);
	}
	
	public static void turnBy(double theta) {
		odo.setMotorSpeeds(Odometer.ROTATE_SPEED, Odometer.ROTATE_SPEED);
		odo.getMotors()[0].rotate(convertAngle(odo.wheelRadius,odo.trackLength,theta * 180.0 / Math.PI), true);
		odo.getMotors()[1].rotate(-convertAngle(odo.wheelRadius,odo.trackLength,theta * 180.0 / Math.PI), false);
	}
	
	private static int convertDistance(double radius, double distance) {
		return (int)(distance * 180.0 / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
