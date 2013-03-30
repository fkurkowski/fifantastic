# --- Just test data

# --- !Ups
insert into player values (1, 'Bruce Grannec');
insert into player values (2, 'Alfonso Ramos');

insert into record values (1, 82, 11, 4, 192, 68, 1);

insert into record values (2, 65, 15, 3, 224, 81, 2);

insert into team values (1, 'Real Madrid');
insert into team values (2, 'Manchester City');

insert into match values (1, 1, 1, 2, 2, 2, 2);
insert into match values (2, 1, 2, 1, 2, 1, 0);
insert into match values (3, 1, 1, 1, 2, 1, 2);


# --- !Downs
delete from record;
delete from player;
delete from team;
delete from match;