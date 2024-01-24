INSERT INTO keycloak.iam_user_attributes (id, expiry_date)
    SELECT id, current_date + interval '12 month'
    FROM keycloak.user_entity WHERE username IN (
        'exampleuser', 'redakteur', 'chefredakteur');
