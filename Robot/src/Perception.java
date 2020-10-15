import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;

//mauvaise version
public class Perception {
	private EV3ColorSensor capteurCouleur;
	private EV3UltrasonicSensor capteurUltra;
	private EV3TouchSensor capteurTouche;
	private float [] colorSample; // tableau des couleurs
	private SampleProvider colorProvider;
	private int R,G,B;
	
	
	// ********************** CONSTRUCTEUR ******************************
	public Perception (Port couleur, Port ultra, Port touche) {
		capteurCouleur = new EV3ColorSensor (couleur);
		capteurUltra = new EV3UltrasonicSensor (ultra);
		capteurTouche= new EV3TouchSensor (touche);
		colorProvider=capteurCouleur.getRGBMode();
		colorSample = new float [colorProvider.sampleSize()];
		R=0;
		G=0;
		B=0;	
		
	}


	
	// ************************* GETTERS ************************************
	public EV3ColorSensor getCapteurCouleur() {
		return capteurCouleur;
	}


	public EV3UltrasonicSensor getCapteurUltra() {
		return capteurUltra;
	}


	public EV3TouchSensor getCapteurTouche() {
		return capteurTouche;
	}


	public float[] getColorSample() {
		return colorSample;
	}


	public int getR() {
		return R;
	}


	public int getG() {
		return G;
	}


	public int getB() {
		return B;
	}
	
	public SampleProvider getColorProvider() {
		return colorProvider;
	}
	
	// ****************************** METHODES******************************
	public void getCouleur() {
		colorProvider.fetchSample(colorSample, 0);
		R=(int)(255*colorSample[0]);
		G=(int)(255*colorSample[1]);
		B=(int)(255*colorSample[2]);
		}




	
	
	
	
	
	
	
	
	
	
	
}
