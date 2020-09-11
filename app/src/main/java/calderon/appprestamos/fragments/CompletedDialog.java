package calderon.appprestamos.fragments;

import android.os.Bundle;
import android.util.Log;
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
import calderon.appprestamos.adapters.MyAdapterCompleted;
import calderon.appprestamos.models.Completado;

import static calderon.appprestamos.Util.Util.*;

public class CompletedDialog extends DialogFragment {

    private Callback callback;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private MyAdapterCompleted myAdapterCompleted;
    private RecyclerView recyclerView;
    private TextView textView;
    private ImageButton imageButton;

    public static CompletedDialog newInstance(){
        return new CompletedDialog();
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
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CompletedDialogTheme);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fullscreen_completed, container, false);

        imageButton = view.findViewById(R.id.fullscreen_dialog_close);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        textView = view.findViewById(R.id.editText_total_dialog);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setData();
            }
        });

        setData();

        recyclerView = view.findViewById(R.id.rv_completed_dialog);

        usersRef = db.collection(USUARIOS)
                .document(user.getUid())
                .collection(COMPLETADOS);

        Query query = usersRef.orderBy(ID, Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Completado> options = new FirestoreRecyclerOptions
                .Builder<Completado>()
                .setQuery(query, Completado.class)
                .build();
        myAdapterCompleted = new MyAdapterCompleted(options, getActivity());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(myAdapterCompleted);

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
        myAdapterCompleted.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        myAdapterCompleted.stopListening();
    }

}
