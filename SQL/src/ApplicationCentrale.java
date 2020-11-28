/*
 * @author Alban Bekteshi
 * @author Adrien de Decker
 */

import java.util.Scanner;
import java.sql.*;	

public class ApplicationCentrale {
	
	private String url = "jdbc:postgresql://localhost:5432/ProjetSQL?user=postgres&password=9797";
	private Connection conn = null;
	private PreparedStatement ajouterLocal;
	private final static Scanner sc = new Scanner(System.in);
	
	
	public ApplicationCentrale() {
		this.conn= this.initConnection();
		
		
		
		
		//Test d'un insert OK
//		try {
//			Statement s = conn.createStatement();
//			
//			s.executeUpdate("INSERT INTO projet.utilisateurs VALUES (DEFAULT,'test','test','test',1);");
//		} catch(SQLException se) {
//			System.out.println("Erreur insertion");
//			se.printStackTrace();
//			System.exit(1);
//		}
		
		
		// Test d'un Select
//		try {
//			Statement s = conn.createStatement();
//			try(ResultSet rs = s.executeQuery("SELECT nom_utilisateur "
//					+ "FROM projet.utilisateurs;")){
//				while(rs.next()) {
//					System.out.println(rs.getString(1));
//				}
//			}
//		} catch (SQLException se) {
//			se.printStackTrace();
//			System.exit(1);
//		}
		
			
	}
	/*
	 * @return Connection connexion au serveur si aucune erreur
	 */
	private Connection initConnection() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
		System.out.println("Driver PostgreSQL manquant !");
		System.exit(1);
		}
		String url = "jdbc:postgresql://localhost:5432/ProjetSQL";
		Connection conn=null;
		try {
			conn=DriverManager.getConnection(url,"postgres","9797");
		} catch (SQLException e) {
			System.out.println("Impossible de joindre le server!");
			System.exit(1);
		}
		try {
			ajouterLocal = conn.prepareStatement("SELECT * FROM projet.ajouterLocal(?,?,?);");
		} catch (SQLException e) {
			System.out.println("Erreur lors de la preparation des statement");
			System.exit(1);
		}
		//System.out.println("Connexion au serveur reussi !");
		return conn;
	}
	
	
	public static void main(String args[]) {
		ApplicationCentrale app = new ApplicationCentrale();
		System.out.println("Bienvenue sur l'application centrale !");
		System.out.println("--------------------------------------\n");
		
		int action = app.choixActionMenu();
		
		switch (action) {
		case 1:
			System.out.println("Ajouter Local\n");
			app.ajouterLocal();
			break;

		default:
			System.out.println("Erreur : Aucune action trouvee pour action "+action+"\n");
			break;
		}
	}
	
	
	
	
	
	/*
	 * @return int numéro de l'action que user souhaite executer
	 */
	private int choixActionMenu() {
		System.out.println("Entrez l'action que vous voulez executer :\n");
		System.out.println("1: Ajouter local");
		
		int action = 0;
		
		do {			
			action =sc.nextInt();
		} while(action<=0 || action >8);

		return action;
	}
	
	
	
	
	private void ajouterLocal() {
		System.out.println("Entrez le nom du local ");
		String nomLocal = sc.next();
		System.out.println("Entrez le nombre de place dans le local");
		int quantitePlace = sc.nextInt();
		System.out.println("Le local dispose-t-il de machines ? \nOui => o\nNon=> n");
		char machineDispo = sc.next().charAt(0);
		try {
			ajouterLocal.setString(1, nomLocal);
			ajouterLocal.setInt(2, quantitePlace);
			ajouterLocal.setString(3, String.valueOf(machineDispo));
			ajouterLocal.executeQuery();
		} catch (SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
	}
}

