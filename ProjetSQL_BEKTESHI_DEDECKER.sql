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
	code_bloc CHARACTER(4)
		CHECK(code_bloc SIMILAR TO '[0-9]BIN'),
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
	date timestamp without time zone NOT NULL,
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
INSERTS
*/

INSERT INTO projet.formations (nom, ecole) 
	VALUES ('Test Formation','IPL');
INSERT INTO projet.blocs(id_bloc,code_bloc,id_formation)
	VALUES(DEFAULT,'2BIN',1);
--SELECT projet.ajouterLocal('2b1',5,'o');
--SELECT projet.inscriptionUtilisateur('admin','admin@vinci.be','123',1);
--SELECT projet.ajoutExamen('IPL123','SQL Exam',1,150,'2020-08-25','e');
--SELECT projet.ajoutLocauxExamens('2b1','IPL123');
--SELECT projet.ajouterInscriptionExamen('IPL123',1);

	
/*
FUNCTIONS
*/

CREATE OR REPLACE FUNCTION projet.ajouterLocal(id_local VARCHAR(10), capacite INT,machine CHAR(1)) RETURNS VOID AS $$
DECLARE -- Faut-il obligatoirement le mettre ? 
BEGIN
	IF(capacite<=0) THEN 
		RAISE 'Capacité doit être > que 0'; -- Lance une erreur ? 
											-- Rajouté un if pour machine ? 
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
		RAISE 'Le bloc nexiste pas';
	END IF;
    INSERT INTO projet.utilisateurs 
        VALUES(DEFAULT,nom_utilisateur,email,mot_de_passe,id_blocN);
    RETURN;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION projet.ajoutExamen(code_examen CHARACTER(6), nom VARCHAR (100), id_blocN INTEGER, duree INTEGER, date timestamp, support CHAR(1)) RETURNS VOID AS $$
DECLARE
BEGIN
	IF NOT EXISTS(SELECT * FROM projet.blocs b 
					WHERE b.id_bloc=id_blocN) THEN	
		RAISE 'Le bloc nexiste pas';
	END IF;
	INSERT INTO projet.Examens(code_examen,nom,id_bloc,duree,date,support) 
		VALUES(code_examen,nom,id_blocN,duree,date,support);
	RETURN;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION projet.ajoutLocauxExamens(id_localN VARCHAR(10), code_examenN CHARACTER(6)) RETURNS VOID AS $$
DECLARE
BEGIN
	IF NOT EXISTS(SELECT * FROM projet.locaux l
					WHERE l.id_local = id_localN) THEN
		RAISE 'Le local nexiste pas';
	END IF;
	IF NOT EXISTS(SELECT * FROM projet.examens e
					WHERE e.code_examen = code_examenN) THEN
		RAISE 'L examen nexiste pas';
	END IF;
	INSERT INTO projet.locaux_examens VALUES (id_localN,code_examenN);
	RETURN;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION projet.ajouterInscriptionExamen(code_examenN CHARACTER(6), id_utilisateurN INTEGER) RETURNS VOID AS $$
DECLARE
BEGIN
	IF NOT EXISTS(SELECT * FROM projet.examens e
					WHERE e.code_examen = code_examenN) THEN
		RAISE 'L examen nexiste pas';
	END IF;
	IF NOT EXISTS(SELECT * FROM projet.utilisateurs u
					WHERE u.id_utilisateur = id_utilisateurN) THEN
		RAISE 'L utilisateur nexiste pas';
	END IF;
	INSERT INTO projet.inscriptions_examens VALUES(code_examenN,id_utilisateurN);
	RETURN;
END;
$$ LANGUAGE plpgsql;