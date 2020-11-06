DROP SCHEMA IF EXISTS projet CASCADE;
CREATE SCHEMA projet;

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
	mot_de_passe VARCHAR(100) NOT NULL (mot_de_passe<>''),
	id_bloc INTEGER REFERENCES projet.blocs (id_bloc) NOT NULL -- est-ce qu'on est sur de sa ? 
);

CREATE TABLE projet.examens(
	code_examen VARCHAR(6) PRIMARY KEY UNIQUE
		CHECK(code_examen SIMILAR TO 'IPL[0-9][0-9][0-9]'),
	nom VARCHAR(100) NOT NULL CHECK(nom<>''),
	id_bloc INTEGER REFERENCES projet.blocs (id_bloc) NOT NULL,
	duree INT(3) NOT NULL, 
	date timestamp without time zone NOT NULL,
	support CHAR(1) NOT NULL CHECK (support='m' || support='e')
);

CREATE TABLE projet.inscriptions_examens(
	code_examen INTEGER REFERENCES projet.examens (code_examen) NOT NULL,
	id_utilisateur INTEGER REFERENCES projet.utilisateurs(id_utilisateur) NOT NULL,
	PRIMARY KEY (code_examen,id_utilisateur)
);

CREATE TABLE projet.locaux (
	id_local VARCHAR(10) PRIMARY KEY UNIQUE CHECK(id_local<>''),
	capacite INT(4) NOT NULL,
	machine CHAR(1) NOT NULL CHECK (machine='o' ||machine='n')
);

CREATE TABLE projet.locaux_examens (
	id_local INTEGER REFERENCES projet.locaux (id_local) NOT NULL,
	code_examen INTEGER REFERENCES projet.examens (code_examen) NOT NULL,
	PRIMARY KEY (id_local,code_examen)
);