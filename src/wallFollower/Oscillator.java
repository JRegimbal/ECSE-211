package wallFollower;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Oscillator extends Thread{
	
	private EV3LargeRegulatedMotor motor;	//Motor to oscillate
	private int fov;	//Field of view of the motor (degrees)
	private float speed;	//Speed at which the motor should rotate
	
	public Oscillator(EV3LargeRegulatedMotor motor, int fov, float speed) {
		this.motor = motor;
		this.fov = fov;
		this.speed = speed;
		this.motor.setSpeed(this.speed);
		this.motor.rotateTo(0);
	}
	
	@Override
	public void run() {
		while(true) {
			try{
				Thread.sleep(100);	//Point sensor to the side for an extra 0.1s
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			this.motor.rotate(-fov);	//Rotate to next point
			this.motor.rotate(fov);	//Rotate back
		}
		//if(motor.getPosition() > angle2) motor.backward();
		//motor.forward();
	}
	
}
