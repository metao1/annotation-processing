package com.metao.annotation;

import com.metao.annotations.PersonBuilder;
import org.junit.jupiter.api.Test;

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

}
