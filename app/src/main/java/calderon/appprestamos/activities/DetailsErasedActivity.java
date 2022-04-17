package calderon.appprestamos.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import calderon.appprestamos.R;
import calderon.appprestamos.databinding.ActivityDetailsErasedBinding;

public class DetailsErasedActivity extends AppCompatActivity {

    private ActivityDetailsErasedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_erased);
    }
}