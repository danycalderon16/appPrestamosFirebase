package calderon.appprestamos.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

import calderon.appprestamos.R;
import calderon.appprestamos.models.Completado;

import static calderon.appprestamos.Util.Util.*;

public class MyAdapterCompleted extends FirestoreRecyclerAdapter<Completado, MyAdapterCompleted.ViewHolder>{
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private DocumentReference reference;

    private Activity activity;

    public MyAdapterCompleted(@NonNull FirestoreRecyclerOptions<Completado> options, Activity activity) {
        super(options);
        this.activity = activity;
        reference = db.collection(USUARIOS)
                .document(user.getUid());
    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i, @NonNull Completado completado) {
        viewHolder.nombre.setText(completado.getNombre());
        viewHolder.ganancia.setText((String.format(Locale.getDefault(), "$%d", completado.getGanancia())));
        viewHolder.cantidadPrestada.setText((String.format(Locale.getDefault(), "$%d", completado.getCantidadPrestada())));
        viewHolder.fecha.setText(completado.getFecha_final());
        viewHolder.fecha_prestamo.setText(completado.getFecha_prestamo());

        viewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(viewHolder.linearLayout.getVisibility() ==View.GONE){
                   TransitionManager.beginDelayedTransition(viewHolder.cardView, new AutoTransition());
                   viewHolder.linearLayout.setVisibility(View.VISIBLE);
                   viewHolder.button.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
               }else{
                   TransitionManager.beginDelayedTransition(viewHolder.cardView, new AutoTransition());
                   viewHolder.linearLayout.setVisibility(View.GONE);
                   viewHolder.button.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
               }
            }
        });
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(viewHolder.linearLayout.getVisibility() ==View.GONE){
                   TransitionManager.beginDelayedTransition(viewHolder.cardView, new AutoTransition());
                   viewHolder.linearLayout.setVisibility(View.VISIBLE);
                   viewHolder.button.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
               }else{
                   TransitionManager.beginDelayedTransition(viewHolder.cardView, new AutoTransition());
                   viewHolder.linearLayout.setVisibility(View.GONE);
                   viewHolder.button.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
               }
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_completados, parent, false);

        return new ViewHolder(v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        TextView nombre;
        TextView ganancia;
        TextView fecha;
        TextView fecha_prestamo;
        TextView cantidadPrestada;

        LinearLayout linearLayout;
        Button button;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nameCompleted);
            ganancia = itemView.findViewById(R.id.quantityCompleted);
            fecha = itemView.findViewById(R.id.dateCompleted);
            fecha_prestamo = itemView.findViewById(R.id.fecha_prestamo);
            cantidadPrestada = itemView.findViewById(R.id.cantidadPrestada);

            button = itemView.findViewById(R.id.btn_expand_collapse);
            linearLayout = itemView.findViewById(R.id.linearLayoutDetails1);
            cardView = itemView.findViewById(R.id.card_view);

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
        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                int totoalGanadao = task.getResult().getLong(TOTAL_COMPLETADO).intValue();
                int ganado = getSnapshots().getSnapshot(adapterPosition).getLong(GANANCIA).intValue();
                reference.update(TOTAL_COMPLETADO,totoalGanadao-ganado)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                getSnapshots().getSnapshot(adapterPosition).getReference().delete();

                            }
                        });
            }
        });
    }
}
