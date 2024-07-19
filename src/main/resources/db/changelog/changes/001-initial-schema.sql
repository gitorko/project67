create table booking
(
    id        bigserial not null primary key,
    customer  varchar(255),
    payload   text,
    created_on timestamp(6)
);

create table booking_event
(
    id         bigserial not null primary key,
    booking_id bigint,
    event      varchar(255),
    created_on  timestamp(6)
);


create table employee
(
    id              bigserial not null primary key,
    name            varchar(255),
    department      varchar(255),
    salary          numeric(10,2),
    employment_type VARCHAR(50)
);
