package com.metao.annotation;

import com.metao.annotations.PersonBuilder;
import com.metao.annotations.StudentBuilder;
import org.junit.jupiter.api.Test;

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
//        PersonBuilder.Person person = new PersonBuilder()
//                .setAddress("Munich")
//                .setAge(10)
//                .setName("Mehrdad")
//                .setPhoneNumber("+11912313232")
//                .build();
//        StudentBuilder.Student student = new StudentBuilder().setNumber(1000).setPerson(person).build();
//        com.metao.annotations.UniversityBuilder.University university = new com.metao.annotations
//                .UniversityBuilder().setStudents(List.of(student)).build();
//
//        assertEquals("Mehrdad", university.getStudents().get(0).getPerson().getName());
//
//        assertEquals("Mehrdad", student.getPerson().getName());
    }

}
