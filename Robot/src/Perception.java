import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;

public class Perception {
	private boolean touch;
	EV3TouchSensor touchSensor;
	EV3ColorSensor colorSensor;

	public int getCouleur() {
		Port capteurCouleur = LocalEV3.get().getPort("D");
		colorSensor=new EV3ColorSensor(capteurCouleur);
		return colorSensor.getColorIDMode();
	}


	public boolean getTouche() {
		Port capteurTouche = LocalEV3.get().getPort("1");
		touchSensor=new EV3TouchSensor(capteurTouche);
		
		if ( touchSensor.getTouchMode == 1)
			this.b=true;
		else
			this.b=false;
			return b ;




	}
