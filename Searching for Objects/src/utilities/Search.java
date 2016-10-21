package utilities;

import chassis.Lab5;
import lejos.robotics.SampleProvider;

public class Search extends Thread {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;
	
	
	public Search(Odometer odo, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
	}
	@Override
	public void run() {
		while(Lab5.state != Lab5.RobotState.k_Search) {
			//wait for localization to finish
			try {
				Thread.sleep(300);
			} catch (Exception e) {}
		}
		
		//TODO: Comb through track, check for detection
		
		//Styrofoam block found - begin capture
		Lab5.state = Lab5.RobotState.k_Capture;
	}
}
