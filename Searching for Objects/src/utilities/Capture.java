package utilities;

import chassis.Lab5;

public class Capture extends Thread {
	private Odometer odo;
	
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
