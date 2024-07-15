create table if not exists book (
     id bigserial not null,
     name varchar not null,
     isbn varchar not null,
     primary key (id),
     UNIQUE (isbn)
);