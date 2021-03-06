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
	private Navigation nav;
	
	public Capture(Odometer odo, EV3LargeRegulatedMotor leftArm, EV3LargeRegulatedMotor rightArm) {
		this.odo = odo;
		this.leftArm = leftArm;
		this.rightArm = rightArm;
		this.nav = new Navigation(this.odo);
	}
	
	@Override
	public void run() {
		while(Lab5.state != Lab5.RobotState.k_Capture) {
			//wait for capture to begin
			try {
				Thread.sleep(300);
			} catch (InterruptedException e){ }
		}
		nav.turnBy(Math.PI);
		odo.moveCM(Odometer.LINEDIR.Backward, 3, true);
		
		getBlock();
		Navigation nav = new Navigation(odo); //travel to scoring zone with block
		nav.travelTo(GOAL_ZONE[0] - 17, GOAL_ZONE[1] - 17); 
		
		while(Navigation.PathBlocked) {
			odo.stopMotors();
			//If it can (without going out of bounds), turn clockwise and move 20cm
			if(inBounds(odo.getX() + 20*Math.cos(odo.getTheta() - Math.PI/2),odo.getY() - Math.sin(odo.getTheta() - Math.PI/2),60,60)) {
				nav.turnBy(Math.PI/2);
				odo.moveCM(Odometer.LINEDIR.Forward,20,true);
			}
			//Otherwise turn counterclockwise and move 20cm
			else {
				nav.turnBy(Math.PI/2);
				odo.moveCM(Odometer.LINEDIR.Forward, 20, true);
			}
			nav.travelTo(GOAL_ZONE[0] - 17, GOAL_ZONE[1] - 17); 
			nav.turnBy(Math.PI);
		}
		
		//TODO implement capture code
		odo.setMotorSpeeds(0, 0); //ensure motors are stopped
		odo.forwardMotors();
		nav.turnBy(Math.PI);

		Lab5.state = Lab5.RobotState.k_Disabled;
		ascendArms();
		Sound.beep();
		Sound.beep();
		Sound.beep();
	}
	
	private void descendArms() {
		leftArm.setSpeed(ARM_SPEED);
		rightArm.setSpeed(ARM_SPEED);
		this.leftArm.rotate(90 + Lab5.RESTING_ARM_POSITION,true);
		this.rightArm.rotate(90 + Lab5.RESTING_ARM_POSITION,false);
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
	
	public static boolean inBounds(double x,double y, double width, double height) {
		return (x < width) && (y < height) && x > 0 && y > 0;
	}
}
