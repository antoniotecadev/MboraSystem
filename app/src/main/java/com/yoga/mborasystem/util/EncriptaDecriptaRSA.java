package com.yoga.mborasystem.util;

import static com.yoga.mborasystem.util.Ultilitario.getFilePathCache;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.xerces.impl.dv.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class EncriptaDecriptaRSA {
    private static final String ALGORITHM = "RSA";
    private static final String PATH_CHAVE_PRIVADA = Common.getAppPath("KEYS-RSA") + "privatekey.key";
    private static final String PATH_CHAVE_PUBLICA = Common.getAppPath("KEYS-RSA") + "publickey.key";

    public static void gerarChave(Context context) {
        try {
            if (!fileExists()) {
                final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
                keyGen.initialize(1024);
                final KeyPair key = keyGen.generateKeyPair();
                writeKey(PATH_CHAVE_PRIVADA, key.getPrivate());
                writeKey(PATH_CHAVE_PUBLICA, key.getPublic());
            }
            descriptografarTexto(assinarTexto("2018-05-18;2018-05-18T11:22:19;FAC 001/18;53.002;"));
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void writeKey(String filePath, Key key) throws IOException {
        ObjectOutputStream chave = new ObjectOutputStream(new FileOutputStream(filePath));
        chave.writeObject(key);
        chave.flush();
        chave.close();
    }

    private static String assinarTexto(String texto) throws Exception {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(EncriptaDecriptaRSA.PATH_CHAVE_PRIVADA));
        final PrivateKey privateKey = (PrivateKey) inputStream.readObject();
        final String textoCriptografado = criptografa(texto, privateKey);
        Log.i("RSA", "Mensagem Criptografada: " + textoCriptografado);
        Log.i("RSA", "Size: " + textoCriptografado.length());
        return textoCriptografado;
    }

    private static void descriptografarTexto(String texto) throws Exception {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(EncriptaDecriptaRSA.PATH_CHAVE_PUBLICA));
        final PublicKey publicKey = (PublicKey) inputStream.readObject();
        final String textoCriptografado = decriptografa(texto, publicKey);
        Log.i("RSA", "Mensagem Descriptografada: " + textoCriptografado);
    }


    private static boolean fileExists() {
        return new File(PATH_CHAVE_PUBLICA).exists() && new File(PATH_CHAVE_PRIVADA).exists();
    }

    public static String criptografa(String texto, PrivateKey privateKey) throws Exception {
        byte[] cipherText;
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        cipherText = cipher.doFinal(texto.getBytes());
        return Base64.encode(cipherText);
    }

    public static String decriptografa(String texto, PublicKey publicKey) throws Exception {
        byte[] dectyptedText;
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        dectyptedText = cipher.doFinal(Base64.decode(texto));
        return new String(dectyptedText);
    }

}
