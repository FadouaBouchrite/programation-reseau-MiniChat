import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.*;

public class Serveur {




    public void envoyerMessage() throws SocketException {

        final int PORT = 10000;
        final int BUFFER_SIZE = 10000;

        DatagramSocket socket = new DatagramSocket(PORT);

        try {
            System.out.println("Serveur en attente de messages...");

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            socket.receive(packet);

            System.out.println("Message reçu du client.");

            String messageString = new String(packet.getData(), 0, packet.getLength());

            JSONObject message = new JSONObject(messageString);

            String identificateur = message.getString("identificateur");
            String id = message.getString("id");
            String contenu = message.getString("contenu");
            String ipAddress = packet.getAddress().getHostAddress();
            int port = packet.getPort();

            // Afficher les champs extraits
            System.out.println("Identificateur: " + identificateur);
            System.out.println("Contenu: " + contenu);
            System.out.println("Adresse IP du client: " + ipAddress);
            System.out.println("Port du client: " + port);

            // Insérer les données dans la base de données
            if (id.equals("2")) {
                connecter(identificateur, contenu);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    private void connecter(String identificateur, String contenu) {
        final String URL = "jdbc:mysql://localhost:3306/chat";
        final String UTILISATEUR = "root";
        final String MOT_DE_PASSE = "";

        try {
            Class.forName("com.mysql.jdbc.Driver");

            try (Connection con = DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE)) {
                String sql = "SELECT port, ip_adress FROM inscription WHERE identificateur = ?";

                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, identificateur);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int port = rs.getInt("port");
                            String adresseIP = rs.getString("ip_adress");

                            try (DatagramSocket socket = new DatagramSocket()) {
                                InetAddress adresseDest = InetAddress.getByName(adresseIP);
                                byte[] data = contenu.getBytes();

                                DatagramPacket packet = new DatagramPacket(data, data.length, adresseDest, port);
                                socket.send(packet);
                                System.out.println("Message envoyé à " + adresseIP + ":" + port);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void recevoir() throws SocketException {
        final int PORT = 10000;
        final int BUFFER_SIZE = 10000;

        DatagramSocket socket = new DatagramSocket(PORT);

        try {


            System.out.println("Serveur en attente de messages...");



            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            System.out.println("Message reçu du client.");

            String messageString = new String(packet.getData(), 0, packet.getLength());
            JSONObject message = new JSONObject(messageString);
            String identificateur = message.getString("identificateur");
            String id = message.getString("id");
            String ipAddress = packet.getAddress().getHostAddress();
            int port = packet.getPort();

            // Afficher les champs extraits
            System.out.println("Identificateur: " + identificateur);
            System.out.println("Adresse IP du client: " + ipAddress);
            System.out.println("Port du client: " + port);

            // Insérer les données dans la base de données
            if (id.equals("inscription")) {
                insererDansBaseDeDonnees(identificateur, ipAddress, port);
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    public static void insererDansBaseDeDonnees(String identificateur, String ipAddress, int port) {
        final String URL = "jdbc:mysql://localhost:3306/chat";
        final String UTILISATEUR = "root";
        final String MOT_DE_PASSE = "";

        try {
            Class.forName("com.mysql.jdbc.Driver");

            try (Connection con = DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE)) {
                String sql = "INSERT INTO inscription (identificateur, ip_adress, port) VALUES (?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, identificateur);
                    ps.setString(2, ipAddress);
                    ps.setInt(3, port);

                    ps.executeUpdate();
                    System.out.println("Données insérées dans la base de données avec succès !");
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}