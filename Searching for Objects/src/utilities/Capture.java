package utilities;

import chassis.Lab5;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Capture extends Thread {
	private Odometer odo;
	private final double [] GOAL_ZONE = new double [] {80, 80}; //(80, 80) in cm, goal zone
	
	private final int ARM_SPEED	= 70;
	
	private EV3LargeRegulatedMotor leftArm;
	private EV3LargeRegulatedMotor rightArm;
	
	public Capture(Odometer odo, EV3LargeRegulatedMotor leftArm, EV3LargeRegulatedMotor rightArm) {
		this.odo = odo;
		this.leftArm = leftArm;
		this.rightArm = rightArm;
	}
	
	@Override
	public void run() {
		while(Lab5.state != Lab5.RobotState.k_Capture) {
			//wait for capture to begin
			try {
				Thread.sleep(300);
			} catch (InterruptedException e){ }
		}
		Navigator.turnBy(Math.PI);
		odo.moveCM(Odometer.LINEDIR.Backward, 3, true);
		
		getBlock();
		Navigation nav = new Navigation(odo); //travel to scoring zone with block
		nav.travelTo(GOAL_ZONE[0] - 17, GOAL_ZONE[1] - 17); 
		Navigator.turnBy(Math.PI);
		
		//TODO implement capture code
		odo.setMotorSpeeds(0, 0); //ensure motors are stopped
		odo.forwardMotors();
		Lab5.state = Lab5.RobotState.k_Disabled;
		ascendArms();
		Sound.beep();
		Sound.beep();
		Sound.beep();
	}
	
	private void descendArms() {
		leftArm.setSpeed(ARM_SPEED);
		rightArm.setSpeed(ARM_SPEED);
		this.leftArm.rotate(90,true);
		this.rightArm.rotate(90,false);
	}
	
	private void ascendArms() {
		leftArm.setSpeed(ARM_SPEED);
		rightArm.setSpeed(ARM_SPEED);
		this.leftArm.rotate(-90,true);
		this.rightArm.rotate(-90,false);
	}
	
	private void getBlock() {
		//TODO CATCH BLOCK, avoid the obstacle if it lies in the path
		Sound.beep();
		descendArms();
	}
}
