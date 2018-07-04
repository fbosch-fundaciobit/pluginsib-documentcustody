package es.caib.signatura.utils;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Clase de utilidad que permite enviar un mensaje multipart/form-data mediante
 * HTTP Post al servidor. http://www.ietf.org/rfc/rfc1867.txt
 * @version 1.0 24/02/2011
 * @author Esteban Luengo
 */
public class ClientMultipartFormData {

    private static String boundary;
    private static String contentDisposition = "Content-Disposition: form-data; name=\"";
    private URLConnection connection;
    private OutputStream output = null;

    //Inicializamos la variable boundary a utilizar en el mensaje multipart/form-data
    static{
        Random random = new Random();
        boundary = "----------------"
                +  Long.toString(random.nextLong(), 48).toUpperCase()
                +  Long.toString(random.nextLong(), 48).toUpperCase()
                +  Long.toString(random.nextLong(), 48).toUpperCase()
                +  Long.toString(random.nextLong(), 48).toUpperCase();
    }

    /**
     * Constructor de la clase. Recibe la URL a la que enviaro el mensaje multipart/form-data.
     * El constructor conecta con dicha URL y espera que no sea autenticada. Por otro lado
     * especifica la propiedad content-type para mensajes  ultipart/form-data. Finalmente
     * Recupera el OutputSteam sobre el que se escribiro el mensaje
     * @param urlString URL a la que enviaro el mensaje
     * @throws IOException en caso de error en la conexion con la URL se lanzaro una IOException
     */
    public ClientMultipartFormData(String urlString) throws IOException {
        URL url = new URL(urlString);
        this.connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        output = connection.getOutputStream();
    }

    /**
     * Escribe un carocter sobre el flujo de salida
     * @param c carocter a escribir
     * @throws IOException en caso de error se lanza una IOException
     */
    private void write(char c) throws IOException {
        if (output == null) {
            throw new IOException("Conexion no establecida");
        }
        output.write(c);
    }

    /**
     * Escribe una cadena de caracteres sobre el flujo de salida
     * @param s cadena de caracteres a escribir
     * @throws IOException en caso de error se lanza una IOException
     */
    private void write(String s) throws IOException {
        if (output == null) {
            throw new IOException("Conexion no establecida");
        }
        output.write(s.getBytes("UTF-8"));
    }

     /**
     * Escribe un array de bytes sobre el flujo de salida
     * @param b array de bytes a escribir
     * @throws IOException en caso de error se lanza una IOException
     */
    private void write(byte[] b) throws IOException {
        if (output == null) {
            throw new IOException("Conexion no establecida");
        }
        output.write(b);
    }

    /**
     * Escribe un salto de lina como "\r\n"
     * @throws IOException en caso de error se lanza una IOException
     */
    private void writeNewline() throws IOException {
        write("\r\n");
    }

    /**
     * Escribe la frontera
     * @throws IOException en caso de error se lanza una IOException
     */
    private void writeBoundary() throws IOException {
        write("--");
        write(boundary);
    }

    /**
     * Escribe la cabecera de un parametro
     * @param name nombre del parometro a escribir
     * @throws IOException en caso de error se lanza una IOException
     */
    private void writeName(String name) throws IOException {
        writeNewline();
        write(contentDisposition);
        write(name);
        write('"');
    }

    /**
     * Escribe el flujo InputStream sobre la salida
     * @param in flujo de entrada que contiene los datos a escribir
     * @param out flujo de salida donde escribimos los datos
     * @throws IOException en caso de error se lanza una IOException
     */
    private void writeInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int read;
        while ((read = in.read(buf, 0, buf.length)) >= 0) {
            out.write(buf, 0, read);
            out.flush();
        }
        out.flush();
    }

    /**
     * Permite aoadir un parametro simple de tipo String sobre el mensaje multipart/form-data
     * @param name nombre del parametro
     * @param value valor del parametro
     * @throws IOException en caso de error se lanza una IOException
     */
    public void addParameter(String name, String value) throws IOException {
        writeBoundary();
        writeName(name);
        writeNewline();
        writeNewline();
        write(value);
        writeNewline();
    }

     /**
     * Permite aoadir un parametro binario sobre el mensaje multipart/form-data
     * @param name nombre del parametro
     * @param filename nombre de fichero que representara estos datos binarios
     * @param buffer array de bytes con los datos a escribir
     * @throws IOException en caso de error se lanza una IOException
     */
    public void addParameter(String name, String filename, byte[] buffer) throws IOException {
        writeBoundary();
        writeName(name);
        write("; filename=\"");
        write(filename);
        write('"');
        writeNewline();
        write("Content-Type: ");
        String type = URLConnection.guessContentTypeFromName(filename);
        if (type == null) {
            type = "application/octet-stream";
        }
        write(type);
        writeNewline();
        writeNewline();
        write(buffer);
        writeNewline();
    }

     /**
     * Permite aoadir un parametro de tipo inputstream sobre el mensaje multipart/form-data
     * @param name nombre del parametro
     * @param filename nombre de fichero que representara estos datos binarios
     * @param is InputSream que contendro los datos a escribir
     * @throws IOException en caso de error se lanza una IOException
     */
    public void addParameter(String name, String filename, InputStream is) throws IOException {
        writeBoundary();
        writeName(name);
        write("; filename=\"");
        write(filename);
        write('"');
        writeNewline();
        write("Content-Type: ");
        String type = URLConnection.guessContentTypeFromName(filename);
        if (type == null) {
            type = "application/octet-stream";
        }
        write(type);
        writeNewline();
        writeNewline();
        writeInputStream(is, output);
        writeNewline();
    }

     /**
     * Permite aoadir un parametro de tipo File sobre el mensaje multipart/form-data
     * @param name nombre del parametro
     * @param file objeto File que contendro los datos a escribir
     * @throws IOException en caso de error se lanza una IOException
     */
    public void addParameter(String name, File file) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            addParameter(name, file.getPath(), fis);
        } catch(IOException e){
            throw e;
        }
        finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Termina de componer el mensaje multipart/form-data y cierra la conexion
     * @return
     * @throws IOException
     */
    public InputStream send() throws IOException {
        writeBoundary();
        write("--");
        output.flush();
        output.close();
        return connection.getInputStream();
    }
}
