create table vacancies
(
    id            serial primary key,
    title         varchar not null,
    description   varchar,
    creation_date timestamp,
    visible       boolean not null,
    city_id       int references cities(id),
    file_id       int references files(id)
);