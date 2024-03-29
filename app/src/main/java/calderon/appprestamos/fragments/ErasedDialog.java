package calderon.appprestamos.fragments;

import static calderon.appprestamos.Util.Util.BORRADOS;
import static calderon.appprestamos.Util.Util.COMPLETADOS;
import static calderon.appprestamos.Util.Util.ID;
import static calderon.appprestamos.Util.Util.PERSONA;
import static calderon.appprestamos.Util.Util.TOTAL_COMPLETADO;
import static calderon.appprestamos.Util.Util.USUARIOS;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Locale;

import calderon.appprestamos.R;
import calderon.appprestamos.activities.DetailsActivity;
import calderon.appprestamos.activities.MainActivity;
import calderon.appprestamos.adapters.MyAdapterCompleted;
import calderon.appprestamos.adapters.MyAdapterPersona;
import calderon.appprestamos.models.Completado;
import calderon.appprestamos.models.Persona;

public class ErasedDialog extends DialogFragment {

    private Callback callback;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private MyAdapterPersona myAdapterPersona;
    private RecyclerView recyclerView;
    private TextView textView;
    private ImageButton imageButton;

    public static ErasedDialog newInstance(){
        return new ErasedDialog();
    }

    public void setCallback(Callback callback){
        this.callback = callback;
    }

    public interface Callback{
        void onActionClick(String name);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ErasedDialogTheme);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fullscreen_erased, container, false);

        imageButton = view.findViewById(R.id.fullscreen_dialog_close_erased);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        recyclerView = view.findViewById(R.id.rv_erased_dialog);

        usersRef = db.collection(USUARIOS)
                .document(user.getUid())
                .collection(BORRADOS);

        Query query = usersRef.orderBy(ID, Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Persona> options = new FirestoreRecyclerOptions
                .Builder<Persona>()
                .setQuery(query, Persona.class)
                .build();
        myAdapterPersona = new MyAdapterPersona(options, getActivity(),new MyAdapterPersona.addClickListener() {
            @Override
            public void onItemClick(Persona persona, int position) {
                Intent intent = new Intent(getContext(),DetailsActivity.class);
                intent.putExtra(ID, persona.getId());
                intent.putExtra(PERSONA, persona);
                intent.putExtra(BORRADOS, true);
                // To retrieve object in second Activity
                getActivity().getIntent().getSerializableExtra("MyClass");
                startActivity(intent);
            }
        },null,null,true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(myAdapterPersona);

        return view;
    }

    public void setData() {
        db.collection(USUARIOS).document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                int ganado = task.getResult().getLong(TOTAL_COMPLETADO).intValue();
                textView.setText(String.format(Locale.getDefault(),"$%d", ganado));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        myAdapterPersona.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        myAdapterPersona.stopListening();
    }

}
