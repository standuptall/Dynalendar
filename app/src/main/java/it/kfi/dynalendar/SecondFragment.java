package it.kfi.dynalendar;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.kfi.dynalendar.adapters.RangeAdapter;
import it.kfi.dynalendar.databinding.FragmentSecondBinding;
import it.kfi.dynalendar.models.HourRange;
import it.kfi.dynalendar.models.Instructor;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private Instructor instructor;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        Bundle bundle = this.getArguments();
        instructor = (Instructor)bundle.getSerializable("instructor");

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        if (instructor == null || (instructor.getAvailabilities().size()==0 && instructor.getUnavailabilities().size()==0))
            instructor = new Instructor();
        else
        {

            binding.edittextSecond.setEnabled(false);
        }
        Spinner dropdown = binding.giornoSettimana;
        String[] items = new String[]{"LUN","MAR","MER","GIO","VEN"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        binding.edittextSecond.setText(instructor.getName());
        updateUI();
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //aggiungi disponibilità
                Integer from = Integer.parseInt(binding.fragmentSecondFrom.getText().toString());
                Integer to = Integer.parseInt(binding.fragmentSecondTo.getText().toString());
                if(!checkInsertion(binding.giornoSettimana.getSelectedItem().toString(),from,to))
                    return;
                HourRange range = new HourRange();
                range.setFrom(from);
                range.setTo(to);
                range.setGiorno(binding.giornoSettimana.getSelectedItem().toString());
                range.setId(instructor.getMaxId()+1);
                range.setStato(0);
                instructor.getAvailabilities().add(range);
                updateUI();
            }
        });
        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HourRange range = new HourRange();
                Integer from = Integer.parseInt(binding.fragmentSecondFrom.getText().toString());
                Integer to = Integer.parseInt(binding.fragmentSecondTo.getText().toString());
                if(!checkInsertion(binding.giornoSettimana.getSelectedItem().toString(),from,to))
                    return;
                range.setFrom(from);
                range.setTo(to);
                range.setId(instructor.getMaxId()+1);
                instructor.getUnavailabilities().add(range);
                range.setGiorno(binding.giornoSettimana.getSelectedItem().toString());
                range.setStato(1);
                updateUI();
            }
        });
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences  mPrefs = getActivity().getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                TextView text = getActivity().findViewById(R.id.textview_second);
                instructor.setName(binding.edittextSecond.getText().toString());
                Gson gson = new Gson();
                String json = gson.toJson(instructor);
                prefsEditor.putString(instructor.getName(), json);
                prefsEditor.commit();
            }
        });
    }
    private boolean checkInsertion(String giorno,Integer from,Integer to){
        if(to<=from){
            Snackbar.make(getActivity().getCurrentFocus(), "L'ora di fine non può essere superiore o uguale a quella di inizio!", Snackbar.LENGTH_SHORT)
                    .show();
            return false;
        }
        List<HourRange> trova = instructor.getAvailabilities().stream().filter(c->c.getGiorno().equals(giorno)).collect(Collectors.toList());
        trova.addAll(instructor.getUnavailabilities().stream().filter(c->c.getGiorno().equals(giorno)).collect(Collectors.toList()));
        for(HourRange range  : trova){
            if ((from >= range.getFrom() && from< range.getTo())
                || (to >= range.getFrom() && to< range.getTo())){
                Snackbar.make(this.getView(), "L'intervallo scelto si sovrappone!", Snackbar.LENGTH_SHORT)
                        .show();
                return false;
            }
        }
        return true;
    }
    private String updateUI(){
        ListView l = binding.list;
        RangeAdapter arr;
        arr
                = new RangeAdapter(
                getContext(),
                instructor, new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return updateUI();
                    }
                });
        l.setAdapter(arr);
        return "";
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}