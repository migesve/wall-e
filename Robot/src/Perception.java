import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.utility.Delay;

/**
 * Classe qui gère les trois capteurs du robot. Le capteur tactile, qui renvoie true ou false
 * selon que le balancier appuie dessus ou non. Le capteur de couleur qui détermine quelle couleur
 * parmi une liste de couleurs définies est la plus proche de celle que le capteur perçoit ; on utilise
 * pour cela la méthode des scalaires précisée plus bas. Le capteur à ultra-son qui renvoie une distance.
 * @author nous <3
 */
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

	/**
	 * Les couleurs principales que le robot doit être capable de discriminer.
	 */
	public final static String[]COLORS = {"blue","red","green","grey","yellow","black","white"};
	/**
	 * Indique si le string en paramètre est une couleur du plateau ou non.
	 * @param c Un string censé représenté une couleur parmi la liste ci-dessus.
	 * @return true si c est contenue dans le tableau des couleurs principales, false sinon.
	 */
	public static boolean isAColor(String c) {
		if (c == null) throw new NullPointerException("String color is null");
		for (String ch : COLORS) {
			if (ch.equals(c)) return true;
		}
		return false;
	}
	/**
	 * Liste des sample des couleurs définies juste au-dessus.
	 */
	private final LinkedList<Sample> sampleList;
	/**
	 * Sample du capteur tactile.
	 */
	private float [] touchSample;
	/**
	 * Sample du capteur à ultra-son.
	 */
	private float [] distanceSample;
	/**
	 * attributs publics pour faciliter l'accès depuis la classe <code>Agent</code>.
	 * volatile = accesseur en lecture/écriture désynchronisé
	 */
	public volatile String color;
	public volatile float distance;
	public volatile boolean touch;

	public Perception (Port touche, Port couleur, Port ultra) { //Port IRSensor
		capteurTouche = new EV3TouchSensor(touche);
		capteurCouleur = new EV3ColorSensor(couleur);
		capteurDistance= new EV3UltrasonicSensor(ultra);

		/*
		 * On définit les modes dans le constructeur puisqu'on les changera jamais.
		 */
		capteurTouche.setCurrentMode(0);
		capteurCouleur.setCurrentMode(2);
		capteurDistance.setCurrentMode(0);
		/*
		 * Les SampleProvider qui créent les sample pour chacun des capteurs.
		 * On utilise un MeanFilter pour le capteur de couleur qui permet de moyenner les échantillons de couleur.
		 */
		colorProvider = new MeanFilter(capteurCouleur.getRGBMode(), 1);
		touchProvider = capteurTouche.getTouchMode();
		distanceProvider = capteurDistance.getDistanceMode();
		
		/*
		 * Création des échantillons une première fois dans le constructeur histoire de pas se
		 * prendre une NULLPOINTEREXCPETIONNN.
		 */
		sampleList = new LinkedList<Sample>();
		try {
			setCalibratedSamples();
		}catch(IOException io) {
			System.out.println("Erreur lors de");
			System.out.println("l'ouverture du fichier");
			Delay.msDelay(10000);
		}
		touchSample = new float [touchProvider.sampleSize()];
		distanceSample = new float [distanceProvider.sampleSize()];

		/*
		 * Pareil ici, on pourrait ne pas les initialiser, mais on reste prudent :)
		 */
		color = getCouleur();
		distance = getDistance();
		touch = getTouche();
	}
	/**
	 * Méthode update() appelée toutes les <code>MS_DELAY</code> depuis la classe <code>Agent</code>.
	 * On met ainsi à jour les données de nos capteurs régulièrement via un deuxième Thread.
	 */
	public void update() {
		color = getCouleur();
		distance = getDistance();
		touch = getTouche();
	}
	/**
	 * Récupère les sample des couleurs principales que le robot est censé discriminer depuis le fichier
	 * qui a été créé lors du calibrage, et les ajoute dans la LinkedList. On construit donc ici 6 samples 
	 * des 6 couleurs principales que l'on récupère depuis un fichier puis qu'on ajoute dans la liste.
	 * @throws IOException si une IOException est levée.
	 */
	public void setCalibratedSamples() throws IOException {
		/*
		 * Le fichier source créé par le calibrage se nomme 'sample.txt', on utilise un BufferedReader pour le lire.
		 */
		BufferedReader br = new BufferedReader(new FileReader("sample.txt"));
		/*
		 * Le String 'line' va prendre la valeur de la nouvelle ligne comme ceci (exemple avec le bleu) :
		 * line = "0.026470589/0.032352943/0.024509804"
		 */
		String line = br.readLine(); int lineId = 0;
		while (line != null) {
			/*
			 * on créé donc un tableau de String dont chaque case contient la valeur de R, G et B :
			 * stringValues = {"0.026470589" , "0.032352943" , "0.024509804"}
			 */
			String[]stringValues = line.split("/");
			int length = stringValues.length;
			/*
			 * On fait de même mais dans un tableau de float qui contient des valeurs numériques en 
			 * parsant les String
			 */
			float[]floatValues = new float[length];
			for (int i = 0; i < length; i++) {
				floatValues[i] = Float.parseFloat(stringValues[i]);
			}
			/*
			 * On ajoute dans notre liste de sample le sample de la couleur bleue
			 */
			sampleList.add(new Sample((MeanFilter)colorProvider,COLORS[lineId],floatValues));
			/*
			 * Puis on incrémente la ligne et l'index.
			 */
			line = br.readLine(); lineId ++;
		}
		br.close();
	}
	/**
	 * Retourne la couleur parmi la liste des couleurs définies comme étant discernables par le robot.
	 * @return un String qui correspond à la couleur en question (en anglais et en minuscule, ex : "blue").
	 */
	public String getCouleur() {
		/*
		 * On créé un nouveau sample (vide)
		 */
		Sample sample = new Sample((MeanFilter)colorProvider,"une couleur");
		/*
		 * On le construit avec le fetchSample du colorProvider.
		 */
		sample.detectColor();
		/*
		 * Puis on retourne la couleur tel que le scalaire de son sample et celui du sample créé est le plus petit.
		 */
		return Calibreur.getNearestSample(sampleList,sample).getName();
	}
	/**
	 * Retourne l'état du capteur tactile, s'il est appuyé ou non.
	 * @return true ou false selon que le capteur tactile est appuyé ou non.
	 */
	public boolean getTouche() {
		touchProvider.fetchSample(touchSample, 0);
		return touchSample[0] == 1;
	}
	/**
	 * Renvoie la distance perçue par le capteur à ultra-son en centimètres.
	 * @return la distance perçue en cm.
	 */
	public float getDistance() {
		distanceProvider.fetchSample(distanceSample, 0);
		float dist = 100*distanceSample[0];
		if (Float.isNaN(dist)) { //NaN = Not A Number
			return Float.POSITIVE_INFINITY;
		}else {
			return dist;
		}
	}
	/**
	 * Notre stratégie consiste à ne pas utiliser ce mode du capteur à ultrason :)
	 * Tout simplement car si nos adversaires entamment déjà une manoeuvre pour nous esquiver,
	 * alors on aura pas à la faire. C'est tout de même beaucoup de temps de perdu. Notre
	 * stratégie consiste également à tourner toujours à l'opposé de où est le robot adverse pour éviter
	 * au plus les collisions.
	 */
	//	public boolean getIR() {  //true = il y a qqln
	//		capteurDistance.setCurrentMode(1);
	//		irProvider = capteurDistance.getListenMode();
	//		irSample = new float [irProvider.sampleSize()];
	//		irProvider.fetchSample(irSample, 0);
	//		return irSample[0]==1 ? true : false;
	//	}
}