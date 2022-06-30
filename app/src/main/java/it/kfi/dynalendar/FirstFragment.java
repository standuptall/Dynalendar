package it.kfi.dynalendar;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.kfi.dynalendar.databinding.FragmentFirstBinding;
import it.kfi.dynalendar.models.HourRange;
import it.kfi.dynalendar.models.Instructor;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private List<Instructor> instructorList;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        instructorList = new ArrayList<>();

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        SharedPreferences mPrefs = getActivity().getPreferences(MODE_PRIVATE);
        Map<String,?> all = mPrefs.getAll();
        for(String js : all.keySet()){
            String json = mPrefs.getString(js, "");
            if (!json.isEmpty())
            {
                Gson gson = new Gson();
                Instructor instructor = gson.fromJson(json,Instructor.class);
                instructorList.add(instructor);
            }
        }
        compileTable();

        return binding.getRoot();

    }
    private int contaIndisponibilità(Instructor inst){
        int ret = 0;
        for(HourRange r : inst.getUnavailabilities()){
            ret += r.getTo() - r.getFrom();
        }
        return ret;
    }
    private Instructor trovaIlPiuDisponibile(){
        Instructor ret = instructorList.get(0);
        for(Instructor inst  : instructorList){
            int rt = contaIndisponibilità((inst));
            if (rt < contaIndisponibilità(ret))
                ret = inst;
        }
        return ret;
    }
    public void compileTable(){
        if (instructorList.size()==0)
            return;
        TableLayout table = (TableLayout)binding.table;
        int oresettimanalitotali = (45);
        int parcel = oresettimanalitotali /  instructorList.size();  //floor
        Map<String,Integer> quotetotale = new HashMap<>();
        for(Instructor i : instructorList)
            quotetotale.put(i.getName(),new Integer(parcel));
        if (parcel * instructorList.size() < oresettimanalitotali){
            //trovo a chi mettere un'ora , colui che ha dichiarato meno indisponibilità
            Instructor meno = trovaIlPiuDisponibile();
            quotetotale.put(meno.getName(),parcel+1);
        }
        List<HourRange> ripartizioni = new ArrayList<>();
        for(int i=0;i<oresettimanalitotali;i++){
            HourRange range = new HourRange();
            int giornosett = i / 9; //floor , 0 = LUN
            int ora = (i - (giornosett*9)) + 9;
            range.setFrom(ora);
            range.setTo(ora+1);
            String giornostring ="";
            switch (giornosett){
                case 0:giornostring = "LUN"; break;
                case 1:giornostring = "MAR"; break;
                case 2:giornostring = "MER"; break;
                case 3:giornostring = "GIO"; break;
                case 4:giornostring = "VEN"; break;
            }
            range.setGiorno(giornostring);
            //trovo disponibili e indisponibili
            List<Instructor> disponibili = new ArrayList<>();
            for(Instructor instructor : instructorList){
                if (instructor.isAvailable(giornostring,ora))
                {
                    disponibili.add(instructor);
                }
            }
            //tra i disponibili, vedo il primo a cui scalare l'ora
            for(Instructor instruct: disponibili){
                int quantoRimane = quotetotale.get(instruct.getName());
                if (quantoRimane>0){
                    range.setOwner(instruct.getName());
                    quotetotale.put(instruct.getName(),quantoRimane - 1);
                    break;
                }
            }
            if (range.getOwner() == null){ //non ho scalato a nessuno....prendo il primo, verifico che non abbia indisponibilità , e scalo l'ora
                for(int ii=0;ii<instructorList.size();ii++){
                    Instructor instructor = instructorList.get(ii);
                    if (instructor.isUnavailable(giornostring,ora))
                        continue;
                    int quantoRimane = quotetotale.get(instructor.getName());
                    if (quantoRimane>0){
                        range.setOwner(instructor.getName());
                        quotetotale.put(instructor.getName(),quantoRimane - 1);
                        break;
                    }
                }
            }
            ripartizioni.add(range);
        }
        for(HourRange range : ripartizioni){
            String giorno = range.getGiorno();
            int intgiorno  = 0;
            switch (giorno){
                case "LUN" : intgiorno = 1;break;
                case "MAR" : intgiorno = 2;break;
                case "MER" : intgiorno = 3;break;
                case "GIO" : intgiorno = 4;break;
                case "VEN" : intgiorno = 5;break;
            }
            int i = range.getFrom() - 8;
            int len = range.getTo() - range.getFrom() + 1;
            for (int indice = 0;indice<len;indice++)
            {
                TableRow row = (TableRow) table.getChildAt(i+indice);
                ((TextView)row.getChildAt(intgiorno)).setText(range.getOwner());
                ((TextView)row.getChildAt(intgiorno)).setBackgroundColor(Color.GREEN);
                ((TextView)row.getChildAt(intgiorno)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Instructor instructo = findInstructorByName(range.getOwner());
                        if (instructo!=null)
                        ((MainActivity)getActivity()).selectInstructor(view,instructo);
                    }
                });
            }
        }
    }
    private Instructor findInstructorByName(String instructorName){
        for(Instructor instructor : instructorList){
            if (instructor.getName().equals(instructorName))
                return instructor;
        }return null;
    }
    public TextView createTextView(int color,String name){
        TextView text = new TextView(getContext());
        text.setBackgroundColor(color);
        text.setText(name);
        text.setTextSize(10);
        text.setId(View.generateViewId());
        return text;
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
//            }
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}