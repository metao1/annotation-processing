package com.metao.annotation;

import com.metao.annotation.processor.Builder;

import java.util.List;

@Builder
public class University {

    private List<Student> students;

    private List<Teacher> teachers;
}
