#!/usr/bin/python3
"""
Database helper functions for database access
"""

import os
import psycopg2
from typing import Optional


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


def delete_institution_from_db(institution_id: str = None, institution_name: str = None, institution_facil_name: str = None) -> bool:
    """
    Delete an institution directly from the database.

    Either the numeric ID or the meas_facil_name must be given.
    """
    if not institution_id and not institution_name and not institution_facil_name:
        raise ValueError("Either the numeric ID or the meas_facil_name must be given.")

    conn = cursor = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        if not institution_id and institution_name:
            cursor.execute("SELECT id from iam_institution WHERE name = %s",
                           (institution_name, ))
            institution_id = cursor.fetchone()
        elif not institution_id and institution_facil_name:
            cursor.execute("SELECT id from iam_institution WHERE meas_facil_name = %s",
                           (institution_facil_name, ))
            institution_id = cursor.fetchone()
        if not institution_id:
            raise ValueError("Could not find the institution to delete.")

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
        conn and conn.rollback()
        cursor and cursor.close()
        conn.close()
        raise


def delete_user_from_db(user_uuid: Optional[str] = None, username: Optional[str] = None) -> bool:
    """
    Delete a user directly from the database.

    Args:
        user_uuid: The Keycloak user ID (UUID string) to delete
    """
    if not user_uuid and not username:
        raise ValueError("Either the UUID or the username must be given.")

    conn = cursor = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        if not user_uuid and username:
            cursor.execute("SELECT id from user_entity WHERE username = %s",
                           (institution_facil_name, ))
            institution_id = cursor.fetchone()
        if not user_uuid:
            raise ValueError("Could not find the user to delete.")


        # Delete in correct order to respect foreign key constraints
        cursor.execute(
            "DELETE FROM iam_institution_user WHERE user_id = %s",
            (user_uuid,)
        )
        cursor.execute(
            "DELETE FROM iam_mail WHERE user_id = %s",
            (user_uuid,)
        )
        cursor.execute(
            "DELETE FROM iam_user_attributes WHERE id = %s",
            (user_uuid,)
        )
        cursor.execute(
            "DELETE FROM user_role_mapping WHERE user_id = %s",
            (user_uuid,)
        )
        cursor.execute(
            "DELETE FROM user_attribute WHERE user_id = %s",
            (user_uuid,)
        )
        cursor.execute(
            "DELETE FROM user_entity WHERE id = %s",
            (user_uuid,)
        )

        conn.commit()
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"Error deleting user {user_uuid} from database: {str(e)}")
        conn and conn.rollback()
        cursor and cursor.close()
        conn and conn.close()
        raise


def enable_admin_direct_access_grants():
    """
    Enable direct access grants as Authentication Flow in the security-admin-console of realm master
    """
    conn = cursor = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Delete in correct order to respect foreign key constraints
        cursor.execute("""
            UPDATE client
            SET direct_access_grants_enabled = true
            WHERE
                client_id = 'security-admin-console' AND
                base_url = '/admin/master/console/'
            """
        )

        conn.commit()
        cursor.close()
        conn.close()
    except Exception as e:
        print(f"Error enabling direct access grants in master realm")
        conn and conn.rollback()
        cursor and cursor.close()
        conn and conn.close()
        raise
