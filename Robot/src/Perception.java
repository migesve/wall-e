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
	SampleProvider colorProvider;
	float [] colorSample; //dans case 0 == red, case 1 == green, case 2 == blue
	float R,G,B;

	public void getCouleur() { //sans retour mais met les valeur dans R, G, B
		Port capteurCouleur = LocalEV3.get().getPort("2"); //capteur couleur = le capteur numéro 2
		colorSensor=new EV3ColorSensor(capteurCouleur);
		colorProvider =colorSensor.getRGBMode();
		colorSample = new float [colorProvider.sampleSize()];
		colorProvider.fetchSample(colorSample, 0);
		R=colorSample[0];
		G=colorSample[1];
		B=colorSample[2];

	}


	public boolean getTouche() {
		Port capteurTouche = LocalEV3.get().getPort("1");
		touchSensor=new EV3TouchSensor(capteurTouche);
		SampleProvider sampleProvider=touchSensor.getTouchMode();
		float [] sample=new float [sampleProvider.sampleSize()];
		if ( sample[0]== 1){
			this.touch=true;
			return true;
		}
		else {
			this.touch=false;
			return touch; 
		}
	}

