package com.metao.annotation;
import org.junit.jupiter.api.Test;
import com.metao.annotations.PersonBuilder;
public class PersonBuilderUnitTest {

    @Test
    public void whenBuildPersonWithBuilder_thenObjectHasPropertyValues() {

        PersonBuilder.Person mehrdad = new PersonBuilder()
                .setAddress("Munich")
                .setAge(10)
                .setName("Mehrdad")
                .setPhoneNumber("1912313")
                .build();
    }

}
