package utilities;

import chassis.Lab5;
import lejos.hardware.Sound;

public class Capture extends Thread {
	private Odometer odo;
	private final double [] GOAL_ZONE = new double [] {80, 80}; //(80, 80) in cm, goal zone
	
	public Capture(Odometer odo) {
		this.odo = odo;
	}
	
	@Override
	public void run() {
		while(Lab5.state != Lab5.RobotState.k_Capture) {
			//wait for capture to begin
			try {
				Thread.sleep(300);
			} catch (InterruptedException e){ }
		}
		
		getBlock();
		odo.travelTo(GOAL_ZONE[0], GOAL_ZONE[1]); //travel to scoring zone with block
		
		//TODO implement capture code
		odo.setMotorSpeeds(0, 0); //ensure motors are stopped
		odo.forwardMotors();
		Lab5.state = Lab5.RobotState.k_Disabled;
		Sound.beep();
		Sound.beep();
		Sound.beep();
	}
	
	private void getBlock() {
		//TODO CATCH BLOCK, avoid the obstacle if it lies in the path
	}
}
