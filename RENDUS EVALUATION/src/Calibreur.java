import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.Color;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
/**
 * Classe qui contient son propre main. Elle sera ex�cut�e avant la comp�tition pour
 * permettre � notre capteur de couleur de se calibrer sur les couleurs du plateau "le jour J".
 * Les donn�es sont des sample (des �chantillons, ou des vecteurs) qui contiennent chacun trois
 * valeurs flottantes : la quantit� de R, G, et B. On d�termine ainsi toutes les couleurs que le
 * robot est cens� pouvoir �tre capable en jeu. Il y en a 6 : bleu, rouge, vert, jaune, blanc, noir.
 * Le capteur de couleur renvoyant des quantit�s de RGB assez "peu probables" (ex : 48/47/16 pour du blanc,
 * l� o� on attend 255/255/255), on utilise ma m�thode des scalaires, qui consiste � d�terminer la couleur
 * actuellement per�ue comme �tant celle qui est la "plus proche" d'une des couleurs de la liste. Pour cela
 * on calcule le scalaire entre la couleur per�ue et chacune des couleurs de la liste, et on dit que la couleur
 * renvoy�e est celle pour qui son scalaire �tait le plus petit avec la couleur per�ue.
 * 
 * <b> D�pendance : les m�thodes de cette classe ne sont utilis�es que dans <code>Calibreur</code> hormis 
 * getNearestSample qui est une m�thode statique utilis�e dans <code>Perception</code>.</b>
 * 
 * @author GATTACIECCA Bastien
 * @author DESCOTILS Juliette
 * @author LATIFI Arita
 * @author mig
 *
 */
