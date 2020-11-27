import java.sql.DriverManager;
import java.sql.*;	

public class main {

	public static void main(String args[]) {
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
		
		/* Test d'un insert OK
		try {
			Statement s = conn.createStatement();
			
			s.executeUpdate("INSERT INTO projet.utilisateurs VALUES (DEFAULT,'test','test','test',1);");
		} catch(SQLException se) {
			System.out.println("Erreur insertion");
			se.printStackTrace();
			System.exit(1);
		}
		*/
		
		/* Test d'un Select
		try {
			Statement s = conn.createStatement();
			try(ResultSet rs = s.executeQuery("SELECT nom_utilisateur "
					+ "FROM projet.utilisateurs;")){
				while(rs.next()) {
					System.out.println(rs.getString(1));
				}
			}
		} catch (SQLException se) {
			se.printStackTrace();
			System.exit(1);
		}
		*/
		
	}
}

