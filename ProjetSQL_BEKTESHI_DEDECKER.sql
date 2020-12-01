DROP SCHEMA IF EXISTS projet CASCADE;
CREATE SCHEMA projet;

/*
CREATE TABLES
*/

CREATE TABLE projet.formations (
	id_formation SERIAL PRIMARY KEY,
	nom VARCHAR(100) NOT NULL CHECK (nom<>''),
	ecole VARCHAR(100) NOT NULL CHECK (ecole<>'')
);

CREATE TABLE projet.blocs (
	id_bloc SERIAL PRIMARY KEY,
	code_bloc CHARACTER(6)
		CHECK(code_bloc SIMILAR TO 'Bloc [0-9]'),
	id_formation INTEGER REFERENCES projet.formations (id_formation) NOT NULL
);

CREATE TABLE projet.utilisateurs(
	id_utilisateur SERIAL PRIMARY KEY,
	nom_utilisateur VARCHAR(100) NOT NULL UNIQUE CHECK(nom_utilisateur<>''),
	email VARCHAR(100) NOT NULL UNIQUE CHECK(email<>''),
	mot_de_passe VARCHAR(100) NOT NULL CHECK (mot_de_passe<>''),
	id_bloc INTEGER REFERENCES projet.blocs (id_bloc) NOT NULL -- est-ce qu'on est sur de sa ? 
);

CREATE TABLE projet.examens(
	code_examen CHARACTER(6) PRIMARY KEY UNIQUE
		CHECK(code_examen SIMILAR TO 'IPL[0-9][0-9][0-9]'),
	nom VARCHAR(100) NOT NULL CHECK(nom<>''),
	id_bloc INTEGER REFERENCES projet.blocs (id_bloc) NOT NULL,
	duree INTEGER NOT NULL CHECK (duree>=0), 
	date timestamp without time zone,
	support CHAR(1) NOT NULL CHECK (support='m' OR support='e')
);

CREATE TABLE projet.inscriptions_examens(
	code_examen VARCHAR(6) REFERENCES projet.examens (code_examen) NOT NULL,
	id_utilisateur INTEGER REFERENCES projet.utilisateurs(id_utilisateur) NOT NULL,
	PRIMARY KEY (code_examen,id_utilisateur)
);

CREATE TABLE projet.locaux (
	id_local VARCHAR(10) PRIMARY KEY UNIQUE CHECK(id_local<>''),
	capacite INT NOT NULL CHECK(capacite>0),
	machine CHAR(1) NOT NULL CHECK (machine='o' OR machine='n')
);

CREATE TABLE projet.locaux_examens (
	id_local VARCHAR(10) REFERENCES projet.locaux (id_local) NOT NULL,
	code_examen VARCHAR(6) REFERENCES projet.examens (code_examen) NOT NULL,
	PRIMARY KEY (id_local,code_examen)
);
	
/*
FUNCTIONS
*/

--Implémenter !!!
CREATE OR REPLACE FUNCTION projet.ajouterLocal(id_local VARCHAR(10), capacite INT,machine CHAR(1)) RETURNS VOID AS $$
DECLARE -- Faut-il obligatoirement le mettre ? 
BEGIN
	IF(capacite<=0) THEN 
		RAISE 'Capacité doit être > que 0'; 											-- Reussi
	END IF;
	INSERT INTO projet.locaux VALUES
		(id_local,capacite,machine);
	RETURN;
END;
$$ LANGUAGE plpgsql;





CREATE OR REPLACE FUNCTION projet.inscriptionUtilisateur(nom_utilisateur VARCHAR(100), email VARCHAR(100), mot_de_passe VARCHAR(100), id_blocN INTEGER) RETURNS VOID AS $$
DECLARE
BEGIN
	IF NOT EXISTS(SELECT * FROM projet.blocs b
				WHERE b.id_bloc =id_blocN) THEN
		RAISE 'Bloc invalide';															-- Reussi
	END IF;
    INSERT INTO projet.utilisateurs 
        VALUES(DEFAULT,nom_utilisateur,email,mot_de_passe,id_blocN);
    RETURN;
END;
$$ LANGUAGE plpgsql;


--Implémenter !!!
CREATE OR REPLACE FUNCTION projet.ajouterExamen(code_examen CHARACTER(6), nom VARCHAR (100), id_blocN INTEGER, duree INTEGER, support CHAR(1)) RETURNS VOID AS $$
DECLARE
BEGIN
	IF NOT EXISTS(SELECT * FROM projet.blocs b 
					WHERE b.id_bloc=id_blocN) THEN	
		RAISE 'Le bloc nexiste pas';													-- Reussi
	END IF;
	INSERT INTO projet.Examens(code_examen,nom,id_bloc,duree,support) 
		VALUES(code_examen,nom,id_blocN,duree,support);
	RETURN;
END;
$$ LANGUAGE plpgsql;


