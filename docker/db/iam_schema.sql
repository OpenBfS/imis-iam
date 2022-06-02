SET search_path = keycloak, pg_catalog;

CREATE TABLE mail_list(
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL
);
GRANT SELECT, INSERT, UPDATE, DELETE ON mail_list TO keycloak;
GRANT USAGE, SELECT ON SEQUENCE mail_list_id_seq TO keycloak;

CREATE TABLE mail_list_user(
    id SERIAL PRIMARY KEY,
    mail_list_id INT REFERENCES mail_list(id) NOT NULL,
    user_id VARCHAR(36) REFERENCES user_entity(id) NOT NULL,
    UNIQUE(mail_list_id, user_id)
);
GRANT SELECT, INSERT, UPDATE, DELETE ON mail_list_user TO keycloak;
GRANT USAGE, SELECT ON SEQUENCE mail_list__user_id_seq TO keycloak;

CREATE TABLE mail_type(
    id INT PRIMARY KEY,
    name VARCHAR NOT NULL
);
GRANT SELECT, INSERT, UPDATE, DELETE ON mail_type TO keycloak;

CREATE TABLE mail(
    id SERIAL PRIMARY KEY,
    send_date timestamp with time zone NOT NULL DEFAULT NOW(),
    expiry_date timestamp with time zone,
    sender VARCHAR NOT NULL,
    text VARCHAR NOT NULL,
    subject VARCHAR NOT NULL,
    publish BOOLEAN NOT NULL DEFAULT true,
    type INT REFERENCES mail_type(id) NOT NULL,
    receipient INT references mail_list(id) NOT NULL,
    user_id VARCHAR(36) REFERENCES user_entity(id) NOT NULL
);
GRANT SELECT, INSERT, UPDATE, DELETE ON mail TO keycloak;
GRANT USAGE, SELECT ON SEQUENCE mail_id_seq TO keycloak;

INSERT INTO mail_type(id, name)
VALUES
    (1, 'Einladung'),
    (2, 'Umfrage'),
    (3, 'Störung'),
    (4, 'Wartung'),
    (5, 'Sprechstunde'),
    (6, 'Release Note'),
    (7, 'Soll/Ist-Vergleich'),
    (8, 'Hinweis');