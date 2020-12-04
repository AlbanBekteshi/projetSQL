/*
 * @author Alban Bekteshi
 * @author Adrien de Decker
 */

import java.util.Scanner;

import javax.naming.spi.DirStateFactory.Result;

import java.sql.*;

public class ApplicationCentrale {
	
	private String url = "jdbc:postgresql://localhost:5432/ProjetSQL?user=postgres&password=9797";
	private Connection conn = null;
	private PreparedStatement ajouterLocal;
	private PreparedStatement ajouterExamen;
	private PreparedStatement ajouterLocauxExamens;
	private PreparedStatement ajouterDateExamen;
	private PreparedStatement horaireExamenBloc;
	private PreparedStatement examenParLocaux;
	private PreparedStatement examenNonReserver;
	private PreparedStatement examenNonReserverBloc;
	private final static Scanner sc = new Scanner(System.in);
	
	
	public ApplicationCentrale() {
		this.conn= this.initConnection();		
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
			horaireExamenBloc = conn.prepareStatement("SELECT e.code_examen, e.nom, e.date, COUNT(l.id_local) FROM projet.examens e LEFT OUTER JOIN projet.locaux_examens l ON e.code_examen = l.code_examen"
					+ " WHERE e.id_bloc = ? GROUP BY e.code_examen ORDER BY e.date;");
			examenParLocaux= conn.prepareStatement("SELECT le.id_local,e.date,le.code_examen,e.nom FROM projet.examens e, projet.locaux_examens le WHERE le.code_examen = e.code_examen AND le.id_local = ? GROUP BY le.id_local,e.date,le.code_examen,e.nom ORDER BY e.date;");
			examenNonReserver = conn.prepareStatement("SELECT DISTINCT e.code_examen, e.nom, e.date FROM projet.examens e LEFT OUTER JOIN  projet.locaux_examens le ON le.code_examen = e.code_examen WHERE(  ((SELECT count(ie.id_utilisateur) FROM projet.inscriptions_examens ie WHERE ie.code_examen = e.code_examen)  > ( SELECT sum(l.capacite) FROM projet.locaux l, projet.locaux_examens ll WHERE ll.id_local = l.id_local ) )OR ( SELECT sum(l.capacite) FROM projet.locaux l WHERE le.id_local = l.id_local ) IS NULL ) ORDER BY e.code_examen;");
			examenNonReserverBloc = conn.prepareCall("SELECT b.id_bloc, b.examen_non_complet FROM projet.blocs b;");
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
				System.out.println("--------------------------------------\n");
				System.out.println("Ajouter Local\n");
				app.ajouterLocal();
				System.out.println("--------------------------------------\n");
				break;
			case 2:
				System.out.println("--------------------------------------\n");
				System.out.println("Ajouter Examen\n");
				app.ajouterExamen();
				System.out.println("--------------------------------------\n");
				break;
			case 3:
				System.out.println("--------------------------------------\n");
				System.out.println("Reserver Locaux pour examen\n");
				app.ajouterLocauxExamens();
				System.out.println("--------------------------------------\n");
				break;
			case 4:
				System.out.println("--------------------------------------\n");
				System.out.println("Ajouter/Modifier La Date a un examen\n");
				app.ajouterDateExamen();
				System.out.println("--------------------------------------\n");
				break;
			case 5:
				System.out.println("--------------------------------------\n");
				System.out.println("Horaire Examen\n");
				app.horaireExamenBloc();
				System.out.println("--------------------------------------\n");
				break;
			case 6:
				System.out.println("--------------------------------------\n");
				System.out.println("Examen dans un local\n");
				app.examenParLocaux();
				System.out.println("--------------------------------------\n");
				break;
			case 7:
				System.out.println("--------------------------------------\n");
				System.out.println("Visualiser les Examens pas encore complet\n");
				app.examenNonReserver();
				System.out.println("--------------------------------------\n");
				break;
			case 8:
				System.out.println("--------------------------------------\n");
				System.out.println("Nombre d'examen non complet par bloc\n");
				app.examenNonReserverBloc();
				System.out.println("--------------------------------------\n");
				break;
			default:
				System.out.println("Erreur : Aucune action trouvee pour action "+action+"\n");
				System.exit(1);
				break;
			}
		}while(action >=1 && action <= 8);
		
	}
	
	
	

	/*
	 * @return int numï¿½ro de l'action que user souhaite executer
	 */
	private int choixActionMenu() {
		System.out.println("Entrez l'action que vous voulez executer :\n");
		System.out.println("1: Ajouter local");
		System.out.println("2: Ajouter Examen");
		System.out.println("3: Reserver un local pour un Examen");
		System.out.println("4: Ajouter/Modifier la date a un Examen");
		System.out.println("5: Horaire Examen");
		System.out.println("6: Examen par locaux");
		System.out.println("7: Visualiser les Examens pas encore complet");
		System.out.println("8: Nombre d'examen non complet par bloc");
		int action = 0;
		
		do {			
			action =sc.nextInt();
		} while(action<=0 || action >9);

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
		System.out.println("Entrez la duree en minute");
		int duree = sc.nextInt();
		System.out.println("L'examen est-t-il sur machines ou ecrit? \nMachine => m\nEcrit=> e");
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
			ajouterLocauxExamens.executeQuery();
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
	
	private void horaireExamenBloc() {
		System.out.println("Entrez le bloc");
		int bloc = sc.nextInt();
		try {
			horaireExamenBloc.setInt(1,bloc);
			try(ResultSet rs = horaireExamenBloc.executeQuery()){
				while(rs.next()) {
					System.out.println("code : "+ rs.getString(1) +"   Nom : "+rs.getString(2)+"   Date : "+rs.getString(3)+"   Nombre de locaux resevé : "+rs.getInt(4));
				}
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
	}
	
	private void examenParLocaux() {
		System.out.println("Entrez le nom du local");
		String id_local = sc.next();
		try {
			examenParLocaux.setString(1, id_local);
			try(ResultSet rs = examenParLocaux.executeQuery()){
				while(rs.next()) {
					System.out.println("Local : "+rs.getString(1)+ "   Date : "+rs.getString(2)+"   Code : "+rs.getString(3)+"   Nom : "+rs.getString(4));
				}
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
	}
	
	private void examenNonReserver() {
		try {
			try (ResultSet rs = examenNonReserver.executeQuery()){
				while(rs.next()) {
					System.out.println("Code : "+rs.getString(1) +"   Nom : "+ rs.getString(2)+"   Date : "+rs.getString(3));
				}
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
	}
	
	private void examenNonReserverBloc() {
		try {
			try(ResultSet rs = examenNonReserverBloc.executeQuery()){
				while(rs.next()) {
					System.out.println("Bloc : "+rs.getString(1)+"   Nombre d'examen non reserver : " +rs.getString(2));
				}
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
	}
}

