package calderon.appprestamos.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Locale;

import calderon.appprestamos.R;
import calderon.appprestamos.adapters.MyAdapterPersona;
import calderon.appprestamos.fragments.CompletedDialog;
import calderon.appprestamos.fragments.FullscreenDialog;
import calderon.appprestamos.models.Persona;

import static calderon.appprestamos.Util.Util.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference usersRef;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private MyAdapterPersona myAdapterPersona;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    private SharedPreferences preferences;

    private TextView calculadora;
    private int total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        usersRef = db.collection(USUARIOS).document(user.getUid());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRecyclerView();
        calculadora = findViewById(R.id.tv_calculadora);
        calculadora.setText(String.format(Locale.getDefault(),"$%d",total));
        calculadora.setOnClickListener(this);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showInfoDialog();
                return false;
            }
        });
    }

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater  = getLayoutInflater();
        View v = inflater.inflate(R.layout.info_layout,null);

        final TextView infoRecuperar = v.findViewById(R.id.txtRecuperar);
        final TextView infoGanar     = v.findViewById(R.id.txtGanar);
        final TextView infoTotal     = v.findViewById(R.id.txtTotal);

        getCifras(new MyAdapterPersona.MyCallbackCifras() {
            @Override
            public void onCallback(int[] cifras) {
                infoTotal.setText(String.format(Locale.getDefault(),"$%d",cifras[0]));
                infoGanar.setText(String.format(Locale.getDefault(),"$%d", cifras[1]));
                infoRecuperar.setText(String.format(Locale.getDefault(),"$%d",cifras[2]));
            }
        });
        builder.setCancelable(true);
        builder.create();
        builder.setView(v);
        builder.show();
    }

    private void setRecyclerView() {
        final CollectionReference ref = usersRef.collection(PRESTAMOS);
        Query query = ref.orderBy(ID, Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Persona> options = new FirestoreRecyclerOptions
                .Builder<Persona>()
                .setQuery(query, Persona.class)
                .build();
        myAdapterPersona = new MyAdapterPersona(options, this, new MyAdapterPersona.addClickListener() {
            @Override
            public void onItemClick(Persona persona, int position) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra(ID, persona.getId());
                intent.putExtra(PERSONA, persona);
                // To retrieve object in second Activity
                getIntent().getSerializableExtra("MyClass");
                startActivity(intent);
            }
        },
        new MyAdapterPersona.addClickListener() {
            @Override
            public void onItemClick(Persona persona, int position) {
                total += persona.getMonto();
                calculadora.setText(String.format(Locale.getDefault(),"$%d",total));
                Log.i("///////////////////77","$"+total);
            }
        },
        new MyAdapterPersona.addClickListener() {
            @Override
            public void onItemClick(Persona persona, int position) {

            }
        });

        recyclerView = findViewById(R.id.rv_personas);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myAdapterPersona);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Toast.makeText(MainActivity.this, "Move", Toast.LENGTH_SHORT).show();
                return false;
            }
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int direction) {
               new AlertDialog.Builder(viewHolder.itemView.getContext())
                        .setMessage("¿Esta seguro de borrar este prestamo?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myAdapterPersona.deleteInBD(viewHolder.getAdapterPosition());
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();                            }
                        })
                        .create()
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_complete){
           /* Intent intent = new Intent(this, CompletedActivity.class);
            startActivity(intent);*/
            DialogFragment dialog = CompletedDialog.newInstance();
            ((CompletedDialog) dialog).setCallback(new CompletedDialog.Callback() {
                @Override
                public void onActionClick(String name) {

                }
            });
            dialog.show(getSupportFragmentManager(),"tag");
            return true;
        }

        if(id == R.id.action_logout){
            GoogleSignInClient mGoogleSignInClient;
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            auth.signOut();
            mGoogleSignInClient.signOut();
            mGoogleSignInClient.revokeAccess();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        myAdapterPersona.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        myAdapterPersona.stopListening();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            DialogFragment dialog = FullscreenDialog.newInstance();
            ((FullscreenDialog) dialog).setCallback(new FullscreenDialog.Callback() {
                @Override
                public void onActionClick(String name) {
                    Toast.makeText(MainActivity.this, name, Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show(getSupportFragmentManager(), "tag");
        }
        if(v.getId() == R.id.tv_calculadora){
            total = 0;
            calculadora.setText(String.format(Locale.getDefault(),"$%d",total));
            Log.i("///////////////////77","$"+total);
        }
    }

    public interface MyCallbackCifras {
        void  onCallback(int[] cifras);
    }

    private void getCifras(final MyAdapterPersona.MyCallbackCifras callbackCifras){
        usersRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            int t = documentSnapshot.getLong(TOTAL).intValue();
                            int tg = documentSnapshot.getLong(TOTAL_GANAR).intValue();
                            int tr = documentSnapshot.getLong(TOTAL_RECUPERAR).intValue();

                            int cifras[] = {t,tg,tr};
                            callbackCifras.onCallback(cifras);
                        }
                    }
                });
    }
}
