drop database if exists school;
create database school;

use school;

create table MAUSRAEDER
(
    id   varchar(24) primary key,
    material varchar(30)
);

create table MAEUSE
(
    id      integer primary key,
    mausrad varchar(24),
    material varchar(30),
    foreign key (mausrad) references MAUSRAEDER (id)
);

CREATE TABLE TEACHERS
(
    ID        VARCHAR(24) PRIMARY KEY,
    NAME      TEXT,
    FIRSTNAME TEXT,
    GENDER    INTEGER,
    BDATE     DATETIME,
    HDATE     DATETIME default NOW(),
    SALARY    INTEGER
);

CREATE TABLE CLASSES
(
    ID       VARCHAR(24) PRIMARY KEY,
    NAME     TEXT,
    KTEACHER VARCHAR(24) NOT NULL,
    foreign key (KTEACHER) references teachers (ID)
        ON DELETE CASCADE
);

CREATE TABLE STUDENTS
(
    ID        VARCHAR(24) PRIMARY KEY,
    NAME      TEXT,
    FIRSTNAME TEXT,
    GENDER    INTEGER,
    BDATE     DATETIME,
    KCLASS    VARCHAR(24),
    GRADE     INTEGER,
    FOREIGN KEY (KCLASS) REFERENCES CLASSES (ID)
        ON DELETE CASCADE
);

CREATE TABLE COURSES
(
    ID       VARCHAR(24) NOT NULL PRIMARY KEY,
    ACTIVE  INTEGER     NOT NULL DEFAULT 0,
    NAME     TEXT,
    KTEACHER VARCHAR(24),
    FOREIGN KEY (KTEACHER) REFERENCES TEACHERS (ID)
        ON DELETE CASCADE
);

CREATE TABLE STUDENTS_COURSES
(
    KSTUDENT VARCHAR(24) NOT NULL,
    KCOURSE  VARCHAR(24) NOT NULL,
    FOREIGN KEY (KSTUDENT) REFERENCES STUDENTS (ID)
        ON DELETE CASCADE,
    FOREIGN KEY (KCOURSE) REFERENCES COURSES (ID)
        ON DELETE CASCADE
);