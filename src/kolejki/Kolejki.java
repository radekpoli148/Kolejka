package kolejki;

import java.util.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Kolejki 
{
    public static void main(String[] args) 
    {
        BlockingQueue<File> kolejkaBlokujaca = new ArrayBlockingQueue<File>(100);
        
        new Thread(new PoszukiwaczScierzek(kolejkaBlokujaca, scierzkaGlowna)).start();
        
        for(int i = 0; i < 50; i++)
            new Thread(new PrzeszukiwaczPlikow(kolejkaBlokujaca, SlowoSzukane)).start();
    }
    final static private File scierzkaGlowna = new File(System.getProperty("user.dir"));
    
    final static private String SlowoSzukane = new String("text");
}
class PoszukiwaczScierzek implements Runnable
{
    BlockingQueue<File> kolejka;
    File sciezkaGlowna;
    public PoszukiwaczScierzek(BlockingQueue<File> kolejka, File sciezkaGlowna) 
    {
        this.kolejka = kolejka;
        this.sciezkaGlowna = sciezkaGlowna;
    }
    
    @Override
    public void run() 
    {
        try 
        {
            szukajSciezek(sciezkaGlowna);
            kolejka.put(new File("pusty"));
        } 
        catch (InterruptedException ex) 
        {
            ex.printStackTrace();
        }
        
    }
    
    public void szukajSciezek(File sciezka) throws InterruptedException
    {
        File[] sciezki = sciezka.listFiles();
        
        for(int i = 0; i < sciezki.length; i++)
            if (sciezki[i].isDirectory())
                szukajSciezek(sciezki[i]);
            else
                kolejka.put(sciezki[i]);
    }
}
class PrzeszukiwaczPlikow implements Runnable
{
    BlockingQueue<File> kolejka;
    String szukaneSlowo;
    public PrzeszukiwaczPlikow(BlockingQueue<File> kolejka, String szukaneSlowo) 
    {
        this.kolejka = kolejka;
        this.szukaneSlowo = szukaneSlowo;
    }

    @Override
    public void run() 
    {
        boolean skonczone = false;
        while(!skonczone)
        {      
            try 
            {
                File przeszukiwanyPlik = kolejka.take();
                if(przeszukiwanyPlik.equals(new File("pusty")))
                {
                    kolejka.put(przeszukiwanyPlik);
                    skonczone = true;
                }
                else

                    szukajSlowa(przeszukiwanyPlik);
            } 
            catch (Exception ex) 
            {
                ex.printStackTrace();
            }
        }
    }
    public void szukajSlowa(File przeszukiwanyPlik) throws FileNotFoundException
    {
        Scanner reader = new Scanner(new BufferedReader(new FileReader(przeszukiwanyPlik)));
        
        int nrLini = 0;
        
        while(reader.hasNextLine())
        {
            nrLini++;
            
            if(reader.nextLine().contains(szukaneSlowo))
                System.out.println("szukane słowo znajduje się w pliku: "+ przeszukiwanyPlik.getPath()+" w lini: "+nrLini);
            
        }
        
        
        reader.close();
    }
}