package calderon.appprestamos.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import calderon.appprestamos.R;
import calderon.appprestamos.adapters.MyAdapterPersona;

import static android.content.Context.VIBRATOR_SERVICE;
import static calderon.appprestamos.Util.Util.*;

public class FullscreenDialog extends DialogFragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private Callback callback;
    private TextInputLayout nombre;
    private TextInputLayout cantidad;
    private TextInputLayout plazos;
    private TextInputLayout pagos;
    private TextView fecha;

    private RadioGroup group;
    private RadioButton rb_quincenal;
    private RadioButton rb_semanal;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private SharedPreferences prefTotales;
    private SharedPreferences prefID;

    private List<String> ids;
    private String tipo;

    public static FullscreenDialog newInstance() {
        return new FullscreenDialog();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        prefTotales = getActivity()
                .getSharedPreferences("totales-"+user.getUid(), Context.MODE_PRIVATE);
        prefID = getActivity()
                .getSharedPreferences("id-"+user.getUid(), Context.MODE_PRIVATE);

        ids = getIDFromSP(prefID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fullscreen_dialog, container, false);

        ImageButton close = view.findViewById(R.id.fullscreen_dialog_close);
        TextView action = view.findViewById(R.id.fullscreen_dialog_action);
        nombre = view.findViewById(R.id.nombre);
        cantidad = view.findViewById(R.id.cantidad);
        plazos = view.findViewById(R.id.plazos);
        fecha = view.findViewById(R.id.fecha);
        pagos = view.findViewById(R.id.pagos);

        group = view.findViewById(R.id.radio_group);
        rb_quincenal = view.findViewById(R.id.quincenal);
        rb_semanal = view.findViewById(R.id.semanal);

        tipo = QUINCENAL;

        fecha.setOnClickListener(this);
        close.setOnClickListener(this);
        action.setOnClickListener(this);

        group.setOnCheckedChangeListener(this);

        getDate(fecha);
        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fullscreen_dialog_close:
                dismiss();
                break;
            case R.id.fullscreen_dialog_action:
                if (guardar()) {
                    dismiss();
                    Snackbar snackBar = Snackbar.make(getActivity().findViewById(R.id.fab),
                            "Prestamo agregado correctamente", Snackbar.LENGTH_LONG);
                    snackBar.show();
                }
                break;
            case R.id.fecha:
                setDate(getContext(), fecha);
                break;
        }
    }

    private boolean guardar() {
        int saldo = 0;
        if (!validateEditText(nombre) | !validateEditText(cantidad) |
                !validateEditText(plazos) | !validateEditText(pagos))
            return false;
        saldo = Integer.parseInt(plazos.getEditText().getText().toString()) *
                Integer.parseInt(pagos.getEditText().getText().toString());

        String text_fecha = fecha.getText().toString();
        int monto = Integer.parseInt(pagos.getEditText().getText().toString());
        int num_plazos = Integer.parseInt(plazos.getEditText().getText().toString());
        final int num_cantidad = Integer.parseInt(cantidad.getEditText().getText().toString());

        int token;
        do
            token = generateID(text_fecha);
        while(!validarID(token));

        ids.add(token+"");
        saveIDInSP(prefID,ids);

        Map<String, Object> mapR = new HashMap<>();
        mapR.put(ID, token);
        mapR.put(NOMBRE, nombre.getEditText().getText().toString());
        mapR.put(CANTIDAD_PRESTADA, num_cantidad);
        mapR.put(FECHA, text_fecha);
        mapR.put(SALDO, saldo);
        mapR.put(TIPO, tipo);
        mapR.put(ABONOS, 0);
        mapR.put(MONTO, monto);
        mapR.put(PLAZOS, num_plazos);
        mapR.put(ABONADO, 0);
        db.collection(USUARIOS)
                .document(user.getUid())
                .collection(PRESTAMOS)
                .document(token + "").set(mapR)
                //.add(mapR)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                });

        final int finalSaldo = saldo;
        final int final_num_cantidad = num_cantidad;
        getCifras(new MyAdapterPersona.MyCallbackCifras() {
            @Override
            public void onCallback(int[] cifras) {
                final int t = (cifras[0] + finalSaldo);
                final int tg = (cifras[1] + (finalSaldo - final_num_cantidad));
                final int tr = (cifras[2] + num_cantidad);

                Map<String, Object> map = new HashMap<>();
                map.put(TOTAL,t);
                map.put(TOTAL_GANAR,tg);
                map.put(TOTAL_RECUPERAR,tr);


                db.collection(USUARIOS)
                        .document(user.getUid())
                        .update(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.i("/////////////77","Agregado");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("/////////////77","Failed");
                                Log.i("/////////////77",e.getMessage());
                            }
                        });
            }
        });

        return true;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch(checkedId) {
            case R.id.semanal:
                tipo = SEMANAL;
                break;
            case R.id.quincenal:
                tipo = QUINCENAL;
                break;
        }

    }

    public interface Callback {
        void onActionClick(String name);
    }

    private boolean validateEditText(TextInputLayout textInputLayout) {
        String text = textInputLayout.getEditText().getText().toString().trim();
        if (text.isEmpty()) {
            textInputLayout.setError("Campo requerido");
            if (Build.VERSION.SDK_INT >= 26) {
                ((Vibrator) getContext()
                        .getSystemService(VIBRATOR_SERVICE))
                        .vibrate(VibrationEffect.createOneShot(150, 10));
            } else {
                ((Vibrator) getContext()
                        .getSystemService(VIBRATOR_SERVICE))
                        .vibrate(150);
            }
            return false;
        } else {
            textInputLayout.setError(null);
            return true;
        }
    }

    private int generateID(String date) {
        int m = 0;
        int p = (int) (Math.random() * 10 + 0);
        String[] s = date.split(" ");
        for (int i = 0; i < 12; i++) {
            if (s[2].equals(meses[i]))
                m = i + 1;
        }
        int id;
        if(Integer.parseInt(s[0]) < 10 && m <10)
            id = Integer.parseInt(s[4] + "0" +  (m) + "0" + s[0] + p);
        else if (Integer.parseInt(s[0]) < 10 && m >= 10)
            id = Integer.parseInt(s[4] + (m) + "0" + s[0] + p);
        else if (Integer.parseInt(s[0]) >= 10 && m <10)
            id = Integer.parseInt(s[4] + "0" + (m) + s[0] + p);
        else
            id = Integer.parseInt(s[4] + (m) + s[0] + p);
        return id;
    }

    private boolean validarID(int id){
        if(ids == null) return true;
        else
            for(int i = 0;i<ids.size();i++) {
                if (ids.get(i).equals(id + ""))
                    return false;
            }
        return true;
    }

    public interface MyCallbackCifras {
        void  onCallback(int[] cifras);
    }

    private void getCifras(final MyAdapterPersona.MyCallbackCifras callbackCifras){
        db.collection(USUARIOS).document(user.getUid()).get()
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

