package com.jacktech.gymik.bakalari;

import java.math.BigDecimal;
import java.util.List;

public class Predmet {

    public static final String PREDMET = "predmet";
    private String name;
	private double prumer;
	
	public Predmet(String name, double prumer){
		this.name = name;
        this.prumer = prumer;
	}

    public void setPrumer(double prumer){
        this.prumer = prumer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrumer() {
        return prumer;
    }

    public static Predmet create(String predmet, List<Znamka> znamky) {
        int soucetHodnot = 0;
        int soucetVah = 0;
        for(Znamka z : znamky){
            soucetHodnot += z.getVaha()*z.getHodnota();
            soucetVah += z.getVaha();
        }
        double prumer = -1;
        if(soucetVah != 0){
            prumer = (double)soucetHodnot/soucetVah;
            BigDecimal a = new BigDecimal(prumer);
            prumer = a.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
        }
        return new Predmet(predmet, prumer);
    }
}
