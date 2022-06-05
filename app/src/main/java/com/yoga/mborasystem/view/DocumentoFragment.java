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
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
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
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentDocumentoBinding;
import com.yoga.mborasystem.util.Ultilitario;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DocumentoFragment extends Fragment {

    private String pasta;
    private GroupAdapter adapter;
    private FragmentDocumentoBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = new GroupAdapter();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDocumentoBinding.inflate(inflater, container, false);
        binding.recyclerViewListaDoc.setAdapter(adapter);
        pasta = "Facturas";
        getDocumentPDF(pasta, R.string.fact_vend, R.string.fac_n_enc, false, null);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.factura:
                    pasta = "Facturas";
                    getDocumentPDF(pasta, R.string.fact_vend, R.string.fac_n_enc, false, null);
                    break;
                case R.id.relatorio:
                    pasta = "Relatorios";
                    getDocumentPDF(pasta, R.string.rel_dia_ven, R.string.rel_n_enc, false, null);
                    break;
                default:
                    break;
            }
            return true;
        });
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_documento, menu);
        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getString(R.string.fich));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.onActionViewExpanded();
        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getDocumentos(null, false);
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
                    getDocumentos(null, false);
                } else {
                    getDocumentos(newText, true);
                }
                return false;
            }
        });
    }

    private void getDocumentos(String ficheiro, boolean isPesquisa) {
        if (pasta.equalsIgnoreCase("Facturas")) {
            getDocumentPDF(pasta, R.string.fact_vend, R.string.fac_n_enc, isPesquisa, ficheiro);
        } else {
            getDocumentPDF(pasta, R.string.rel_dia_ven, R.string.rel_n_enc, isPesquisa, ficheiro);
        }
    }

    private void getDocumentPDF(String pasta, int title, int msg, boolean isPesquisa, String ficheiro) {
        List<Ultilitario.Documento> pdfList = new ArrayList<>();
        pdfList.addAll(getPdfList(pasta, isPesquisa, ficheiro, requireContext()));
        binding.chipQuantDoc.setText(String.valueOf(pdfList.size()));
        adapter.clear();
        requireActivity().setTitle(getString(title));
        if (pdfList.isEmpty()) {
            Ultilitario.naoEncontrado(getContext(), adapter, msg);
        } else {
            for (Ultilitario.Documento documento : pdfList)
                adapter.add(new ItemDocumento(documento, requireContext(), pasta, title, msg));
        }
    }

    class ItemDocumento extends Item<GroupieViewHolder> {

        private final String pasta;
        private final int title;
        private final int msg;
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
            nomeDocumento.setText(documento.getNome());
            descricao.setText(Ultilitario.converterData(documento.getData_modifica()) + " - " + formatSize(documento.getTamanho()));
            viewHolder.itemView.setOnClickListener(v -> {
                abrirDocumentoPDF(v);
            });
            registerForContextMenu(menu);
            menu.setOnClickListener(View::showContextMenu);
            viewHolder.itemView.setOnCreateContextMenuListener((menu1, v, menuInfo) -> {
                menu1.setHeaderIcon(R.drawable.ic_baseline_store_24);
                menu1.setHeaderTitle(documento.getNome());
                menu1.add(getString(R.string.abrir)).setOnMenuItemClickListener(item -> {
                    abrirDocumentoPDF(v);
                    return false;
                });
                menu1.add(getString(R.string.partilhar)).setOnMenuItemClickListener(item -> {
                    partilharDocumentoPDF(v);
                    return false;
                });
                menu1.add(getString(R.string.eliminar)).setOnMenuItemClickListener(item -> {
                    File file = new File(documento.getCaminho());
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
                            Snackbar.make(v, documento.getNome() + " " + getString(R.string.elmnd), Snackbar.LENGTH_LONG).show();
                            getDocumentPDF(pasta, title, msg, false, null);
                        }
                    } else {
                        Snackbar.make(v, documento.getNome() + " " + getString(R.string.elmnd), Snackbar.LENGTH_LONG).show();
                        getDocumentPDF(pasta, title, msg, false, null);
                    }
                    return false;
                });
                menu1.add(getString(R.string.det)).setOnMenuItemClickListener(item -> {
                    detalhes();
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
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                v.setBackgroundColor(Color.WHITE);
            }, 1000);
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
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                v.setBackgroundColor(Color.WHITE);
            }, 1000);
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

        private void detalhes() {
            Ultilitario.alertDialog(getString(R.string.det), getString(R.string.nome_fich) + ": " + documento.getNome()
                            + "\n" + getString(R.string.tipo_fich) + ": " + documento.getTipo()
                            + "\n" + getString(R.string.tama_fich) + ": " + formatSize(documento.getTamanho())
                            + "\n" + getString(R.string.data_modifica) + ": " + Ultilitario.converterData(documento.getData_modifica())
                            + "\n" + getString(R.string.caminho) + ": " + documento.getCaminho()
                    , context, R.drawable.ic_baseline_store_24);
        }
    }
}