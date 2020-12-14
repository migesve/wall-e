import lejos.hardware.Button;
import lejos.robotics.filter.MeanFilter;
/**
 * Un Sample peut �tre traduit par �chantillon ou vecteur. Il s'agit des trois quantit�s de R G B.
 * Un Sample utilise un MeanFilter pour moyenner le Sample per�u avec N pr�c�dents. On d�finit un
 * nom pour un sample, c'est important (pour les couleurs principales) d'avoir un nom type String 
 * pour pouvoir les diff�rencier ou les comparer entre eux facilement ensuite.
 * 
 * <b> D�pendance : les m�thodes de cette classe sont utilis�es dans <code>Calibreur</code> pour
 * g�n�rer les <code>Sample</code> des couleurs de base, et dans <code>Perception</code> pour
 * g�n�rer les sample de la couleur per�ue par le capteur de couleur. </b>
 * 
 * @author GATTACIECCA Bastien
 * @author DESCOTILS Juliette
 * @author LATIFI Arita
 * @author mig
 */
public class Sample {
	/**
	 * Notre provider.
	 */
	private MeanFilter average;
	/**
	 * Le nom attribu� � la couleur.
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
	 * On utilisera cette m�thode lors du calibrage des sample des couleurs principales.
	 * La m�thode waitForPress est bloquante, et nous permettra de d�placer le capteur de
	 * couleur au dessus de celle qu'on souhaite calibrer.
	 * Puis ensuite on d�tecte la couleur.
	 */
	public void calibrateColor() {
		System.out.println("ENTRER pour");
		System.out.println("detecter "+colorName+"..");
		Button.ENTER.waitForPress();
		detectColor();
	}
	/**
	 * D�tecte une couleur, permet de construire le sample � partir du sample provider.
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
