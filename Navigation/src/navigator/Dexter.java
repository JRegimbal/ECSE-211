package navigator;

import lejos.hardware.Button;

public class Dexter extends Thread {
	
	public void run() {
		int option = 0;
		while(option == 0) option = Button.waitForAnyPress();
		if(option == Button.ID_ESCAPE) {
			System.exit(1);
		}
	}

}