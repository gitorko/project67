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
