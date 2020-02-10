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
import java.util.*;

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
            out.print("   private final ");
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
                String filedType = reviseFieldType(element.asType().toString());
                out.print(filedType);
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
        stringBuffer.append("\tpublic class ");
        stringBuffer.append(className);
        stringBuffer.append(" {");
        stringBuffer.append("\n");
        fields.forEach(field -> {
            stringBuffer.append("\t\t");
            String fileType = reviseFieldType(field.asType().toString());
            stringBuffer.append("private " + fileType + " ");
            stringBuffer.append(field.getSimpleName().toString() + ";\n");
        });

        stringBuffer.append("\t\t");
        stringBuffer.append(className);
        stringBuffer.append("(");
        stringBuffer.append(") {}");
        stringBuffer.append("\n");
        fields.forEach(field -> {
            if (field == null) {
                return;
            }
            stringBuffer.append("\t\t");
            stringBuffer.append("private void set");
            String fieldName = field.getSimpleName().toString();
            String methodName = capitalize(fieldName);
            String filedType = field.asType().toString();
            filedType = reviseFieldType(filedType);
            stringBuffer.append(methodName).append("(").append(filedType).append(" ").append(fieldName).append(") {");
            stringBuffer.append(" this.").append(fieldName).append("=").append(fieldName).append(";}\n");
            stringBuffer.append("\t\t");
            filedType = reviseFieldType(filedType);
            stringBuffer.append("public ").append(filedType).append(" get");
            fieldName = field.getSimpleName().toString();
            methodName = capitalize(fieldName);
            stringBuffer.append(methodName).append("() {");
            stringBuffer.append(" return this.").append(fieldName).append(";}\n");
        });
        stringBuffer.append("\t}");
        stringBuffer.append("\n");
        out.print(stringBuffer.toString());
    }

    private String reviseFieldType(String filedType) {
        if (!isPrimitiveType(filedType)) {
            int lastDot = filedType.lastIndexOf('.');
            if (lastDot > 0) {
                String realFiledType = filedType.substring(lastDot + 1);
                boolean isGeneric = false;
                if (realFiledType.contains(">")) {
                    isGeneric = true;
                    realFiledType = realFiledType.replace(">", "");
                }
                String firstFieldType = "";
                if (filedType.contains("<")) {
                    firstFieldType = filedType.substring(0, filedType.lastIndexOf("<") + 1);
                }
                return firstFieldType +
                        "com.metao.annotations." +
                        realFiledType +
                        "Builder" +
                        "." +
                        realFiledType +
                        (isGeneric ? ">" : "");
            }
        }
        return filedType;
    }

    private static boolean isPrimitiveType(String type) {
        String[] primitiveTypesArray = {"int", "java.lang.String", "short", "float", "double"};
        return Arrays.stream(primitiveTypesArray).anyMatch(matcher -> matcher.matches(type));
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
