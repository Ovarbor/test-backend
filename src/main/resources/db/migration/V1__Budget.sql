create table author (
    id serial primary key,
    full_name text not null,
    created text not null
);

insert into author (id, full_name, created) values (0, 'default', 'default');

create table budget (
    id     serial primary key,
    year   int  not null,
    month  int  not null,
    amount int  not null,
    type   text not null,
    author_id  int references author(id)
);



