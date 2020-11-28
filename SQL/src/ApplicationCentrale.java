/*
 * @author Alban Bekteshi
 * @author Adrien de Decker
 */

import java.util.Scanner;
import java.sql.*;	

public class ApplicationCentrale {
	
	private Connection conn;
	Scanner scanner = new Scanner(System.in);
	
	public ApplicationCentrale() {
		
		this.conn= this.initConnection();
		
		System.out.println("Bienvenue sur l'application centrale !");
		System.out.println("--------------------------------------\n");
		
		int action = choixActionMenu();
		
		switch (action) {
		case 1:
			System.out.println("Ajouter Local\n");
			this.ajouterLocalForm();
			break;

		default:
			System.out.println("Erreur : Aucune action trouvee pour action "+action+"\n");
			break;
		}
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
	
	
	public static void main(String args[]) {
		ApplicationCentrale app = new ApplicationCentrale();
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
		//System.out.println("Connexion au serveur reussi !");
		return conn;
	}
	
	
	/*
	 * @return int numéro de l'action que user souhaite executer
	 */
	private int choixActionMenu() {
		System.out.println("Entrez l'action que vous voulez executer :\n");
		System.out.println("1: Ajouter local");
		
		int action = 0;
		
		do {			
			action =scanner.nextInt();
		} while(action<=0 || action >8);

		return action;
	}
	
	
	private void ajouterLocalForm() {
		//max 10 char ex:('A025')
		String nomLocal="";
		
		int quantitePlace=0;
		
		// oui => 'o' || non => 'n'
		char machineDispo='a';
		
		System.out.println("Vous avez choisi d'ajouter un local\n");
		
		while(nomLocal.length()<=0 || nomLocal.length()>10) {
			System.out.println("Entrez le nom du local (max 10 char)");
			nomLocal = scanner.nextLine();
		}

		while(quantitePlace<=0) {
			System.out.println("Entrez le nombre de place dans le local");
			quantitePlace = scanner.nextInt();
		}
		
		while(machineDispo!='o' && machineDispo!='O' && machineDispo!='n' && machineDispo!='N') {
			System.out.println("Le local dispose-t-il de machines ? \nOui => o\nNon=> n");
			machineDispo = scanner.next().charAt(0);
		}
		System.out.println();
		System.out.println("nomLocal : "+nomLocal);
		System.out.println("quantitePlace : "+quantitePlace);
		System.out.println("machineDispo : "+machineDispo);
		
		
		
	}
}

