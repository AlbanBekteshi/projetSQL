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


CREATE OR REPLACE FUNCTION projet.ajouterLocal(id_local VARCHAR(10), capacite INT,machine CHAR(1)) RETURNS VOID AS $$
DECLARE
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



-- Ajouter un Examen
CREATE OR REPLACE FUNCTION projet.ajouterExamen(CHARACTER(6),VARCHAR (100),INTEGER,INTEGER,CHAR(1)) RETURNS VOID AS $$
DECLARE
	v_code_examen ALIAS FOR $1;
	v_nom ALIAS FOR $2;
	v_id_bloc ALIAS FOR $3;
	v_duree ALIAS FOR $4;
	v_support ALIAS FOR $5;
BEGIN
	INSERT INTO projet.examens(code_examen,nom,id_bloc,duree,support) 
		VALUES(v_code_examen,v_nom,v_id_bloc,v_duree,v_support);
	RETURN;
END;
$$ LANGUAGE plpgsql;

-- Verif Ajouter un Examen a l'aide de TRIGGER
CREATE OR REPLACE FUNCTION projet.verif_ajouterExamen () RETURNS TRIGGER AS $$
	BEGIN
		IF NOT EXISTS (SELECT * FROM projet.blocs b
					WHERE b.id_bloc = NEW.id_bloc) THEN
			RAISE 'Le bloc nexiste pas';
		END IF;
		RETURN NEW;
	END;
$$LANGUAGE plpgsql;
CREATE TRIGGER trigger_verifi_ajouterExam BEFORE INSERT ON projet.examens
	FOR EACH ROW EXECUTE PROCEDURE projet.verif_ajouterExamen();





-- Attribuer un local a un examen
CREATE OR REPLACE FUNCTION projet.ajouterLocauxExamens(VARCHAR(10), CHARACTER(6)) RETURNS VOID AS $$
DECLARE
	n_id_Local ALIAS FOR $1;
	n_code_examen ALIAS FOR $2;
BEGIN
	-- Si l’examen est déjà complètement réservé. ????
	INSERT INTO projet.locaux_examens VALUES (n_id_Local,n_code_examen);
	RETURN;
END;
$$ LANGUAGE plpgsql;

-- Verif d'attribution d'un local via TRIGGER
CREATE OR REPLACE FUNCTION projet.verif_ajouterLocauxExamens() RETURNS TRIGGER AS $$
	BEGIN
		IF EXISTS (SELECT e.date FROM projet.examens e 
					WHERE NEW.code_examen = e.code_examen AND e.date IS NULL) THEN
			RAISE 'Heure du debut pas encore fixer';
		END IF;
		IF NOT EXISTS(SELECT * FROM projet.locaux l
						WHERE l.id_local = NEW.id_local) THEN
			RAISE 'Le local nexiste pas';
		END IF;
		IF NOT EXISTS(SELECT * FROM projet.examens e
						WHERE e.code_examen = NEW.code_examen) THEN
			RAISE 'L examen nexiste pas';													
		END IF;
		IF ((SELECT support FROM projet.examens e 
					WHERE e.code_examen=NEW.code_examen) = 'm') THEN
			IF((SELECT machine FROM projet.locaux l WHERE l.id_local=NEW.id_local)='n') THEN
				RAISE 'Pas de machines dispo dans le local';								
			END IF;
		END IF;
		RETURN NEW;
	END;
$$LANGUAGE plpgsql;
CREATE TRIGGER trigger_verifi_ajouterLocauxExamens BEFORE INSERT ON projet.locaux_examens
	FOR EACH  ROW EXECUTE PROCEDURE projet.verif_ajouterLocauxExamens();





CREATE OR REPLACE FUNCTION projet.ajouterInscriptionExamen(code_examenN CHARACTER(6), id_utilisateurN INTEGER) RETURNS BOOLEAN AS $$
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
	RETURN TRUE;
END;
$$ LANGUAGE plpgsql;


--Implémenter !!!
CREATE OR REPLACE FUNCTION projet.ajouterDateExamen(CHARACTER(6), timestamp) RETURNS BOOLEAN AS $$
DECLARE
	n_code_examen ALIAS FOR $1;
	n_date ALIAS FOR $2;
BEGIN
	
	--Vérifier si date est sur autre examen
	UPDATE projet.examens SET date=n_date WHERE n_code_examen = code_examen;
	RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION projet.verif_ajouterDateExamen() RETURNS TRIGGER AS $$
	BEGIN
		IF NOT EXISTS (SELECT i.id_utilisateur FROM projet.inscriptions_examens i
					WHERE i.code_examen = NEW.code_examen) THEN
		RAISE 'Pas d etudiant Inscrit';														-- Reussi
		END IF;
		IF EXISTS (SELECT l.id_local FROM projet.locaux_examens l
        	        WHERE l.code_examen = NEW.code_examen) THEN
        	RAISE 'Un local a déjà été réservé';												-- Reussi
		END IF;
		IF EXISTS (SELECT * FROM projet.examens e 
					WHERE e.date::TIMESTAMP::DATE = NEW.date::TIMESTAMP::DATE
					AND e.id_bloc = (SELECT e.id_bloc FROM projet.examens e WHERE e.code_examen=NEW.code_examen)) THEN
			RAISE 'Un examen du même bloc existe deja ce jour la';								-- Reussi
		END IF;


		--IF EXISTS (SELECT * FROM projet.examens e
		--			WHERE NEW.date::TIMESTAMP::DATE IN (SELECT ee.date::TIMESTAMP::DATE FROM projet.examens ee
		--				WHERE ee.code_examen IN (SELECT ie.code_examen FROM projet.inscriptions_examens ie
		--					WHERE ie.id_utilisateur IN (SELECT iee.id_utilisateur FROM projet.inscriptions_examens iee
		--						WHERE iee.code_examen = NEW.code_examen)))) THEN
		--	RAISE 'Conflit Horaire';
		--END IF;
		RETURN NEW;
	END;
