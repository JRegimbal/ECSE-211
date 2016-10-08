package navigator;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

public class Printer extends Thread {
	private UltrasonicController cont;

	private final int option;

	public static TextLCD t = LocalEV3.get().getTextLCD();

	public Printer(int option, UltrasonicController cont) {
		this.option = option;
		this.cont = cont;
	}

	@Override
	public void run() {
		while(true) {
			t.clear();
			t.drawString("On course: ",0,0);
			if(this.option == Button.ID_LEFT) {
				t.drawString("Part 1",0,1);
			}
			else if (this.option == Button.ID_RIGHT) {
				t.drawString("Part 2",0,1);
			}
			t.drawString("US Distance: " + cont.readUSDistance(),0,2);

			try {
				Thread.sleep(200);
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void printMainMenu() {
		t.clear();
		t.drawString("Left: Part 1",0,0);
		t.drawString("Right: Part 2",1,0);
	}
}
