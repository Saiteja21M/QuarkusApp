CREATE TABLE subject (
    subject_id SERIAL PRIMARY KEY,
    telugu INT DEFAULT 0,
    hindi INT DEFAULT 0,
    english INT DEFAULT 0,
    maths INT DEFAULT 0
);

CREATE TABLE student (
    student_id SERIAL PRIMARY KEY,
    age INT,
    name VARCHAR(255),
    total_marks INT,
    subject_id INT,
    CONSTRAINT fk_student_subject FOREIGN KEY (subject_id) REFERENCES subject(subject_id)
);