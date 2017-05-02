package com.eisti.android.project;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class Cellule {
    public Color cellColor;
    public int size;
    public Vector2 root;
    public Vector2 letterRoot;
    public Vector2 scoreRoot;
    public String value;
    public int score;

    public Cellule(Color c, int s, Vector2 r, String v, int sc) {
        cellColor=c;
        size=s;
        root=r;
        score=sc;
        value=v;
        if (value.equals("I"))
            letterRoot=new Vector2(r.x+32,r.y+20);
        else if (value.equals("W"))
            letterRoot=new Vector2(r.x+10,r.y+20);
        else
            letterRoot=new Vector2(r.x+17,r.y+20);
        if (score==10)
            scoreRoot=new Vector2(r.x+size-18, r.y+size-15);
        else
            scoreRoot=new Vector2(r.x+size-10, r.y+size-15);
    }
}
