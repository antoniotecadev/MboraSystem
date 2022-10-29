package com.yoga.mborasystem;

import static com.yoga.mborasystem.util.Ultilitario.definirModoEscuro;
import static com.yoga.mborasystem.util.Ultilitario.getDataSplitDispositivo;
import static com.yoga.mborasystem.util.Ultilitario.monthInglesFrances;
import static com.yoga.mborasystem.util.Ultilitario.setBooleanPreference;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.view.FacturaFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    public static TextView percentagem;
    public static ProgressBar progressBar;
    public static DrawerLayout drawerLayout;
    public static ProgressDialog progressDialog;
    public static NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!getDataSplitDispositivo(Ultilitario.getValueSharedPreferences(getApplicationContext(), "data", "00-00-0000")).equals(getDataSplitDispositivo(monthInglesFrances(Ultilitario.getDateCurrent()))))
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
}