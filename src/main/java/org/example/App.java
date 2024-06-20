package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class App {

    private static final String GET_ROLES_URL = "http://193.19.100.32:7000/api/get-roles";
    private static final String SIGN_UP_URL = "http://193.19.100.32:7000/api/sign-up";
    private static final String GET_CODE_URL = "http://193.19.100.32:7000/api/get-code";
    private static final String SET_STATUS_URL = "http://193.19.100.32:7000/api/set-status";

    private static List<String> getRoles() {
        List<String> listRoles = new ArrayList<>();
        try {
            URL url = new URL(GET_ROLES_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray rolesArray = jsonObject.getJSONArray("roles");

                for (int i = 0; i < rolesArray.length(); i++) {
                    listRoles.add(rolesArray.getString(i));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return listRoles;
    }

    private static void signUp(String firstName, String lastName, String email,String role){
        try {
            URL url = new URL(SIGN_UP_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("last_name", lastName);
            jsonObject.put("first_name", firstName);
            jsonObject.put("email", email);
            jsonObject.put("role", role);

            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonObject.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String getCode(String email){
        String code = "";
        try {
            URL url = new URL(GET_CODE_URL + "?email="+email);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }
                code = stringBuilder.substring(1,stringBuilder.length()-1).toString();
                in.close();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return code;
    }
    private static String getEncodeCode(String code, String email){
        return Base64.getEncoder().encodeToString((email+":"+code).getBytes());
    }

    private static void setStatus(String encodeString){
        try {
            URL url = new URL(SET_STATUS_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("token", encodeString);
            jsonObject.put("status", "increased");

            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonObject.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                }
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        List<String> rolesList = getRoles();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите имя");
        String firstName = scanner.next();
        System.out.println("Введите фамилию");
        String lastName = scanner.next();
        System.out.println("Введите почту");
        String email = scanner.next();

        System.out.println("Выберите роль:");
        for (int i = 0; i < rolesList.size(); i++) {
            System.out.println((i+1) + " : "+ rolesList.get(i));
        }

        int roleIndex = scanner.nextInt();
        if (roleIndex < 0 || roleIndex >= rolesList.size()){
            System.out.println("ОШИБКА");
            return;
        }

        String role = rolesList.get(roleIndex-1);

        signUp(firstName,lastName,email,role);

        String code = getCode(email);

        String encode = getEncodeCode(code,email);

        setStatus(encode);
    }
}
