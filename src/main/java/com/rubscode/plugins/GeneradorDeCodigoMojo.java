package com.rubscode.plugins;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Goal generar un clase java a partir de un esquema de clave valor.
 *
 * @goal generate
 * 
 * @phase process-sources
 */
public class GeneradorDeCodigoMojo
        extends AbstractMojo {
    /**
     * Nombre del archivo que contiene el esquema de la clase a generar.
     * 
     * @parameter property="inputFile"
     * @required
     */
    private String inputFile;
    /**
     * Nombre del package donde se va generar la clase de java.
     * 
     * @parameter property="packageName" default-value="${project.groupId}"
     * @required
     */
    private String packageName;

    public void execute()
            throws MojoExecutionException {

        try {
            getLog().info("Generando clase java a partir del esquema: " + inputFile);
            generateCode(
                    stringToMap(
                            fileToString(this.inputFile)));
        } catch (IOException | ClassNotFoundException | JClassAlreadyExistsException e) {
            throw new MojoExecutionException("Error creating file " + this.inputFile + e);
        }

    }

    /**
     * Extrae el contenido de un archivo como una cadena de texto
     * 
     * @param file
     * @throws IOException
     */
    private String fileToString(String file) throws IOException {
        Path fileName = Path.of(file);
        String str = Files.readString(fileName);
        return str;
    }

    /**
     * Convierte una cadena de texto que representa un mapa de clave valor, en un
     * mapa de java
     * 
     * @param str
     */
    private Map<String, String> stringToMap(String str) {
        Map<String, String> map = new HashMap<String, String>();

        String[] lines = str.split(System.getProperty("line.separator"));
        for (String string : lines) {
            String[] keyValue = string.split(":");
            map.put(keyValue[0].replaceAll("\\s", ""), keyValue[1].replaceAll("\\s", ""));
        }
        return map;
    }

    /**
     * Genera el codigo fuente de una clase java a partir de un mapa de clave valor
     * 
     * @param map
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws JClassAlreadyExistsException
     */
    private void generateCode(Map<String, String> map)
            throws JClassAlreadyExistsException, ClassNotFoundException, IOException {
        JCodeModel codeModel = new JCodeModel();
        // Se extrae el nombre de la clase a partir del nombre del archivo de entrada y
        // se capitaliza la primera letra.
        String className = this.inputFile.split("\\.")[0];
        String classNameCapitalize = className.substring(0, 1).toUpperCase() + className.substring(1);
        // Se crea la clase con los atributos indicados.
        JDefinedClass definedClass = codeModel._class(packageName + "." + classNameCapitalize);
        for (Entry<String, String> entry : map.entrySet()) {
            definedClass.field(JMod.PRIVATE, Class.forName("java.lang." + entry.getValue()), entry.getKey());
        }
        // Se genera el archivo .java
        codeModel.build(new File("src/main/java"));
    }
}
