-- ===============================
-- Drop tables if they already exist
-- ===============================

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE employees CASCADE CONSTRAINTS';
EXCEPTION
  WHEN OTHERS THEN NULL;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE departments CASCADE CONSTRAINTS';
EXCEPTION
  WHEN OTHERS THEN NULL;
END;
/

-- ===============================
-- Create tables
-- ===============================

CREATE TABLE departments (
  dept_id    NUMBER PRIMARY KEY,
  dept_name  VARCHAR2(50) NOT NULL
);

CREATE TABLE employees (
  emp_id     NUMBER PRIMARY KEY,
  emp_name   VARCHAR2(50) NOT NULL,
  salary     NUMBER(10,2),
  dept_id    NUMBER,
  CONSTRAINT fk_employees_dept
    FOREIGN KEY (dept_id)
    REFERENCES departments(dept_id)
);
