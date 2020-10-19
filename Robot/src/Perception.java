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


public class Perception {
	//CAPTEUR
	private EV3ColorSensor capteurCouleur;
	private EV3UltrasonicSensor capteurUltra;
	private EV3TouchSensor capteurTouche;
	private EV3IRSensor distanceSensor;
    private static float[] distance;
   // Sample provider
    private static SampleProvider average;
    private SampleProvider colorProvider;
    private SampleProvider touchProvider;
    //Tableau 
	private float [] colorSample; // tableau des couleurs
	private float [] touchSample;
	
	//
	private int R,G,B;
	private boolean touche;
	
	
	// ********************** CONSTRUCTEUR ******************************
	public Perception (Port couleur, Port ultra, Port touche, Port ultra) { //Port IRSensor
		capteurCouleur = new EV3ColorSensor (couleur);
		capteurUltra = new EV3UltrasonicSensor (ultra);
		capteurTouche= new EV3TouchSensor (touche);

    	distanceSensor = new EV3IRSensor(IRSensor);
		average = new MeanFilter(distanceSensor.getDistanceMode(), 1);
    	
		colorProvider=capteurCouleur.getRGBMode();
		touchProvider=capteurTouche.getTouchMode();
		
		colorSample = new float [colorProvider.sampleSize()];
		touchSample = new float [touchProvider.sampleSize()];
		
		R=0;
		G=0;
		B=0;
		this.touche=false;
		
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



	public EV3IRSensor getDistanceSensor() {
		return distanceSensor;
	}



	public static float[] getDistance() {
		return distance;
	}



	public static SampleProvider getAverage() {
		return average;
	}



	public SampleProvider getColorProvider() {
		return colorProvider;
	}



	public SampleProvider getTouchProvider() {
		return touchProvider;
	}



	public float[] getColorSample() {
		return colorSample;
	}



	public float[] getTouchSample() {
		return touchSample;
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


	
	// ****************************** METHODES******************************
	public void getCouleur() {
		colorProvider.fetchSample(colorSample, 0);
		R=(int)(255*colorSample[0]);
		G=(int)(255*colorSample[1]);
		B=(int)(255*colorSample[2]);
		}



	public boolean getTouche() {
		touchProvider.fetchSample(touchSample, 0);
		if (touchSample[0]==1)
			touche=true;
		else
			touche=false;
		return touche;

	}
	
	public float getDistance() {
    	distance=new float[average.sampleSize()];
		distance.fetchSample(distanceSensor, 0);
		return distance;

	}



	
	
	
	
	
	
	
	
	
	
	
}

