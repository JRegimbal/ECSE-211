package wallFollower;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 200, FILTER_OUT = 20;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;
	private final int filterDistance = 50;	//difference between previous and current errors to signal a false positive
	
	private int lastErrorCM;
	
	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
					   int bandCenter, int bandwidth) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(motorStraight);					// Initialize motor rolling forward
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
		lastErrorCM = 0;
	}
	
	@Override
	public void processUSData(int distance) {
		
		// rudimentary filter - toss out invalid samples corresponding to null signal.
		// (n.b. this was not included in the Bang-bang controller, but easily could have).
		//
		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the filter value
			filterControl ++;
		} else if (distance >= 255){
			// true 255, therefore set distance to 255
			this.distance = distance;
		} else {
			// distance went below 255, therefore reset everything.
			filterControl = 0;
			this.distance = distance;
		}
		
		int errorCM = this.distance - this.bandCenter;	//offset between current position and ideal distance from wall (in cm)
		//check if the reported error suddenly changed - ignore value if so (false positive)
		if(errorCM - this.lastErrorCM > this.filterDistance)
		{
			this.lastErrorCM = errorCM;
			return;
		}
		if(Math.abs(errorCM) <= this.bandwidth)	//straight (in dead band)
		{
			this.leftMotor.setSpeed(this.motorStraight);
			this.rightMotor.setSpeed(this.motorStraight);
			this.leftMotor.forward();
			this.rightMotor.forward();
		}
		else if(errorCM < 0)								//too close to wall - swerve right
		{
			this.leftMotor.setSpeed(this.motorStraight + (bandCenter - Math.abs(this.distance)) * 11.0f); //constant higher because the distance closer to the wall is constrained (0-bandcenter)
			this.rightMotor.setSpeed(this.motorStraight);
			this.leftMotor.forward();
			this.rightMotor.forward();
		}
		else {									//too far from wall - swerve left
			this.leftMotor.setSpeed(this.motorStraight);	
			this.rightMotor.setSpeed(this.motorStraight + this.distance * 1.5f);	//smaller constant as distance can get much higher (bandcenter-255)
			this.leftMotor.forward();
			this.rightMotor.forward();
		}
		this.lastErrorCM = errorCM;	//record last error
	}

	
	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
