import java.awt.Color;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.RangeFinder;
import lejos.robotics.SampleProvider;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.robotics.objectdetection.RangeFeatureDetector;


public class Perception {
	/**
	 * Capteurs.
	 */
	private final EV3ColorSensor capteurCouleur;
	private final EV3UltrasonicSensor capteurDistance;
	private final EV3TouchSensor capteurTouche;

	/**
	 * Sample providers
	 */
	private SampleProvider colorProvider;
	private SampleProvider touchProvider;
	private SampleProvider distanceProvider;
	private SampleProvider irProvider;

	/**
	 * data
	 */
	private float [] colorSample; // tableau des couleurs
	private float [] touchSample;
	private float [] distanceSample;
	private float [] irSample;

	/**
	 * attributs public mis à jour !!!
	 * volatile = accesseur désynchronisé
	 */
	public volatile Color currentColor;
	public volatile float distance;
	public volatile boolean touch;
	public volatile boolean detection;

	public int MAX_DISTANCE; // en centimètre
	public int PERIOD;
	private FeatureDetector fd;

	//=============================

	// ********************** CONSTRUCTEUR ******************************
	public Perception (Port touche, Port couleur, Port ultra) { //Port IRSensor
		capteurCouleur = new EV3ColorSensor(couleur);
		capteurDistance= new EV3UltrasonicSensor(ultra);
		capteurTouche= new EV3TouchSensor(touche);
		//capteurIR = new EV3IRSensor(ultra);

		distanceProvider = capteurDistance.getDistanceMode();
		colorProvider = capteurCouleur.getRGBMode();
		touchProvider = capteurTouche.getTouchMode();
		irProvider = capteurDistance.getListenMode();

		colorSample = new float [colorProvider.sampleSize()];
		touchSample = new float [touchProvider.sampleSize()];
		distanceSample = new float [distanceProvider.sampleSize()];
		irSample = new float [irProvider.sampleSize()];

		currentColor = new Color(0,0,0);
		distance = 0;
		touch = false;
		detection = false;

		MAX_DISTANCE = 250;

		PERIOD=10000;

		fd = new RangeFeatureDetector((RangeFinder)capteurDistance, MAX_DISTANCE, PERIOD);
	}
	// ****************************** METHODES******************************
	public void update() {
		currentColor = getCouleur();
		distance = getDistance();
		touch = getTouche();
		detection = getIR();
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
		return touchSample[0]==1 ? true : false;
	}

	//return en cm
	public float getDistance() {
		//capteurDistance.disable(); // jsp si on en a besoin, il faut tester
		distanceProvider.fetchSample(distanceSample, 0);
		return 100*distanceSample[0];

		/*//test
		  System.out.println(distanceSample[0]);
		  Delay.msDelay(10000);*/

	}

	public boolean getIR() {  //true = il y a qqln 
		//capteurDistance.enable(); //tester avec un autre robot si getIR() marche sans cette ligne
		irProvider.fetchSample(irSample, 0);
		return irSample[0]==1 ? true : false;

		/*test
		System.out.println(irSample[0]);
		Delay.msDelay(10000);*/
	}
	public void detectionObjet() {
		Feature result = fd.scan();
		if(result != null) {
			System.out.println("Range: " + result.getRangeReading().getRange());
		}
	}
}