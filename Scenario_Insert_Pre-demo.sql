/*
INSERT PRE DEMO
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


/*
INSERT SCENARIO 1
*/

INSERT INTO projet.utilisateurs(id_utilisateur,nom_utilisateur,email,mot_de_passe,id_bloc)
	VALUES (DEFAULT,'Damas','Damas@email.be','DamasCode',1);
INSERT INTO projet.utilisateurs(id_utilisateur,nom_utilisateur,email,mot_de_passe,id_bloc)
	VALUES (DEFAULT,'Ferneeuw','Ferneeuw@email.be','FerneeuxCode',2);
INSERT INTO projet.utilisateurs(id_utilisateur,nom_utilisateur,email,mot_de_passe,id_bloc)
	VALUES (DEFAULT,'Cambron','Cambron@email.be','CambronCode',2);