package junglespeedserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class JungleSpeedServer {
    public static void main(String[] args) {
        
        ServerSocket sockConn = null;
        Socket sockComm = null;
        int portServ = 4000;
        
        Game game;
        
        //Création du serveur

        try{
            sockConn = new ServerSocket(portServ);
        }
        catch(IOException e) {
            System.out.println("pb creation serveur : "+e.toString());
            System.exit(1) ;
        }
        
        game = new Game();
        
        try{
            while(true){
                System.out.println("Attente clients");
                sockComm = sockConn.accept();
                System.out.println("Nouveau client !");
                JungleServerThread t = new JungleServerThread(sockComm, game);
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
