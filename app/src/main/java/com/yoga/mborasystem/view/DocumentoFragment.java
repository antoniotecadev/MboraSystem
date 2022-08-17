package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.getPdfList;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentDocumentoBinding;
import com.yoga.mborasystem.util.EventObserver;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.VendaViewModel;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class DocumentoFragment extends Fragment {

    private String pasta;
    private GroupAdapter adapter;
    private VendaViewModel vendaViewModel;
    private FragmentDocumentoBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GroupAdapter();
        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDocumentoBinding.inflate(inflater, container, false);
        binding.recyclerViewListaDoc.setAdapter(adapter);
        pasta = "Facturas";
        getDocumentPDF(pasta, R.string.fact_vend, R.string.fac_n_enc, false, null, "", false);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.factura:
                    pasta = "Facturas";
                    getDocumentPDF(pasta, R.string.fact_vend, R.string.fac_n_enc, false, null, "", false);
                    break;
                case R.id.relatorio:
                    pasta = "Relatorios";
                    getDocumentPDF(pasta, R.string.rel_dia_ven, R.string.rel_n_enc, false, null, "", false);
                    break;
                default:
                    break;
            }
            return true;
        });
        vendaViewModel.getDocumentoDatatAppLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(data -> {
            Toast.makeText(getContext(), data, Toast.LENGTH_LONG).show();
            getDocumentos(null, false, data, true);
        }));
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_documento, menu);
                SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
                MenuItem menuItem = menu.findItem(R.id.app_bar_search);
                SearchView searchView = (SearchView) menuItem.getActionView();
                searchView.setQueryHint(getString(R.string.nm) + ", " + getString(R.string.referencia));
                searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
                searchView.onActionViewExpanded();
                menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        getDocumentos(null, false, "", false);
                        return true;
                    }
                });

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (newText.isEmpty()) {
                            getDocumentos(null, false, "", false);
                        } else {
                            getDocumentos(newText.replace("/", "_"), true, "", false);
                        }
                        return false;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
                if (menuItem.getItemId() == R.id.btnData) {
                    DocumentoFragmentDirections.ActionDocumentoFragmentToDatePickerFragment direction = DocumentoFragmentDirections.actionDocumentoFragmentToDatePickerFragment(false).setIdcliente(1).setIsDivida(false).setIdusuario(1).setIsPesquisa(true);
                    Navigation.findNavController(requireView()).navigate(direction);
                }
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        }, getViewLifecycleOwner());
        return binding.getRoot();
    }

    private void getDocumentos(String ficheiro, boolean isPesquisa, String data, boolean isPesquisaData) {
        if (pasta.equalsIgnoreCase("Facturas")) {
            getDocumentPDF(pasta, R.string.fact_vend, R.string.fac_n_enc, isPesquisa, ficheiro, data, isPesquisaData);
        } else {
            getDocumentPDF(pasta, R.string.rel_dia_ven, R.string.rel_n_enc, isPesquisa, ficheiro, data, isPesquisaData);
        }
    }

    @SuppressLint("SetTextI18n")
    private void getDocumentPDF(String pasta, int title, int msg, boolean isPesquisa, String
            ficheiro, String data, boolean isPesquisaData) {
        MainActivity.getProgressBar();
        requireActivity().setTitle(getString(title));
        List<Ultilitario.Documento> pdfList = new ArrayList<>(getPdfList(pasta, isPesquisa, ficheiro, requireContext()));
        binding.chipQuantDoc.setText(String.valueOf(pdfList.size()));
        adapter.clear();
        if (pdfList.isEmpty()) {
            Ultilitario.naoEncontrado(getContext(), adapter, msg);
        } else {
            if (isPesquisaData) {
                int i = 0;
                for (Ultilitario.Documento documento : pdfList) {
                    if (data.equals(Ultilitario.converterData(documento.getData_cria(), false))) {
                        binding.chipQuantDoc.setText(++i + "");
                        adapter.add(new ItemDocumento(documento, requireContext(), pasta, title, msg));
                    }
                }
                if (i == 0) {
                    binding.chipQuantDoc.setText(i + "");
                    Ultilitario.naoEncontrado(getContext(), adapter, msg);
                }
            } else {
                for (Ultilitario.Documento documento : pdfList) {
                    adapter.add(new ItemDocumento(documento, requireContext(), pasta, title, msg));
                }
            }
        }
        MainActivity.dismissProgressBar();
    }

    class ItemDocumento extends Item<GroupieViewHolder> {

        private final String pasta;
        private final int title;
        private final int msg;
        private String titulo;
        private final Context context;
        private final Ultilitario.Documento documento;

        public ItemDocumento(Ultilitario.Documento documento, Context context, String pasta, int title, int msg) {
            this.documento = documento;
            this.context = context;
            this.pasta = pasta;
            this.title = title;
            this.msg = msg;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView nomeDocumento = viewHolder.itemView.findViewById(R.id.txtNomeDocumento);
            TextView descricao = viewHolder.itemView.findViewById(R.id.txtDescricao);
            ImageButton menu = viewHolder.itemView.findViewById(R.id.imgBtnMenu);
            String[] refFact = TextUtils.split(documento.getNome(), "_");
            try {
                titulo = refFact[0].trim() + "/" + refFact[1].trim();
            } catch (ArrayIndexOutOfBoundsException e) {
                titulo = documento.getNome();
            }
            nomeDocumento.setText(titulo);
            descricao.setText(documento.getNome() + "\n" + Ultilitario.converterData(documento.getData_cria(), true) + " - " + formatSize(documento.getTamanho()));
            viewHolder.itemView.setOnClickListener(this::abrirDocumentoPDF);
            registerForContextMenu(menu);
            menu.setOnClickListener(View::showContextMenu);
            viewHolder.itemView.setOnCreateContextMenuListener((menu1, v, menuInfo) -> {
                menu1.setHeaderIcon(R.drawable.ic_baseline_store_24);
                menu1.setHeaderTitle(titulo);
                if (getArguments() != null) {
                    if (pasta.equalsIgnoreCase("Relatorios") && getArguments().getBoolean("master") ||
                            pasta.equalsIgnoreCase("Facturas") && getArguments().getBoolean("master")) {
                        menu1.add(getString(R.string.abrir)).setOnMenuItemClickListener(item -> {
                            abrirDocumentoPDF(v);
                            return false;
                        });
                        menu1.add(getString(R.string.partilhar)).setOnMenuItemClickListener(item -> {
                            partilharDocumentoPDF(v);
                            return false;
                        });
                    } else if (pasta.equalsIgnoreCase("Facturas")) {
                        menu1.add(getString(R.string.abrir)).setOnMenuItemClickListener(item -> {
                            abrirDocumentoPDF(v);
                            return false;
                        });
                        menu1.add(getString(R.string.partilhar)).setOnMenuItemClickListener(item -> {
                            partilharDocumentoPDF(v);
                            return false;
                        });
                    }
                }
                if (getArguments() != null) {
                    if (getArguments().getBoolean("master")) {
                        menu1.add(getString(R.string.eliminar)).setOnMenuItemClickListener(item -> {
                            File file = new File(documento.getCaminho());
                            new AlertDialog.Builder(requireContext())
                                    .setIcon(R.drawable.ic_baseline_store_24)
                                    .setTitle(titulo)
                                    .setMessage(R.string.tem_cert_elim_fich)
                                    .setNegativeButton(R.string.nao, (dialogInterface, i) -> dialogInterface.dismiss())
                                    .setPositiveButton(R.string.sim, (dialogInterface, i) -> {
                                        file.delete();
                                        if (file.exists()) {
                                            try {
                                                file.getCanonicalFile().delete();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                            if (file.exists()) {
                                                requireContext().deleteFile(file.getName());
                                            } else {
                                                Snackbar.make(v, titulo + " " + getString(R.string.elmnd), Snackbar.LENGTH_LONG).show();
                                                getDocumentPDF(pasta, title, msg, false, null, "", false);
                                            }
                                        } else {
                                            Snackbar.make(v, titulo + " " + getString(R.string.elmnd), Snackbar.LENGTH_LONG).show();
                                            getDocumentPDF(pasta, title, msg, false, null, "", false);
                                        }
                                    }).show();
                            return false;
                        });
                    }
                }
                menu1.add(getString(R.string.det)).setOnMenuItemClickListener(item -> {
                    detalhes(titulo);
                    return false;
                });
            });
        }

        @Override
        public int getLayout() {
            return R.layout.layout_documento_fragment;
        }

        private void abrirDocumentoPDF(View v) {
            Uri fileURI;
            v.setBackgroundColor(Color.parseColor("#6BD3D8D7"));
            new Handler(Looper.getMainLooper()).postDelayed(() -> v.setBackgroundColor(Color.WHITE), 1000);
            File file = new File(documento.getCaminho());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileURI = FileProvider.getUriForFile(context, "com.yoga.mborasystem", file);
            } else {
                fileURI = Uri.fromFile(file);
            }
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(fileURI, "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent intent = Intent.createChooser(target, context.getString(R.string.ab_fi));

            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            try {
                if (activities.size() > 0) {
                    context.startActivity(intent);
                } else {
                    Ultilitario.alertDialog(context.getString(R.string.fal_ab_pdf), context.getString(R.string.inst_app), context, R.drawable.ic_baseline_store_24);
                }
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        private String formatSize(long size) {
            if (size <= 0)
                return "0";
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

        private void partilharDocumentoPDF(View v) {
            Uri fileURI;
            v.setBackgroundColor(Color.parseColor("#6BD3D8D7"));
            new Handler(Looper.getMainLooper()).postDelayed(() -> v.setBackgroundColor(Color.WHITE), 1000);
            File file = new File(documento.getCaminho());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileURI = FileProvider.getUriForFile(context, "com.yoga.mborasystem", file);
            } else {
                fileURI = Uri.fromFile(file);
            }
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.setType("application/pdf");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.putExtra(Intent.EXTRA_STREAM, fileURI);
            startActivity(Intent.createChooser(share, getString(R.string.part_fich)));
        }

        private void detalhes(String titulo) {
            Ultilitario.alertDialog(getString(R.string.det), getString(R.string.nome_fich) + ": " + documento.getNome()
                            + "\n" + getString(R.string.referencia) + ": " + titulo
                            + "\n" + getString(R.string.tipo_fich) + ": " + documento.getTipo()
                            + "\n" + getString(R.string.tama_fich) + ": " + formatSize(documento.getTamanho())
                            + "\n" + getString(R.string.data_modifica) + ": " + Ultilitario.converterData(documento.getData_modifica(), true)
                            + "\n" + getString(R.string.caminho) + ": " + documento.getCaminho()
                    , context, R.drawable.ic_baseline_store_24);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}