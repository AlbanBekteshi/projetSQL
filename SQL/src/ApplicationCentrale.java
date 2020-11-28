import java.sql.DriverManager;
import java.util.Scanner;
import java.sql.*;	

public class ApplicationCentrale {
	
	private Connection conn;
	
	public ApplicationCentrale() {
		
		this.conn= this.initConnection();
		
		System.out.println("Bienvenue sur l'application centrale !\n");
		System.out.println("--------------------------------------\n");
		
		int action = choixActionMenu();
		
		switch (action) {
		case 1:
			System.out.println("Ajouter Local\n");
			
			break;

		default:
			System.out.println("Erreur : Aucune action trouvee\n");
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
	
	private int choixActionMenu() {
		System.out.println("choixActionMenu\n");
		Scanner scanner = new Scanner(System.in);
		int action = 0;
		do {
			
			action =scanner.nextInt();
		} while(action<=0 || action >8);
		System.out.println("input :"+action);
		
		return action;
	}
}

