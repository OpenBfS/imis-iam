#!/usr/bin/python3
"""
Database helper functions for database access
"""

import os
import psycopg2


def get_db_connection():
    """
    Returns a connection to the Keycloak database.
    """
    return psycopg2.connect(
        # Defaults from docker-compose.dev.yml
        host=os.getenv('DB_HOST', 'localhost'),
        port=int(os.getenv('DB_PORT', '2345')),
        user=os.getenv('DB_USER', 'keycloak'),
        password=os.getenv('DB_PASSWORD', 'secret'),
        database=os.getenv('DB_NAME', 'keycloak')
    )


def delete_institution_from_db(institution_id: int) -> bool:
    """
    Delete an institution directly from the database.
    """
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Delete in correct order to respect foreign key constraints
        cursor.execute(
            "DELETE FROM iam_institution_tags WHERE institution_id = %s",
            (institution_id,)
        )
        cursor.execute(
            "DELETE FROM iam_institution_user WHERE institution_id = %s",
            (institution_id,)
        )
        cursor.execute(
            "DELETE FROM iam_institution_operation_mode_change_mail_addresses WHERE institution_id = %s",
            (institution_id,)
        )
        cursor.execute(
            "DELETE FROM iam_institution_operation_mode_change_phone_numbers WHERE institution_id = %s",
            (institution_id,)
        )
        cursor.execute(
            "DELETE FROM iam_institution_operation_mode_change_sms_phone_numbers WHERE institution_id = %s",
            (institution_id,)
        )
        cursor.execute(
            "DELETE FROM iam_institution_mail WHERE institution_id = %s",
            (institution_id,)
        )
        cursor.execute(
            "DELETE FROM iam_institution_phone WHERE institution_id = %s",
            (institution_id,)
        )
        cursor.execute(
            "DELETE FROM iam_institution WHERE id = %s",
            (institution_id,)
        )

        conn.commit()
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"Error deleting institution {institution_id} from database: {str(e)}")
        conn.rollback()
        cursor.close()
        conn.close()
        raise


def delete_user_from_db(user_id: str) -> bool:
    """
    Delete a user directly from the database.

    Args:
        user_id: The Keycloak user ID (UUID string) to delete
    """
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Delete in correct order to respect foreign key constraints
        cursor.execute(
            "DELETE FROM iam_institution_user WHERE user_id = %s",
            (user_id,)
        )
        cursor.execute(
            "DELETE FROM iam_mail WHERE user_id = %s",
            (user_id,)
        )
        cursor.execute(
            "DELETE FROM iam_user_attributes WHERE id = %s",
            (user_id,)
        )
        cursor.execute(
            "DELETE FROM user_role_mapping WHERE user_id = %s",
            (user_id,)
        )
        cursor.execute(
            "DELETE FROM user_attribute WHERE user_id = %s",
            (user_id,)
        )
        cursor.execute(
            "DELETE FROM user_entity WHERE id = %s",
            (user_id,)
        )

        conn.commit()
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"Error deleting user {user_id} from database: {str(e)}")
        conn.rollback()
        cursor.close()
        conn.close()
        raise
