package com.yoga.mborasystem.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class Ultilitario {

    private static Float parsed;
    private static Locale pt_AO;
    public static String categoria = "";
    public static boolean isLocal = true;
    private static String formatted, current = "";
    private static ArrayAdapter<Integer> itemAdapter;
    private static ArrayList<Integer> listaQuantidade;
    public static Pattern letras = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-ÛÇç. ]");
    public static Pattern letraNumero = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-ÛÇç0-9\n ]");
    public static final int EXPORTAR_PRODUTO = 1, IMPORTAR_PRODUTO = 2, EXPORTAR_CATEGORIA = 3, IMPORTAR_CATEGORIA = 4;
    public static final int ZERO = 0, UM = 1, DOIS = 2, TRES = 3, QUATRO = 4, SINCO = 5, CREATE_FILE_PRODUTO = 1, CREATE_FILE_CATEGORIA = 2, LENGTH_LONG = 5;

    public Ultilitario() { }

    @SuppressLint("WrongConstant")
    public static void showToast(Context context, int color, String s, int imagem) {
        Toast toast = new Toast(context);
        View view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
        TextView text = view.findViewById(R.id.toast_text);
        ImageView img = view.findViewById(R.id.toast_image);
        text.setText(s);
        img.setImageResource(imagem);
        view.setBackgroundColor(color);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 50);
        toast.setDuration(LENGTH_LONG);
        toast.show();
    }

    public enum Operacao {
        CRIAR,
        ACTUALIZAR,
        NENHUMA
    }

    public enum Existe {
        SIM,
        NAO
    }

    public static void onClickColorRecyclerView(View viewHolder) {
        viewHolder.setBackgroundColor(Color.parseColor("#6BD3D8D7"));
    }

    public static String formatPreco(String preco) {
        pt_AO = new Locale("pt", "AO");
        Float parsed;
        String formatted;
        String cleanSting = preco.replaceAll("[^\\d.]", "");
        if (cleanSting.isEmpty()) {
            parsed = Float.parseFloat("0");
        } else {
            parsed = Float.parseFloat(cleanSting);
        }
        formatted = NumberFormat.getCurrencyInstance(pt_AO).format((parsed / 100));
        return formatted;
    }

    private static MutableLiveData<Existe> existeMutableLiveData;

    public static MutableLiveData<Ultilitario.Existe> getExisteMutableLiveData() {
        if (existeMutableLiveData == null) {
            existeMutableLiveData = new MutableLiveData<>();
        }
        return existeMutableLiveData;
    }

    private static MutableLiveData<Ultilitario.Operacao> valido;

    public static MutableLiveData<Ultilitario.Operacao> getValido() {
        if (valido == null) {
            valido = new MutableLiveData<>();
        }
        return valido;
    }

    public static void dialogConta(String message, Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.conta)
                .setMessage(message)
                .setNegativeButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static void precoFormat(Context context, TextInputEditText preco) {
        pt_AO = new Locale("pt", "AO");
        preco.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    preco.removeTextChangedListener(this);
                    String cleanSting = s.toString().replaceAll("[^\\d.]", "");
                    try {
                        if (cleanSting.isEmpty()) {
                            parsed = Float.parseFloat("0");
                        } else {
                            parsed = Float.parseFloat(cleanSting);
                        }
                        formatted = NumberFormat.getCurrencyInstance(pt_AO).format((parsed / 100));
                        current = formatted;
                        preco.setText(formatted);
                        preco.setSelection(formatted.length());
                        preco.addTextChangedListener(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public static void naoEncontrado(Context context, GroupAdapter adapter, int m) {
        adapter.add(new Item<GroupieViewHolder>() {
            @Override
            public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
                TextView msg = viewHolder.itemView.findViewById(R.id.textView2);
                msg.setText(context.getText(m));
            }

            @Override
            public int getLayout() {
                return R.layout.fragment_vazio;
            }
        });
    }

    public static String generateKey(char[] senhaPin) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final int ITERACAO = 1000;
        final int OUT_PUT_KEY_LENGTH = 64 * 8;
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte salt[] = new byte[16];
        sr.nextBytes(salt);
        KeySpec ks = new PBEKeySpec(senhaPin, salt, ITERACAO, OUT_PUT_KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] secretKey = skf.generateSecret(ks).getEncoded();
        return ITERACAO + ":" + toHex(salt) + ":" + toHex(secretKey);
    }

    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    public static boolean validateSenhaPin(String sp, String storedSP) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String[] pt = storedSP.split(":");
        int iteracao = Integer.parseInt(pt[0]);
        byte[] salt = fromHex(pt[1]);
        byte[] hash = fromHex(pt[2]);

        KeySpec ks = new PBEKeySpec(sp.toCharArray(), salt, iteracao, hash.length * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] secretKey = skf.generateSecret(ks).getEncoded();

        int diff = hash.length ^ secretKey.length;
        for (int i = 0; i < hash.length && i < secretKey.length; i++) {
            diff |= hash[i] ^ secretKey[i];
        }
        return diff == 0;
    }

    private static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    public static String gerarHash(String pin) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(pin.getBytes());
        return bytesToHexString(md.digest());
    }

    private static String bytesToHexString(byte[] digest) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            String hex = Integer.toHexString(0xFF & digest[i]);
            if (hex.length() == 1) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static OnBackPressedCallback sairApp(Activity activity, Context context) {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            private long backPressedTime;

            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    activity.finish();
                    return;
                } else {
                    Toast.makeText(context, (R.string.pressior_sair), Toast.LENGTH_SHORT).show();
                }
                backPressedTime = System.currentTimeMillis();
            }
        };
        return callback;
    }

    public static void fullScreenDialog(Dialog dialog) {
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    private static String dt = null;

    public static String getDateCurrent() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMMM-yyyy HH:mm:ss", Locale.getDefault());
            dt = date.format(dtf);
        } else {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss", Locale.getDefault());
            dt = sdf.format(date);
        }
        return dt;
    }

    public static int removerKZ(TextInputEditText editText) {
        return Integer.parseInt(editText.getText().toString().replaceAll(",", "").replaceAll("Kz", "").replaceAll("\\s+", ""));
    }

    public static void addItemOnSpinner(Spinner spinner, Context context) {
        listaQuantidade = new ArrayList<>();
        for (int i = 1; i <= 255; ++i) {
            listaQuantidade.add(i);
        }
        itemAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, listaQuantidade);
        spinner.setAdapter(itemAdapter);
    }

    public static void exportarLocal(Activity activity, StringBuilder data, String ficheiro, String nomeFicheiro, int CREATE_FILE) {
        try {
            FileOutputStream out = activity.openFileOutput(ficheiro, Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/csv");
            intent.putExtra(Intent.EXTRA_TITLE, nomeFicheiro + new Random().nextInt((1000 - 1) + 1) + 1 + " " + Ultilitario.getDateCurrent() + ".csv");
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "");
            activity.startActivityForResult(intent, CREATE_FILE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity.getBaseContext(), activity.getString(R.string.falha) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void alterDocument(Uri uri, StringBuilder data, Activity activity) {
        try {
            ParcelFileDescriptor csv = activity.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(csv.getFileDescriptor());
            fileOutputStream.write((data.toString()).getBytes());
            fileOutputStream.close();
            csv.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportarNuvem(Context context, StringBuilder data, String ficheiro, String nomeFicheiro) {
        try {
            //saving the file into device
            FileOutputStream out = context.openFileOutput(ficheiro, Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();
            //exporting
            File filelocation = new File(context.getFilesDir(), ficheiro);
            Uri path = FileProvider.getUriForFile(context, "com.yoga.mborasystem", filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, nomeFicheiro + new Random().nextInt((1000 - 1) + 1) + 1 + " " + Ultilitario.getDateCurrent());
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            context.startActivity(Intent.createChooser(fileIntent, context.getString(R.string.partilhar)));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, context.getString(R.string.falha) + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void importarCategoriasProdutos(Activity activity, int PICK_CSV_FILE) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimetypes = {"text/csv", "text/comma-separated-values", "application/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        activity.startActivityForResult(intent, PICK_CSV_FILE);
    }

    public static void swipeRefreshLayout(SwipeRefreshLayout mySwipeRefreshLayout){
        if (mySwipeRefreshLayout != null) {
            mySwipeRefreshLayout.setRefreshing(false);
        }
    }

    public static void zerarPreco(TextInputEditText preco) {
        String formatted = NumberFormat.getCurrencyInstance(pt_AO).format((0));
        preco.setText(formatted);
        preco.setSelection(formatted.length());
    }

}
