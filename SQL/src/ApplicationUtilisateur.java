/*
 * @author Alban Bekteshi
 * @author Adrien de Decker
 */

import java.sql.*;
import java.util.Scanner;

public class ApplicationUtilisateur {
	
	private Connection conn;
	Scanner scanner = new Scanner(System.in);
	
	public ApplicationUtilisateur() {
		 this.conn = this.initConnection();

		System.out.println("Bienvenue sur l'application utilisateur !");
		System.out.println("--------------------------------------\n");
	}
	
	int action = choixActionMenu();
	
	
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
}
