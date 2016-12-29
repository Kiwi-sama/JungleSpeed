package junglespeedserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JungleServerThread extends Thread {
    
    // Socket et flux 
    private Socket sockComm = null;
    
    // Status des étapes pour le joueur connécté
    private boolean pseudoEnregistre;
    private boolean temp = false;
    
    // Objets partagés 
    ArrayList<String> joueurs;
    ArrayList<Partie> parties;
    
    String pseudo;
    
    public JungleServerThread(Socket sockComm, ArrayList<String> joueurs, 
            ArrayList<Partie> parties){
        this.sockComm = sockComm;
        this.joueurs = joueurs;
        this.parties = parties;
        pseudo = "";
        // Mise à false des étapes
        this.pseudoEnregistre = false;
    }
    
    @Override
    public void run(){
        System.out.println(this.getName()+" run");
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        
        try{
            System.out.println(this.getName()+" debut création flux");
            ois = new ObjectInputStream(new BufferedInputStream(sockComm.getInputStream()));
            oos = new ObjectOutputStream(new BufferedOutputStream(sockComm.getOutputStream()));
            oos.flush();
            dis = new DataInputStream(new BufferedInputStream(sockComm.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(sockComm.getOutputStream()));
            dos.flush();
            System.out.println(this.getName()+" flux ok");
            System.out.println(this.getName()+" debut étape 1");
            // Etape 1
            
            do{
                // Reception pseudo
                pseudo = dis.readUTF();
                System.out.println(this.getName()+" pseudo reçu "+pseudo);
                // Check si pseudo est present
                if (!joueurs.contains(pseudo)){
                    // Pseudo non present dans la liste des joueurs, on l'ajoute donc
                    // à cette liste et envoie true au client
                    joueurs.add(pseudo);
                    dos.writeBoolean(true);
                    dos.flush();
                    pseudoEnregistre=true;
                    System.out.println(this.getName()+" pseudo ajouter à la liste des joueurs prêt pour passage étape 2");
                }
                else{
                    // Pseudo déjà présent dans la liste des joueurs
                    // On envoie false au client   
                    dos.writeBoolean(false);
                    dos.flush();
                    System.out.println(this.getName()+" pseudo déjà présent dans la liste.");
                }
                System.out.println("Pseudo présent :");
                for(String s : joueurs){
                    System.out.println(s);
                }
            }while(!pseudoEnregistre);   
            
            // Etape 2
            //System.out.println(this.getName()+" début étape 2");
            
            do{

            }while(!temp);
            
        }
        catch(IOException e){
            System.out.println(e.toString());
            e.printStackTrace();
        }
        finally{
            System.out.println(this.getName()+"Finally");
            try{
                if (ois != null)
                    ois.close();
                if (oos != null){
                    oos.flush();
                    oos.close();
                }
                if (dis != null)
                    dis.close();
                if (dos != null){
                    dos.flush();
                    dos.close();
                }
            }
            catch(IOException e){
                System.out.println("Closing Error");
            }
        }
    }
    
}
