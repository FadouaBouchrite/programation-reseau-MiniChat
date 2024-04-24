import java.net.SocketException;

public class Test {
    public static void main(String[] args) {
        // Démarrer le serveur dans un thread séparé
        Serveur serveur = new Serveur();
        Thread threadServeur = new Thread(() -> {
            try {
                serveur.envoyerMessage();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        });
        threadServeur.start();

        // Laisser un court délai pour que le serveur démarre
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Créer et connecter le client
        Client client = new Client();
        client.envoyerUnMessage("amal qabani","hello");
        ClientRecepteur clientRecepteur=new ClientRecepteur();
        clientRecepteur.recevoirMessage();
    }
}