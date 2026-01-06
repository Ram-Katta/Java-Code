-- Drop tables if they already exist (ignore errors if not present)
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE departments';
EXCEPTION
  WHEN OTHERS THEN NULL;
END;
/

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE employees';
EXCEPTION
  WHEN OTHERS THEN NULL;
END;
/

-- Create DEPARTMENTS table
CREATE TABLE departments (
  dept_id     NUMBER PRIMARY KEY,
  dept_name   VARCHAR2(50)
);

-- Create EMPLOYEES table
CREATE TABLE employees (
  emp_id      NUMBER PRIMARY KEY,
  emp_name    VARCHAR2(50),
  salary      NUMBER(10,2),
  dept_id     NUMBER,
  CONSTRAINT fk_emp_dept
    FOREIGN KEY (dept_id)
    REFERENCES departments(dept_id)
);
