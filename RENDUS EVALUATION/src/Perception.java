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
 * Classe qui g�re les trois capteurs du robot. Le capteur tactile, qui renvoie true ou false
 * selon que le balancier appuie dessus ou non. Le capteur de couleur qui d�termine quelle couleur
 * parmi une liste de couleurs d�finies est la plus proche de celle que le capteur per�oit ; on utilise
 * pour cela la m�thode des scalaires pr�cis�e plus bas. Le capteur � ultra-son qui renvoie une distance.
 * 
 * <b> D�pendance : les m�thodes de cette classe sont utilis�es dans <code>Agent</code> uniquement. </b>
 * 
 * @author GATTACIECCA Bastien
 * @author DESCOTILS Juliette
 * @author LATIFI Arita
 * @author mig
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
	 * Les couleurs principales que le robot doit �tre capable de discriminer.
	 */
	public final static String[]COLORS = {"blue","red","green","grey","yellow","black","white"};
	/**
	 * Indique si le string en param�tre est une couleur du plateau ou non.
	 * @param c Un string cens� repr�sent� une couleur parmi la liste ci-dessus.
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
	 * Liste des sample des couleurs d�finies juste au-dessus.
	 */
	private final LinkedList<Sample> sampleList;
	/**
	 * Sample du capteur tactile.
	 */
	private float [] touchSample;
	/**
	 * Sample du capteur � ultra-son.
	 */
	private float [] distanceSample;
	/**
	 * attributs publics pour faciliter l'acc�s depuis la classe <code>Agent</code>.
	 * volatile = accesseur en lecture/�criture d�synchronis�
	 */
	public volatile String color;
	public volatile float distance;
	public volatile boolean touch;

	public Perception (Port touche, Port couleur, Port ultra) { //Port IRSensor
		capteurTouche = new EV3TouchSensor(touche);
		capteurCouleur = new EV3ColorSensor(couleur);
		capteurDistance= new EV3UltrasonicSensor(ultra);

		/*
		 * On d�finit les modes dans le constructeur puisqu'on les changera jamais.
		 */
		capteurTouche.setCurrentMode(0);
		capteurCouleur.setCurrentMode(2);
		capteurDistance.setCurrentMode(0);
		/*
		 * Les SampleProvider qui cr�ent les sample pour chacun des capteurs.
		 * On utilise un MeanFilter pour le capteur de couleur qui permet de moyenner les �chantillons de couleur.
		 */
		colorProvider = new MeanFilter(capteurCouleur.getRGBMode(), 1);
		touchProvider = capteurTouche.getTouchMode();
		distanceProvider = capteurDistance.getDistanceMode();
		
		/*
		 * Cr�ation des �chantillons une premi�re fois dans le constructeur histoire de pas se
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
	 * M�thode update() appel�e toutes les <code>MS_DELAY</code> depuis la classe <code>Agent</code>.
	 * On met ainsi � jour les donn�es de nos capteurs r�guli�rement via un deuxi�me Thread.
	 */
	public void update() {
		color = getCouleur();
		distance = getDistance();
		touch = getTouche();
	}
	/**
	 * R�cup�re les sample des couleurs principales que le robot est cens� discriminer depuis le fichier
	 * qui a �t� cr�� lors du calibrage, et les ajoute dans la LinkedList. On construit donc ici 6 samples 
	 * des 6 couleurs principales que l'on r�cup�re depuis un fichier puis qu'on ajoute dans la liste.
	 * @throws IOException si une IOException est lev�e.
	 */
	public void setCalibratedSamples() throws IOException {
		/*
		 * Le fichier source cr�� par le calibrage se nomme 'sample.txt', on utilise un BufferedReader pour le lire.
		 */
		BufferedReader br = new BufferedReader(new FileReader("sample.txt"));
		/*
		 * Le String 'line' va prendre la valeur de la nouvelle ligne comme ceci (exemple avec le bleu) :
		 * line = "0.026470589/0.032352943/0.024509804"
		 */
		String line = br.readLine(); int lineId = 0;
		while (line != null) {
			/*
			 * on cr�� donc un tableau de String dont chaque case contient la valeur de R, G et B :
			 * stringValues = {"0.026470589" , "0.032352943" , "0.024509804"}
			 */
			String[]stringValues = line.split("/");
			int length = stringValues.length;
			/*
			 * On fait de m�me mais dans un tableau de float qui contient des valeurs num�riques en 
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
			 * Puis on incr�mente la ligne et l'index.
			 */
			line = br.readLine(); lineId ++;
		}
		br.close();
	}
	/**
	 * Retourne la couleur parmi la liste des couleurs d�finies comme �tant discernables par le robot.
	 * @return un String qui correspond � la couleur en question (en anglais et en minuscule, ex : "blue").
	 */
	public String getCouleur() {
		/*
		 * On cr�� un nouveau sample (vide)
		 */
		Sample sample = new Sample((MeanFilter)colorProvider,"une couleur");
		/*
		 * On le construit avec le fetchSample du colorProvider.
		 */
		sample.detectColor();
		/*
		 * Puis on retourne la couleur tel que le scalaire de son sample et celui du sample cr�� est le plus petit.
		 */
		return Calibreur.getNearestSample(sampleList,sample).getName();
	}
	/**
	 * Retourne l'�tat du capteur tactile, s'il est appuy� ou non.
	 * @return true ou false selon que le capteur tactile est appuy� ou non.
	 */
	public boolean getTouche() {
		touchProvider.fetchSample(touchSample, 0);
		return touchSample[0] == 1;
	}
	/**
	 * Renvoie la distance per�ue par le capteur � ultra-son en centim�tres.
	 * @return la distance per�ue en cm.
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
	 * Notre strat�gie consiste � ne pas utiliser ce mode du capteur � ultrason :)
	 * Tout simplement car si nos adversaires entamment d�j� une manoeuvre pour nous esquiver,
	 * alors on aura pas � la faire. C'est tout de m�me beaucoup de temps de perdu. Notre
	 * strat�gie consiste �galement � tourner toujours � l'oppos� de o� est le robot adverse pour �viter
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