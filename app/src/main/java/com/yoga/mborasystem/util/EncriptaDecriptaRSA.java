package com.yoga.mborasystem.util;

import static com.yoga.mborasystem.util.Ultilitario.getFilePathCache;

import android.content.Context;
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
import java.security.Signature;

import javax.crypto.Cipher;

public class EncriptaDecriptaRSA {
    private static final String ALGORITHM = "RSA";
    private static final String PATH_CHAVE_PRIVADA = Common.getAppPath("KEYS-RSA") + "privatekey.key";
    private static final String PATH_CHAVE_PUBLICA = Common.getAppPath("KEYS-RSA") + "publickey.key";

    private static void gerarChave(Context context) {
        try {
            if (!fileExists()) {
                final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
                keyGen.initialize(1024);
                final KeyPair key = keyGen.generateKeyPair();
                writeKey(PATH_CHAVE_PRIVADA, key.getPrivate());
                writeKey(PATH_CHAVE_PUBLICA, key.getPublic());
            }
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

    public static byte[] criptografarTexto(String texto, Context context) throws Exception {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(getFilePathCache(context, "publickey.key").getAbsolutePath()));
        final PublicKey publicKey = (PublicKey) inputStream.readObject();
        return criptografa(texto, publicKey);
    }

    public static String decriptografarTexto(byte[] texto, Context context) throws Exception {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(getFilePathCache(context, "privatekey.key").getAbsolutePath()));
        final PrivateKey privateKey = (PrivateKey) inputStream.readObject();
        return decriptografa(texto, privateKey);
    }


    private static boolean fileExists() {
        return new File(PATH_CHAVE_PUBLICA).exists() && new File(PATH_CHAVE_PRIVADA).exists();
    }

    private static byte[] criptografa(String texto, PublicKey publicKey) throws Exception {
        byte[] cipherText;
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        cipherText = cipher.doFinal(texto.getBytes());
        return cipherText;
    }

    private static String decriptografa(byte[] texto, PrivateKey privateKey) throws Exception {
        byte[] dectyptedText;
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        dectyptedText = cipher.doFinal(texto);
        return new String(dectyptedText);
    }

    public static String assinarTexto(String text, String filePathPrivate, String filePathPublic) throws Exception {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filePathPrivate));
        final PrivateKey key = (PrivateKey) inputStream.readObject();
        Signature s = Signature.getInstance("SHA1withRSA");
        s.initSign(key);
        s.update(text.getBytes());
        byte[] signature = s.sign();
        return verificarAssinaturaText(text.getBytes(), filePathPublic, signature);
    }

    public static String verificarAssinaturaText(byte[] text, String filePath, byte[] signature) throws Exception {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filePath));
        final PublicKey key = (PublicKey) inputStream.readObject();
        Signature s = Signature.getInstance("SHA1withRSA");
        s.initVerify(key);
        s.update(text);
        if (s.verify(signature))
            return Base64.encode(signature);
        else
            return null;
    }

}
