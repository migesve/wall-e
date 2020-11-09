import lejos.hardware.ev3.LocalEV3;
//test ari

public class Exe {

    public static void main(String[] args) {
        Actionneur a = new Actionneur(
                LocalEV3.get().getPort("A"),
                LocalEV3.get().getPort("B"),
                LocalEV3.get().getPort("C"));
        //a.fermerPinces();
        //a.ouvrirPinces();
        a.rotation(360);
    }
}