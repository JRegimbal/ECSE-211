package wallFollower;
import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private int distance;
	private EV3LargeRegulatedMotor leftMotor, rightMotor, sensorMotor;
	
	public BangBangController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,EV3LargeRegulatedMotor sensorMotor,
							  int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.sensorMotor = sensorMotor;

		/*this.highDistance = false;
		this.tl = new TimerListener () 
				{
					public void timedOut()
					{
						highDistance = true;
					}
				};
		this.timer = new Timer(4000, this.tl);*/

		leftMotor.setSpeed(motorHigh);				// Start robot moving forward
		rightMotor.setSpeed(motorHigh);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	@Override
	public void processUSData(int distance) {
		this.distance = distance;
		// TODO: process a movement based on the us distance passed in (BANG-BANG style)
		//check for high distance (either gap or going through a turn, wait a few seconds)
		/*if(this.distance > 200 && !highDistance)
		{
			timer.start();								//timer that waits 4s (4000ms) and triggers tl.timedOut() at the end
			this.leftMotor.setSpeed(this.motorHigh);	//move forward
			this.rightMotor.setSpeed(this.motorHigh);
			this.leftMotor.forward();
			this.rightMotor.forward();
			return;										//exit function - no need to continue with exact error
		}
		else		//high distance condition set (turn hard left) or no longer in high distance warning (passed gap)
		{
			timer.stop();
		}*/
		int errorCM = this.distance - this.bandCenter;	//offset between current position and ideal distance from wall (in cm)
		if(Math.abs(errorCM) <= this.bandwidth)	//conditions to swerve slight right - straight (in dead band)
		{
			//this.leftMotor.setSpeed(this.motorHigh + 30); //I don't think we need the swerve, but it's here just in case
			this.leftMotor.setSpeed(this.motorHigh);
			this.rightMotor.setSpeed(this.motorHigh);
			this.leftMotor.forward();
			this.rightMotor.forward();
		}
		else if(errorCM < 0) 					//condition to swerve right - too close to wall
		{	
			this.leftMotor.setSpeed(this.motorHigh + 100);	//Make the correction when a wall is detected more drastic
			this.rightMotor.setSpeed(this.motorLow);
			this.leftMotor.forward();
			this.rightMotor.forward();
			//this.rightMotor.stop();
		}
		else									//too far from wall - swerve left
		{
			this.leftMotor.setSpeed(this.motorLow);
			this.rightMotor.setSpeed(this.motorHigh);
			//this.leftMotor.stop();
			this.leftMotor.forward();
			this.rightMotor.forward();
		}
	}

	@Override
	
	public int readUSDistance() {
		return this.distance;
	}
}
