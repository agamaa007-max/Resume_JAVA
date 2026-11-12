-- ==========================================================
-- BlindHire Database Reset Script
-- Drops all tables and recreates the fresh schema
-- ==========================================================

DROP DATABASE IF EXISTS blindhire;
CREATE DATABASE blindhire CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blindhire;

SOURCE schema.sql;
SOURCE sample-data.sql;
