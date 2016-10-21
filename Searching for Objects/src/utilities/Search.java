package utilities;

import chassis.Lab5;
import lejos.robotics.SampleProvider;

public class Search extends Thread {
	private Odometer odo;
	private SampleProvider colorSensor, usSensor;
	private float[] colorData, usData;
	private final float FIELD_BOUNDS = 120; //cm
	private float lastDistanceDetected;
	private final float DISTANCE_THRESHOLD = 25; //cm
	private final double fieldToSearch = Math.PI/2;
	private final double fieldIncrement = 5*Math.PI/180; //5-degree increments
	private final float[][] scanPoints = new float[][] {{0, 0, 0}, {0, 60, (float)Math.PI/2}};
	private final int SEARCH_SPEED = 200;
	
	public Search(Odometer odo, SampleProvider colorSensor, float[] colorData, SampleProvider usSensor, float[] usData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.usSensor = usSensor;
		this.usData = usData;
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
		//sweep track for obstacles from position (0,0) starting at 0-radians
		int scanPointNumber = 0;
		while(true) {
			lastDistanceDetected = FIELD_BOUNDS;
			double thetaScanStart = odo.getTheta();
			boolean objectFound;
			while(!(objectFound = isObjectDetected()) && odo.getTheta() < thetaScanStart + fieldToSearch) {	//check if there is an object at current heading or if area has been scanned
				odo.turnTo(odo.getTheta() + fieldIncrement);
			}
			if(objectFound) {	//go to object, check if it is a styrofoam block
				odo.setMotorSpeeds(SEARCH_SPEED, SEARCH_SPEED);
				odo.forwardMotors();
				while(getFilteredDataBasic() > 5.0f); //wait until 5 centimeters from object
				odo.setMotorSpeeds(0, 0);
				odo.forwardMotors();
				if(isStyrofoamBlock()) {	//begin capture
					break;
				}
			} else {	//go to next scan point
				scanPointNumber++;
			}
			odo.travelTo(scanPoints[scanPointNumber][0], scanPoints[scanPointNumber][1]);	//travel to scan point
			odo.turnTo(scanPoints[scanPointNumber][2]);
		}
		
		//Styrofoam block found - begin capture
		Lab5.state = Lab5.RobotState.k_Capture;
	}
	
	private boolean isStyrofoamBlock() {
		return false;
	}
	
	private boolean isObjectDetected() {
		float currentDistance = getFilteredDataBasic();
		boolean isObject = (lastDistanceDetected - currentDistance > DISTANCE_THRESHOLD);
		lastDistanceDetected = currentDistance;
		return isObject;
	}
	
	private float getFilteredDataBasic() {
		usSensor.fetchSample(usData, 0); //Store distance in usData
		return (usData[0] * 100.0f >= FIELD_BOUNDS) ? FIELD_BOUNDS : usData[0] * 100.0f; //Cap data at field bounds, scale data by 100.
	}
	
}
