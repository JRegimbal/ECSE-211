package navigator;

public class Util {
	public static int convertDistance(double radius, double distance) {
		return (int)(distance * 180.0 / (Math.PI * radius));
	}

	public static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}
