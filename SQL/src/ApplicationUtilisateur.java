/*
 * @author Alban Bekteshi
 * @author Adrien de Decker
 */

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ApplicationUtilisateur {
	
	private PreparedStatement ajouterUtilisateur;
	private PreparedStatement obtenirUtilisateurDepuisNomUtilisateur;
	private PreparedStatement obtenirUtilisateurDepuisIdUtilisateur;
	private PreparedStatement afficherHoraireUtilisateur;
	private PreparedStatement visualiserExamenBloc;
	private PreparedStatement ajouterInscriptionExamen;
	private PreparedStatement ajouterInscriptionExamenBloc;
	
	
	private Connection conn;
	private final static Scanner scanner = new Scanner(System.in);
	private boolean isConnected = false;
	private int idUtilisateur;
	private int id_bloc;
	private boolean stopped = false;
	
	public ApplicationUtilisateur() {
		 this.conn = this.initConnection();
	}
	
	public static void main(String args[]) {
		ApplicationUtilisateur app = new ApplicationUtilisateur();
		System.out.println("Bienvenue sur l'application utilisateur !");
		System.out.println("--------------------------------------\n");
		
		int action=0;
		
		//While user is not connected
		do {
			action = app.choixActionMenuDeconnecte();
			switch(action) {
				case 1:
					app.inscription();
					break;
				case 2:
					app.connexion();
					break;
				default:
					System.out.println("Erreur : Aucune action trouvee pour action "+action+"\n");
					System.exit(1);
			}
		} while(!app.isConnected);
		
		// While user is connected and app not stopped
		do {
			action = app.choixActionMenuConnecte();
			switch(action) {
				case 1:
					app.visualiserExamens();
					break;
				case 2:
					app.inscriptionExamen();
					break;
				case 3:
					app.inscriptionTousExamensBloc();
					break;
				case 4:
					app.afficherHorraire();
					break;
					
				default:
					System.out.println("Erreur : Aucune action trouvee pour action "+action+"\n");
					System.exit(1);
					break;
					
			}
		} while(!app.stopped);
	}
	
	private void visualiserExamens() {
		System.out.println("Voici la liste de tous les examens");
		
		try {
			visualiserExamenBloc.setInt(1, id_bloc);
			try(ResultSet rs = visualiserExamenBloc.executeQuery()) {
				while(rs.next()) {
					String stringAAfficher ="";
					stringAAfficher+= rs.getString("code_examen");
					stringAAfficher+="\t";
					stringAAfficher+=rs.getString("nom");
					stringAAfficher+="\t";
					stringAAfficher+=rs.getInt("id_bloc");
					stringAAfficher+="\t";
					stringAAfficher+=rs.getInt("duree");
					System.out.println(stringAAfficher);
				}
			}
		} catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
		System.out.println();
	}
	
	private void inscriptionExamen() {
		System.out.println("Inscription a un examen :");
		System.out.println("Entrez le code de l'examen auquel vous voulez vous inscrire :");
		String codeExamen = scanner.next();
		
		try {
			ajouterInscriptionExamen.setString(1, codeExamen);
			ajouterInscriptionExamen.setInt(2, idUtilisateur);
			ajouterInscriptionExamen.executeQuery();
		} catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
	}

	private void inscriptionTousExamensBloc() {
		// TODO
		try {
			ajouterInscriptionExamenBloc.setInt(1, this.idUtilisateur);
			try(ResultSet rs = ajouterInscriptionExamen.executeQuery()){
				while(rs.next()) {
					if(rs.getBoolean("boolean")) {
						System.out.println("Inscription reussie !");
					}else {
						System.out.println("Il y a eu un problème lors de l'inscription à tous les examens du bloc");
					}
				}
			}
		} catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
	}
	
	private void afficherHorraire() {
		try {
			afficherHoraireUtilisateur.setInt(1, idUtilisateur);
			try(ResultSet rs = afficherHoraireUtilisateur.executeQuery()) {
				while(rs.next()) {
					String stringAAfficher ="";
					stringAAfficher+= rs.getString("code_examen");
					stringAAfficher+="\t";
					stringAAfficher+=rs.getString("nom");
					stringAAfficher+="\t";
					
					Timestamp dateDebut = rs.getTimestamp("dateDebut");
					if(dateDebut!=null) {
						stringAAfficher+=dateDebut;
					}
					else {
						stringAAfficher+="Aucune date";
					}
					stringAAfficher+="\t";
					int duree = rs.getInt("duree");
					if(dateDebut!=null) {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
						LocalDateTime dateFin = dateDebut.toLocalDateTime().plusMinutes(duree);
						
						stringAAfficher+=dateFin.format(formatter);
					}
					else {
						stringAAfficher+="Aucune date";
					}
										
					stringAAfficher+="\t";
					String locaux = rs.getString("locaux");
					if(locaux!=null) {
						stringAAfficher+=locaux;
					}
					else {
						stringAAfficher+="Aucun local";
					}
					System.out.println(stringAAfficher);
				}
			}
		}catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
		System.out.println();
	}

	private void inscription() {
		System.out.println("INSCRIPTION\n");
		System.out.println("Entrez votre nom d'utilisateur :");
		String username = scanner.next();
		
		System.out.println("Entrez votre email :");
		String email=scanner.next();
		
		System.out.println("Entrez votre mot de passe :");
		String password = scanner.next();
		String salt = BCrypt.gensalt();
		String hashedPassword = BCrypt.hashpw(password, salt);
		
		System.out.println("Entrez le bloc dans lequel vous etes :");
		int idBlock = scanner.nextInt();
		
		try {
			ajouterUtilisateur.setString(1, username);
			ajouterUtilisateur.setString(2, email);
			ajouterUtilisateur.setString(3, hashedPassword);
			ajouterUtilisateur.setInt(4, idBlock);
			ajouterUtilisateur.executeQuery();
		} catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
		System.out.println("Inscription reussie, vous pouvez désormais vous connecter !");
		
	}
	
	private void connexion() {
		
		System.out.println("CONNEXION\n");
		System.out.println("Entrez votre nom d'utlilisateur :");
		String username= scanner.next();
		
		System.out.println("Entrez votre mot de passe :");
		String password = scanner.next();
		String hashedPassword="";
				
		try {
			obtenirUtilisateurDepuisNomUtilisateur.setString(1, username);

			try(ResultSet rs = obtenirUtilisateurDepuisNomUtilisateur.executeQuery()){
				while(rs.next()) {
					hashedPassword = rs.getString("mot_de_passe");
					idUtilisateur = rs.getInt("id_utilisateur");
					id_bloc = rs.getInt("id_bloc");
				}
			}
		} catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
		if(hashedPassword!="") {
			if(BCrypt.checkpw(password, hashedPassword)) {
				System.out.println("Connexion reussie !\n\n\n\n\n");
				isConnected=true;
			}
			else {
				idUtilisateur=0;
				id_bloc=0;
				System.out.println("Nom d'utilisateur ou mot de passe incorrect !");
			}
		}
		else {
			idUtilisateur=0;
			id_bloc=0;
			System.out.println("Nom d'utilisateur ou mot de passe incorrect !");
		}
		
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
			ajouterUtilisateur = conn.prepareStatement("SELECT * FROM projet.inscriptionUtilisateur(?,?,?,?);");
			obtenirUtilisateurDepuisNomUtilisateur = conn.prepareStatement("SELECT * FROM projet.utilisateurs WHERE nom_utilisateur = ?;");
			obtenirUtilisateurDepuisIdUtilisateur = conn.prepareStatement("SELECT * FROM projet.utilisateurs WHERE id_utilisateur = ?;");
			visualiserExamenBloc = conn.prepareStatement("SELECT * FROM projet.examens WHERE id_bloc = ?");
			ajouterInscriptionExamen = conn.prepareStatement("SELECT * FROM projet.ajouterInscriptionExamen(?,?);");
			ajouterInscriptionExamenBloc = conn.prepareStatement("SELECT projet.ajouterInscriptionExamenBloc(?);");
			afficherHoraireUtilisateur = conn.prepareStatement("SELECT * FROM projet.obtenirHoraireExamen(?) t(code_examen VARCHAR,nom VARCHAR, dateDebut TIMESTAMP, duree INTEGER, locaux VARCHAR);");
		} catch(SQLException e) {
			System.out.println("Erreur lors de la preparation des statement");
			System.exit(1);
		}
		
		return conn;
	}
	
	private int choixActionMenuDeconnecte(){
		int action =0;
		
		System.out.println("Entrez l'action que vous voulez executer :");
		System.out.println("1: Inscription Utilisateur");
		System.out.println("2: Connexion");
		
		do{
			action = scanner.nextInt();
		} while(action<=0 || action >2) ;
			
		return action;
	}
	
	private int choixActionMenuConnecte() {
		int action =0;
		
		System.out.println("Entrez l'action que vous voulez executer :");
		System.out.println("1: Visualiser les examens");
		System.out.println("2: S'inscrire a un examen");
		System.out.println("3: S'inscrire a tous les examens du bloc");
		System.out.println("4: Voir son horaire d'examen");
		
		do {
			action = scanner.nextInt();
		} while(action <=0 || action >4);
		
		return action;
	}
	
}