$$LANGUAGE plpgsql;
CREATE TRIGGER trigger_verifi_ajouterDateExam BEFORE UPDATE ON projet.examens
	FOR EACH ROW EXECUTE PROCEDURE projet.verif_ajouterDateExamen();


CREATE OR REPLACE FUNCTION projet.obtenirHeureFin(code_examenN VARCHAR(6),code_examenN2 VARCHAR(6)) RETURNS BOOLEAN AS $$
	DECLARE
		n_duree INTEGER;
		n_dateDebut TIMESTAMP;
		n2_duree INTEGER;
		n2_dateDebut TIMESTAMP;
		nn_datefin VARCHAR='100 minutes';
	BEGIN
		SELECT e.duree FROM projet.examens e WHERE e.code_examen = code_examenN INTO n_duree;
		SELECT e.date FROM projet.examens e WHERE e.code_examen = code_examenN INTO n_dateDebut;
		SELECT e.duree FROM projet.examens e WHERE e.code_examen = code_examenN2 INTO n2_duree;
		SELECT e.date FROM projet.examens e WHERE e.code_examen = code_examenN2 INTO n2_dateDebut;
	RETURN ( n_dateDebut, nn_datefin::INTERVAL) OVERLAPS ( n2_dateDebut, INTERVAL '60 minutes');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION projet.obtenirHoraireExamen(id_utilisateurN INTEGER) RETURNS SETOF RECORD AS $$
DECLARE
	plusSymbol VARCHAR;

	code_examen VARCHAR;
	nom VARCHAR;
	dateDebut TIMESTAMP;
	locaux VARCHAR;
	duree INTEGER;

	examen RECORD;
	local RECORD;
	sortie RECORD;
BEGIN
	FOR examen IN (SELECT * FROM projet.examens e WHERE e.code_examen IN (SELECT ie.code_examen FROM projet.inscriptions_examens ie WHERE ie.id_utilisateur=id_utilisateurN) ORDER BY e.date) LOOP
		SELECT examen.duree INTO duree;
		SELECT examen.code_examen INTO code_examen;
		SELECT examen.nom INTO nom;
		SELECT examen.date::TIMESTAMP INTO dateDebut;

		FOR local IN SELECT * FROM projet.locaux_examens le WHERE le.code_examen=examen.code_examen LOOP
			plusSymbol:='+';
			locaux:=local.id_local || plusSymbol;
		END LOOP;

		SELECT code_examen,nom,dateDebut,duree,locaux INTO sortie;
		RETURN NEXT sortie;
	END LOOP;
END;
$$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION projet.ajouterInscriptionExamenBloc(id_utilisateurN INTEGER) RETURNS BOOLEAN AS $$
DECLARE
	examen RECORD;
BEGIN
	FOR examen IN SELECT * FROM projet.examens e WHERE e.id_bloc = (SELECT u.id_bloc FROM projet.utilisateurs u WHERE u.id_utilisateur=id_utilisateurN) LOOP
		IF NOT(projet.ajouterInscriptionExamen(examen.code_examen,id_utilisateurN)) THEN
			ROLLBACK;
			RETURN FALSE;
		END IF;
	END LOOP;
	RETURN TRUE;
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

--Tous les mdp sont 123
INSERT INTO projet.utilisateurs (id_utilisateur,nom_utilisateur,email,mot_de_passe,id_bloc) 
	VALUES (DEFAULT,'adrien','adrien@email.com','$2a$10$kS/c5ug2K4ptRtPNXFHarOLONg2SIrFgS/W.NEPMj2iqxQqfQt9dG',1);
INSERT INTO projet.utilisateurs (id_utilisateur,nom_utilisateur,email,mot_de_passe,id_bloc) 
	VALUES (DEFAULT,'alban','alban@email.com','$2a$10$kS/c5ug2K4ptRtPNXFHarOLONg2SIrFgS/W.NEPMj2iqxQqfQt9dG',1);
SELECT * FROM projet.utilisateurs;

SELECT * FROM projet.obtenirHoraireExamen(1) 
	t(code_examen VARCHAR,nom VARCHAR, dateDebut TIMESTAMP, duree INTEGER, locaux VARCHAR);


SELECT projet.ajouterInscriptionExamenBloc(1);
SELECT * FROM projet.ajouterInscriptionExamen('IPL200','1');
SELECT * FROM projet.ajouterDateExamen('IPL100','2020-12-25 09:00:00');
SELECT * FROM projet.ajouterDateExamen('IPL200','2020-12-25 10:00:00');
SELECT * FROM projet.obtenirHeureFin('IPL100','IPL200');