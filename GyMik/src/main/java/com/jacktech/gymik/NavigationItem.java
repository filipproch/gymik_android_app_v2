package com.jacktech.gymik;

public class NavigationItem {

	public int drawable = 0;
	public String text = "prázdný";
    public String header = "Header";
	public boolean delimiter = false;

	public NavigationItem(int drawable, String text, String header){
		this.drawable = drawable;
		this.text = text;
        this.header = header;
        if(header != null)
            delimiter = true;
    }
	
}
