package src;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.utility.Delay;

//mauvaise version
public class Perception {
	private EV3ColorSensor capteurCouleur;
	private EV3UltrasonicSensor capteurUltra;
	private EV3TouchSensor capteurTouche;
	private EV3IRSensor distanceSensor;
    private static float[] distance;
    private static SampleProvider average;
	private float [] colorSample; // tableau des couleurs
	private SampleProvider colorProvider;
	private int R,G,B;
	
	
	// ********************** CONSTRUCTEUR ******************************
	public Perception (Port couleur, Port ultra, Port touche, Port IRSensor) {
    	distanceSensor = new EV3IRSensor(IRSensor);
		capteurCouleur = new EV3ColorSensor (couleur);
		capteurUltra = new EV3UltrasonicSensor (ultra);
		capteurTouche= new EV3TouchSensor (touche); //IRSensor
    	average = new MeanFilter(distanceSensor.getDistanceMode(), 1);
    	distance=new float[average.sampleSize()];
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
