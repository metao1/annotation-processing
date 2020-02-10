package com.metao.annotation.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("com.metao.annotation.processor.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Collection<? extends Element> annotatedStateDefinitions = roundEnv.getElementsAnnotatedWith(Builder.class);
        for (Element element : annotatedStateDefinitions) {
            PackageElement packageElement = getPackage(element);
            info("STATE ENUM " + packageElement.getQualifiedName() + "." + element.getSimpleName());
            if (element.getKind() != ElementKind.CLASS) {
                error("The annotation @BuilderProperty must only be applied on Class: ", element);
            } else {
                checkForElements(element);
            }
        }
        return true;
    }

    private void checkForElements(Element type) {
        List<Element> fields = new ArrayList<>();

        type.getEnclosedElements().stream()
                .filter(o -> o.getKind().equals(ElementKind.FIELD))
                .forEach(fields::add);
        String className = type.getSimpleName().toString();

        if (fields.size() == 0) {
            error("A class with no field is not accepted", type);
        } else {
            try {
                writeBuilderFile(className, fields);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void info(String message) {
        this.messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    private void error(String error, Element element) {
        this.messager.printMessage(Diagnostic.Kind.ERROR, error, element);
    }

    private PackageElement getPackage(Element element) {
        Element enclosing = element;
        while (enclosing.getKind() != ElementKind.PACKAGE) {
            enclosing = enclosing.getEnclosingElement();
        }
        return (PackageElement) enclosing;
    }

    private void writeBuilderFile(String className, List<Element> fields) throws IOException {

        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }else {
            packageName = "com.metao.annotations";
        }
        String simpleClassName = className.substring(lastDot + 1);
        String builderClassName = className + "Builder";
        String builderSimpleClassName = builderClassName.substring(lastDot + 1);

        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(builderClassName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

            if (packageName != null) {
                out.print("package ");
                out.print(packageName);
                out.println(";");
                out.println();
            }

            out.print("public class ");
            out.print(builderSimpleClassName);
            out.println(" {");
            out.println();
            out.print("    private final ");
            out.print(simpleClassName);
            out.print(" object = new ");
            out.print(simpleClassName);
            out.println("();");
            out.println();

            createInnerClass(out, className, fields);
            out.print("    public ");
            out.print(simpleClassName);
            out.println(" build() {");
            out.println("        return object;");
            out.println("    }");
            out.println();

            fields.forEach(element -> {
                if (element == null) {
                    return;
                }
                String methodName = capitalize(element.toString());
                String setterMethod = "set" + methodName;

                out.print("    public ");
                out.print(builderSimpleClassName);
                out.print(" ");
                out.print(setterMethod);

                out.print("(");

                out.print(element.asType());
                out.println(" value) {");
                out.print("        object.");
                out.print(setterMethod);
                out.println("(value);");
                out.println("        return this;");
                out.println("    }");
                out.println();
            });

            out.println("}");

        }
    }

    private void createInnerClass(PrintWriter out, String className, List<Element> fields) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("   public class ");
        stringBuffer.append(className);
        stringBuffer.append(" {");
        stringBuffer.append("\n");
        fields.forEach(field -> {
            stringBuffer.append("        ");
            stringBuffer.append("private " + field.asType() + " ");
            stringBuffer.append(field.getSimpleName().toString() + ";\n");
        });

        stringBuffer.append("        ");
        stringBuffer.append(className);
        stringBuffer.append("(");
        stringBuffer.append(") {}");
        stringBuffer.append("\n");
        fields.forEach(field -> {
            if (field == null) {
                return;
            }
            stringBuffer.append("        ");
            stringBuffer.append("private void set");
            String fieldName = field.getSimpleName().toString();
            String methodName = capitalize(fieldName);
            stringBuffer.append(methodName + "(" + field.asType() + " " + fieldName + ") {");
            stringBuffer.append(" this." + fieldName + "=" + fieldName + ";}\n");
            stringBuffer.append("        ");
            stringBuffer.append("public " + field.asType() + " get");
            fieldName = field.getSimpleName().toString();
            methodName = capitalize(fieldName);
            stringBuffer.append(methodName + "() {");
            stringBuffer.append(" return this." + fieldName + ";}\n");
        });
        stringBuffer.append("    }");
        stringBuffer.append("\n");
        out.print(stringBuffer.toString());
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
