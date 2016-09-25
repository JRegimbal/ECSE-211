package wallFollower;
import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private int distance;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	public BangBangController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
							  int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(motorHigh);				// Start robot moving forward
		rightMotor.setSpeed(motorHigh);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	@Override
	public void processUSData(int distance) {
		this.distance = distance;
		// TODO: process a movement based on the us distance passed in (BANG-BANG style)
		int errorCM = this.distance - this.bandCenter;	//offset between current position and ideal distance from wall (in cm)
		if(Math.abs(errorCM) <= this.bandwidth)	//straight (in dead band)
		{
			this.leftMotor.setSpeed(this.motorHigh);
			this.rightMotor.setSpeed(this.motorHigh);
			this.leftMotor.forward();
			this.rightMotor.forward();
		}
		else if(errorCM < 0) 					//condition to swerve right - too close to wall
		{	
			this.leftMotor.setSpeed(this.motorHigh + 250);	//Make the correction when a wall is detected more drastic
			this.rightMotor.setSpeed(this.motorLow);
			this.leftMotor.forward();
			this.rightMotor.forward();
		}
		else									//too far from wall - swerve left
		{
			this.leftMotor.setSpeed(this.motorLow);
			this.rightMotor.setSpeed(this.motorHigh);
			this.leftMotor.forward();
			this.rightMotor.forward();
		}
	}

	@Override
	
	public int readUSDistance() {
		return this.distance;
	}
}
