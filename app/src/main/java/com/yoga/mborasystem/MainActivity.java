package com.yoga.mborasystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.yoga.mborasystem.view.FacturaFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavController navController;
    public static ProgressDialog progressDialog;
    private NavigationView navigationView;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        toolbar = findViewById(R.id.toolbarMainActivity);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        setSupportActionBar(toolbar);

        navController = Navigation.findNavController(this, R.id.fragment);
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.splashFragment, R.id.homeFragment, R.id.dialogAlterarCliente, R.id.bloquearFragment, R.id.activarMbora, R.id.cadastrarClienteFragment)
                .setDrawerLayout(drawerLayout)
                .build();

        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
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
            }
        });
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
        for (Fragment fragment : getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager().getFragments()) {
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
        for (Fragment fragment : getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager().getFragments()) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}