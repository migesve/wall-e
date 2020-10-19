package src;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;

public class test extends Perception{
	
	public test(Port couleur, Port ultra, Port touche) {
		super(couleur, ultra, touche);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		Port couleur=LocalEV3.get().getPort("S2");
		Port ultra=LocalEV3.get().getPort("S3");
		Port touche=LocalEV3.get().getPort("S1");
		//Port IR=LocalEV3.get().getPort("S3");
		
		Perception cedric = new Perception (couleur, ultra, touche,ultra);
		
		cedric.getTouche();
		
	}

}