--Implémenter !!!
CREATE OR REPLACE FUNCTION projet.ajouterLocauxExamens(id_localN VARCHAR(10), code_examenN CHARACTER(6)) RETURNS VOID AS $$
DECLARE
BEGIN
	IF EXISTS (SELECT e.date FROM projet.examens e 
					WHERE code_examenN = e.code_examen AND e.date IS NULL) THEN
		RAISE 'Heure du debut pas encore fixer';										-- Reussi
	END IF;
	IF NOT EXISTS(SELECT * FROM projet.locaux l
					WHERE l.id_local = id_localN) THEN
		RAISE 'Le local nexiste pas';													-- Reussi
	END IF;
	IF NOT EXISTS(SELECT * FROM projet.examens e
					WHERE e.code_examen = code_examenN) THEN
		RAISE 'L examen nexiste pas';													-- Reussi
	END IF;
	IF ((SELECT support FROM projet.examens e 
				WHERE e.code_examen=code_examenN) = 'm') THEN
		IF((SELECT machine FROM projet.locaux l WHERE l.id_local=id_localN)='n') THEN
			RAISE 'Pas de machines dispo dans le local';								-- Reussi
		END IF;
	END IF;
	-- Si l’examen est déjà complètement réservé. ????
	INSERT INTO projet.locaux_examens VALUES (id_localN,code_examenN);
	RETURN;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION projet.ajouterInscriptionExamen(code_examenN CHARACTER(6), id_utilisateurN INTEGER) RETURNS VOID AS $$
DECLARE
BEGIN
	IF NOT EXISTS(SELECT * FROM projet.examens e
					WHERE e.code_examen = code_examenN) THEN
		RAISE 'L examen nexiste pas';														-- Reussi
	END IF;
	IF NOT EXISTS(SELECT * FROM projet.utilisateurs u
					WHERE u.id_utilisateur = id_utilisateurN) THEN
		RAISE 'L utilisateur nexiste pas';													-- Reussi
	END IF;
	IF EXISTS (SELECT date FROM projet.examens e 										
				WHERE e.code_examen = code_examenN AND e.date IS NOT NULL) THEN
		RAISE 'Date d examen déjà declare';													-- Reussi
	END IF;
	INSERT INTO projet.inscriptions_examens VALUES(code_examenN,id_utilisateurN);
	RETURN;
END;
$$ LANGUAGE plpgsql;


--Implémenter !!!
CREATE OR REPLACE FUNCTION projet.ajouterDateExamen(code_examenN CHARACTER(6),dateN timestamp) RETURNS BOOLEAN AS $$
DECLARE
BEGIN
	--Vérifier si date est sur autre examen
	--TODO
	IF NOT EXISTS (SELECT i.id_utilisateur FROM projet.inscriptions_examens i
					WHERE i.code_examen = code_examenN) THEN
		RAISE 'Pas d etudiant Inscrit';														-- Reussi
	END IF;

	IF EXISTS (SELECT * FROM projet.examens e 
				WHERE e.date::TIMESTAMP::DATE = dateN::TIMESTAMP::DATE
				AND e.id_bloc = (SELECT e.id_bloc FROM projet.examens e WHERE e.code_examen=code_examenN)) THEN
		RAISE 'Un examen du même bloc existe deja ce jour la';								-- Reussi
	END IF;

	IF EXISTS (SELECT l.id_local FROM projet.locaux_examens l
                WHERE l.code_examen = code_examenN) THEN
        RAISE 'Un local a déjà été réservé';												-- Reussi
	END IF;
	
	UPDATE projet.examens SET date=dateN WHERE code_examenN = code_examen;
	RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION projet.obtenirUtilisateurDepuisId(INTEGER id_utilisateurN) RETURN SETOF RECORD AS $$
DECLARE
	user RECORD;
BEGIN
	IF NOT EXISTS(SELECT * FROM projet.utilisateurs u WHERE u.id_utilisateur=id_utilisateurN) THEN
		RAISE 'Aucun utilisateur avec cet id';
	END IF;
	SELECT * FROM projet.utilisateurs u WHERE u.id_utilisateur=id_utilisateurN INTO user;
	RETURN user;
END;
$$ LANGUAGE 'plpgsql';


/*
 DEMO
*/

INSERT INTO projet.formations (nom, ecole) 
	VALUES ('Bachelier en Informatique de Gestion','IPL');
INSERT INTO projet.blocs(id_bloc,code_bloc,id_formation)
	VALUES (DEFAULT,'Bloc 1',1);
INSERT INTO projet.blocs(id_bloc,code_bloc,id_formation)
	VALUES (DEFAULT,'Bloc 2',1);
INSERT INTO projet.examens (code_examen,nom,id_bloc,duree,support)
	VALUES ('IPL100','APOO',1,120,'e');
INSERT INTO projet.examens (code_examen,nom,id_bloc,duree,support)
	VALUES ('IPL150','ALGO',1,60,'m');
INSERT INTO projet.examens (code_examen,nom,id_bloc,duree,support)
	VALUES ('IPL200','JAVASCRIPT',2,120,'m');
INSERT INTO projet.locaux (id_local,capacite,machine)
	VALUES ('A017',2,'o');
INSERT INTO projet.locaux (id_local,capacite,machine)
	VALUES ('A019',1,'o');


INSERT INTO projet.utilisateurs (id_utilisateur,nom_utilisateur,email,mot_de_passe,id_bloc) 
	VALUES (DEFAULT,'adrien','adrien@email.com','$2a$10$kS/c5ug2K4ptRtPNXFHarOLONg2SIrFgS/W.NEPMj2iqxQqfQt9dG',1);
SELECT * FROM projet.utilisateurs;