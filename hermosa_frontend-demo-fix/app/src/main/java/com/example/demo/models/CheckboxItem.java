package com.example.demo.models;

public class CheckboxItem {
    private String label;
    private boolean checked;

    public CheckboxItem(String label, boolean checked) {
        this.label = label;
        this.checked = checked;
    }

    public String getLabel() { return label; }
    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
}

