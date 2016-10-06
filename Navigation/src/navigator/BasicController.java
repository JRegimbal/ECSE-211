package navigator;

import lejos.hardware.motor.*;

public class BasicController implements UltrasonicController {
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	private int distance;
	private final int bandCenter, bandWidth;

	public BasicController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int bandCenter, int bandWidth) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.distance = 0;
		this.bandCenter = bandCenter;
		this.bandWidth = bandWidth;
	}

	@Override
	public void processUSData(int distance) {
		this.distance = distance;
		int errorCM = this.distance - this.bandCenter;
		if(Math.abs(errorCM) <= this.bandWidth) {
			//TODO Go straight
		}
		else if (errorCM < 0) {
			//TODO Turn
		}
		else {
			//TODO Turn
		}
	}

	@Override
	public int readUSDistance() {
		return this.distance();
	}
}
