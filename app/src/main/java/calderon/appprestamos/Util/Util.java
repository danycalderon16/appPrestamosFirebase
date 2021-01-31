package calderon.appprestamos.Util;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import calderon.appprestamos.activities.MainActivity;
import calderon.appprestamos.models.Totales;

public class Util {

    public static int dia;
    public static int mes;
    public static int year;
    public static String meses[] = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "novienbre", "diciembre"};
    public static String mesesCortos[] = {"ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sept", "oct", "nov", "dic"};
    public static Calendar c = Calendar.getInstance();
    public final static String SEMANAL = "Semanal";
    public final static String QUINCENAL = "Quincenal";
    public final static String USUARIOS = "usuarios";
    public final static String PRESTAMOS = "prestamos";
    public final static String COMPLETADOS = "completados";
    public final static String PERSONA = "Persona";
    public final static String ABONOS = "abonos";
    public final static String ID = "id";
    public final static String TIPO = "tipo";
    public final static String NOMBRE = "nombre";
    public final static String EMAIL = "email";
    public final static String FECHA = "fecha";
    public final static String FECHA_PRESTAMO = "fecha_prestamo";
    public final static String FECHA_FINAL = "fecha_final";
    public final static String CANTIDAD_PRESTADA = "cantidadPrestada";
    public final static String GANANCIA = "ganancia";
    public final static String MONTO = "monto";
    public final static String ABONO = "abono";
    public final static String ABONADO = "abonado";
    public final static String PLAZOS = "plazos";
    public final static String SALDO = "saldo";
    public final static String TOTAL = "total";
    public final static String TOTAL_GANAR  = "totalGanar";
    public final static String TOTAL_RECUPERAR  = "totalRecuperar";
    public final static String TOTAL_COMPLETADO  = "totalCompletado";

    public static void goMain(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
     //   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    public static void getDate(TextView textView) {
        dia = c.get(Calendar.DAY_OF_MONTH);
        mes = c.get(Calendar.MONTH);
        year = c.get(Calendar.YEAR);
        textView.setText(String.format(Locale.getDefault(), "%d de %s del %d", dia, meses[mes], year));
    }

    public static String getDate() {
        dia = c.get(Calendar.DAY_OF_MONTH);
        mes = c.get(Calendar.MONTH);
        year = c.get(Calendar.YEAR);
        return  (String.format(Locale.getDefault(), "%d de %s del %d", dia, meses[mes], year));
    }

    public static String getDate(String abreviada) {
        dia = c.get(Calendar.DAY_OF_MONTH);
        mes = c.get(Calendar.MONTH);
        year = c.get(Calendar.YEAR);
        return (String.format(Locale.getDefault(), "%d/%s/%d", dia, mesesCortos[mes], year));
    }

    public static void getDate(TextView textView, String abreviada) {
        dia = c.get(Calendar.DAY_OF_MONTH);
        mes = c.get(Calendar.MONTH);
        year = c.get(Calendar.YEAR);
        textView.setText(String.format(Locale.getDefault(), "%d/%s/%d", dia, mesesCortos[mes], year));
    }

    public static void setDate(Context context, final TextView textView) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int motnhOfYear, int dayOfMonth) {
                    textView.setText(String.format(Locale.getDefault(), "%d de %s del %d", dayOfMonth, meses[motnhOfYear], year));
                }
            }, year, mes, dia);
            datePickerDialog.show();
        }
    }
    public static void setDate(Context context, final TextView textView, String abreviada) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int motnhOfYear, int dayOfMonth) {
                    textView.setText(String.format(Locale.getDefault(), "%d/%s/%d", dayOfMonth, mesesCortos[motnhOfYear], year));
                }
            }, year, mes, dia);
            datePickerDialog.show();
        }
    }

    public static String dateShortToLong(String date){
        String words[] = date.split("/");
        String month = "";
        for (int i = 0; i < mesesCortos.length; i++) {
            if(mesesCortos[i].equals(words[1]))
                month = meses[i];
        }
        return words[0]+" de "+month+" de "+words[2];
    }

    public static Totales getTotalesFromSP(SharedPreferences preferences){
        return new Totales(preferences.getInt("total",0),
                preferences.getInt("totalRecuperar",0),
                preferences.getInt("totalGanar",0));
    }
    public static void updateTotalesInSP(SharedPreferences preferences,int total, int totalRecuperar, int totalGanar){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("total",total);
        editor.putInt("totalRecuperar", totalRecuperar);
        editor.putInt("totalGanar",totalGanar);
        Log.i("***************** ADD","Actualizado");
        editor.apply();
    }

    public static List<String> getIDFromSP(SharedPreferences preferences) {
        Gson gson = new Gson();
        String json = preferences.getString("IDs", null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> ids = gson.fromJson(json, type);

        if (ids == null)
            ids = new ArrayList<>();

        return ids;
    }

    public static void saveIDInSP(SharedPreferences preferences, List<String> ids) {
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(ids);
        editor.putString("IDs", json);
        editor.apply();
    }

    public static interface MyCallbackCifras {
        void  onCallback(int[] cifras);
    }

    public static void getCifras(final MyCallbackCifras callbackCifras, FirebaseFirestore db, FirebaseUser user){
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
