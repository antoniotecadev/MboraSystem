package com.yoga.mborasystem.model.connectiondatabase;

import android.content.Context;

import com.yoga.mborasystem.model.dao.CategoriaDao;
import com.yoga.mborasystem.model.dao.ChaveAppDao;
import com.yoga.mborasystem.model.dao.ClienteCantinaDao;
import com.yoga.mborasystem.model.dao.ClienteDao;
import com.yoga.mborasystem.model.dao.ProdutoDao;
import com.yoga.mborasystem.model.dao.UsuarioDao;
import com.yoga.mborasystem.model.dao.VendaDao;
import com.yoga.mborasystem.model.datapopulate.CategoriaData;
import com.yoga.mborasystem.model.entidade.Categoria;
import com.yoga.mborasystem.model.entidade.ChaveApp;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.model.entidade.Usuario;
import com.yoga.mborasystem.model.entidade.Venda;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Usuario.class, Categoria.class, Produto.class, Cliente.class, ChaveApp.class, Venda.class, ProdutoVenda.class, ClienteCantina.class}, version = 1, exportSchema = false)
public abstract class AppDataBase extends RoomDatabase {

    public abstract UsuarioDao usuarioDao();

    public abstract CategoriaDao categoriaDao();

    public abstract ProdutoDao produtoDao();

    public abstract ClienteDao clienteDao();

    public abstract ChaveAppDao chaveAppDao();

    public abstract VendaDao vendaDao();

    public  abstract ClienteCantinaDao clienteCantinaDao();

    private static AppDataBase INSTANCIA;

    public static AppDataBase getAppDataBase(Context context) {

        if (INSTANCIA == null) {
            synchronized (AppDataBase.class) {
                INSTANCIA = Room.databaseBuilder(context, AppDataBase.class, "database-mborasystem")
                        .addCallback(new Callback() {
                            @Override
                            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                super.onCreate(db);
                                new CategoriaData(context, db).execute();
                            }
                        })
                        .build();
            }
        }
        return INSTANCIA;
    }

}
