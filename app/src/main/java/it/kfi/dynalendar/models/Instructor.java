package it.kfi.dynalendar.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Instructor implements Serializable {
    String name;
    List<HourRange> availabilities;
    List<HourRange> unavailabilities;

    public Instructor(){
        this.availabilities = new ArrayList<>();
        this.unavailabilities = new ArrayList<>();
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<HourRange> getAvailabilities() {
        return availabilities;
    }

    public List<HourRange> getUnavailabilities() {
        return unavailabilities;
    }
    public Integer getMaxId(){
        return getAvailabilities().size() + getUnavailabilities().size();
    }

    public boolean isAvailable(String giornostring, int ora) {
        List<HourRange> giornata = availabilities.stream().filter(c->c.getGiorno().equals(giornostring)).collect(Collectors.toList());
        for(HourRange range : giornata){
            if (range.getFrom()<=ora && range.getTo()>ora)
                return true;
        }
        return  false;
    }
    public boolean isUnavailable(String giornostring, int ora) {
        List<HourRange> giornata = unavailabilities.stream().filter(c->c.getGiorno().equals(giornostring)).collect(Collectors.toList());
        for(HourRange range : giornata){
            if (range.getFrom()<=ora && range.getTo()>ora)
                return true;
        }
        return  false;
    }
}
