/*
 * @author Alban Bekteshi
 * @author Adrien de Decker
 */

import java.sql.*;
import java.util.Scanner;

public class ApplicationUtilisateur {
	
	private PreparedStatement ajouterUtilisateur;
	private PreparedStatement connexion;
	private PreparedStatement getPasswordFromUSername;
	private PreparedStatement ajouterInscriptionExamen;
	
	
	private Connection conn;
	private final static Scanner scanner = new Scanner(System.in);
	private boolean isConnected = false;
	private int idUtilisateur;
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
			Statement s = conn.createStatement();
			try(ResultSet rs = s.executeQuery("SELECT * FROM projet.examens")) {
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
		//visualiserExamens();
		System.out.println("Entrez le code de l'examen auquel vous voulez vous inscrire :");
		String codeExamen = scanner.next();
		
		try {
			ajouterInscriptionExamen.setString(1, codeExamen);
			ajouterInscriptionExamen.setInt(2, idUtilisateur);
			ajouterInscriptionExamen.executeQuery();
		} catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
		System.out.println("fin inscriptionExamen()");
	}

	private void inscriptionTousExamensBloc() {
		// TODO Auto-generated method stub
		
	}
	
	private void afficherHorraire() {
		// TODO Auto-generated method stub
		
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
			Statement s = conn.createStatement();
			try(ResultSet rs= s.executeQuery("SELECT mot_de_passe,id_utilisateur FROM projet.utilisateurs WHERE nom_utilisateur ='"+username+"';")){
				while(rs.next()) {
					hashedPassword = rs.getString("mot_de_passe");
					idUtilisateur = rs.getInt("id_utilisateur");
				}
			}
		} catch(SQLException e) {
			System.out.println(e.getMessage().split("\n")[0]);
		}
		
		if(hashedPassword!="") {
			if(BCrypt.checkpw(password, hashedPassword)) {
				System.out.println("Connexion reussie !");
				isConnected=true;
			}
			else {
				idUtilisateur=0;
				System.out.println("Nom d'utilisateur ou mot de passe incorrect !");
			}
		}
		else {
			idUtilisateur=0;
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
			connexion = conn.prepareStatement("SELECT * FROM projet.connexion;");
			getPasswordFromUSername = conn.prepareStatement("SELECT mot_de_passe FROM projet.utilisateurs WHERE nom_utilisateur = ?;");
			ajouterInscriptionExamen = conn.prepareStatement("SELECT * FROM projet.ajouterInscriptionExamen(?,?);");
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


