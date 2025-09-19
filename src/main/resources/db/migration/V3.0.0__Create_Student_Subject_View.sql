CREATE OR REPLACE VIEW v_student_marks AS
SELECT 
    s.student_id,
    s.name,
    s.age,
    s.total_marks,
    sub.subject_id,
    sub.telugu,
    sub.hindi,
    sub.english,
    sub.maths
FROM student s
JOIN subject sub ON s.subject_id = sub.subject_id;