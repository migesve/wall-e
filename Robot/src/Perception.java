import java.awt.Color;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;


public class Perception {
	/**
	 * Capteurs.
	 */
	private final EV3TouchSensor capteurTouche;
	private final EV3ColorSensor capteurCouleur;
	private final EV3UltrasonicSensor capteurDistance;
	/**
	 * Sample providers
	 */
	private SampleProvider colorProvider;
	private SampleProvider touchProvider;
	private SampleProvider distanceProvider;
	//private SampleProvider irProvider;

	/**
	 * data
	 */
	private float [] colorSample; // tableau des couleurs
	private float [] touchSample;
	private float [] distanceSample;
	//private float [] irSample;

	/**
	 * attributs public mis à jour !!!
	 * volatile = accesseur désynchronisé
	 */
	public volatile Color color;
	public volatile float distance;
	public volatile boolean touch;
	//public volatile boolean detection;

	//=============================

	// ********************** CONSTRUCTEUR ******************************
	public Perception (Port touche, Port couleur, Port ultra) { //Port IRSensor
		capteurTouche = new EV3TouchSensor(touche);
		capteurCouleur = new EV3ColorSensor(couleur);
		capteurDistance= new EV3UltrasonicSensor(ultra);
		
		capteurTouche.setCurrentMode(0);
		capteurCouleur.setCurrentMode(2);
		capteurDistance.setCurrentMode(0);
		
		colorProvider = capteurCouleur.getRGBMode();
		touchProvider = capteurTouche.getTouchMode();
		distanceProvider = capteurDistance.getDistanceMode();
		
		colorSample = new float [colorProvider.sampleSize()];
		touchSample = new float [touchProvider.sampleSize()];
		distanceSample = new float [distanceProvider.sampleSize()];

		color = getCouleur();
		distance = getDistance();
		touch = getTouche();
		//detection = false;
	}
	// ****************************** METHODES******************************
	public void update() {
		color = getCouleur();
		distance = getDistance();
		touch = getTouche();
		//detection = getIR();
	}
	public Color getCouleur() {
		colorProvider.fetchSample(colorSample, 0);
		int R=(int)(255*colorSample[0]);
		int G=(int)(255*colorSample[1]);
		int B=(int)(255*colorSample[2]);
		if (R < 10 && G < 10 & B < 10) {
			return Color.BLACK;
		}else if(R > 245 && G > 245 && B > 245) {
			return Color.WHITE;
		}else if(R > 180 && G > 180 && B > 180 && R > 200 && G > 200 && B > 200) {
			return Color.GRAY;
		}else if(R > 245 && G > 245 && B < 10) {
			return Color.YELLOW;
		}else if(R > 245 && G < 10 & B < 10) {
			return Color.RED;
		}else if(R < 10 && G > 245 & B < 10) {
			return Color.GREEN;
		}else {
			return Color.BLUE;
		}
	}

	public boolean getTouche() {
		touchProvider.fetchSample(touchSample, 0);
		return touchSample[0] == 1;
	}

	//return en cm
	public float getDistance() {
		//capteurDistance.setCurrentMode(0);
		
		distanceProvider.fetchSample(distanceSample, 0);
		float dist = 100*distanceSample[0];
		if (Float.isNaN(dist)) { //NaN = Not A Number
			return Float.POSITIVE_INFINITY;
		}else {
			return dist;
		}

		/*//test
		  System.out.println(distanceSample[0]);
		  Delay.msDelay(10000);*/

	}

//	public boolean getIR() {  //true = il y a qqln 
//		//capteurDistance.enable(); //tester avec un autre robot si getIR() marche sans cette ligne
//		capteurDistance.setCurrentMode(1);
//		irProvider = capteurDistance.getListenMode();
//		irSample = new float [irProvider.sampleSize()];
//		irProvider.fetchSample(irSample, 0);
//		return irSample[0]==1 ? true : false;
//
//		/*test
//		System.out.println(irSample[0]);
//		Delay.msDelay(10000);*/
//	}
}