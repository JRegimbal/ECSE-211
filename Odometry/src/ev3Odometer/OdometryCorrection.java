/* 
 * OdometryCorrection.java
 */
package ev3Odometer;

import java.util.Arrays;

import lejos.hardware.sensor.EV3ColorSensor;

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;
	private float color[];
	
	private static final double THRESHOLD_THETA = 40.0 * Math.PI / 180.0;
	private static final double THRESHOLD_POS = 5.0;
	public static int count;
	private int colorID;
	private double correctedValue;
	private double lastPosition[];
	private int idleColor;
	private float lastColor;
	private final boolean useCorrection = true;
	private final boolean updateAll[] = {true, true, true};

	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
		count = 0;
		color = new float[Lab2.colorSensor.sampleSize()];
		lastPosition = new double[3]; //x, y, theta
		odometer.getPosition(lastPosition, updateAll);	//need to set theta - may as well get current x and y
	}

	// run method (required for Thread)
	public void run() {
		if(!useCorrection)
		{
			return;
		}
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
			//count = count > 12 ? 12 : count;

			// put your correction code here
			
			if(color[0] < 300) //Detected black
			{
				System.out.println("         LINE");
				/*
				//Every third line comes after a corner, thus there is no guaranteed difference in x or y.
				//Therefore, store the brick's position at these points.
				if(count % 3 == 0) {
					checkPoints[(2*count)/3] = odometer.getY();
					checkPoints[(2*count)/3 + 1] = odometer.getX(); 
					//odometer.setTheta(count * Math.PI / 2);
				}
				else if(count < 3) { //First vertical 
					odometer.setX(checkPoints[1] + count * Lab2.SQUARE_LENGTH); //Add fifteen to y once per line after last checkpoint
				}
				else if(count < 6) { //First horizontal
					odometer.setY(checkPoints[2] - (count - 3)* Lab2.SQUARE_LENGTH); //Add fifteen to x once per line after last checkpoint
				}
				else if(count < 9) { //Second vertical
					odometer.setX(checkPoints[5] + (6 - count) * Lab2.SQUARE_LENGTH); //Subtract 15 from y once per line after last checkpoint
				}
				else { //Second horizontal
					odometer.setY(checkPoints[6] - (9 - count) * Lab2.SQUARE_LENGTH); //Subtract 15 from x once per line after last checkpoint
				}
				count++; //Keep track of the amount of lines seen
				count = count % 12; //So that we don't get an out of bounds exception after the test run
				*/
				double currentPosition[] = new double[3];
				odometer.getPosition(currentPosition, updateAll);
				//check theta (see if we changed heading and turned)
				if(Math.abs(currentPosition[2] - lastPosition[2]) < THRESHOLD_THETA)
				{
					if(Math.abs(Lab2.SQUARE_LENGTH + lastPosition[1] - currentPosition[1]) < THRESHOLD_POS ||	//no line missed
							Math.abs(Lab2.SQUARE_LENGTH + lastPosition[0] - currentPosition[0]) < THRESHOLD_POS)
					{
						if(Math.abs(currentPosition[2] - 0.00) < THRESHOLD_THETA) //+x direction
							odometer.setX(lastPosition[0] + Lab2.SQUARE_LENGTH);
						else if(Math.abs(currentPosition[2] - (-Math.PI/2)) < THRESHOLD_THETA) //-y
							odometer.setY(lastPosition[1] - Lab2.SQUARE_LENGTH);
						else if(Math.abs(currentPosition[2] - (-Math.PI)) < THRESHOLD_THETA) //-x
							odometer.setX(lastPosition[0] - Lab2.SQUARE_LENGTH);
						else if(Math.abs(currentPosition[2] - (-Math.PI*3/2)) < THRESHOLD_THETA) //+y
							odometer.setY(lastPosition[1] + Lab2.SQUARE_LENGTH);
					}
				}
				//if nothing was done in the above block we either missed a line or turned and we just need to update
				lastPosition = currentPosition.clone();
				count++;
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