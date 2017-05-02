package com.eisti.android.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.List;

public class Dico {
    public List<String> dictionnary;
    public Dico(){
        dictionnary=new ArrayList<String>();
        FileHandle handle = Gdx.files.internal("mots.txt");
        String text = handle.readString();
        String wordsArray[] = text.split("\\r?\\n");
        for(String word : wordsArray) {
            dictionnary.add(word);
        }
    }
}
