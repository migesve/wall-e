import lejos.hardware.Button;
import lejos.robotics.filter.MeanFilter;
/**
 * Un Sample peut être traduit par échantillon ou vecteur. Il s'agit des trois quantités de R G B.
 * Un Sample utilise un MeanFilter pour moyenner le Sample perçu avec N précédents. On définit un
 * nom pour un sample, c'est important (pour les couleurs principales) d'avoir un nom type String 
 * pour pouvoir les différencier ou les comparer entre eux facilement ensuite.
 * @author nous <3
 */
public class Sample {
	/**
	 * Notre provider.
	 */
	private MeanFilter average;
	/**
	 * Le nom attribué à la couleur.
	 */
	private final String colorName;
	/**
	 * Notre vecteur.
	 */
	private float[] sample;
	
	public Sample(MeanFilter average, String name) {
		this(average,name, new float[average.sampleSize()]);
	}
	public Sample(MeanFilter average, String name, float[] sample) {
		this.average = average;
		colorName = name;
		this.sample = sample;
	}
	/**
	 * On utilisera cette méthode lors du calibrage des sample des couleurs principales.
	 * La méthode waitForPress est bloquante, et nous permettra de déplacer le capteur de
	 * couleur au dessus de celle qu'on souhaite calibrer.
	 * Puis ensuite on détecte la couleur.
	 */
	public void calibrateColor() {
		System.out.println("ENTRER pour");
		System.out.println("detecter "+colorName+"..");
		Button.ENTER.waitForPress();
		detectColor();
	}
	/**
	 * Détecte une couleur, permet de construire le sample à partir du sample provider.
	 */
	public void detectColor() {
		average.fetchSample(sample, 0);
	}
	
	//Accesseurs publics aux attributs d'instance.
	public float[] getEchantillon() {
		return sample;
	}
	public String getName() {
		return colorName;
	}
}
