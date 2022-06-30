package it.kfi.dynalendar.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.kfi.dynalendar.R;
import it.kfi.dynalendar.models.HourRange;
import it.kfi.dynalendar.models.Instructor;

public class RangeAdapter  extends BaseAdapter {
    private Instructor instructor;
    private LayoutInflater inflter;
    private List<HourRange> list = new ArrayList<>();
    private Context context;
    private Function<String,String> updateUi;
    public RangeAdapter(Context applicationContext, Instructor instructor,Function<String,String> updateUi){
        this.instructor = instructor;
        inflter = (LayoutInflater.from(applicationContext));
        this.context = applicationContext;
        this.list.addAll(instructor.getAvailabilities());
        this.list.addAll(instructor.getUnavailabilities());
        this.updateUi = updateUi;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return ((HourRange)getItem(i)).getId();
    }
    public String getGiornoDescription(String g) {
        switch(g)
        {
            case "LUN":return "Lunedì";
            case "MAR":return "Martedì";
            case "MER":return "Mercoledì";
            case "GIO":return "Giovedì";
            case "VEN":return "Venerdì";
        }
        return "";
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.range_item, null);
        HourRange item = ((HourRange)getItem(i));
        TextView description = view.findViewById(R.id.item_description);
        Button delete = (Button)view.findViewById(R.id.button_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                instructor.getAvailabilities().removeIf(c->c.getId()==item.getId());
                instructor.getUnavailabilities().removeIf(c->c.getId()==item.getId());
                updateUi.apply("");
            }
        });
        description.setText((item.getStato()==0 ? "Disponibile " : "Non disponibile ")+getGiornoDescription(item.getGiorno())+" dalle " +item.getFrom()+ " alle " + item.getTo());
        return view;
    }
}
