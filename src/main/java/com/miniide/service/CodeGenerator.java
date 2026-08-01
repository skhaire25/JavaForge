package com.miniide.service;

import org.springframework.stereotype.Service;

@Service
public class CodeGenerator {

    public String generateClass(
            String packageName,
            String className,
            boolean addMain) {

        if (addMain) {

            return "package " + packageName + ";\n\n"
                    + "public class " + className + " {\n\n"
                    + "    public static void main(String[] args) {\n\n"
                    + "    }\n"
                    + "}";

        }

        return "package " + packageName + ";\n\n"
                + "public class " + className + " {\n\n"
                + "}";
    }

    public String generateInterface(
            String packageName,
            String interfaceName) {

        return "package " + packageName + ";\n\n"
                + "public interface " + interfaceName + " {\n\n"
                + "}";
    }

    public String generateEnum(
            String packageName,
            String enumName) {

        return "package " + packageName + ";\n\n"
                + "public enum " + enumName + " {\n\n"
                + "    VALUE1,\n"
                + "    VALUE2,\n"
                + "    VALUE3;\n\n"
                + "}";
    }

    public String generateRecord(
            String packageName,
            String recordName) {

        return "package " + packageName + ";\n\n"
                + "public record " + recordName + "(\n"
                + "        String name,\n"
                + "        int age\n"
                + ") {\n\n"
                + "}";
    }

    public String generateAnnotation(
            String packageName,
            String annotationName) {

        return "package " + packageName + ";\n\n"
                + "import java.lang.annotation.ElementType;\n"
                + "import java.lang.annotation.Retention;\n"
                + "import java.lang.annotation.RetentionPolicy;\n"
                + "import java.lang.annotation.Target;\n\n"
                + "@Retention(RetentionPolicy.RUNTIME)\n"
                + "@Target(ElementType.TYPE)\n"
                + "public @interface " + annotationName + " {\n\n"
                + "    String value() default \"\";\n\n"
                + "}";
    }

}