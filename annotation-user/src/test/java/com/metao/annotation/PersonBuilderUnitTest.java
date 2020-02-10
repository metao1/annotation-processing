package com.metao.annotation;

import com.metao.annotations.PersonBuilder;
import com.metao.annotations.StudentBuilder;
import com.metao.annotations.TeacherBuilder;
import com.metao.annotations.UniversityBuilder;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonBuilderUnitTest {

    @Test
    public void whenBuildPersonWithBuilder_thenObjectHasPropertyValues() {

        PersonBuilder.Person mehrdad = new PersonBuilder()
                .setAddress("Munich")
                .setAge(10)
                .setName("Mehrdad")
                .setPhoneNumber("+11912313232")
                .build();

        assertEquals(10, mehrdad.getAge());
        assertEquals("Mehrdad", mehrdad.getName());
        assertEquals("+11912313232", mehrdad.getPhoneNumber());
    }

    @Test
    public void testUniversity() {
        List<TeacherBuilder.Teacher> teachers = new LinkedList<>();
        List<StudentBuilder.Student> students = new LinkedList<>();

        PersonBuilder.Person studentPerson = new PersonBuilder()
                .setAddress("Munich")
                .setAge(10)
                .setName("Mehrdad")
                .setPhoneNumber("+11912313232")
                .build();
        PersonBuilder.Person teacherPerson = new PersonBuilder()
                .setAddress("Munich")
                .setAge(42)
                .setPhoneNumber("+1882882313")
                .setName("teacher")
                .build();

        StudentBuilder.Student student = new StudentBuilder()
                .setNumber(1000)
                .setPerson(studentPerson)
                .build();

        students.add(student);

        assertEquals("Mehrdad", student.getPerson().getName());

        TeacherBuilder.Teacher teacher = new TeacherBuilder()
                .setPerson(teacherPerson)
                .setNumber(100)
                .setName("teacher")
                .build();
        teachers.add(teacher);
        UniversityBuilder.University university = new UniversityBuilder()
                .setTeachers(teachers).setStudents(students).build();

        assertEquals(university.getStudents().size(), 1);
        assertEquals(university.getTeachers().size(), 1);
    }

}
