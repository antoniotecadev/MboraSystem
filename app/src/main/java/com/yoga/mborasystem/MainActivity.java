package com.yoga.mborasystem;

import static com.yoga.mborasystem.util.Ultilitario.alertDialog;
import static com.yoga.mborasystem.util.Ultilitario.definirModoEscuro;
import static com.yoga.mborasystem.util.Ultilitario.getDataSplitDispositivo;
import static com.yoga.mborasystem.util.Ultilitario.getValueSharedPreferences;
import static com.yoga.mborasystem.util.Ultilitario.monthInglesFrances;
import static com.yoga.mborasystem.util.Ultilitario.setBooleanPreference;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.view.FacturaFragment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    public static TextView percentagem;
    public static ProgressBar progressBar;
    public static DrawerLayout drawerLayout;
    public static ProgressDialog progressDialog;
    public static NavigationView navigationView;
    private static final String CHANNEL_ID = "HEADS_UP_NOTIFICATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!getDataSplitDispositivo(getValueSharedPreferences(getApplicationContext(), "data", "00-00-0000")).equals(getDataSplitDispositivo(monthInglesFrances(Ultilitario.getDateCurrent()))))
            setBooleanPreference(getApplicationContext(), false, "estado_conta");

        definirModoEscuro(getApplicationContext());

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        toolbar = findViewById(R.id.toolbarMainActivity);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        percentagem = findViewById(R.id.percentagem);
        progressBar = findViewById(R.id.progressBarFirebase);

        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.splashFragment, R.id.homeFragment, R.id.dialogAlterarCliente, R.id.bloquearFragment, R.id.activarMbora, R.id.cadastrarClienteFragment)
                .setOpenableLayout(drawerLayout)
                .build();

        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.splashFragment) {
                toolbar.setVisibility(View.GONE);
                navigationView.getMenu().clear();
            } else if (destination.getId() == R.id.bloquearFragment) {
                toolbar.setVisibility(View.GONE);
                navigationView.getMenu().clear();
            } else if (destination.getId() == R.id.dialogCodigoPin) {
                toolbar.setVisibility(View.GONE);
                navigationView.getMenu().clear();
            } else if (destination.getId() == R.id.activarMbora) {
                toolbar.setVisibility(View.GONE);
                navigationView.getMenu().clear();
            } else if (destination.getId() == R.id.homeFragment) {
                toolbar.setVisibility(View.VISIBLE);
                navigationView.inflateMenu(R.menu.menu_drawer);
            } else {
                toolbar.setVisibility(View.VISIBLE);
                navigationView.getMenu().clear();
            }
        });
        asNotificationPermission();
        createNotificationChannel();
    }

    public static void setVisibilityProgressBar(int view) {
        percentagem.setVisibility(view);
        progressBar.setVisibility(view);
    }

    public static void setProgressBar(int progress) {
        percentagem.setText(progress + " %");
        progressBar.setProgress(progress);
    }

    public static void getProgressBar() {
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialogo_view);
        progressDialog.getWindow().setLayout(200, 200);
    }

    public static void dismissProgressBar() {
        if (progressDialog.isShowing() && progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void asNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
                requestPermissionLauncherNotification.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private final ActivityResultLauncher<String> requestPermissionLauncherNotification = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (!result)
                    alertDialog(getString(R.string.erro), getString(R.string.sm_perm_n_pod_rec_not), this, R.drawable.ic_baseline_privacy_tip_24);
            }
    );

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Informações do aplicativo", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Mensagem enviada pela YOGA");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : Objects.requireNonNull(getSupportFragmentManager().getPrimaryNavigationFragment()).getChildFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        FacturaFragment.myOnKeyDown(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (Fragment fragment : Objects.requireNonNull(getSupportFragmentManager().getPrimaryNavigationFragment()).getChildFragmentManager().getFragments()) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static class MyFirebaseMessagingService extends FirebaseMessagingService {
        @Override
        public void onMessageReceived(@NonNull RemoteMessage message) {
            super.onMessageReceived(message);
            if (message.getNotification() != null) {
                String titulo = message.getNotification().getTitle();
                String corpo = message.getNotification().getBody();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_store_24)
                        .setContentTitle(titulo)
                        .setContentText(corpo)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(corpo))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify((int) message.getSentTime(), builder.build());
            }
        }
        /**
         * There are two scenarios when onNewToken is called:
         * 1) When a new token is generated on initial app startup
         * 2) Whenever an existing token is changed
         * Under #2, there are three scenarios when the existing token is changed:
         * A) App is restored to a new device
         * B) User uninstalls/reinstalls the app
         * C) User clears app data
         */
        @Override
        public void onNewToken(@NonNull String token) {
            // If you want to send messages to this application instance or
            // manage this apps subscriptions on the server side, send the
            // FCM registration token to your app server.
            Map<String, Object> update = new HashMap<>();
            String imei = getValueSharedPreferences(this, "imei", "0000000000");
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("parceiros");
            update.put("/" + imei + "/token", token);
            mDatabase.updateChildren(update).addOnCompleteListener(task -> {
                if (!task.isSuccessful())
                    alertDialog("Update Token", task.getException().getMessage(), this, R.drawable.ic_baseline_close_24);
            });
        }
    }
}