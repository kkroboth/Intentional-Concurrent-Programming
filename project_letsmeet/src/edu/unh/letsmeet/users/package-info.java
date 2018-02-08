/**
 * SQlite implementation for storing user information and their associated
 * credentials. Passwords are stored using a secure hashing function and random
 * salt.
 * <p>
 * The purpose of this package is to test 3rd party threaded libraries with ICP.
 * Sqlite driver in this case. Unlike the database package, all interactions are
 * piped to sqlite.
 */
package edu.unh.letsmeet.users;