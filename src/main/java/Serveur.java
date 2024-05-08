import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.sql.*;

public class Serveur {

    public static void main(String[] args) {
        Serveur serveur = new Serveur();
        try {
            serveur.envoyerMessage();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

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

            String id = message.getString("id");

            String ipAddress = packet.getAddress().getHostAddress();
            int port = packet.getPort();

            System.out.println("Adresse IP du client: " + ipAddress);
            System.out.println("Port du client: " + port);

            switch (id) {
                case "1":
                    String identificateur = message.getString("identificateur");
                    insererDansBaseDeDonnees(identificateur, ipAddress, Integer.toString(port));
                    break;
                case "2":
                    String identificateur1 = message.getString("identificateur");
                    String contenu = message.getString("contenu");
                    connecter(identificateur1, contenu, Integer.toString(port), ipAddress);
                    break;
                case "3":
                    String identificateur2 = message.getString("identificateur");
                    String ami = message.getString("ami");
                    ajouterAmi(identificateur2, ami);
                    break;
                default:
                    System.out.println("ID non reconnu : " + id);
                    break;
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    public void ajouterAmi(String identificateur, String ami) {
        final String URL = "jdbc:mysql://localhost:3306/chat";
        final String UTILISATEUR = "root";
        final String MOT_DE_PASSE = "";
        try {

            Class.forName("com.mysql.jdbc.Driver");

            try (Connection con = DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE)) {
                // Insérer les valeurs dans la table "ami"

                String sql = "INSERT INTO ami (utilisateur, ami) VALUES (?, ?)";
                try (PreparedStatement psInsert = con.prepareStatement(sql)) {
                    psInsert.setString(1, identificateur);
                    psInsert.setString(2, ami);
                    psInsert.executeUpdate();

                    System.out.println("Données insérées dans la table ami avec succès !");
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }


    private void connecter(String identificateur, String contenu, String port, String ipAddress) {
        final String URL = "jdbc:mysql://localhost:3306/chat";
        final String UTILISATEUR = "root";
        final String MOT_DE_PASSE = "";

        try {
            Class.forName("com.mysql.jdbc.Driver");

            try (Connection con = DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE)) {
                String selectSql = "SELECT COUNT(*) AS count FROM inscription WHERE identificateur = ?";
                String selectUserDataSql = "SELECT port, ip_adress FROM inscription WHERE identificateur = ?";

                try (PreparedStatement psCount = con.prepareStatement(selectSql);
                     PreparedStatement psUserData = con.prepareStatement(selectUserDataSql)) {

                    // Vérifier si l'identificateur existe dans la base de données
                    psCount.setString(1, identificateur);
                    ResultSet resultSet = psCount.executeQuery();

                    if (resultSet.next() && resultSet.getInt("count") > 0) {
                        // L'identificateur existe, récupérer les données correspondantes
                        psUserData.setString(1, identificateur);
                        ResultSet rs = psUserData.executeQuery();

                        while (rs.next()) {
                            int portNumber = rs.getInt("port");
                            String adresseIP = rs.getString("ip_adress");

                            try (DatagramSocket socket = new DatagramSocket()) {
                                InetAddress adresseDest = InetAddress.getByName(adresseIP);
                                byte[] data = contenu.getBytes();

                                DatagramPacket packet = new DatagramPacket(data, data.length, adresseDest, portNumber);
                                socket.send(packet);
                                System.out.println("Message envoyé à " + adresseIP + ":" + portNumber);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {

                        try (DatagramSocket socket = new DatagramSocket()) {
                            InetAddress adresseDest = InetAddress.getByName(ipAddress);
                            String message = "L'identificateur n'existe pas dans la base de données.";
                            byte[] data = message.getBytes();

                            DatagramPacket packet = new DatagramPacket(data, data.length, adresseDest, Integer.parseInt(port));
                            socket.send(packet);
                            System.out.println("Message envoyé à " + ipAddress + ":" + port);
                        } catch (SocketException | UnknownHostException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void insererDansBaseDeDonnees(String identificateur, String ipAddress, String port) {
        final String URL = "jdbc:mysql://localhost:3306/chat";
        final String UTILISATEUR = "root";
        final String MOT_DE_PASSE = "";

        try {
            Class.forName("com.mysql.jdbc.Driver");

            try (Connection con = DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE)) {
                String selectSql = "SELECT COUNT(*) AS count FROM inscription WHERE identificateur = ?";
                String insertSql = "INSERT INTO inscription (identificateur, ip_adress, port) VALUES (?, ?, ?)";

                try (PreparedStatement psSelect = con.prepareStatement(selectSql);
                     PreparedStatement psInsert = con.prepareStatement(insertSql)) {

                    // Vérifier si l'utilisateur existe déjà dans la base de données
                    psSelect.setString(1, identificateur);
                    ResultSet resultSet = psSelect.executeQuery();

                    if (resultSet.next() && resultSet.getInt("count") > 0) {
                        try (DatagramSocket socket = new DatagramSocket()) {
                            InetAddress adresseDest = InetAddress.getByName(ipAddress);
                            String message = "Ce utilisateur existe déjà";
                            byte[] data = message.getBytes();

                            DatagramPacket packet = new DatagramPacket(data, data.length, adresseDest, Integer.parseInt(port));
                            socket.send(packet);
                            System.out.println("Message envoyé à " + ipAddress + ":" + port);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Insérer les données dans la table "inscription"
                        psInsert.setString(1, identificateur);
                        psInsert.setString(2, ipAddress);
                        psInsert.setInt(3, Integer.parseInt(port));
                        psInsert.executeUpdate();
                        System.out.println("Données insérées dans la base de données avec succès !");
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
