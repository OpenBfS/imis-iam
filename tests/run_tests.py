#!/usr/bin/python3
"""
Usage: ./run_tests.py --help
"""

import sys
import os
import argparse
import pytest

from textwrap import dedent


# Test suites mapping
TEST_SUITES = {
    'user': 'backend/test_user_api.py',
    'institution': 'backend/test_institution_api.py',
    'mail': 'backend/test_mail_api.py',
    'event': 'backend/test_event_api.py',
    'export': 'backend/test_export_api.py',
    'network': 'backend/test_network_api.py',
    'frontend': 'frontend/test_frontend.py',
}


def main():
    """Main test runner"""
    parser = argparse.ArgumentParser(
        description='Run Keycloak iam_spi API tests',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=dedent("""
            Test Suites:
                user          User API tests
                institution   Institution API tests
                mail          Mail API tests
                event         Event API tests
                export        Export API tests
                network       Network API tests
                frontend      Frontend tests
                backend       All backend API tests
                all           All test suites (default)

            Examples:
                %(prog)s                    Run all tests
                %(prog)s user               Run user API tests only
                %(prog)s backend            Run all backend API tests
                %(prog)s frontend           Run frontend tests only
                %(prog)s --verbose          Run all tests with verbose output
                %(prog)s user institution   Run user and institution tests
        """)
    )

    parser.add_argument(
        'suites',
        nargs='*',
        default=['all'],
        choices=list(TEST_SUITES.keys()) + ['all', 'backend'],
        help='Test suite(s) to run (default: all)'
    )

    parser.add_argument(
        '-v', '--verbose',
        action='store_true',
        help='Verbose output'
    )

    parser.add_argument(
        '-k', '--keyword',
        help='Run tests matching keyword expression'
    )

    args = parser.parse_args()

    # Change to tests directory
    tests_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(tests_dir)

    # Build pytest arguments
    pytest_args = ['--color=yes', '-v']
    if args.keyword:
        pytest_args.extend(['-k', args.keyword])

    # Add test files
    if 'all' in args.suites:
        # Run all test files (backend and frontend)
        pytest_args.extend(['backend/', 'frontend/'])
    elif 'backend' in args.suites:
        # Run all backend tests
        pytest_args.append('backend/')
        # If other specific suites are also requested, add them
        for suite in args.suites:
            if suite != 'backend' and suite in TEST_SUITES:
                test_file = TEST_SUITES[suite]
                if os.path.exists(test_file):
                    pytest_args.append(test_file)
    else:
        # Run specific suites
        for suite in args.suites:
            test_file = TEST_SUITES.get(suite)
            if test_file and os.path.exists(test_file):
                pytest_args.append(test_file)

    # Run pytest
    exit_code = pytest.main(pytest_args)

    return exit_code


if __name__ == '__main__':
    sys.exit(main())
