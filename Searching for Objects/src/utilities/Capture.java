package utilities;

import chassis.Lab5;

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
		
		//TODO implement capture code
		
		Lab5.state = Lab5.RobotState.k_Disabled;
	}
}
