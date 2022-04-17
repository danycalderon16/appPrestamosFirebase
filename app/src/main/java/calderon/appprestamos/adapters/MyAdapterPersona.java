package calderon.appprestamos.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import calderon.appprestamos.R;
import calderon.appprestamos.models.Persona;

import static calderon.appprestamos.Util.Util.*;

public class MyAdapterPersona extends FirestoreRecyclerAdapter<Persona, MyAdapterPersona.ViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    private SharedPreferences prefsID;

    private Activity activity;
    private addClickListener listener;
    private addClickListener listener_calculadora;
    private addClickListener longListener;

    public MyAdapterPersona(@NonNull FirestoreRecyclerOptions<Persona> options,
                            Activity activity,
                            addClickListener listener,
                            addClickListener listener_calculadora) {
        super(options);
        this.activity = activity;
        this.listener = listener;
        this.listener_calculadora = listener_calculadora;
        this.longListener = longListener;
        prefsID = activity.getSharedPreferences("id-" + user.getUid(), Context.MODE_PRIVATE);
    }


    @Override
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position, @NonNull final Persona persona) {
        viewHolder.nombre.setText(persona.getNombre());
        viewHolder.prestamo.setText(String.format(Locale.getDefault(), "$%d", persona.getCantidadPrestada()));
        viewHolder.fecha.setText(persona.getFecha());
        viewHolder.saldo.setText(String.format(Locale.getDefault(), "$%d", persona.getSaldo()));
        viewHolder.abono.setText(String.format(Locale.getDefault(), "$%d", persona.getMonto()));
        viewHolder.abonos.setText(String.format(Locale.getDefault(), "%d/%d", persona.getAbonos(), persona.getPlazos()));
        viewHolder.saldoInicial.setText(String.format(Locale.getDefault(), "$%d", persona.getPlazos() * persona.getMonto()));
        viewHolder.abonado.setText(String.format(Locale.getDefault(), "$%d", persona.getAbonado()));
        String tipo = persona.getTipo();
        viewHolder.hideLy.setVisibility(persona.isExpanded() ? View.VISIBLE : View.GONE);
        if (tipo.equals(SEMANAL))
            viewHolder.view.setBackgroundResource(R.color.semanal);
        if (tipo.equals(QUINCENAL))
            viewHolder.view.setBackgroundResource(R.color.quincenal);

        viewHolder.nombre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(persona, position);
            }
        });
        viewHolder.fecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean expanded = persona.isExpanded();
                persona.setExpanded(!expanded);
                notifyItemChanged(position);
            }
        });
        viewHolder.abonos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener_calculadora.onItemClick(persona, position);
            }
        });
        viewHolder.card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longListener.onItemClick(persona, position);
                return false;
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_main, parent, false);
        return new ViewHolder(v);
    }

    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        TextView nombre;
        TextView prestamo;
        TextView fecha;
        TextView saldo;
        TextView abonos;
        TextView abono;
        TextView saldoInicial;
        TextView abonado;
        CardView card;
        View view;
        LinearLayout hideLy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombre);
            prestamo = itemView.findViewById(R.id.prestamo);
            fecha = itemView.findViewById(R.id.fecha);
            saldo = itemView.findViewById(R.id.saldo);
            abonos = itemView.findViewById(R.id.abonos);
            abono = itemView.findViewById(R.id.tv_hd_abono);
            saldoInicial = itemView.findViewById(R.id.tv_hd_saldo_inicial);
            abonado = itemView.findViewById(R.id.tv_hd_abonado);
            view = itemView.findViewById(R.id.noteColorView);
            card = itemView.findViewById(R.id.card_view_personas);
            hideLy = itemView.findViewById(R.id.hide_ly);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuInflater inflater = activity.getMenuInflater();
            inflater.inflate(R.menu.meno_delete, menu);
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setOnMenuItemClickListener(this);
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.delete) {
                showConfirmDeleteDiaglog();
                return true;
            }
            return false;
        }

        public void showConfirmDeleteDiaglog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setCancelable(true);
            builder.setMessage("¿Desea borrar prestamo?");
            builder.setPositiveButton("Sí",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteInBD(getAdapterPosition());
                        }
                    });
            builder.setNeutralButton("TEST", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String id = getSnapshots().getSnapshot(getAdapterPosition()).getReference().getId();


                    DocumentReference doc = db.collection(USUARIOS)
                            .document(user.getUid())
                            .collection(PRESTAMOS)
                            .document(id);
                    doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    DocumentReference docRef = db.collection(USUARIOS)
                                            .document(user.getUid())
                                            .collection(BORRADOS)
                                            .document(id);
                                    docRef.set(document.getData()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            int  noAbobos = 0;
                                            noAbobos = document.getLong(ABONOS).intValue();
                                            Log.i("#########", "DocumentSnapshot data: " + document.getData());
                                            int finalNoAbobos = noAbobos;
                                            doc.collection(ABONOS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    int i = 0;
                                                    while(i<finalNoAbobos){
                                                        String idRef = task.getResult().getDocuments().get(i).getId();
                                                        docRef.collection(ABONOS)
                                                                .document(idRef)
                                                                .set(task.getResult().getDocuments().get(i).getData())
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                    Log.i("### abono agregado bo","exito");

                                                                    }
                                                                });
                                                        i++;
                                                    }
                                                }
                                            });
                                            Log.i("### borrado","exito");

                                        }
                                    });
                                } else {
                                    Log.i("#########", "No such document");
                                }
                            } else {
                                Log.i("#########", "get failed with ", task.getException());
                            }
                        }
                    });
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void deleteInBD(final int position) {
        getNumbersFromDB(new MyCallbackPerosana() {
            @Override
            public void onCallback(final Persona persona, final String id) {
                getCifras(new MyCallbackCifras() {
                    @Override
                    public void onCallback(int[] cifras) {
                        int saldo = persona.getSaldo();
                        int cantidad = persona.getCantidadPrestada();
                        int totalPagar = persona.getPlazos() * persona.getMonto();
                        int abonado = persona.getAbonado();

                        int t = totalPagar - abonado;

                        int tr = 0;
                        int tg = 0;
                        if (abonado < cantidad) {
                            tr = cantidad - abonado;
                            tg = totalPagar - cantidad;
                        } else
                            tg = saldo;

                        final int total = cifras[0] - t;
                        final int totalGanar = cifras[1] - tg;
                        final int totalRecuperar = cifras[2] - tr;

                        Map<String, Object> map = new HashMap<>();
                        map.put(TOTAL, total);
                        map.put(TOTAL_RECUPERAR, totalRecuperar);
                        map.put(TOTAL_GANAR, totalGanar);

                        final List<String> ids = getIDFromSP(prefsID);
                        Log.i("$$$$$$", ids.toString());
                        for (int i = 0; i < ids.size(); i++) {
                            if (ids.get(i).equals(id))
                                ids.remove(i);
                        }
                        Log.i("$$$$$$ despues", ids.toString());

                        db.collection(USUARIOS).document(user.getUid()).update(map)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.i("///////////7", "Atualizado");
                                        getSnapshots().getSnapshot(position).getReference().delete();
                                        //Toast.makeText(activity, , Toast.LENGTH_SHORT).show();
                                        View v = (activity).findViewById(R.id.fab);
                                        Snackbar.make(v, "Borrado exitoso", Snackbar.LENGTH_SHORT).show();
                                        saveIDInSP(prefsID, ids);
                                    }
                                });
                    }
                });
            }
        }, position);
    }

    public interface MyCallbackPerosana {
        void onCallback(Persona persona, String id);
    }

    private void getNumbersFromDB(final MyCallbackPerosana callback, final int position) {
        final String id = getSnapshots().getSnapshot(position).getReference().getId();
        db.collection(USUARIOS)
                .document(user.getUid())
                .collection(PRESTAMOS)
                .document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                int plazos = document.getLong(PLAZOS).intValue();
                                int abonado = document.getLong(ABONADO).intValue();
                                int cantidad = document.getLong(CANTIDAD_PRESTADA).intValue();
                                int abonos_dados = document.getLong(ABONOS).intValue();
                                int monto = document.getLong(MONTO).intValue();
                                int saldo = document.getLong(SALDO).intValue();

                                callback.onCallback(new Persona(cantidad, saldo, abonos_dados, monto, plazos, abonado), id);
                            } else
                                Log.d("########", "No such document");
                        } else
                            Log.d("############", "get failed with ", task.getException());
                    }
                });
    }

    public interface MyCallbackCifras {
        void onCallback(int[] cifras);
    }

    private void getCifras(final MyCallbackCifras callbackCifras) {
        db.collection(USUARIOS).document(user.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            int t = documentSnapshot.getLong(TOTAL).intValue();
                            int tg = documentSnapshot.getLong(TOTAL_GANAR).intValue();
                            int tr = documentSnapshot.getLong(TOTAL_RECUPERAR).intValue();

                            int cifras[] = {t, tg, tr};
                            callbackCifras.onCallback(cifras);
                        }
                    }
                });
    }

    public interface addClickListener {
        void onItemClick(Persona persona, int position);
    }


}
