INSERT INTO iam_mail_type (id, name) VALUES
    (1, 'Einladung'),
    (2, 'Umfrage'),
    (3, 'Störung'),
    (4, 'Wartung'),
    (5, 'Sprechstunde'),
    (6, 'Release Note'),
    (7, 'Soll/Ist-Vergleich'),
    (8, 'Hinweis');

INSERT INTO iam_institution_tag (name) VALUES
    ('Bundesministerium'),
    ('Leitstelle'),
    ('Bundesbehörde'),
    ('Landesministerium'),
    ('Landesdatenzentrale'),
    ('Landesmessstelle'),
    ('Landesmessstelle REI'),
    ('Betreiber KTA'),
    ('Aufsichtsbehörde'),
    ('Sonstige'),
    ('Alle');

INSERT INTO keycloak.iam_institution(
    name,
    meas_facil_name,
    service_building_street,
    service_building_postal_code,
    service_building_location,
    service_building_state,
    central_phone,
    central_mail,
    meas_facil_id
) VALUES
    ('Institution 1', 'inst_1', 'Examplestreet 1', '12345', 'ExampleLocation-1', 'berlin', '0123/456789', 'inst1@example.test', 'inst1'),
    ('Institution 2', 'inst_2', 'Examplestreet 2', '22345', 'ExampleLocation-2', 'berlin', '0123/456789', 'inst2@example.test', 'inst2');

INSERT INTO keycloak.iam_user_attributes (id, expiry_date)
    SELECT id, current_date + interval '12 month'
    FROM keycloak.user_entity WHERE username IN (
        'exampleuser', 'redakteur', 'chefredakteur');

INSERT INTO keycloak.iam_institution_user(user_id, institution_id) VALUES
((SELECT id FROM keycloak.user_entity WHERE username = 'exampleuser'), (SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 1')),
((SELECT id FROM keycloak.user_entity WHERE username = 'redakteur'), (SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 1')),
((SELECT id FROM keycloak.user_entity WHERE username = 'chefredakteur'), (SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 1'));

INSERT INTO keycloak.iam_institution_tags(institution_id, tag_name) VALUES
(((SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 1')), 'Bundesministerium'),
(((SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 2')), 'Bundesbehörde');

INSERT INTO keycloak.iam_institution_central_alarm_mail_addresses(institution_id, mail) VALUES
(((SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 1')), 'inst1_alert@example.test'),
(((SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 2')), 'inst2_alert@example.test');

INSERT INTO keycloak.iam_institution_central_alarm_phone_numbers(institution_id, phone) VALUES
(((SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 1')), '0123/111111'),
(((SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 1')), '0123/101010'),
(((SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 2')), '0123/222222');
