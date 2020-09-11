package calderon.appprestamos.adapters;

import android.app.Activity;
import android.content.DialogInterface;
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
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import calderon.appprestamos.R;
import calderon.appprestamos.activities.DetailsActivity;
import calderon.appprestamos.models.Abono;
import calderon.appprestamos.models.Persona;
import static calderon.appprestamos.Util.Util.*;


public class MyAdapterAbono extends FirestoreRecyclerAdapter<Abono, MyAdapterAbono.ViewHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private CollectionReference reference;
    private AddClickListener listener;
    private Activity activity;
    private Persona persona;
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MyAdapterAbono(@NonNull FirestoreRecyclerOptions<Abono> options, AddClickListener listener, Activity activity, Persona persona) {
        super(options);
        this.listener = listener;
        this.activity = activity;
        this.persona = persona;
        reference = db.collection(USUARIOS)
                .document(user.getUid())
                .collection(PRESTAMOS)
                .document(persona.getId()+"")
                .collection(ABONOS);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyAdapterAbono.ViewHolder viewHolder, final int position, @NonNull final Abono abono) {
        viewHolder.date.setText(abono.getFecha());
        viewHolder.quanity.setText(String.format(Locale.getDefault(),"-$%d", abono.getAbono()));
        viewHolder.saldoAnterior.setText(String.format(Locale.getDefault(),"$%d", abono.getSaldo()));
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(abono, position);
            }
        });;
    }

    @NonNull
    @Override
    public MyAdapterAbono.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_details, parent, false);

        return new ViewHolder(view);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        TextView date;
        TextView quanity;
        TextView saldoAnterior;
        LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.dateDetails);
            quanity = itemView.findViewById(R.id.quantityDetails);
            saldoAnterior = itemView.findViewById(R.id.quantitySaldo);
            linearLayout = itemView.findViewById(R.id.cardDetails);

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
            builder.setMessage("¿Desea borrar Abono?");
            builder.setPositiveButton("Sí",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteInBD(getAdapterPosition());
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

    private void deleteInBD(final int adapterPosition) {
        reference.document(getSnapshots().getSnapshot(adapterPosition).getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                final int abono = document.getLong(ABONO).intValue();
                                document.getReference().getParent().getParent().get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        DocumentSnapshot document = task.getResult();
                                        if(document.exists()){
                                            final Map<String, Object> map = document.getData();
                                            long long_abonado = ((long) map.get(ABONADO) ) - abono;
                                            long long_saldo = ((long) map.get(SALDO) ) + abono;
                                            long long_abonos = ((long) map.get(ABONOS) ) - 1;
                                            long long_cantidad = (long) map.get(CANTIDAD_PRESTADA);

                                            final int new_abonado = (int) long_abonado;
                                            final int new_abonos = (int) long_abonos;
                                            final int new_saldo = (int) long_saldo;
                                            final int cantidadPrestada = (int) long_cantidad;

                                            map.put(ABONADO, new_abonado);
                                            map.put(SALDO,new_saldo);
                                            map.put(ABONOS, new_abonos);
                                            getCifras(new MyCallbackCifras() {
                                                @Override
                                                public void onCallback(int[] cifras) {
                                                    int t = cifras[0] + abono;
                                                    int tg = 0;
                                                    int tr = 0;

                                                    int abonodo_anterior = new_abonado + abono;
                                                    if(cantidadPrestada >= abonodo_anterior){ //Si aún no se ha cubierto la cantidad prestada
                                                        tg = cifras[1]; //No se resta a la ganancia
                                                        tr = cifras[2] + abono; //Se Resta a la cantidad a recuperar
                                                    }else { //Si ya se cubrio la cantidad prestada
                                                        if(new_abonado < cantidadPrestada) {//Se divide en ganar y recuperar
                                                            Log.i("#####","if");
                                                            int parte_g = abonodo_anterior- cantidadPrestada;
                                                            tg = cifras[1] + parte_g;
                                                            tr = cifras[2] + (abono-parte_g);
                                                            Log.i("#######",abono+"-"+parte_g+"="+tr);
                                                        }else {
                                                            Log.i("#####","else");
                                                            tg = cifras[1] + abono; //Se resta a la cantidad a ganar
                                                            tr = cifras[2]; // no se resta a la cantidad a recuperar
                                                        }
                                                    }

                                                    Map<String, Object> totales = new HashMap<>();
                                                    totales.put(TOTAL, t);
                                                    totales.put(TOTAL_RECUPERAR, tr);
                                                    totales.put(TOTAL_GANAR, tg);

                                                    db.collection(USUARIOS)
                                                            .document(user.getUid())
                                                            .update(totales);
                                                }
                                            },db,user);
                                            document.getReference().update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    getSnapshots().getSnapshot(adapterPosition).getReference().delete();
                                                    persona.setSaldo(new_saldo);
                                                    persona.setAbonado(new_abonado);
                                                    persona.setAbonos(new_abonos);
                                                    ((DetailsActivity)activity).setData(persona);
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    }
                })
        ;
    }

    public interface AddClickListener{
        void onItemClick(Abono abono, int position);
    }
}

