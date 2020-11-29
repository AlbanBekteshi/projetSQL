/*
 * @author Alban Bekteshi
 * @author Adrien de Decker
 */

import java.util.Scanner;
import java.sql.*;
import java.sql.Date;

public class ApplicationCentrale {
	
	private String url = "jdbc:postgresql://localhost:5432/ProjetSQL?user=postgres&password=9797";
	private Connection conn = null;
	private PreparedStatement ajouterLocal;
	private PreparedStatement ajouterExamen;
	private PreparedStatement ajouterLocauxExamens;
	private PreparedStatement ajouterDateExamen;
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
			ajouterExamen = conn.prepareStatement("SELECT * FROM projet.ajouterExamen(?,?,?,?,?);");
			ajouterLocauxExamens = conn.prepareStatement("SELECT * FROM projet.ajouterLocauxExamens(?,?);");
			ajouterDateExamen = conn.prepareStatement("SELECT * FROM projet.ajouterDateExamen(?,?);");
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
		int action;
		do {
			action = app.choixActionMenu();
			
			switch (action) {
			case 1:
				System.out.println("Ajouter Local\n");
				app.ajouterLocal();
				break;
			case 2:
				System.out.println("Ajouter Examen\n");
				app.ajouterExamen();
				break;
			case 3:
				System.out.println("Ajouter Locaux pour examen");
				app.ajouterLocauxExamens();
				break;
			case 4:
				System.out.println("Ajouter La Date a un examen");
				app.ajouterDateExamen();
				break;
				
			default:
				System.out.println("Erreur : Aucune action trouvee pour action "+action+"\n");
				System.exit(1);
				break;
			}
		}while(action >=1 && action <= 4);
		
	}
	
	
	

	/*
	 * @return int num�ro de l'action que user souhaite executer
	 */
	private int choixActionMenu() {
		System.out.println("Entrez l'action que vous voulez executer :\n");
		System.out.println("1: Ajouter local");
		System.out.println("2: Ajouter Examen");
		System.out.println("3: Ajouter un local pour un Examen");
		System.out.println("4: Ajouter la date a un Examen");
		
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
	
	private void ajouterExamen() {
		System.out.println("Entrez le Code Examen sous format 'IPL[0-9][0-9][0-9]'");
		String code = sc.next();
		System.out.println("Entrez le nom de l'examen");
		String nom = sc.next();
		System.out.println("Entrez le bloc");
		int bloc = sc.nextInt();
		System.out.println("Entrez la dur�e en minute");
		int duree = sc.nextInt();
		System.out.println("L'examen est-t-il sur machines ou �crit? \nMachine => m\nEcrit=> e");
		char type = sc.next().charAt(0);
		try {
			ajouterExamen.setString(1, code);
			ajouterExamen.setString(2, nom);
			ajouterExamen.setInt(3, bloc);
			ajouterExamen.setInt(4, duree);
			ajouterExamen.setString(5, String.valueOf(type));
			ajouterExamen.executeQuery();
		} catch (SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
	}
	
	private void ajouterLocauxExamens() {
		System.out.println("Entrez le nom du local ");
		String nomLocal = sc.next();
		System.out.println("Entrez le Code Examen sous format 'IPL[0-9][0-9][0-9]'");
		String code = sc.next();
		try {
			ajouterLocauxExamens.setString(1, nomLocal);
			ajouterLocauxExamens.setString(2, code);
			ajouterExamen.executeQuery();
		} catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
	}
	
	private void ajouterDateExamen() {
		System.out.println("Entrez le Code Examen sous format 'IPL[0-9][0-9][0-9]'");
		String code = sc.next();
		System.out.println("Entrez la date sous format'aaaa-mm-jj hh:mm:ss'");
		sc.nextLine();
		String dateString = sc.nextLine();
		Timestamp date = Timestamp.valueOf(dateString);
		try {
			ajouterDateExamen.setString(1, code);
			ajouterDateExamen.setTimestamp(2, date);
			ajouterDateExamen.executeQuery();
		}catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}	
	}
}

