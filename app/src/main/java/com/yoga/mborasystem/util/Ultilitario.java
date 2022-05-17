package com.yoga.mborasystem.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
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
import com.yoga.mborasystem.MainActivity;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class Ultilitario {

    private static Float parsed;
    private static Locale pt_AO;
    public static String categoria = "";
    public static final String MBORASYSTEM = "ryogamborasystem";
    public static boolean isLocal = true;
    private static String formatted, current = "";
    public static Pattern letras = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-ÛÇç. ]");
    public static Pattern letraNumero = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-ÛÇç0-9\n ]");
    public static final int EXPORTAR_PRODUTO = 1, IMPORTAR_PRODUTO = 2, EXPORTAR_CATEGORIA = 3, IMPORTAR_CATEGORIA = 4;
    public static final int ZERO = 0, UM = 1, DOIS = 2, TRES = 3, QUATRO = 4, SINCO = 5, CREATE_FILE_PRODUTO = 1, CREATE_FILE_CATEGORIA = 2, LENGTH_LONG = 10;

    public Ultilitario() {
    }

    @SuppressLint("WrongConstant")
    public static void showToast(Context context, int color, String s, int imagem) {
        Toast toast = new Toast(context);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
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

    public static String formatPreco(String preco) {
        pt_AO = new Locale("pt", "AO");
        float parsed;
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

    public static AlertDialog.Builder dialogConta(String message, Context context) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(context.getString(R.string.conta));
        alert.setMessage(message);
        alert.setNegativeButton(R.string.ok, (dialog, which) -> dialog.dismiss());
        return alert;
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
        byte[] salt = new byte[16];
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
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static OnBackPressedCallback sairApp(Activity activity, Context context) {
        return new OnBackPressedCallback(true) {
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
    }

    public static void fullScreenDialog(Dialog dialog) {
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    public static String getDateCurrent() {
        String dt;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMMM-yyyy-HH:mm:ss", Locale.getDefault());
            dt = date.format(dtf);
        } else {
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy-HH:mm:ss", Locale.getDefault());
            dt = sdf.format(date);
        }
        return dt;
    }

    public static int removerKZ(TextInputEditText editText) {
        return Integer.parseInt(Objects.requireNonNull(editText.getText()).toString().replaceAll(",", "").replaceAll("Kz", "").replaceAll("\\s+", ""));
    }

    public static String trocarVírgulaPorPonto(TextInputEditText editText) {
        return Objects.requireNonNull(editText.getText()).toString().replaceAll(",", ".");
    }

    public static void addItemOnSpinner(Spinner spinner, int qtd, Context context) {
        ArrayList<Integer> listaQuantidade = new ArrayList<>();
        for (int i = 1; i <= qtd; ++i) {
            listaQuantidade.add(i);
        }
        ArrayAdapter<Integer> itemAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, listaQuantidade);
        spinner.setAdapter(itemAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void exportarLocal(ActivityResultLauncher<Intent> exportActivityResultLauncher, Activity activity, StringBuilder dataStringBuilder, String ficheiro, String nomeFicheiro, String data) {
        try {
            FileOutputStream out = activity.openFileOutput(ficheiro, Context.MODE_PRIVATE);
            out.write((dataStringBuilder.toString()).getBytes());
            out.close();

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/csv");
            intent.putExtra(Intent.EXTRA_TITLE, nomeFicheiro + new Random().nextInt((1000 - 1) + 1) + 1 + " " + data + ".csv");
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "");
            exportActivityResultLauncher.launch(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity.getBaseContext(), activity.getString(R.string.falha) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void alterDocument(Uri uri, StringBuilder data, Activity activity) {

        ParcelFileDescriptor csv = null;
        try {
            csv = activity.getContentResolver().openFileDescriptor(uri, "w");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(activity.getBaseContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(Objects.requireNonNull(csv).getFileDescriptor());

        new ExportarAsyncTask(csv, fileOutputStream, uri, activity).execute(data.toString());

    }

    public static void exportarNuvem(Context context, StringBuilder dataStringBuilder, String ficheiro, String nomeFicheiro, String data) {
        try {
            //saving the file into device
            FileOutputStream out = context.openFileOutput(ficheiro, Context.MODE_PRIVATE);
            out.write((dataStringBuilder.toString()).getBytes());
            out.close();
            //exporting
            File filelocation = new File(context.getFilesDir(), ficheiro);
            Uri path = FileProvider.getUriForFile(context, "com.yoga.mborasystem", filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, nomeFicheiro + new Random().nextInt((1000 - 1) + 1) + 1 + " " + data);
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            context.startActivity(Intent.createChooser(fileIntent, context.getString(R.string.partilhar)));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, context.getString(R.string.falha) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void importarCategoriasProdutos(ActivityResultLauncher<Intent> importActivityResultLauncher, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimetypes = {"text/csv", "text/comma-separated-values", "application/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        if (importActivityResultLauncher == null) {
            activity.startActivityForResult(intent, QUATRO);
        } else {
            importActivityResultLauncher.launch(intent);
        }
    }

    public static void swipeRefreshLayout(SwipeRefreshLayout mySwipeRefreshLayout) {
        if (mySwipeRefreshLayout != null) {
            mySwipeRefreshLayout.setRefreshing(false);
        }
    }

    public static void zerarPreco(TextInputEditText preco) {
        String formatted = NumberFormat.getCurrencyInstance(pt_AO).format((0));
        preco.setText(formatted);
        preco.setSelection(formatted.length());
    }

    public static void openWhatsApp(Activity activity, String numero) {
        boolean isWhatsappInstalled = whatsappInstalledOrNot(activity);
        if (isWhatsappInstalled) {
            Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
            sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators("244" + numero) + "@s.whatsapp.net");//phone number without "+" prefix
            activity.startActivity(sendIntent);
        } else {
            Uri uri = Uri.parse("market://details?id=com.whatsapp");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            Toast.makeText(activity.getApplicationContext(), "WhatsApp not Installed",
                    Toast.LENGTH_SHORT).show();
            activity.startActivity(goToMarket);
        }
    }

    private static boolean whatsappInstalledOrNot(Activity activity) {
        PackageManager pm = activity.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public static String getMonth(int month) {

        Map<Integer, String> listMonth = new HashMap<>();

        listMonth.put(1, "janeiro");
        listMonth.put(2, "fevereiro");
        listMonth.put(3, "março");
        listMonth.put(4, "abril");
        listMonth.put(5, "maio");
        listMonth.put(6, "junho");
        listMonth.put(7, "julho");
        listMonth.put(8, "agosto");
        listMonth.put(9, "setembro");
        listMonth.put(10, "outubro");
        listMonth.put(11, "novembro");
        listMonth.put(12, "dezembro");

        return listMonth.get(month);
    }

    private static class ExportarAsyncTask extends AsyncTask<String, Void, String> {

        private final Uri uri;
        @SuppressLint("StaticFieldLeak")
        private final Activity activity;
        private final ParcelFileDescriptor csv;
        private final FileOutputStream fileOutputStream;

        public ExportarAsyncTask(ParcelFileDescriptor csv, FileOutputStream fileOutputStream, Uri uri, Activity activity) {
            this.uri = uri;
            this.csv = csv;
            this.activity = activity;
            this.fileOutputStream = fileOutputStream;
        }

        @Override
        protected String doInBackground(String... data) {

            try {

                fileOutputStream.write((data[0]).getBytes());
                fileOutputStream.close();
                csv.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(activity.getBaseContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(activity.getBaseContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return activity.getString(R.string.expo_concl) + "\n" + uri;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(activity.getBaseContext(), s, Toast.LENGTH_LONG).show();
        }
    }

    public static void colorRandomImage(ImageView i, Random rand) {
        int r, g, b;
        r = rand.nextInt();
        g = rand.nextInt();
        b = rand.nextInt();
        i.setColorFilter(Color.rgb(r, g, b));
    }

    @SuppressLint("MissingPermission")
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        // For 29 api or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            Network nw = cm.getActiveNetwork();
            if (nw == null) return false;
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
        } else return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

    public static void alertDialog(String titulo, String mensagem, Context context) {
        MainActivity.dismissProgressBar();
        new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_logotipo_yoga_original)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setNegativeButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static void setSharedPreferencesDataDispositivo(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences("DATE_DAVICE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("data", monthInglesFrances(Ultilitario.getDateCurrent()));
        editor.apply();
    }

    public static String getSharedPreferencesDataDispositivo(Activity activity) {
        SharedPreferences sharedPref = activity.getSharedPreferences("DATE_DAVICE", Context.MODE_PRIVATE);
        return sharedPref.getString("data", "00-00-0000");
    }

    public static String getPercentagemIva(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return sharedPreferences.getString("percentagem_iva", "");
    }

    public static String monthInglesFrances(String data) {
        Map<String, String> listMonth = new HashMap<>();
        listMonth.put("january", "janeiro");
        listMonth.put("february", "fevereiro");
        listMonth.put("march", "março");
        listMonth.put("april", "abril");
        listMonth.put("may", "maio");
        listMonth.put("june", "junho");
        listMonth.put("july", "julho");
        listMonth.put("august", "agosto");
        listMonth.put("september", "setembro");
        listMonth.put("october", "outubro");
        listMonth.put("november", "novembro");
        listMonth.put("december", "dezembro");
        listMonth.put("janvier", "janeiro");
        listMonth.put("février", "fevereiro");
        listMonth.put("mars", "março");
        listMonth.put("avril", "abril");
        listMonth.put("mai", "maio");
        listMonth.put("juin", "junho");
        listMonth.put("juillet", "julho");
        listMonth.put("août", "agosto");
        listMonth.put("septembre", "setembro");
        listMonth.put("octobre", "outubro");
        listMonth.put("novembre", "novembro");
        listMonth.put("décembre", "dezembro");

        String[] date = TextUtils.split(data.trim(), "-");

        if (listMonth.get(date[1]) == null)
            return date[0] + "-" + date[1] + "-" + date[2];
        else
            return date[0] + "-" + listMonth.get(date[1]) + "-" + date[2];
    }

}
