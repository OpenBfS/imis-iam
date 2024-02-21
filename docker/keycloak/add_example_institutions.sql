INSERT INTO keycloak.iam_institution(name, short_name, service_building_street, service_building_postal_code, service_building_location, central_phone, central_mail, imis_id, imis_usergroup_id) VALUES
    ('Institution 1', 'inst_1', 'Examplestreet 1', '12345', 'ExampleLocation-1', '0123/456789', 'inst1@example.test', 'inst1', 'ABC'),
    ('Institution 2', 'inst_2', 'Examplestreet 2', '22345', 'ExampleLocation-2', '0123/456789', 'inst2@example.test', 'inst2', 'DEF');

INSERT INTO keycloak.iam_institution_user(user_id, institution_id) VALUES
((SELECT id FROM keycloak.user_entity WHERE username = 'exampleuser'), (SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 1')),
((SELECT id FROM keycloak.user_entity WHERE username = 'redakteur'), (SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 1')),
((SELECT id FROM keycloak.user_entity WHERE username = 'chefredakteur'), (SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 1'));

INSERT INTO keycloak.iam_institution_categories(institution_id, category_name) VALUES
(((SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 1')), 'Bundesministerium'),
(((SELECT id FROM keycloak.iam_institution WHERE name = 'Institution 2')), 'Bundesbehörde');

