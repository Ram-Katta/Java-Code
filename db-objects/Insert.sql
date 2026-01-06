-- Insert into DEPARTMENTS
INSERT INTO departments (dept_id, dept_name)
VALUES (10, 'HR');

INSERT INTO departments (dept_id, dept_name)
VALUES (20, 'IT');

INSERT INTO departments (dept_id, dept_name)
VALUES (30, 'Finance');

-- Insert into EMPLOYEES
INSERT INTO employees (emp_id, emp_name, salary, dept_id)
VALUES (1, 'Alice', 50000, 10);

INSERT INTO employees (emp_id, emp_name, salary, dept_id)
VALUES (2, 'Bob', 65000, 20);

INSERT INTO employees (emp_id, emp_name, salary, dept_id)
VALUES (3, 'Charlie', 70000, 20);

INSERT INTO employees (emp_id, emp_name, salary, dept_id)
VALUES (4, 'Diana', 60000, 30);

COMMIT;
