package dev.marcelloa;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;


public class App
{
    public static void main( String[] args ) throws IOException {

        System.out.print("Insira o token: ");
        Scanner input = new Scanner(System.in);
        String token = input.nextLine();
        String url = "https://api.codenation.dev/v1/challenge/dev-ps/generate-data?token=" + token;
        URLConnection connection = new URL(url).openConnection();
        InputStream response = connection.getInputStream();

        try(Scanner scanner = new Scanner(response)){
            String responseBody = scanner.useDelimiter("\\A").next();

            try(FileWriter file = new FileWriter("answer.json")){
                file.write(responseBody);
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JSONObject answerObj;
        try(Reader reader = new FileReader("answer.json")){
            answerObj = (JSONObject) JSONValue.parse(reader);
            System.out.println("JSON OBJECT: " + answerObj);
        }
        String ciphertext = (String) answerObj.get("cifrado");
        long shift = (long) answerObj.get("numero_casas");
        char[] plaintextCharVector = new char[ciphertext.length()];

        final int ASCII_a = 97;
        final int ASCII_z = 122;
        final int ALFABET_LENGHT = 26;

        for(int iteratorCipher = 0; iteratorCipher < ciphertext.length(); iteratorCipher++){
            if(ciphertext.charAt(iteratorCipher) >= ASCII_a && ciphertext.charAt(iteratorCipher) <= ASCII_z){
                int decryption = ciphertext.charAt(iteratorCipher) - (int) shift;
                if((decryption) < ASCII_a) {
                    int boundBack = ALFABET_LENGHT + decryption;
                    plaintextCharVector[iteratorCipher] = (char) boundBack;
                } else {
                    plaintextCharVector[iteratorCipher] = (char) decryption;
                }
            } else {
                plaintextCharVector[iteratorCipher] = ciphertext.charAt(iteratorCipher);
            }
        }

        String plaintext = new String(plaintextCharVector);
        answerObj.put("decifrado", plaintext);
        System.out.println("A mensagem decifrada é: \"" + plaintext + "\"");

        String sha1 = DigestUtils.sha1Hex(plaintext);
        answerObj.put("resumo_criptografico", sha1);
        System.out.println("O sha1 de \""+ plaintext + "\" é: " + sha1);

        try(FileWriter file = new FileWriter("answer.json")){
            file.write(answerObj.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