public class Calibreur {
	/**
	 * Le capteur de couleur.
	 */
	private final EV3ColorSensor colorSensor;
	/**
	 * Le provider qui cr�� l'�chantillon.
	 */
	private final SampleProvider average;
	/**
	 * La liste de Sample (Sample �tant un objet que nous avons cr��) 
	 * des couleurs principales du plateau.
	 */
	private final LinkedList<Sample> sampleList;
	/**
	 * Construit un Calibreur.
	 * @throws IOException Peut lever une IOException vu qu'on �crit dans un fichier.
	 */
	public Calibreur() throws IOException {
		GraphicsLCD g = LocalEV3.get().getGraphicsLCD();
		Port p = LocalEV3.get().getPort("S2");
		/*
		 * Message de pr�sentation.
		 */
		goMessage(g);
		
		g.drawString(" --Creation des echantillons-- ", 0, 10, 0);
		colorSensor = new EV3ColorSensor(p);
		colorSensor.setFloodlight(Color.WHITE);
		average = new MeanFilter(colorSensor.getRGBMode(), 1);
		sampleList = new LinkedList<Sample>();
		/*
		 * Ou ouvre un fichier dans la brick qu'on nomme sample.txt
		 */
		BufferedWriter writer = new BufferedWriter(new FileWriter("sample.txt"));
		/*
		 * On parcourt le nombre de couleurs principales.
		 */
		for (int i = 0; i < Perception.COLORS.length; i++) {
			/*
			 * On nettoie l'affichage.
			 */
			System.out.println("\n\n\n\n\n");
			/*
			 * On cr�� un sample vide avec le nom de la couleur qu'on souhaite calibrer.
			 */
			Sample s = new Sample((MeanFilter)average,Perception.COLORS[i]);
			sampleList.add(s);
			s.calibrateColor();
			float[]f = s.getEchantillon();
			/*
			 * On �crit dans le fichier les 3 valeurs s�par�es par un "/".
			 */
			writer.write(f[0]+"/"+f[1]+"/"+f[2]);
			if (i < Perception.COLORS.length - 1) writer.newLine(); //condition pour ne pas faire une nouvelle ligne apr�s la derni�re ligne de valeurs.
		}
		writer.close();
		g.clear();
//		--- CODE DE TEST POUR LE CALIBREUR ---
//		//Permet de tester quelques couleurs apr�s l'op�ration de calibrage pour
//		//savoir si celui-ci a �t� fait correctement.
//		while (true) {
//			System.out.println("\n\n\n\n\n");
//			Sample sample = new Sample((MeanFilter)average,"une couleur");
//			sample.calibrateColor();
//
//			String nearestColor = Calibreur.getNearestSample(sampleList,sample).getName();
//
//			System.out.println("\n\n\n\n\n"
//					+ "La couleur est \n"
//					+ "du "+nearestColor+"\n"
//					+ "-----------------\n"
//					+ "CONTINUER ?");
//			int id = Button.waitForAnyPress();
//			if(id == Button.ID_ESCAPE) {
//				colorSensor.setFloodlight(false);
//				break;
//			}
//			g.clear();
//		}
	}
	/**
	 * Construis le message de pr�sentation.
	 * Honnetement �a sert � rien mais c'est sympa :)
	 * @param g le contexte graphique de l'�cran du robot.
	 */
	private void goMessage(GraphicsLCD g) {
		g.clear();
		g.drawString("Calibrage du", 5, 0, 0);
		g.drawString("ColorSensor", 5, 15, 0);
		g.setFont(Font.getSmallFont());

		g.drawString("Verifier que le capteur", 2, 40, 0);
		g.drawString("de couleur est bien branche", 2, 50, 0);
		g.drawString("sur le Port S2.", 2, 60, 0);
		g.drawString("Appuyer sur entree pour", 2, 70, 0);
		g.drawString("demarrer le calibrage.", 2, 80, 0);
		g.drawString("Appuyer sur Echap pour quitter", 2, 90, 0); 

		g.setFont(Font.getSmallFont());
		g.drawString("Let's go", 9, 115, 0);
		g.drawRect(2, 105, 65, 22);

		int id = Button.waitForAnyPress();
		if(id == Button.ID_ESCAPE) {
			System.exit(0);
		}
		g.clear();
	}
	/**
	 * Cette m�thode renvoie le sample pr�sent dans la collection des sample principaux dont
	 * le scalaire est le plus petit avec le sample en deuxi�me param�tre de la fonction.
	 * Ainsi, on passe en premier param�tre la liste de nos couleurs principales et en deuxi�me
	 * param�tre le sample de la couleur actuellement per�ue.
	 * @param c La liste de nos couleurs principales.
	 * @param echantillon Le sample de la couleur actuellement per�ue.
	 * @return le sample de la collection 'c' dont le scalaire est le plus petit avec le Sample 'echantillon'
	 */
	public static Sample getNearestSample(Collection<Sample> c, Sample echantillon) {
		/*
		 * On cherche un minimum donc on initialise au maximum.
		 */
		double minScal = Double.MAX_VALUE;
		Sample nearestSample = null;
		for (Sample s : c) {
			double scal = Calibreur.scalaire(s.getEchantillon(),echantillon.getEchantillon());
			if (scal < minScal) {
				minScal = scal;
				nearestSample = s;
			}
		}
		return nearestSample;
	}
	/**
	 * Calcule le scalaire de deux vecteurs de m�me tailles.
	 * @param v1 un scalaire de taille n.
	 * @param v2 un scalaire de taille n.
	 * @return le scalaire de v1 et de v2.
	 */
	public static double scalaire(float[] v1, float[] v2) {
		if (v1 == null || v2 == null) throw new IllegalArgumentException("L'un des echantillons est 'null'.");
		if (v1.length != v2.length) throw new IllegalArgumentException("Les deux echantillons doivent contenir le m�me nombre de couleurs.");
		double pow = 0;
		for (int i = 0; i < v1.length; i++) {
			pow += Math.pow(v1[i] - v2[i], 2.0d);
		}
		return Math.sqrt(pow);
	}
	public static void main(String[] args) throws IOException {
		new Calibreur();
	}
}
