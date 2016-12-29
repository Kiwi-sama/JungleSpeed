package junglespeedserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class JungleSpeedServer {
    public static void main(String[] args) {
        
        ServerSocket sockConn = null;
        Socket sockComm = null;
        int portServ = 4000;
        
        ArrayList<String> joueurs = new ArrayList<String>();
        ArrayList<Partie> parties = new ArrayList<Partie>();
        
        //Création du serveur
        try{
            sockConn = new ServerSocket(portServ);
        }
        catch(IOException e) {
            System.out.println("pb creation serveur : "+e.toString());
            System.exit(1) ;
        }
        
        try{
            while(true){
                System.out.println("Attente clients");
                System.out.println("Total psuedo : "+joueurs.size());
                sockComm = sockConn.accept();
                System.out.println("Nouveau client !");
                JungleServerThread t = new JungleServerThread(sockComm, joueurs, parties);
                t.start();   
            }
        }
        catch(IOException e){
            System.out.println("pb connexion client : "+e.toString());
            e.printStackTrace();
        }
        finally{
            try{
                if (sockConn !=  null)
                    sockConn.close();
            }
            catch(IOException e) {
                System. out.println(e.toString());
                e.printStackTrace();
            }
        }
        
    }
}
