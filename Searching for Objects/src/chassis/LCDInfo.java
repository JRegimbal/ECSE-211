package chassis;

import lejos.hardware.lcd.TextLCD;
import lejos.utility.Timer;
import lejos.utility.TimerListener;
import utilities.Odometer;

public class LCDInfo implements TimerListener{
	public static final int LCD_REFRESH = 100;
	private Odometer odo;
	private Timer lcdTimer;
	private static TextLCD LCD;
	
	// arrays for displaying data
	private double [] pos;
	
	public LCDInfo(Odometer odo, TextLCD LCD, boolean start) {
		this.odo = odo;
		this.LCD = LCD;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		
		// initialise the arrays for displaying data
		pos = new double [3];
		
		// start the timer
		if(start) lcdTimer.start();
	}
	
	public void timedOut() { 
		odo.getPosition(pos);
		LCD.clear();
		//LCD.drawString("US: " + odo.getFilteredData(), 0, 0);
		LCD.drawString("X: ", 0, 0);
		LCD.drawString("Y: ", 0, 1);
		LCD.drawString("H: ", 0, 2);
		LCD.drawInt((int)(pos[0]), 3, 0);
		LCD.drawInt((int)(pos[1]), 3, 1);
		LCD.drawInt((int)(pos[2] * 180.0 / Math.PI), 3, 2);
	}
	
	public void pause() {
		lcdTimer.stop();
	}
	
	public void resume() {
		lcdTimer.start();
	}
	
	public static TextLCD getLCD() {
		return LCD;
	}
}
