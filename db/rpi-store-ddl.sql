drop table if exists inventory;
drop table if exists model;

create table model (
    id integer primary key,
    name varchar(20),
    model varchar(20),
    soc varchar(20),
    memory_mb integer,
    ethernet boolean,
    release_year integer
);

create table inventory (
    id integer primary key,
    model_id integer references model (id),
    quantity integer
);
