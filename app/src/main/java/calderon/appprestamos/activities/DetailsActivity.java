package calderon.appprestamos.activities;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import calderon.appprestamos.R;
import calderon.appprestamos.adapters.MyAdapterAbono;
import calderon.appprestamos.adapters.MyAdapterPersona;
import calderon.appprestamos.models.Abono;
import calderon.appprestamos.models.Persona;

import static calderon.appprestamos.Util.Util.*;

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference userReference;
    private CollectionReference collectionReference;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();

    private RecyclerView recyclerView;
    private MyAdapterAbono myAdapterDetails;

    private FloatingActionButton fabAbono;
    private Toolbar toolbar;

    private int id;

    private TextView title;
    private TextView tvFecha;
    private TextView tvCantidad;
    private TextView tvSaldo;
    private TextView tvSaldoInicai;
    private TextView tvAbonos;
    private TextView tvAbonado;
    private TextView tvAbono;
    private TextView tvTipo;
    private TextView changeMethod;

    private TextView fechaPick;
    private EditText etAbono;

    private Persona persona;

    private boolean exitoso;
    private boolean borrado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Bundle bundle = getIntent().getExtras();

        userReference = db.collection(USUARIOS).document(user.getUid());

        if (bundle!=null) {
            id = bundle.getInt(ID);
            persona = (Persona) bundle.getSerializable(PERSONA);
            borrado = bundle.getBoolean(BORRADOS);
        }
        sendBind();
        setToolbar();
        getDate(fechaPick,"");
        setRecyclerView();
    }

    private void setRecyclerView() {
        collectionReference = userReference
                .collection(PRESTAMOS)
                .document(""+id)
                .collection(ABONOS);

        Query query = collectionReference.orderBy(ID, Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Abono> options = new FirestoreRecyclerOptions
                .Builder<Abono>()
                .setQuery(query, Abono.class)
                .build();

        myAdapterDetails = new MyAdapterAbono(options, new MyAdapterAbono.AddClickListener() {
            @Override
            public void onItemClick(Abono abono, int position) {

            }
        }, this, persona);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myAdapterDetails);
    }

    private void setToolbar() {
        title.setText(persona.getNombre());
        setSupportActionBar(toolbar);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("RestrictedApi")
    private void sendBind() {
        toolbar = findViewById(R.id.toolbar);
        title = findViewById(R.id.toolbar_title);
        fabAbono = findViewById(R.id.fabAbono);
        tvFecha = findViewById(R.id.fechaDetails);
        tvCantidad = findViewById(R.id.cantidadDetails);
        tvSaldo = findViewById(R.id.saldodDetails);
        tvSaldoInicai = findViewById(R.id.saldoInicial);
        tvTipo = findViewById(R.id.tipo_de_pago);
        fechaPick = findViewById(R.id.fechaDetailsPick);
        etAbono = findViewById(R.id.abono);
        recyclerView = findViewById(R.id.rvDetails);
        tvAbonos = findViewById(R.id.abonosDetails);
        tvAbono = findViewById(R.id.tv_abono);
        tvAbonado = findViewById(R.id.abonado_details);

        changeMethod = findViewById(R.id.changeMethod);

        fabAbono.setOnClickListener(this);
        fechaPick.setOnClickListener(this);

        tvAbono.setOnClickListener(this);

        if (borrado){
            fabAbono.setVisibility(View.GONE);
            etAbono.setEnabled(false);
        }

        final Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        final Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        etAbono.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0) {
                    fabAbono.setVisibility(View.GONE);
                    fabAbono.startAnimation(slideDown);
                }
                else {
                    if(s.length()==1 && before == 0) {
                        fabAbono.setVisibility(View.VISIBLE);
                        fabAbono.startAnimation(slideUp);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setData(persona);

        changeMethod.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                changeMethod();
                return true;
            }
        });
    }

    public void setData(Persona new_persona){
        if (new_persona.getFecha() != null) {
            tvFecha.setText(new_persona.getFecha());
            tvCantidad.setText(String.format(Locale.getDefault(), "$%d", new_persona.getCantidadPrestada()));
            tvSaldoInicai.setText(String.format(Locale.getDefault(), "$%d", new_persona.getMonto()*new_persona.getPlazos() ));
            tvTipo.setText(new_persona.getTipo());
        }
        tvAbonos.setText(String.format(Locale.getDefault(), "%d/%d", new_persona.getAbonos(), new_persona.getPlazos()));
        tvSaldo.setText(String.format(Locale.getDefault(), "$%d", new_persona.getSaldo()));
        tvAbonado.setText(String.format(Locale.getDefault(),"$%d",new_persona.getAbonado()));
    }

    private void changeMethod() {
        String new_type = tvTipo.getText().toString().equals(SEMANAL) ? QUINCENAL : SEMANAL;
        Log.i("id///////id",id+"-"+tvTipo.getText().toString()+"-"+new_type);
        userReference
                .collection(PRESTAMOS)
                .document(id+"")
                .update(TIPO,new_type)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(DetailsActivity.this, "Ha cambiado forma de pago", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_complete).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_add_black_24dp));
        if(borrado)
            menu.findItem(R.id.action_complete).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_deletes_items).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //goMain(this);
                onBackPressed();
                return true;
            case R.id.action_complete:
                addAbonoCompleted();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addAbonoCompleted() {
        final CollectionReference collectionReference = db.collection(USUARIOS).document(user.getUid()).collection(COMPLETADOS);
        collectionReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean exists = false;
                        List<DocumentSnapshot> documentSnapshot_list =  task.getResult().getDocuments();
                        for (DocumentSnapshot documentSnapshot : documentSnapshot_list) {
                            String id_ref = documentSnapshot.getReference().getId();
                            if((id+"").equals(id_ref)){
                                exists = true;
                                break;
                            }
                        }
                        if (exists) {
                            Toast.makeText(DetailsActivity.this, "El prestamo ya ha sido agregado", Toast.LENGTH_SHORT).show();
                        }else
                            userReference
                                    .collection(PRESTAMOS)
                                    .document(id+"")
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    final DocumentSnapshot documentSnapshot = task.getResult();
                                    userReference
                                            .collection(PRESTAMOS)
                                            .document(id+"")
                                            .collection(ABONOS)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    final List<DocumentSnapshot> list = task.getResult().getDocuments();

                                                    list.get(list.size()-1).getReference().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                            String fecha = task.getResult().getString(FECHA);

                                                            if (documentSnapshot.exists()) {
                                                                String nombre = documentSnapshot.getString(NOMBRE);
                                                                final int cantidadPrestada = documentSnapshot.getLong(CANTIDAD_PRESTADA).intValue();
                                                                final int monto = documentSnapshot.getLong(MONTO).intValue();
                                                                final int plazos = documentSnapshot.getLong(PLAZOS).intValue();
                                                                String fecha_prestamo = documentSnapshot.getString(FECHA);

                                                                Map<String,Object> map = new HashMap<>();
                                                                map.put(ID,id);
                                                                map.put(NOMBRE, nombre);
                                                                map.put(CANTIDAD_PRESTADA,cantidadPrestada);
                                                                map.put(GANANCIA,(monto*plazos)-cantidadPrestada);
                                                                map.put(FECHA_PRESTAMO,fecha_prestamo);
                                                                map.put(FECHA_FINAL,fecha);

                                                                collectionReference.document(id+"")
                                                                        .set(map)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                Snackbar.make(fabAbono,"Agregado Correctamente", Snackbar.LENGTH_SHORT).show();
                                                                                userReference
                                                                                        .get()
                                                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                int tc = task.getResult().getLong(TOTAL_COMPLETADO).intValue();
                                                                                               userReference.update(TOTAL_COMPLETADO,tc+(monto*plazos - cantidadPrestada));
                                                                                            }
                                                                                        });
                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                }
                            });
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R. id.fabAbono:
                dismissKeyboard();
                if (addAbono()) {
                    myAdapterDetails.notifyItemChanged(myAdapterDetails.getItemCount()-1);
                    recyclerView.scrollToPosition(myAdapterDetails.getItemCount()-1);
                }
                break;
            case R.id.fechaDetailsPick:
                setDate(this, fechaPick,"");
                break;
            case R.id.tv_abono:
                Log.i("CLICK",persona.getMonto()+"");
                etAbono.setText(persona.getMonto()+"");
                break;
        }
    }

    private boolean addAbono() {
        exitoso = false;
        if(etAbono.getText().toString().isEmpty()){
            etAbono.setError("Ingrese una cantidad");
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(150);
            }
        }else{

            final int abono = Integer.parseInt(etAbono.getText().toString());
            String fecha = fechaPick.getText().toString();

            int token = generateID(fecha);
            if(persona.getAbonos()<10)
                token = Integer.parseInt(token+"0"+persona.getAbonos());
            else
                token = Integer.parseInt(token+""+persona.getAbonos());


            Map<String, Object> map = new HashMap<>();
            map.put(ID,token);
            map.put(FECHA, dateShortToLong(fecha));
            map.put(ABONO,abono);
            map.put(SALDO, persona.getSaldo()-abono);
           userReference
                    .collection(PRESTAMOS)
                    .document(""+id)
                    .collection(ABONOS)
                    .document(token+"")
                    .set(map)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Snackbar.make(fabAbono, "Abono agregado", Snackbar.LENGTH_SHORT).show();
                            etAbono.setText("");
                            etAbono.requestFocus();
                            exitoso = true;
                            setData(updateData(persona,abono));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            exitoso = false;
                            Toast.makeText(DetailsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
            exitoso = true;
        }

        return exitoso;
    }

    @Override
    protected void onStart() {
        super.onStart();
        myAdapterDetails.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        myAdapterDetails.stopListening();
    }

    private int generateID(String date) {
        int m = 0;
        int p = (int) (Math.random() * 99 + 0);
        String plus = p+"";
        if(p<10)
            plus = "0"+p;
        String[] s = date.split("/");
        for (int i = 0; i < 12; i++) {
            if (s[1].equals(mesesCortos[i]))
                m = i + 1;
        }
        int id;
        if(Integer.parseInt(s[0]) < 10 && m <10)
            id = Integer.parseInt(s[2] + "0" +  (m) + "0" + s[0]);
        else if (Integer.parseInt(s[0]) < 10 && m >= 10)
            id = Integer.parseInt(s[2] + (m) + "0" + s[0]);
        else if (Integer.parseInt(s[0]) >= 10 && m <10)
            id = Integer.parseInt(s[2] + "0" + (m) + s[0]);
        else
            id = Integer.parseInt(s[2] + (m) + s[0]);
        return id;
    }

    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (null != getCurrentFocus())
            imm.hideSoftInputFromWindow(getCurrentFocus()
                    .getApplicationWindowToken(), 0);
    }

    private Persona updateData(final Persona p, final int abono){
        CollectionReference collectionReference = userReference
                .collection(PRESTAMOS);

        p.setAbonado(p.getAbonado()+abono);
        p.setSaldo(p.getSaldo()-abono);
        p.setAbonos(p.getAbonos()+1);
        final Map<String, Object> map = new HashMap<>();
        map.put(ABONADO,p.getAbonado());
        map.put(ABONOS,p.getAbonos());
        map.put(SALDO,p.getSaldo());
        collectionReference.document(""+id)
                .update(map);
        getCifras(new MyAdapterPersona.MyCallbackCifras() {
            @Override
            public void onCallback(int[] cifras) {
                Map<String, Object> map = new HashMap<>();

                int t = cifras[0]-abono;
                int tg;
                int tr;
                int antes_de_abonar = p.getAbonado() - abono;
                Log.i("##########",persona.toString());
                if(p.getCantidadPrestada() >= p.getAbonado()){ //Si aún no se ha cubierto la cantidad prestada
                        tg = cifras[1]; //No se resta a la ganancia
                        tr = cifras[2] - abono; //Se Resta a la cantidad a recuperar
                }else { //Si ya se cubrio la cantidad prestada
                    Log.i("#####",antes_de_abonar+"-"+p.getCantidadPrestada());
                    if(antes_de_abonar < p.getCantidadPrestada()) {//Se divide en ganar y recuperar
                        Log.i("#####","if");
                        int parte_g = p.getAbonado()-p.getCantidadPrestada();
                        tg = cifras[1] - parte_g;
                        tr = cifras[2] - (abono-parte_g);
                        Log.i("#######",abono+"-"+parte_g+"="+tr);
                    }else {
                        Log.i("#####","else");
                        tg = cifras[1] - abono; //Se resta a la cantidad a ganar
                        tr = cifras[2]; // no se resta a la cantidad a recuperar
                    }
                }

                map.put(TOTAL,t);
                map.put(TOTAL_GANAR,tg);
                map.put(TOTAL_RECUPERAR,tr);
                userReference.update(map);
            }
        });
        return  p;
    }

    public interface MyCallbackCifras {
        void  onCallback(int[] cifras);
    }

    private void getCifras(final MyAdapterPersona.MyCallbackCifras callbackCifras){
        userReference.get()
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
