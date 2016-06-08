package dbk.com.swiperapp;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by hp on 25-03-2016.
 */
public class ParaGetter {

    Context mcontext;
    String para;
    ArrayList<String> wordList;
    int lineNumber;
    int totalLines;
    int currentIndex;
    Random random;
    AssetManager amgr;
    InputStream is;
    BufferedReader br;


    ParaGetter(Context C){
        mcontext = C;
        totalLines=0;
        currentIndex=0;
        para = "default para ";
        amgr = mcontext.getAssets();
        random = new Random();
        //to get total lines in file
        try {
            is = amgr.open("paragraphs.txt");
            br = new BufferedReader(new InputStreamReader(is));
            while(br.readLine()!=null)
                totalLines++;

        }catch (Exception e)
         {
         }

    }

    public  String getRandomPara()
    {
        lineNumber = random.nextInt(totalLines);
        try{
            is = amgr.open("paragraphs.txt");
            br = new BufferedReader(new InputStreamReader(is));
            for (int i = 0; i < lineNumber - 1; i++) {
               br.readLine();
           }
        para = br.readLine();
        }catch (Exception e)
        {

        }
        wordList = getWords(para);
        return para;
    }


    public String getPara(int N)
    {
        try{
            is = amgr.open("paragraphs.txt");
            br = new BufferedReader(new InputStreamReader(is));
            for (int i = 0; i < N - 1; i++) {
                br.readLine();
            }
            para = br.readLine();
        }catch (Exception e)
        {

        }
        wordList = getWords(para);
        return para;
    }
    public int getNoWords()
    {
        return wordList.size();
    }


    ArrayList<String> getWords(String para)
    {
        ArrayList<String> words = new ArrayList<>();
        for(String word : para.split(" "))
        {
            words.add(word);
        }
        return words;
    }





    public int getNextIndex(int wordNumber)
    {
        currentIndex+=wordList.get(wordNumber).length() +1;
        return currentIndex;
    }
    public int getCurrentIndex()
    {
        return currentIndex;
    }
    public int lookaheadIndex(int wordNumber)
    {
        return currentIndex + wordList.get(wordNumber).length() + 1;
    }
}
