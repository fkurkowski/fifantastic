# --- Just test data

# --- !Ups
insert into record (id, wins, draws, losses, goals_scored, goals_conceded) 
	values (1, 82, 11, 4, 192, 68);

insert into record (id, wins, draws, losses, goals_scored, goals_conceded) 
	values (2, 65, 15, 3, 224, 81);

insert into player(name, record_id) values ('Bruce Grannec', 1);
insert into player(name, record_id) values ('Alfonso Ramos Cuevas', 2);


# --- !Downs
delete from record;
delete from player;