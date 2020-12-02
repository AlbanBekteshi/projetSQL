CREATE OR REPLACE FUNCTION projet.ajouterLocal(id_local VARCHAR(10), capacite INT,machine CHAR(1)) RETURNS VOID AS $$
DECLARE
	v_id_local ALIAS FOR $1;
	v_capacite ALIAS FOR $2;
	v_machine ALIAS FOR $3;
BEGIN
	INSERT INTO projet.locaux VALUES
		(v_id_local,v_capacite,v_machine);
	RETURN;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION projet.verifie_ajouterLocal() RETURNS TRIGGER AS $$
	BEGIN
		IF((SELECT NEW.capacite FROM projet.locaux l)<=0) THEN 
		RAISE EXCEPTION 'Capacité doit être > que 0'; 
		END IF;
		RETURN NEW;
	END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trigger_verifie_ajouterLocal BEFORE INSERT ON projet.locaux
	FOR EACH ROW EXECUTE PROCEDURE projet.verifie_ajouterLocal();


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





CREATE OR REPLACE FUNCTION projet.ajouterLocauxExamens(id_localN VARCHAR(10), code_examenN CHARACTER(6)) RETURNS VOID AS $$
DECLARE
	n_id_Local ALIAS FOR $1;
	n_code_examen ALIS FOR $2;
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









CREATE OR REPLACE FUNCTION projet.ajouterDateExamen(code_examenN CHARACTER(6),dateN timestamp) RETURNS BOOLEAN AS $$
DECLARE
	dateFinNouveauExamen timestamp:=0;
	examen RECORD;
	dateDebutVerifExam timestamp:=0;
	dateFinVerifExam timestamp:=0;
BEGIN
	SELECT TIMESTAMPADD(MINUTE,(SELECT e.duree FROM projet.examen e WHERE e.code_examen=code_examenN),dateN) INTO dateFinNouveauExamen;
	
	FOR examen IN SELECT * FROM projet.examens e WHERE dateN::TIMESTAMP::DATE = e.date::TIMESTAMP::DATE LOOP
		SELECT date FROM examen INTO dateDebutVerifExam;
		SELECT TIMESTAMPADD(MINUTE,(SELECT duree FROM examen),(SELECT date FROM examen)) INTO dateFinVerifExam;

		--examen qui commence avant et termine pendant celui ajouté
		IF((dateDebutVerifExam::TIMESTAMP::TIME < dateN::TIMESTAMP::TIME) AND (dateFinVerifExam::TIMESTAMP::TIME > dateN::TIMESTAMP::TIME)) THEN
			RAISE 'Conflit Horaire (examen précédent pas encore terminé)';
		END IF;

		--examen qui commence avant et termine après celui ajouté
		IF((dateDebutVerifExam::TIMESTAMP::TIME < dateN::TIMESTAMP::TIME) AND (dateFinNouveauExamen::TIMESTAMP::TIME < dateFinVerifExam::TIMESTAMP::TIME)) THEN
			RAISE 'Conflit Horaire (examen précédent termine après cet examen)';
		END IF;

		--examen qui commence avant la fin de celui ajout et termine après
		IF((dateDebutVerifExam::TIMESTAMP::TIME < dateFinNouveauExamen::TIMESTAMP::TIME) AND (dateFinVerifExam::TIMESTAMP::TIME > dateFinNouveauExamen::TIMESTAMP::TIME)) THEN
			RAISE 'Conflit Horaire (examen débute au milieu de l examen ajouté et termine après)';
		END IF;

		--examen qui commence après le début de celui ajoute et termine avant
		IF((dateN::TIMESTAMP::TIME < dateDebutVerifExam::TIMESTAMP::TIME) AND (dateFinVerifExam::TIMESTAMP::TIME < dateFinNouveauExamen::TIMESTAMP::TIME)) THEN
			RAISE 'Conflit Horaire (examen débute au milieu de l examen ajouté et termine avant)';
		END IF;

	END LOOP;

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




