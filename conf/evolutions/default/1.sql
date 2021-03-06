# --- Original database

# --- !Ups

set ignorecase true;

create table player (
	id 									bigint not null auto_increment,
	name								varchar(255) not null,
	wins								int not null,
	draws								int not null,
	losses							int not null,
	goals_scored				int not null,
	goals_conceded			int not null,
	constraint pk_player primary key (id))
;

create table team (
	id 									bigint not null auto_increment,
	name								varchar(255) not null,
	constraint pk_team primary key (id))
;

create table match (
	id 									bigint not null auto_increment,
	when			 					datetime not null,
	home_player_id 			bigint not null,
	home_team_id				bigint not null,
	home_goals					int not null,
	away_player_id			bigint not null,
	away_team_id				bigint not null,
	away_goals					int not null,
	constraint pk_match primary key (id),
	constraint fk_home_player foreign key (home_player_id) references player(id),
	constraint fk_home_team foreign key (home_team_id) references team(id),
	constraint fk_away_player foreign key (away_player_id) references player(id),
	constraint fk_away_team foreign key (away_team_id) references team(id))
;


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists player;

drop table if exists team;

drop table if exists match;

SET REFERENTIAL_INTEGRITY TRUE;


