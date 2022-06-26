
DROP TABLE DEPT_INFO CASCADE;

CREATE TABLE DEPT_INFO 
( 
  DEPT_ID INTEGER NOT NULL, 
  DEPT_NAME VARCHAR(50), 
  PRIMARY KEY (DEPT_ID) 
);

DROP TABLE COURSE_INFO CASCADE;

CREATE TABLE COURSE_INFO 
( 
  COURSE_ID INTEGER NOT NULL, 
  COURSE_NAME VARCHAR(50), 
  OFFERED_DEPT INTEGER, 
  GRADUATE_LEVEL SMALLINT, 
  PRIMARY KEY (COURSE_ID),
  FOREIGN KEY (OFFERED_DEPT) REFERENCES DEPT_INFO (DEPT_ID) 
);


DROP TABLE OFFICE_INFO CASCADE;

CREATE TABLE OFFICE_INFO 
( 
  OFFICE_ID INTEGER NOT NULL, 
  OFFICE_NAME VARCHAR(50), 
  HAS_PRINTER SMALLINT, 
  PRIMARY KEY (OFFICE_ID) 
);

DROP TABLE RACE_INFO CASCADE;

CREATE TABLE RACE_INFO 
( 
  RACE_CODE INTEGER NOT NULL, 
  RACE VARCHAR(50),
  PRIMARY KEY (RACE_CODE) 
);

DROP TABLE USER_INFO CASCADE;

CREATE TABLE USER_INFO 
( 
  USER_ID VARCHAR(50) NOT NULL, 
  FIRST_NAME VARCHAR(50), 
  LAST_NAME VARCHAR(50), 
  SEX VARCHAR(1), 
  DEPT_ID INTEGER,
  OFFICE_ID INTEGER, 
  GRADUATE SMALLINT, 
  RACE INTEGER, 
  PASSWORD VARCHAR(50) NOT NULL, 
  YEARS_USING_UNIX INTEGER, 
  ENROLL_DATE DATE,
  PRIMARY KEY (USER_ID), 
  FOREIGN KEY (DEPT_ID) REFERENCES DEPT_INFO (DEPT_ID),
  FOREIGN KEY (OFFICE_ID) REFERENCES OFFICE_INFO (OFFICE_ID),
  FOREIGN KEY (RACE) REFERENCES RACE_INFO (RACE_CODE)
);

DROP TABLE TRANSCRIPT CASCADE;

CREATE TABLE TRANSCRIPT 
( 
  USER_ID VARCHAR(50) NOT NULL, 
  COURSE_ID INTEGER NOT NULL, 
  SCORE INTEGER, 
  PRIMARY KEY (USER_ID, COURSE_ID),
  FOREIGN KEY (USER_ID) REFERENCES USER_INFO (USER_ID),
  FOREIGN KEY (COURSE_ID) REFERENCES COURSE_INFO (COURSE_ID)	 
);

DROP TABLE UNIX_COMMAND CASCADE;

CREATE TABLE UNIX_COMMAND 
( 
  UNIX_COMMAND VARCHAR(50) NOT NULL,
  CATEGORY VARCHAR(50), 
  PRIMARY KEY (UNIX_COMMAND) 
);

DROP TABLE USAGE_HISTORY CASCADE;

CREATE TABLE USAGE_HISTORY 
( 
  USER_ID VARCHAR(50) NOT NULL, 
  SESSION_ID INTEGER, 
  LINE_NO INTEGER, 
  COMMAND_SEQ INTEGER, 
  COMMAND VARCHAR(50),
  FOREIGN KEY (USER_ID) REFERENCES USER_INFO (USER_ID) 
);



-- ALTER TABLE COURSE_INFO ADD FOREIGN KEY (OFFERED_DEPT) REFERENCES DEPT_INFO (DEPT_ID);
-- ALTER TABLE TRANSCRIPT ADD FOREIGN KEY (USER_ID) REFERENCES USER_INFO (USER_ID);
-- ALTER TABLE TRANSCRIPT ADD FOREIGN KEY (COURSE_ID) REFERENCES COURSE_INFO (COURSE_ID);
-- ALTER TABLE USAGE_HISTORY ADD FOREIGN KEY (USER_ID) REFERENCES USER_INFO (USER_ID);
-- ALTER TABLE USER_INFO ADD FOREIGN KEY (DEPT_ID) REFERENCES DEPT_INFO (DEPT_ID);
-- ALTER TABLE USER_INFO ADD FOREIGN KEY (OFFICE_ID) REFERENCES OFFICE_INFO (OFFICE_ID);
-- ALTER TABLE USER_INFO ADD FOREIGN KEY (RACE) REFERENCES RACE_INFO (RACE_CODE);
