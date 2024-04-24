import org.json.JSONObject;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    public void connecter(String nom) {
        final String SERVEUR_IP = "127.0.0.1";
        final int SERVEUR_PORT = 10000;

        try (DatagramSocket socket = new DatagramSocket()) {
            // Créer un objet JSON avec l'identificateur seulement
            JSONObject message = new JSONObject();
            message.put("identificateur", nom);
            message.put("id", "inscription");

            // Convertir le message JSON en tableau de bytes
            byte[] data = message.toString().getBytes();

            // Créer un datagramme contenant le message, l'adresse IP et le port du serveur
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(SERVEUR_IP), SERVEUR_PORT);

            // Envoyer le datagramme au serveur
            socket.send(packet);
            System.out.println("Message envoyé au serveur.");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void envoyerUnMessage(String identificateur ,String contenu){

        final String SERVEUR_IP = "127.0.0.1";
        final int SERVEUR_PORT = 10000;

        try (DatagramSocket socket = new DatagramSocket()) {
            // Créer un objet JSON avec l'identificateur seulement
            JSONObject message = new JSONObject();
            message.put("identificateur", identificateur);
            message.put("contenu",contenu);
            message.put("id", "2");

            // Convertir le message JSON en tableau de bytes
            byte[] data = message.toString().getBytes();

            // Créer un datagramme contenant le message, l'adresse IP et le port du serveur
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(SERVEUR_IP), SERVEUR_PORT);

            // Envoyer le datagramme au serveur
            socket.send(packet);
            System.out.println("Message envoyé au serveur.");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}