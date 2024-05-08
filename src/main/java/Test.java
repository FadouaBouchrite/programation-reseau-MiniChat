import java.net.SocketException;

public class Test {
    public static void main(String[] args) {

        Serveur serveur = new Serveur();
        Thread threadServeur = new Thread(() -> {
            try {
                serveur.envoyerMessage();

            } catch (SocketException e) {
                e.printStackTrace();
            }
        });
        threadServeur.start();


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


   /*     Client client = new Client();
        client.connecter("amani el aaly");
        client.envoyerUnMessage("amal qabani", "hello");
        ClientRecepteur clientRecepteur = new ClientRecepteur();
        clientRecepteur.recevoirMessage();
       */
        Client client=new Client();
        client.ajouterUnAmi("fadoua Bouchrite","amal qabani");
    }
}
