package com.eisti.android.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Grille {
    int sizeX;
    int sizeY;
    int cellSize;
    int offset;
    Cellule[][] grille;
    Vector2 origin;
    private String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private String[] score={"1","3","3","2","1","4","2","4","1","8","10","1","2","1","1","3","8","1","1","1","1","4","10","10","10","10"};
    private int[] distribution = {9,11,13,16,31,33,35,37,45,46,47,52,55,61,67,69,70,76,82,88,94,96,97,98,99,100};

    public List<String> found;
    public List<Cellule> touched;
    public Color gray=new Color(0.6f,0.6f,0.6f,1);
    public Color blue=new Color(0.2f,0.4f,0.6f,1);
    public Color red=new Color(0.8f,0.1f,0.1f,1);
    public Color green=new Color(0.1f,0.8f,0.1f,1);
    public int gridScore;
    public boolean isNull;


    public Grille(int x, int y, int c, Vector2 o, int off) {
        isNull=false;
        sizeX=x;
        sizeY=y;
        cellSize=c;
        origin=o;
        offset=off;
        gridScore=0;
        grille=new Cellule[x][y];
        touched=new ArrayList<Cellule>();
        found=new ArrayList<String>();
        String[][] game=GenerateGrid();
        for (int i=0; i<sizeX;i++) {
            for (int j=0; j<sizeY; j++) {
                try {
                    grille[i][j]=new Cellule(gray, cellSize, new Vector2(origin.x+i*(cellSize+offset), origin.y+j*(cellSize+offset)),game[i+sizeX*j][0],Integer.parseInt(game[i+sizeX*j][1]));
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }

            }
        }
    }

    String[][] GenerateGrid() {
        Random r = new Random();
        int roll,index, previous;
        String[][] ret=new String[sizeX*sizeY][2];
        roll=0;
        for(int i=0; i<sizeX*sizeY; i++) {
            if (roll==70)
                roll=94;
            else
                roll=r.nextInt(100)+1;
            index=0;
            previous=0;
            while(index<alphabet.length) {
                if (roll>previous && roll<=distribution[index])
                    break;
                else {
                    previous=distribution[index];
                    index++;
                }
            }
            ret[i][0]=alphabet[index];
            ret[i][1]=score[index];
        }
        return ret;
    }

    public Vector2 IsInside(double x, double y) {
        Vector2 ret = new Vector2(-1,-1);
        insideLoop:
        for (int i=0; i<sizeX; i++) {
            for (int j=0; j<sizeY; j++) {
                if (x>=grille[i][j].root.x && x<=grille[i][j].root.x+cellSize && y>=grille[i][j].root.y && y<=grille[i][j].root.y+cellSize) {
                    ret.x=i;
                    ret.y=j;
                    break insideLoop;
                }
            }
        }
        if (ret.x>=0) return ret;
        else return null;
    }

    public void addTouched(Vector2 v){
        if (touched.size()<10 && !touched.contains(grille[(int)v.x][(int)v.y])) {
            touched.add(grille[(int)v.x][(int)v.y]);
            grille[(int)v.x][(int)v.y].cellColor=blue;
        }
    }

    public int ComputeScore() {
        int ret=0;
        for (Cellule c : touched) {
            ret+=c.score;
        }
        return ret;
    }
}
