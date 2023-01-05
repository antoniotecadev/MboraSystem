package com.yoga.mborasystem.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.DialogSenhaBinding;
import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.entidade.Cliente;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Ultilitario {

    private static Float parsed;
    private static Locale pt_AO;
    public static String categoria = "";

    public static final String MBORASYSTEM = "8e67fe66551c69731085ffb8d7746f6fec923b1af4f27066ba903219f0c60fb9";
    public static boolean isLocal = true;
    private static String formatted, current = "";
    public static Pattern letras = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-ÛÇç. ]");
    public static Pattern letraNumero = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-ÛÇç0-9\n ]");
    public static final int EXPORTAR_CATEGORIA = 3, IMPORTAR_CATEGORIA = 4;
    public static final int ZERO = 0, UM = 1, DOIS = 2, TRES = 3, QUATRO = 4, LENGTH_TOAST = 100, LENGTH_LONG = 10;

    public Ultilitario() {
    }

    @SuppressLint("WrongConstant")
    public static void showToast(Context context, int color, String s, int imagem) {
        Toast toast = new Toast(context);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.image_layout, null);
        TextView text = view.findViewById(R.id.detalhe_text);
        ImageView img = view.findViewById(R.id.image);
        text.setText(s);
        img.setImageResource(imagem);
        view.setBackgroundColor(color);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 50);
        toast.setDuration(LENGTH_LONG);
        toast.show();
    }

    @SuppressLint({"WrongConstant", "UseCompatLoadingForDrawables"})
    public static void showToastOrAlertDialogQrCode(Context context, Bitmap qrCode, boolean isQrCodeUser, ActivityResultLauncher<String> requestPermissionLauncherSaveQrCode, String nome, String estabalecimento, String imei) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_layout, null);
        ImageView img = view.findViewById(R.id.image);
        img.setImageBitmap(qrCode);

        if (!isQrCodeUser) {
            Toast toast = new Toast(context);
            toast.setView(view);
            toast.setGravity(Gravity.CENTER, 0, 50);
            toast.setDuration(LENGTH_TOAST);
            toast.show();
        } else {
            img.setBackground(context.getResources().getDrawable(R.drawable.border_image));
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setIcon(R.drawable.ic_baseline_store_24)
                    .setTitle(R.string.cod_qr)
                    .setMessage(context.getString(R.string.nm) + ": " + nome + "\n" + context.getString(R.string.emps) + ": " + estabalecimento + "\n" + context.getString(R.string.imei) + ": " + imei)
                    .setView(view)
                    .setNeutralButton(context.getString(R.string.guardar), (dialogInterface, i) -> requestPermissionLauncherSaveQrCode.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    .setNegativeButton(context.getString(R.string.partilhar), (dialogInterface, i) -> {
                        try {
                            partilharImagem(context, qrCode, estabalecimento.replace(".", "").replace(",", "").trim());
                        } catch (IOException e) {
                            alertDialog(context.getString(R.string.erro), e.getMessage(), context, R.drawable.ic_baseline_privacy_tip_24);
                        }
                    })
                    .setPositiveButton(R.string.fechar, (dialogInterface, i) -> dialogInterface.dismiss()).show();
        }
    }

    @SuppressLint("Range")
    public static String getFileName(Uri uri, Context context) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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
        if (cleanSting.isEmpty())
            parsed = Float.parseFloat("0");
        else
            parsed = Float.parseFloat(cleanSting);
        formatted = NumberFormat.getCurrencyInstance(pt_AO).format((parsed / 100));
        return formatted;
    }

    public static AlertDialog.Builder dialogConta(String message, Context context) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setIcon(R.drawable.ic_baseline_store_24);
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
                        if (cleanSting.isEmpty())
                            parsed = Float.parseFloat("0");
                        else
                            parsed = Float.parseFloat(cleanSting);

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

    @SuppressWarnings("rawtypes")
    public static GroupAdapter naoEncontrado(Context context, GroupAdapter adapter, int m) {
        adapter.clear();
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
        return adapter;
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
        if (paddingLength > 0)
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
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
        for (int i = 0; i < hash.length && i < secretKey.length; i++)
            diff |= hash[i] ^ secretKey[i];
        return diff == 0;
    }

    private static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
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
            if (hex.length() == 1)
                sb.append(0);
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
                } else
                    Toast.makeText(context, (R.string.pressior_sair), Toast.LENGTH_SHORT).show();

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

    @SuppressWarnings("NonAsciiCharacters")
    public static String trocarVírgulaPorPonto(TextInputEditText editText) {
        return Objects.requireNonNull(editText.getText()).toString().replaceAll(",", ".");
    }

    public static void addItemOnSpinner(AppCompatSpinner spinner, int qtd, Context context, int inicio) {
        ArrayList<Integer> listaQuantidade = new ArrayList<>();
        for (int i = inicio; i <= qtd; ++i)
            listaQuantidade.add(i);

        ArrayAdapter<Integer> itemAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, listaQuantidade);
        spinner.setAdapter(itemAdapter);
    }

    public static void setItemselectedSpinner(Context context, int array_values, String value, AppCompatSpinner list) {
        final String[] values = context.getResources().getStringArray(array_values);
        for (int i = 0; i <= values.length; i++) {
            if (value.equalsIgnoreCase(values[i])) {
                list.setSelection(i);
                break;
            }
        }
    }

    public static void exportarLocal(ActivityResultLauncher<Intent> exportActivityResultLauncher, Activity activity, String nomeFicheiro, String data) {
        try {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/csv");
            intent.putExtra(Intent.EXTRA_TITLE, nomeFicheiro + new Random().nextInt((1000 - 1) + 1) + 1 + " " + data + ".csv");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "");
            else
                Toast.makeText(activity, "API >= 26", Toast.LENGTH_LONG).show();

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
            Toast.makeText(activity.getBaseContext(), "" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        FileOutputStream fileOutputStream = null;
        if (csv != null)
            fileOutputStream = new FileOutputStream(csv.getFileDescriptor());
        else
            showToast(activity.getBaseContext(), Color.rgb(204, 0, 0), activity.getString(R.string.dds_n_enc), R.drawable.ic_toast_erro);

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
            fileIntent.putExtra(Intent.EXTRA_TITLE, nomeFicheiro + new Random().nextInt((1000 - 1) + 1) + 1 + " " + data);
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, nomeFicheiro + new Random().nextInt((1000 - 1) + 1) + 1 + " " + data);
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            context.startActivity(Intent.createChooser(fileIntent, context.getString(R.string.partilhar)));
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.falha) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void importarCategoriasProdutosClientes(ActivityResultLauncher<Intent> importActivityResultLauncher, Activity activity, boolean isDB) {
        String[] mimetypes;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        if (isDB)
            mimetypes = new String[]{"application/x-sqlite3", "application/vnd.sqlite3", "application/octet-stream"};
        else
            mimetypes = new String[]{"text/csv", "text/comma-separated-values", "application/csv"};

        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        if (importActivityResultLauncher == null)
            activity.startActivityForResult(intent, QUATRO);
        else
            importActivityResultLauncher.launch(intent);
    }

    private static void getImageCameraOrGallery(ActivityResultLauncher<Intent> imageActivityResultLauncher, boolean isCamera) {
        Intent imagePickerIntent;
        if (isCamera)
            imagePickerIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        else {
            imagePickerIntent = new Intent(Intent.ACTION_PICK);
            imagePickerIntent.setType("image/*");
        }
        imageActivityResultLauncher.launch(imagePickerIntent);
    }

    public static void alertDialogSelectImage(Context context, ActivityResultLauncher<Intent> imageActivityResultLauncher) {
        new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_baseline_store_24)
                .setTitle(context.getString(R.string.selec_image))
                .setNeutralButton(context.getString(R.string.cancelar), (dialogInterface, i) -> dialogInterface.dismiss())
                .setNegativeButton(context.getString(R.string.camera), (dialogInterface, i) -> getImageCameraOrGallery(imageActivityResultLauncher, true))
                .setPositiveButton(context.getString(R.string.galeria), (dialogInterface, i) -> getImageCameraOrGallery(imageActivityResultLauncher, false))
                .show();
    }

    public static void alertDialogSelectImage(Cliente cliente, Context context, ActivityResultLauncher<Intent> imageActivityResultLauncher) {
        new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_baseline_store_24)
                .setTitle(context.getString(R.string.selec_image))
                .setMessage(cliente.getImei() + "\n" + cliente.getNome() + "" + cliente.getSobrenome() + "\n" + cliente.getNomeEmpresa() + "\n" + cliente.getTelefone() + "\n" + cliente.getEmail() + "\n" + cliente.getCodigoPlus() + "\n" + cliente.getMunicipio() + ", " + cliente.getBairro() + ", " + cliente.getRua())
                .setNeutralButton(context.getString(R.string.cancelar), (dialogInterface, i) -> dialogInterface.dismiss())
                .setNegativeButton(context.getString(R.string.camera), (dialogInterface, i) -> getImageCameraOrGallery(imageActivityResultLauncher, true))
                .setPositiveButton(context.getString(R.string.galeria), (dialogInterface, i) -> getImageCameraOrGallery(imageActivityResultLauncher, false))
                .show();
    }

    public static void swipeRefreshLayout(SwipeRefreshLayout mySwipeRefreshLayout) {
        if (mySwipeRefreshLayout != null)
            mySwipeRefreshLayout.setRefreshing(false);
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

    public static String getDataFormatMonth(String data) {

        Map<String, String> listData = new HashMap<>();

        listData.put("janeiro", "01");
        listData.put("fevereiro", "02");
        listData.put("março", "03");
        listData.put("abril", "04");
        listData.put("maio", "05");
        listData.put("junho", "06");
        listData.put("julho", "07");
        listData.put("agosto", "08");
        listData.put("setembro", "09");
        listData.put("outubro", "10");
        listData.put("novembro", "11");
        listData.put("dezembro", "12");

        String[] date = TextUtils.split(data.trim(), "-");
        return date[2] + "-" + listData.get(date[1]) + "-" + date[0];
    }

    public static FirebaseUser verifyAuthenticationInFirebase() {
        // Check if user is signed in (non-null) and update UI accordingly.
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static void authenticationInFirebase(Activity activity, DialogSenhaBinding binding, ActivityResultLauncher<Intent> imageActivityResultLauncher) {
        binding.textInputSenha.setVisibility(View.GONE);
        binding.layoutPin.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        binding.buttonFechar.setText(activity.getString(R.string.cancelar));

        editTextLayout(binding.layoutPin, binding.pin, R.string.Email, InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS, activity);
        editTextLayout(binding.layoutPinRepete, binding.pinRepete, R.string.senha, InputType.TYPE_TEXT_VARIATION_PASSWORD, activity);

        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setIcon(R.drawable.ic_baseline_store_24)
                .setTitle(activity.getString(R.string.aut) + activity.getString(R.string.nvm))
                .setView(binding.getRoot())
                .show();

        binding.btnEntrar.setOnClickListener(view -> {
            if (isEmailValido(Objects.requireNonNull(binding.pin.getText()).toString())) {
                binding.pin.requestFocus();
                binding.layoutPin.setError(activity.getString(R.string.email_invalido));
            } else if ((isCampoVazio(Objects.requireNonNull(binding.pinRepete.getText()).toString()) || letraNumero.matcher(binding.pinRepete.getText().toString()).find())) {
                binding.pinRepete.requestFocus();
                binding.layoutPinRepete.setError(activity.getString(R.string.senha_invalida));
            } else
                signInFirebase(activity, binding.pin.getText().toString(), binding.pinRepete.getText().toString(), alertDialog, imageActivityResultLauncher);
        });
        binding.buttonFechar.setOnClickListener(view -> alertDialog.dismiss());
    }

    private static void editTextLayout(TextInputLayout textInputLayout, TextInputEditText textInputEditText, int hint, int inputType, Context context) {
        textInputLayout.setVisibility(View.VISIBLE);
        textInputLayout.setHint(context.getString(hint));
        textInputEditText.setInputType(inputType);
    }

    private static void signInFirebase(Activity activity, String email, String password, AlertDialog alertDialog, ActivityResultLauncher<Intent> imageActivityResultLauncher) {
        MainActivity.getProgressBar();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("parceiros");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        alertDialog.dismiss();
                        reference.child(getValueSharedPreferences(activity.getBaseContext(), "imei", "0000000000")).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                MainActivity.dismissProgressBar();
                                if (task1.getResult().exists()) {
                                    Cliente cliente = task1.getResult().getValue(Cliente.class);
                                    showToast(activity, Color.rgb(102, 153, 0), activity.getString(R.string.autent), R.drawable.ic_toast_feito);
                                    if (cliente != null)
                                        alertDialogSelectImage(cliente, activity, imageActivityResultLauncher);
                                    else
                                        showToast(activity.getBaseContext(), Color.rgb(204, 0, 0), activity.getString(R.string.dds_n_enc), R.drawable.ic_toast_erro);
                                } else
                                    showToast(activity.getBaseContext(), Color.rgb(204, 0, 0), activity.getString(R.string.imei_n_enc), R.drawable.ic_toast_erro);
                            } else {
                                mAuth.signOut();
                                MainActivity.dismissProgressBar();
                                alertDialog(activity.getString(R.string.erro), task1.getException().getMessage(), activity, R.drawable.ic_baseline_privacy_tip_24);
                            }
                        });
                    } else {
                        MainActivity.dismissProgressBar();
                        alertDialog(activity.getString(R.string.erro), task.getException().getMessage(), activity, R.drawable.ic_baseline_privacy_tip_24);
                    }
                });
    }

    public static void storageImageProductInFirebase(String imei, ImageView imageView, List<String> detalhes, Context context) {
        MainActivity.getProgressBar();
        String filename = UUID.randomUUID().toString();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("produtos/" + imei);
        StorageReference storeRef = FirebaseStorage.getInstance().getReference("parceiros/" + imei + "/imagens/produtos/" + filename);

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();
        mDatabase.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long countProduct = task.getResult().getChildrenCount();
                long quantidadeProduto = Long.parseLong(getValueSharedPreferences(context, "pac_qtd_pro", "0"));
//                if (countProduct <= quantidadeProduto) {
                if (true) {
                    UploadTask uploadTask = storeRef.putBytes(data);
                    uploadTask.addOnFailureListener(e -> {
                        MainActivity.dismissProgressBar();
                        alertDialog(context.getString(R.string.erro), e.getMessage(), context, R.drawable.ic_baseline_privacy_tip_24);
                    }).addOnSuccessListener(taskSnapshot -> storeRef.getDownloadUrl().addOnSuccessListener(url -> {
                        Map<String, String> produto = new HashMap<>();
                        String key = mDatabase.push().getKey();
                        produto.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        produto.put("nome", detalhes.get(0));
                        produto.put("preco", detalhes.get(1));
                        produto.put("codigoBarra", detalhes.get(2));
                        produto.put("categoria", detalhes.get(3));
                        produto.put("urlImage", url.toString());
                        produto.put("endereco", detalhes.get(4));
                        produto.put("empresa", detalhes.get(5));
                        produto.put("imei", detalhes.get(6));
                        mDatabase.child(key).setValue(produto).addOnSuccessListener(unused -> {
                            MainActivity.dismissProgressBar();
                            alertDialog(context.getString(R.string.prod_env_mbo), context.getString(R.string.prod) + ": " + detalhes.get(0) + "\n" + context.getString(R.string.preco) + ": " + formatPreco(detalhes.get(1)) + "\n" + (detalhes.get(2).isEmpty() ? "" : "CB: " + detalhes.get(2)), context, R.drawable.ic_baseline_done_24);
                        }).addOnFailureListener(e -> {
                            FirebaseStorage.getInstance().getReferenceFromUrl(url.toString()).delete().addOnSuccessListener(unused -> showToast(context, Color.rgb(102, 153, 0), context.getString(R.string.img_prod_eli), R.drawable.ic_toast_feito)).addOnFailureListener(e1 -> showToast(context, Color.rgb(204, 0, 0), context.getString(R.string.img_prod_nao_eli), R.drawable.ic_toast_erro));
                            MainActivity.dismissProgressBar();
                            alertDialog(context.getString(R.string.erro), e.getMessage(), context, R.drawable.ic_baseline_privacy_tip_24);
                        });
                    }).addOnFailureListener(e -> {
                        MainActivity.dismissProgressBar();
                        alertDialog(context.getString(R.string.erro), e.getMessage(), context, R.drawable.ic_baseline_privacy_tip_24);
                    }));
                } else
                    alertDialog(context.getString(R.string.erro), context.getString(R.string.atg_limit) + countProduct, context, R.drawable.ic_baseline_privacy_tip_24);
            } else
                alertDialog(context.getString(R.string.erro), task.getException().getMessage(), context, R.drawable.ic_baseline_privacy_tip_24);
        });
    }

    public static boolean isCampoVazio(String valor) {
        return (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
    }

    public static boolean isEmailValido(String email) {
        return (isCampoVazio(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    public static boolean isNumeroValido(String numero) {
        return (isCampoVazio(numero) || !Patterns.PHONE.matcher(numero).matches());
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

            } catch (IOException e) {
                showToast(activity.getBaseContext(), Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_erro);
            }
            return activity.getString(R.string.expo_concl) + "\n" + uri.getPath();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Ultilitario.showToast(activity.getBaseContext(), Color.rgb(102, 153, 0), s, R.drawable.ic_toast_feito);
        }
    }

    @SuppressWarnings("deprecation")
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

    public static void alertDialog(String titulo, String mensagem, Context context, int icon) {
        MainActivity.dismissProgressBar();
        new AlertDialog.Builder(context)
                .setIcon(icon)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setNegativeButton(R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static void setValueSharedPreferences(Context context, String idValue, String value) {
        context.getSharedPreferences("VALUE_STRING", Context.MODE_PRIVATE).edit().putString(idValue, value).apply();
    }

    public static String getValueSharedPreferences(Context context, String idValue, String defaultValue) {
        return context.getSharedPreferences("VALUE_STRING", Context.MODE_PRIVATE).getString(idValue, defaultValue);
    }

    public static String getTaxaIva(Activity activity) {
        return PreferenceManager.getDefaultSharedPreferences(activity).getString("taxa_iva", "0");
    }

    public static String getMotivoIsencao(Activity activity) {
        return PreferenceManager.getDefaultSharedPreferences(activity).getString("motivo_isencao", "M00");
    }

    public static boolean getNaoMostrarNovamente(Activity activity) {
        return PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("switch_most_nov", false);
    }

    public static void setNaoMostrarNovamente(Activity activity, boolean estado) {
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putBoolean("switch_most_nov", estado).apply();
    }

    public static String getAPN(Context context) {
        String apn = PreferenceManager.getDefaultSharedPreferences(context).getString("apn", "");
        return apn.isEmpty() ? context.getString(R.string.apn_mborasystem) : apn;
    }

    public static boolean getActivarAutenticacaoBiometrica(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("autenticacaobiometrica", true);
    }

    public static boolean getBooleanValue(Context context, String idvalue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(idvalue, false);
    }

    public static boolean getBooleanPreference(Context context, String idvalue) {
        return context.getSharedPreferences("VALUE_BOOLEAN", Context.MODE_PRIVATE).getBoolean(idvalue, false);
    }

    public static void setBooleanPreference(Context context, boolean value, String idvalue) {
        context.getSharedPreferences("VALUE_BOOLEAN", Context.MODE_PRIVATE).edit().putBoolean(idvalue, value).apply();
    }

    public static void setIntPreference(Context context, int value, String idvalue) {
        context.getSharedPreferences("VALUE_INT", Context.MODE_PRIVATE).edit().putInt(idvalue, value).apply();
    }

    public static int getIntPreference(Context context, String idvalue) {
        return context.getSharedPreferences("VALUE_INT", Context.MODE_PRIVATE).getInt(idvalue, 0);
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

    public static String getDataSplitDispositivo(String dataSplit) {
        String[] dataDavice = TextUtils.split(dataSplit, "-");
        return dataDavice[0].trim() + '-' + dataDavice[1].trim() + '-' + dataDavice[2].trim();
    }

    public static void getSelectedIdioma(Activity activity, String codigoIdioma, String msg, boolean isHome, boolean isSplash) {
        Locale locale = new Locale(codigoIdioma);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        activity.getBaseContext().getResources().updateConfiguration(config, activity.getBaseContext().getResources().getDisplayMetrics());
        if (isHome) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("lista_idioma", msg);
            editor.apply();
        }
        if (!isSplash) {
            restartActivity(activity);
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
        }
    }

    public static void restartActivity(Activity activity) {
        activity.finish();
        activity.startActivity(activity.getIntent());
    }

    public static String getSharedPreferencesIdioma(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("lista_idioma", "Português");
    }

    public static int getIdIdioma(Context context) {
        if (getSharedPreferencesIdioma(context).equalsIgnoreCase("Francês"))
            return 0;
        else if (getSharedPreferencesIdioma(context).equalsIgnoreCase("Inglês"))
            return 1;
        else if (getSharedPreferencesIdioma(context).equalsIgnoreCase("Português"))
            return 2;
        return 2;
    }

    @SuppressLint("Range")
    public static List<Documento> getPdfList(String pasta, boolean isPesquisa, String ficheiro, Context context) {
        Uri collection;
        String selection;
        String[] selectionArgs;
        List<Documento> pdfList = new ArrayList<>();
        final String[] projection = new String[]{
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
        };

        File dir = new File(String.valueOf(android.os.Environment.getExternalStorageDirectory()));
        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
        final String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
        if (isPesquisa) {
            selection = MediaStore.Files.FileColumns.DATA + " like ?"
                    + " AND " + MediaStore.Files.FileColumns.TITLE + " like ?"
                    + " AND " + MediaStore.Files.FileColumns.MIME_TYPE + " = ?";
            selectionArgs = new String[]{"%" + dir.getPath() + "/MboraSystem/" + pasta + "%", "%" + ficheiro + "%", mimeType};
        } else {
            selection = MediaStore.Files.FileColumns.DATA + " like ?"
                    + " AND " + MediaStore.Files.FileColumns.MIME_TYPE + " = ?";
            selectionArgs = new String[]{"%" + dir.getPath() + "/MboraSystem/" + pasta + "%", mimeType};
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
        else
            collection = MediaStore.Files.getContentUri("external");

        try (Cursor cursor = context.getContentResolver().query(collection, projection, selection, selectionArgs, sortOrder)) {
            assert cursor != null;
            if (cursor.moveToFirst()) {
                int columnTitle = cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE);
                int columnData = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                int columnDateCr = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED);
                int columnDateMo = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED);
                int columnSize = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                int columnType = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);
                do {
                    if (new File(cursor.getString(columnData)).exists()) {
                        Documento doc = new Documento();
                        doc.setNome(cursor.getString(columnTitle));
                        doc.setCaminho(cursor.getString(columnData));
                        doc.setData_cria(cursor.getLong(columnDateCr));
                        doc.setData_modifica(cursor.getLong(columnDateMo));
                        doc.setTamanho(cursor.getLong(columnSize));
                        doc.setTipo(cursor.getString(columnType));
                        pdfList.add(doc);
                    }
                } while (cursor.moveToNext());
            }
        }
        return pdfList;
    }

    public static void addFileContentProvider(Context context, String filePath) {
        File dir = new File(String.valueOf(android.os.Environment.getExternalStorageDirectory()));
        final String pdf = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
        final String[] selectionArgs = new String[]{dir.getPath() + "/MboraSystem/" + filePath};
        MediaScannerConnection.scanFile(context, selectionArgs, new String[]{pdf},
                (path, uri) -> {
                });
    }

    public static class Documento {
        private String nome;
        private String caminho;
        private long data_cria;
        private long data_modifica;
        private long tamanho;
        private String tipo;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getCaminho() {
            return caminho;
        }

        public void setCaminho(String caminho) {
            this.caminho = caminho;
        }

        public long getData_cria() {
            return data_cria;
        }

        public void setData_cria(long data_cria) {
            this.data_cria = data_cria;
        }

        public long getData_modifica() {
            return data_modifica;
        }

        public void setData_modifica(long data_modifica) {
            this.data_modifica = data_modifica;
        }

        public long getTamanho() {
            return tamanho;
        }

        public void setTamanho(long tamanho) {
            this.tamanho = tamanho;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }
    }

    public static String converterData(long data, boolean comHora) {
        SimpleDateFormat dateFormat;
        Date d = new Date(data * 1000);
        if (comHora)
            dateFormat = new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss", Locale.getDefault());
        else
            dateFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault());
        return dateFormat.format(d);  // formatted date in string
    }

    public static void spinnerProvincias(Context context, AppCompatSpinner provincias) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.provincias, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        provincias.setAdapter(adapter);
    }

    public static void spinnerMunicipios(Context context, AppCompatSpinner municipios) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.municipios, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        municipios.setAdapter(adapter);
    }

    public static String formatarValor(int valor) {
        return Ultilitario.formatPreco(String.valueOf(valor)).replaceAll(",", ".").replaceAll("Kz", "").replaceAll("\\s+", "");
    }

    public static File getFilePathCache(Context context, String file) throws IOException {
        File cacheFile = new File(context.getCacheDir(), file);
        try (InputStream inputStream = context.getAssets().open(file)) {
            try (FileOutputStream outputStream = new FileOutputStream(cacheFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
            }
        }
        return cacheFile;
    }

    public static void partilharImagem(Context context, Bitmap bitmap, String nomeEmpresa) throws IOException {
        File cachePath = new File(context.getCacheDir(), "images");
        cachePath.mkdirs(); // don't forget to make the directory
        FileOutputStream stream = new FileOutputStream(new File(cachePath, nomeEmpresa + ".jpg")); // overwrites this image every time
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        stream.close();
        Uri contentUri = FileProvider.getUriForFile(context, "com.yoga.mborasystem", new File(cachePath, nomeEmpresa + ".jpg"));
        if (contentUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.part_me_cod_qr)));
        }
    }

    public static void partilharDocumento(String filePath, Context context, String fileType, String titulo) {
        try {
            Uri fileURI;
            File file = new File(filePath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                fileURI = FileProvider.getUriForFile(context, "com.yoga.mborasystem", file);
            else
                fileURI = Uri.fromFile(file);

            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.setType(fileType);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.putExtra(Intent.EXTRA_STREAM, fileURI);
            context.startActivity(Intent.createChooser(share, titulo));
        } catch (Exception e) {
            alertDialog(context.getString(R.string.erro), e.getMessage(), context, R.drawable.ic_baseline_privacy_tip_24);
        }
    }

    public static void exportDB(Context context, Handler handler, String deviceID, String imei) {
        try {
            byte[] bytesID = getHash(reverse(deviceID) + "-" + reverse(imei));
            WeakReference<Context> contextWeakReference = new WeakReference<>(context);
            AppDataBase appDataBase = AppDataBase.getAppDataBase(contextWeakReference.get());
            String query = "PRAGMA wal_checkpoint(full)";
            Cursor cursor = appDataBase.query(query, null);
            if (cursor.moveToFirst()) {
                cursor.getInt(0);
                cursor.getInt(1);
                cursor.getInt(2);
            }
            cursor.close();
            File direct = new File(Environment.getExternalStorageDirectory() + "/MboraSystem/DATABASE-BACKUP");
            if (!direct.exists()) direct.mkdirs();
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String hora = TextUtils.split(getDateCurrent(), "-")[3];
                String nameDB = "database-mborasystem-" + bytesToHex(bytesID) + "-" + getDataFormatMonth(monthInglesFrances(getDateCurrent())) + "T" + hora + ".db";
                String currentDBPath = "//data//" + "com.yoga.mborasystem" + "//databases//" + "database-mborasystem";
                String backupDBPath = "/MboraSystem/DATABASE-BACKUP/" + nameDB;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                handler.post(() -> new AlertDialog.Builder(context)
                        .setIcon(R.drawable.ic_baseline_done_24)
                        .setTitle(context.getString(R.string.bd_expo))
                        .setMessage(context.getString(R.string.dds_exp) + "\n\n" + backupDB)
                        .setPositiveButton(context.getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss())
                        .setNegativeButton(context.getString(R.string.partilhar), (dialogInterface, i) -> partilharDocumento(Common.getAppPath("DATABASE-BACKUP") + nameDB, context, "application/db", context.getString(R.string.partilhar)))
                        .show());
            }
        } catch (Exception e) {
            handler.post(() -> alertDialog(context.getString(R.string.erro), e.getMessage(), context, R.drawable.ic_baseline_privacy_tip_24));
        } finally {
            MainActivity.dismissProgressBar();
        }
    }

    public static void importDB(Context context, Handler handler, String fileName) {
        try {
            File sd, data, backupDB, currentDB;
            sd = Environment.getExternalStorageDirectory();
            data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                String currentDBPath = "//data//" + "com.yoga.mborasystem" + "//databases//" + "database-mborasystem";
                String backupDBPath = "/MboraSystem/DATABASE-BACKUP/" + fileName;
                backupDB = new File(data, currentDBPath);
                currentDB = new File(sd, backupDBPath);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    File dbshm = new File(backupDB.getPath() + "-shm");
                    File dbwal = new File(backupDB.getPath() + "-wal");
                    if (dbshm.exists()) dbshm.delete();
                    if (dbwal.exists()) dbwal.delete();
                }
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                handler.post(() -> new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setIcon(R.drawable.ic_baseline_done_24)
                        .setTitle(context.getString(R.string.bd_impo))
                        .setMessage(context.getString(R.string.app_fis))
                        .setPositiveButton(context.getString(R.string.ok), (dialogInterface, i) -> System.exit(0))
                        .show());
            }
        } catch (Exception e) {
            handler.post(() -> alertDialog(context.getString(R.string.erro), e.getMessage(), context, R.drawable.ic_baseline_privacy_tip_24));
        } finally {
            MainActivity.dismissProgressBar();
        }
    }

    public static String reverse(String str) {
        StringBuilder sb = new StringBuilder(str);
        sb.reverse();
        return sb.toString();
    }

    public static void setValueUsuarioMaster(Bundle bundle, List<Cliente> cliente, Context context) {
        bundle.putString("nome", cliente.get(0).getNome() + " " + cliente.get(0).getSobrenome());
        bundle.putParcelable("cliente", cliente.get(0));
        Ultilitario.setBooleanPreference(context, true, "master");
        Ultilitario.setValueSharedPreferences(context, "imei", cliente.get(0).getImei());
        Ultilitario.setValueSharedPreferences(context, "nomeempresa", cliente.get(0).getNomeEmpresa());
    }

    public static String getDeviceUniqueID(Activity activity) {
        return Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static byte[] getHash(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(text.getBytes());
        return md.digest();
    }

    public static String bytesToHex(byte[] md) {
        return String.format("%064x", new BigInteger(1, md));
    }

    public static void definirModoEscuro(Context context) {
        switch (PreferenceManager.getDefaultSharedPreferences(context).getString("mod_esc", "")) {
            case "1":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "2":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "0":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }

    public static void getDetailDevice(Context context) {
        String data = "FABRICANTE: " + Build.MANUFACTURER + "\n" +
                "MARCA: " + Build.BRAND + "\n" +
                "PRODUTO: " + Build.PRODUCT + "\n" +
                "MODELO: " + Build.MODEL + "\n" +
                "VERSÃO: " + Build.VERSION.RELEASE + "\n" +
                "API: " + Build.VERSION.SDK_INT;
        alertDialog(context.getString(R.string.sob_tel), data, context, R.drawable.ic_baseline_store_24);
    }

    public static String getDetailDeviceString(Activity activity) {
        String data = Build.MANUFACTURER +
                Build.BRAND +
                Build.PRODUCT +
                Build.MODEL +
                Build.VERSION.RELEASE +
                Build.VERSION.SDK_INT +
                getDeviceUniqueID(activity);
        return data.trim();
    }

    public static void acercaMboraSystem(Context context, Activity activity) {
        MainActivity.getProgressBar();
        if (isNetworkConnected(context)) {
            if (internetIsConnected()) {
                String URL = Ultilitario.getAPN(context) + "/mborasystem-admin/public/api/contacts/contactos";
                Ion.with(activity)
                        .load(URL)
                        .asJsonArray()
                        .setCallback((e, jsonElements) -> {
                            try {
                                JsonObject parceiro = jsonElements.get(0).getAsJsonObject();
                                String contactos = parceiro.get("contactos").getAsString();
                                alertDialog(context.getString(R.string.nome_sistema), context.getString(R.string.acerca) + "\n" + contactos, context, R.drawable.ic_baseline_store_24);
                            } catch (Exception ex) {
                                MainActivity.dismissProgressBar();
                                new AlertDialog.Builder(context)
                                        .setIcon(R.drawable.ic_baseline_store_24)
                                        .setTitle(context.getString(R.string.erro))
                                        .setMessage(ex.getMessage())
                                        .setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.dismiss())
                                        .setPositiveButton(R.string.tent_nov, (dialog, which) -> {
                                            dialog.dismiss();
                                            MainActivity.getProgressBar();
                                            acercaMboraSystem(context, activity);
                                        })
                                        .show();
                            }
                        });
            } else {
                MainActivity.dismissProgressBar();
                Ultilitario.alertDialog(context.getString(R.string.erro), context.getString(R.string.sm_int), context, R.drawable.ic_baseline_privacy_tip_24);
            }
        } else {
            MainActivity.dismissProgressBar();
            Ultilitario.alertDialog(context.getString(R.string.erro), context.getString(R.string.conec_wif_dad), context, R.drawable.ic_baseline_privacy_tip_24);
        }
    }

    public static String getRasaoISE(Context context, String codigoRasaoISE) {
        final String[] codigo = context.getResources().getStringArray(R.array.array_motivo_isecao_valor);
        final String[] rasao = context.getResources().getStringArray(R.array.array_motivo_isecao);
        for (int i = 0; i <= codigo.length; i++) {
            if (codigoRasaoISE.equalsIgnoreCase(codigo[i]))
                return rasao[i];
        }
        return "";
    }

    public static String getDataEmissao(Context context) {
        return getValueSharedPreferences(context, "dataemissao", "");
    }

    public static int getValueWithDesconto(int valor, int descPerc) {
        return valor - ((valor * descPerc) / 100);
    }
}
