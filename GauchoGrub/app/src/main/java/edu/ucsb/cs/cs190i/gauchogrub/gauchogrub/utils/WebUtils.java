package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.logging.Logger;

public class WebUtils {

    private final static Logger logger = Logger.getLogger("WebUtils");

    /**
     * getBitmap queries a URL and returns a bitmap image
     * @param url the URL being queried
     * @param timeout the time in milliseconds before it fails
     * @return a bitmap image
     */
    public Bitmap getBitmap(URL url, int timeout) {
        HttpURLConnection connection = null;
        InputStream stream = null;
        Bitmap image = null;
        try {
            logger.info(url.toString());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.connect();

            stream = connection.getInputStream();
            byte[] response = readByteStream(stream);
            logger.info("GET image/jpeg: " + response.length + " bytes");
            image = BitmapFactory.decodeByteArray(response, 0, response.length);
        } catch (IOException e) {
            return null;
        } finally {
            Closeables.closeQuietly(stream);
            connection.disconnect();
        }
        return image;
    }

    /* Asynchronous method that performs an HTTP request returning data received from the sever as a String */
    public String httpRequest(URL url, HttpMethod method, int timeout) throws IOException {
        return httpRequest(url, method, timeout, new Hashtable<String, String>());
    }

    public String httpRequest(URL url, HttpMethod method, int timeout, Hashtable<String, String> headers) throws IOException {
        log(url);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if (headers != null) {
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
        }
        connection.setRequestMethod(method.toString());
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.connect();

        return readResponse(connection);
    }

    /* Helper method */
    private String readResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode == 200 || responseCode == 204) {
            InputStream inputStream = null;
            try {
                inputStream = connection.getInputStream();
                return readInputStream(inputStream);
            } finally {
                Closeables.closeQuietly(inputStream);
            }
        } else {
            throw new RuntimeException("Bad request");
        }
    }

    /* Reads data (String) from an input stream */
    private String readInputStream(InputStream stream) throws IOException {
        return CharStreams.toString(new InputStreamReader(stream, Charsets.UTF_8));
    }

    /* Reads data (byte[]) from an input stream */
    private byte[] readByteStream(InputStream stream) throws IOException {
        return ByteStreams.toByteArray(stream);
    }

    /* Creates a query String from key-value pairs in the dictionary */
    public static String toQuery(Hashtable<String, String> dictionary) throws UnsupportedEncodingException {
        if (dictionary == null) {
            return "";
        }
        String query = "";
        for (String key : dictionary.keySet()) {
            query += ((query.length() == 0) ? "" : "&") + key + "=" + encodeString(dictionary.get(key));
        }
        return query;
    }

    /* Encodes the supplied Url into an escaped format */
    public static String encodeString(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, "UTF-8");
    }

    /* Logs the URL */
    private void log(URL url) {
        logger.info(url.toString());
    }

    public enum HttpMethod {
        GET, POST, DELETE, PUT
    }
}