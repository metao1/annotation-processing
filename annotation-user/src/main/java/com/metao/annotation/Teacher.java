package com.metao.annotation;

import com.metao.annotation.processor.Builder;

@Builder
public class Teacher {
    private int number;
    private String name;
    private Person person;
}
