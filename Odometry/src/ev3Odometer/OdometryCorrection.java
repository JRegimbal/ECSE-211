/* 
 * OdometryCorrection.java
 */
package ev3Odometer;

import lejos.hardware.sensor.EV3ColorSensor;

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;
	private float color[];
	
	private static final float THRESHOLD = 0.5f;
	public static int count;
	private int colorID;
	private double correctedValue;
	private double checkPoints[];
	private int idleColor;
	private float lastColor;

	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
		count = 0;
		color = new float[Lab2.colorSensor.sampleSize()];
		checkPoints = new double[8];
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;
		Lab2.colorSensor.fetchSample(color,0);
		lastColor = color[0] * 1000; //Scale the intensity value
		
		while (true) {
			correctionStart = System.currentTimeMillis();
			Lab2.colorSensor.fetchSample(color,0); //Get intensity sample
			color[0] *= 1000; //Scale intensity value
			//Intensity at line is < 300.
			if(color[0] < 300 && lastColor < 300) continue; //Only register line when the last measurement DID NOT measure a line
			lastColor = color[0];
			count = count % 12; //So that we don't get an out of bounds exception after the test run

			// put your correction code here
			
			if(color[0] < 300) //Detected black
			{
				System.out.println("         LINE");
				//Every third line comes after a corner, thus there is no guaranteed difference in x or y.
				//Therefore, store the brick's position at these points.
				if(count % 3 == 0) {
					checkPoints[(2*count)/3] = odometer.getX();
					checkPoints[(2*count)/3 + 1] = odometer.getY(); 
					//odometer.setTheta(count * Math.PI / 2);
				}
				else if(count < 3) { //First vertical 
					odometer.setY(checkPoints[1] + count * 15.0); //Add fifteen to y once per line after last checkpoint
				}
				else if(count < 6) { //First horizontal
					odometer.setX(checkPoints[2] + (count - 3)* 15.0); //Add fifteen to x once per line after last checkpoint
				}
				else if(count < 9) { //Second vertical
					odometer.setY(checkPoints[5] + (6 - count) * 15.0); //Subtract 15 from y once per line after last checkpoint
				}
				else { //Second horizontal
					odometer.setX(checkPoints[6] + (9 - count) * 15.0); //Subtract 15 from x once per line after last checkpoint
				}
				count++; //Keep track of the amount of lines seen
			}
			
			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
}