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
	static EV3ColorSensor colorSensor;
	static SampleProvider colorProvider;
	static float [] colorSample; //dans case 0 == red, case 1 == green, case 2 == blue
	static int R,G,B;


	public static void getCouleur() { //sans retour mais met les valeur dans R, G, B
		Port capteurCouleur = LocalEV3.get().getPort("S2"); //capteur couleur = le capteur numéro 2
		colorSensor=new EV3ColorSensor(capteurCouleur);
		colorProvider =colorSensor.getRGBMode();
		colorSample = new float [colorProvider.sampleSize()];
		colorProvider.fetchSample(colorSample, 0);
		R=(int)(255*colorSample[0]);
		G=(int)(255*colorSample[1]);
		B=(int)(255*colorSample[2]);
		System.out.println(R);
		System.out.println(G);	
		System.out.println(B);	
		Delay.msDelay(10000);

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

	public static void main(String[] args) {
		getCouleur();
	}

	}

