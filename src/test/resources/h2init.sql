CREATE SCHEMA IF NOT EXISTS public;
SET SCHEMA public;

CREATE TABLE IF NOT EXISTS TASKS (ID varchar(36) NOT NULL UNIQUE, NAME varchar(255), VERSION integer, PRIMARY KEY (ID));