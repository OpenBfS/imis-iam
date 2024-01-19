INSERT INTO keycloak.iam_user_attributes (
        id, position_id, phone, mobile, fax, oe, bfs_location, expiry_date)
    SELECT id, 1, '01234 123456', '0654-321', '0123-654', 'oe', 'location',
        current_date + interval '12 month'
    FROM keycloak.user_entity WHERE username IN (
        'exampleuser', 'redakteur', 'chefredakteur');
