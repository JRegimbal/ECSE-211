package utilities;

public class Navigator {
	private static Odometer odo;
	private static final double NAVIGATION_THRESHOLD = 0.5;
	
	public Navigator() {
	}
	
	public static void setOdometer(Odometer odo) {
		Navigator.odo = odo;
	}
	
	public static void travelTo(double x, double y) {
		double [] currentPosition = new double[] {0, 0, 0};
		odo.getPosition(currentPosition);
		if(odo.euclideanDistance(currentPosition, new double[] {x, y}) < NAVIGATION_THRESHOLD) {
			double dx = x-currentPosition[0];
			double dy = y-currentPosition[1];
			double newTheta = Math.atan2(dy, dx);
			turnTo(newTheta);
			
			odo.setMotorSpeeds(Odometer.NAVIGATE_SPEED, Odometer.NAVIGATE_SPEED);
			odo.forwardMotors();
		}
	}
	
	public static void turnTo(double theta) {
		double currentTheta = odo.getTheta();
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
