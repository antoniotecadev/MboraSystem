package com.yoga.mborasystem.model.datapopulate;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.util.Ultilitario;

import java.util.HashMap;
import java.util.Map;

import androidx.sqlite.db.SupportSQLiteDatabase;

public class CategoriaData extends AsyncTask<Void, Void, Void> {


    private Context context;
    private SupportSQLiteDatabase db;

    public CategoriaData(Context context, SupportSQLiteDatabase db) {
        this.db = db;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        Map<String, String> categorias = new HashMap<>();

        categorias.put(context.getString(R.string.bazar), context.getString(R.string.maquina_electrodomestico));
        categorias.put(context.getString(R.string.bebidas), context.getString(R.string.agua_gasosa_sumo));
        categorias.put(context.getString(R.string.charcutaria_lacticios), context.getString(R.string.chourico_queijo_leite_yogurte));
        categorias.put(context.getString(R.string.frutas_legumes), context.getString(R.string.tomate_maca_laranja));
        categorias.put(context.getString(R.string.higiene_beleza), context.getString(R.string.papelhigienico_detergente_pastadedente_batom));
        categorias.put(context.getString(R.string.merciaria), context.getString(R.string.arroz_fuba_bolacha_cereais_cha_oleo));
        categorias.put(context.getString(R.string.padaria_pastelaria), context.getString(R.string.pao_bolo_pasteis_bolinho));
        categorias.put(context.getString(R.string.peixaria), context.getString(R.string.peixe_lagosta_caranguejo));
        categorias.put(context.getString(R.string.talho_congelados), context.getString(R.string.carne_frango_salsicha));

        ContentValues contentValues = new ContentValues();

        for (String ct : categorias.keySet()) {
            contentValues.put("categoria", ct);
            contentValues.put("descricao", categorias.get(ct));
            contentValues.put("estado", true);
            contentValues.put("data_cria", Ultilitario.getDateCurrent());
            db.insert("categorias", 0, contentValues);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(context, R.string.categorias_carregar, Toast.LENGTH_LONG).show();
    }

}
