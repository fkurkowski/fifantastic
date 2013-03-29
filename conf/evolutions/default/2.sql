# --- Just test data

# --- !Ups
insert into player(id, name) values (1, 'Bruce Grannec');
insert into player(id, name) values (2, 'Alfonso Ramos Cuevas');

insert into record (id, wins, draws, losses, goals_scored, goals_conceded, player_id) 
	values (1, 82, 11, 4, 192, 68, 1);

insert into record (id, wins, draws, losses, goals_scored, goals_conceded, player_id) 
	values (2, 65, 15, 3, 224, 81, 2);

insert into team(id, name) values (1, 'Real Madrid');


# --- !Downs
delete from record;
delete from player;
delete from team;