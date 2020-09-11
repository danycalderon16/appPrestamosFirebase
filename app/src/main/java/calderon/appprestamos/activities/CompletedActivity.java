package calderon.appprestamos.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import calderon.appprestamos.R;
import calderon.appprestamos.adapters.MyAdapterCompleted;
import calderon.appprestamos.models.Completado;

import static calderon.appprestamos.Util.Util.*;

public class CompletedActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private MyAdapterCompleted myAdapterCompleted;
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        setToolbar();
        setRecyclerView();
    }

    private void setRecyclerView() {
        usersRef = db.collection(USUARIOS)
                .document(user.getUid())
                .collection(COMPLETADOS);

        Query query = usersRef.orderBy(ID, Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Completado> options = new FirestoreRecyclerOptions
                .Builder<Completado>()
                .setQuery(query, Completado.class)
                .build();
        myAdapterCompleted = new MyAdapterCompleted(options, this);


        recyclerView = findViewById(R.id.rv_completed);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myAdapterCompleted);
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar_completed);
        setSupportActionBar(toolbar);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                goMain(this);
                return true;
             default:
                 return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        myAdapterCompleted.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        myAdapterCompleted.stopListening();
    }
}
