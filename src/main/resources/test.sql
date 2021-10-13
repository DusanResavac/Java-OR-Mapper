drop database if exists school;
create database school;

use school;


CREATE TABLE TEACHERS
(
    ID        VARCHAR(24) PRIMARY KEY,
    NAME      TEXT,
    FIRSTNAME TEXT,
    GENDER    INTEGER,
    BDATE     TIMESTAMP,
    HDATE     TIMESTAMP default NOW(),
    SALARY    INTEGER
);

CREATE TABLE CLASSES
(
    ID       VARCHAR(24) PRIMARY KEY,
    NAME     TEXT,
    KTEACHER VARCHAR(24) NOT NULL
);

CREATE TABLE STUDENTS
(
    ID        VARCHAR(24) PRIMARY KEY,
    NAME      TEXT,
    FIRSTNAME TEXT,
    GENDER    INTEGER,
    BDATE     TIMESTAMP,
    KCLASS    VARCHAR(24),
    GRADE     INTEGER,
    FOREIGN KEY (KCLASS) REFERENCES CLASSES (ID)
        ON DELETE CASCADE
);

CREATE TABLE COURSES
(
    ID       VARCHAR(24) NOT NULL PRIMARY KEY,
    HACTIVE  INTEGER     NOT NULL DEFAULT 0,
    NAME     TEXT,
    KTEACHER VARCHAR(24) NOT NULL,
    FOREIGN KEY (KTEACHER) REFERENCES TEACHERS (ID)
        ON DELETE CASCADE
);

CREATE TABLE STUDENT_COURSES
(
    KSTUDENT VARCHAR(24) NOT NULL,
    KCOURSE  VARCHAR(24) NOT NULL,
    FOREIGN KEY (KSTUDENT) REFERENCES STUDENTS (ID)
        ON DELETE CASCADE,
    FOREIGN KEY (KCOURSE) REFERENCES COURSES (ID)
        ON DELETE CASCADE
);