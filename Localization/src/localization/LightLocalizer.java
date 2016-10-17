package localization;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import localization.Odometer.LINEDIR;
import localization.Odometer.TURNDIR;

public class LightLocalizer {
	private Odometer odo;
	private Navigation nav;
	private SampleProvider colorSensor;
	private float[] colorData;	
	
	private static final double BLACK_LINE	= 50.0;
	
	private static final int ROTATE_SPEED	= 40;
	private static final int FORWARD_SPEED	= 160;
	
	private static final double LS_DISTANCE	= 14.0; // 17
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		nav = new Navigation(odo);
	}
	
	public void doLocalization() {
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
		Lab4.Text_LCD.clear();
		int lineCount = 0;
		double thetas[] = new double[4];
		double lastColor; //To figure out when the sensor has stopped detecting a line
		
		nav.turnTo(0.25 * Math.PI, true); //Point along diagonal
		//Move until color sensor detects gridline
		odo.setMotorSpeed(FORWARD_SPEED);
		while(getLightData() > 60.0) {
			odo.forwardMotors();
			Lab4.Text_LCD.drawString("Color: " + getLightData(),0,4);
		}
		odo.setMotorSpeed(0);
		odo.forwardMotors();
		//Back up a bit, to bring center of rotation close to grid line
		odo.setMotorSpeed(FORWARD_SPEED);
		odo.moveCM(LINEDIR.Backward, 17, true);
		
		//Start rotating, collect positions at line crossings.
		odo.setMotorSpeed(ROTATE_SPEED);
		odo.spin(TURNDIR.CW);
		lastColor = getLightData();
		
		double t;
		
		while(lineCount < 4) {
			Lab4.Text_LCD.drawString("Lines: " + lineCount,0,4);
			if(getLightData() < BLACK_LINE) {
				t = odo.getTheta();
				double nextTheta = (t < 0.0) ? t + 2*Math.PI : t;
				if(lastColor < BLACK_LINE) continue;
				if(lineCount > 0) if(Math.abs(thetas[lineCount-1] - nextTheta) < Math.PI/18.0) continue;
				thetas[lineCount++] = nextTheta; //Latch headings when grid line is detected
				lastColor = BLACK_LINE - 1.0f;
				Sound.beep();
			}
			else lastColor = getLightData();
		}
		odo.setMotorSpeed(0);
		odo.forwardMotors();
		
		double thetay, thetax;
		double realx, realy;
		double dTheta;
		thetay = Math.abs(thetas[1] - thetas[3]);
		thetax = Math.abs(thetas[0] - thetas[2]);
		
		//Logging info - for debugging purposes.
		
		realx = -1.0*LS_DISTANCE * Math.cos(0.5 * thetay);
		realy = -1.0*LS_DISTANCE * Math.cos(0.5 * thetax);
		dTheta = /*0.5 * Math.PI + */-0.5 * thetay + Math.PI - thetas[3];
		Lab4.Text_LCD.drawString("Real x: " + realx, 0, 5);
		Lab4.Text_LCD.drawString("Real y: " + realy, 0, 6);
		Lab4.Text_LCD.drawString("dTheta: " + dTheta, 0, 7);
		
		try {
			PrintWriter writer = new PrintWriter("data.txt","UTF-8");
			writer.println("t1    : " + (int)(thetas[0] * 180.0 / Math.PI));
			writer.println("t2    : " + (int)(thetas[1] * 180.0 / Math.PI));
			writer.println("t3    : " + (int)(thetas[2] * 180.0 / Math.PI));
			writer.println("t4    : " + (int)(thetas[3] * 180.0 / Math.PI));
			writer.println("thetay: " + (int)(thetay * 180.0 / Math.PI));
			writer.println("thetax: " + (int)(thetax * 180.0 / Math.PI));
			writer.println("dTheta: " + (int)(dTheta * 180.0 / Math.PI));
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double[] realPos = {realx,realy,odo.getTheta() + dTheta};
		odo.setPosition(realPos, new boolean[] {true,true,true});
		
		odo.setMotorSpeed(FORWARD_SPEED);
		nav.travelTo(0, 0);
		nav.turnTo(0, true);
	}
	
	public double getLightData() {
		colorSensor.fetchSample(colorData, 0);
		double color = colorData[0] * 100.0f;
		
		return color;
	}

}
