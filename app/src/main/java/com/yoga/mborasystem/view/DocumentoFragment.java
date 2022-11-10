package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.getDataFormatMonth;
import static com.yoga.mborasystem.util.Ultilitario.getPdfList;

import android.Manifest;
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
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentDocumentoBinding;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.model.entidade.Venda;
import com.yoga.mborasystem.util.EventObserver;
import com.yoga.mborasystem.util.SaftXMLDocument;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteCantinaViewModel;
import com.yoga.mborasystem.viewmodel.VendaViewModel;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

@SuppressWarnings("rawtypes")
public class DocumentoFragment extends Fragment {

    private int accao;
    private String pasta;
    private List<Venda> vendas;
    private GroupAdapter adapter;
    private VendaViewModel vendaViewModel;
    private FragmentDocumentoBinding binding;
    private List<ClienteCantina> clienteCantinas;
    private TextInputEditText dataInicio, dataFim;
    ClienteCantinaViewModel clienteCantinaViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new GroupAdapter();
        dataInicio = new TextInputEditText(requireContext());
        dataFim = new TextInputEditText(requireContext());
        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);
        clienteCantinaViewModel = new ViewModelProvider(requireActivity()).get(ClienteCantinaViewModel.class);
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
        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> getDocumentos(null, false, "", false));
        vendaViewModel.getProdutosVendaLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(produtoVendas -> {
            try {
                if (getArguments() != null)
                    new SaftXMLDocument().criarDocumentoSaft(requireContext(), getArguments().getParcelable("cliente"), getDataFormatMonth(Objects.requireNonNull(dataInicio.getText()).toString()), getDataFormatMonth(Objects.requireNonNull(dataFim.getText()).toString()), this.clienteCantinas, produtoVendas, this.vendas);
                else
                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.arg_null), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            } catch (ParserConfigurationException | TransformerException | IOException | SAXException e) {
                Ultilitario.alertDialog(getString(R.string.exp_saft), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
            MainActivity.dismissProgressBar();
        }));

        clienteCantinaViewModel.getCliente().observe(getViewLifecycleOwner(), clienteCantinas -> {
            this.clienteCantinas = clienteCantinas;
            List<Long> idvenda = new ArrayList<>();
            try {
                for (Venda venda : vendas)
                    idvenda.add(venda.getId());
                vendaViewModel.getProdutosVenda(0, null, null, false, true, idvenda);
            } catch (Exception e) {
                // Não eliminar este bloco try catch, ele pode gerar uma exceção, quando vendas for null
            }
        });
        vendaViewModel.getDocumentoDatatAppLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(data -> {
            if (accao == 0) {
                Toast.makeText(getContext(), data, Toast.LENGTH_LONG).show();
                getDocumentos(null, false, data, true);
            } else if (accao == 1)
                dataInicio.setText(data);
            else if (accao == 2)
                dataFim.setText(data);
        }));
        vendaViewModel.getVendasParaExportar().observe(getViewLifecycleOwner(), new EventObserver<>(vendas -> {
            if (vendas.isEmpty())
                Ultilitario.alertDialog(getString(R.string.exp_saft), getString(R.string.nao_tem_venda), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            else {
                this.vendas = vendas;
                List<Long> idcliente = new ArrayList<>();
                for (Venda venda : vendas)
                    idcliente.add(venda.getIdclicant());
                clienteCantinaViewModel.consultarClienteCantina(null, true, idcliente);
            }
        }));
        requireActivity().addMenuProvider(new MenuProvider() {
                                              @Override
                                              public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                                                  menuInflater.inflate(R.menu.menu_documento, menu);
                                                  if (getArguments() != null)
                                                      if (!getArguments().getBoolean("master"))
                                                          menu.findItem(R.id.itemSaft).setVisible(false);
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
                                                          if (newText.isEmpty())
                                                              getDocumentos(null, false, "", false);
                                                          else
                                                              getDocumentos(newText.replace("/", "_"), true, "", false);
                                                          return false;
                                                      }
                                                  });
                                              }

                                              @Override
                                              public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                                                  NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
                                                  if (menuItem.getItemId() == R.id.itemData)
                                                      getData(0);
                                                  else if (menuItem.getItemId() == R.id.itemSaft) {
                                                      Dexter.withContext(requireContext())
                                                              .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                              .withListener(new PermissionListener() {
                                                                  @Override
                                                                  public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                                                      dialogExportarDocumentoSaft();
                                                                  }

                                                                  @Override
                                                                  public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                                                      dialogExportarDocumentoSaft();
                                                                  }

                                                                  @Override
                                                                  public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                                                                  }
                                                              }).check();
                                                  }
                                                  return NavigationUI.onNavDestinationSelected(menuItem, navController);
                                              }
                                          },

                getViewLifecycleOwner());
        return binding.getRoot();
    }

    private void dialogExportarDocumentoSaft() {
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getContext());
        alert.setCancelable(false);
        alert.setIcon(R.drawable.ic_baseline_insert_drive_file_24);
        alert.setTitle(getString(R.string.exp_saft));
        final LinearLayoutCompat layout = new LinearLayoutCompat(requireContext());
        final TextView inicio = new TextView(requireContext());
        inicio.setText(getText(R.string.de));
        final TextView fim = new TextView(requireContext());
        fim.setText(getText(R.string.ate));
        layout.setOrientation(LinearLayoutCompat.VERTICAL);
        layout.setPadding(55, 0, 55, 0);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.addView(inicio);
        layout.addView(setTextInputEditText(dataInicio, 1));
        layout.addView(fim);
        layout.addView(setTextInputEditText(dataFim, 2));
        alert.setView(layout);
        alert.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy", Locale.getDefault());
                    try {
                        layout.removeAllViews();
                        if (Objects.requireNonNull(dataInicio.getText()).toString().isEmpty()) {
                            this.dialogExportarDocumentoSaft();
                            dataInicio.requestFocus();
                            Ultilitario.showToast(requireContext(), Color.rgb(200, 0, 0), getString(R.string.pri_dat_vaz), R.drawable.ic_toast_erro);
                        } else if (Objects.requireNonNull(dataFim.getText()).toString().isEmpty()) {
                            this.dialogExportarDocumentoSaft();
                            dataFim.requestFocus();
                            Ultilitario.showToast(requireContext(), Color.rgb(200, 0, 0), getString(R.string.seg_dat_vaz), R.drawable.ic_toast_erro);
                        } else if (Objects.requireNonNull(sdf.parse(dataFim.getText().toString())).compareTo(sdf.parse(dataInicio.getText().toString())) >= 0) {
                            MainActivity.getProgressBar();
                            vendaViewModel.getVendaSaft(Objects.requireNonNull(dataInicio.getText()).toString(), Objects.requireNonNull(dataFim.getText()).toString());
                        } else {
                            this.dialogExportarDocumentoSaft();
                            Ultilitario.alertDialog(getString(R.string.exp_saft), getString(R.string.dat_1_nao_dat_2, dataInicio.getText().toString(), dataFim.getText().toString()), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        }
                    } catch (ParseException e) {
                        Ultilitario.alertDialog(getString(R.string.exp_saft), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                    }
                }).setNegativeButton(getString(R.string.cancelar), (dialog, which) -> {
                    layout.removeAllViews();
                    dialog.dismiss();
                })
                .show();
    }

    private TextInputEditText setTextInputEditText(TextInputEditText textInputEditText,
                                                   int accao) {
        textInputEditText.setMaxLines(1);
        textInputEditText.setHint(getString(R.string.selec_data));
        textInputEditText.setOnFocusChangeListener((view, b) -> {
            if (b) getData(accao);
        });
        textInputEditText.setOnClickListener(view -> getData(accao));
        return textInputEditText;
    }

    private void getData(int accao) {
        this.accao = accao;
        DocumentoFragmentDirections.ActionDocumentoFragmentToDatePickerFragment direction = DocumentoFragmentDirections.actionDocumentoFragmentToDatePickerFragment(false).setIdcliente(1).setIsDivida(false).setIdusuario(1).setIsPesquisa(true);
        Navigation.findNavController(requireView()).navigate(direction);
    }

    private void getDocumentos(String ficheiro, boolean isPesquisa, String data,
                               boolean isPesquisaData) {
        if (pasta.equalsIgnoreCase("Facturas"))
            getDocumentPDF(pasta, R.string.fact_vend, R.string.fac_n_enc, isPesquisa, ficheiro, data, isPesquisaData);
        else
            getDocumentPDF(pasta, R.string.rel_dia_ven, R.string.rel_n_enc, isPesquisa, ficheiro, data, isPesquisaData);
    }

    @SuppressLint("SetTextI18n")
    private void getDocumentPDF(String pasta, int title, int msg, boolean isPesquisa, String
            ficheiro, String data, boolean isPesquisaData) {
        MainActivity.getProgressBar();
        requireActivity().setTitle(getString(title));
        List<Ultilitario.Documento> pdfList = new ArrayList<>(getPdfList(pasta, isPesquisa, ficheiro, requireContext()));
        binding.chipQuantDoc.setText(String.valueOf(pdfList.size()));
        adapter.clear();
        if (pdfList.isEmpty())
            Ultilitario.naoEncontrado(getContext(), adapter, msg);
        else {
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
                Ultilitario.swipeRefreshLayout(binding.mySwipeRefreshLayout);
                for (Ultilitario.Documento documento : pdfList)
                    adapter.add(new ItemDocumento(documento, requireContext(), pasta, title, msg));
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
                if (pasta.equals("Facturas"))
                    titulo = refFact[0].trim() + "/" + refFact[1].trim();
                else if (pasta.equals("Relatorios"))
                    titulo = documento.getNome();
            } catch (ArrayIndexOutOfBoundsException e) {
                titulo = documento.getNome();
            }
            nomeDocumento.setText(titulo);
            descricao.setText(Ultilitario.converterData(documento.getData_cria(), true) + "\n" + formatSize(documento.getTamanho()));
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
                            partilharDocumentoPDF(v, titulo);
                            return false;
                        });
                    } else if (pasta.equalsIgnoreCase("Facturas")) {
                        menu1.add(getString(R.string.abrir)).setOnMenuItemClickListener(item -> {
                            abrirDocumentoPDF(v);
                            return false;
                        });
                        menu1.add(getString(R.string.partilhar)).setOnMenuItemClickListener(item -> {
                            partilharDocumentoPDF(v, titulo);
                            return false;
                        });
                    }
                }
                if (getArguments() != null) {
                    if (getArguments().getBoolean("master")) {
                        menu1.add(getString(R.string.eliminar)).setOnMenuItemClickListener(item -> {
                            File file = new File(documento.getCaminho());
                            new AlertDialog.Builder(requireContext())
                                    .setIcon(R.drawable.ic_baseline_delete_40)
                                    .setTitle(titulo)
                                    .setMessage(R.string.tem_cert_elim_fich)
                                    .setNegativeButton(R.string.nao, (dialogInterface, i) -> dialogInterface.dismiss())
                                    .setPositiveButton(R.string.sim, (dialogInterface, i) -> {
                                        file.delete();
                                        if (file.exists()) {
                                            try {
                                                file.getCanonicalFile().delete();
                                            } catch (IOException e) {
                                                Ultilitario.alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                                            }
                                            if (file.exists())
                                                requireContext().deleteFile(file.getName());
                                            else {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                fileURI = FileProvider.getUriForFile(context, "com.yoga.mborasystem", file);
            else
                fileURI = Uri.fromFile(file);

            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(fileURI, "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent intent = Intent.createChooser(target, context.getString(R.string.ab_fi));

            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            try {
                if (activities.size() > 0)
                    context.startActivity(intent);
                else
                    Ultilitario.alertDialog(context.getString(R.string.fal_ab_pdf), context.getString(R.string.inst_app), context, R.drawable.ic_baseline_store_24);
            } catch (ActivityNotFoundException e) {
                Ultilitario.alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
        }

        private String formatSize(long size) {
            if (size <= 0)
                return "0";
            final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

        private void partilharDocumentoPDF(View v, String titulo) {
            v.setBackgroundColor(Color.parseColor("#6BD3D8D7"));
            new Handler(Looper.getMainLooper()).postDelayed(() -> v.setBackgroundColor(Color.WHITE), 1000);
            Ultilitario.partilharDocumento(documento.getCaminho(), context, "application/pdf", getString(R.string.part_doc) + " " + titulo);
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