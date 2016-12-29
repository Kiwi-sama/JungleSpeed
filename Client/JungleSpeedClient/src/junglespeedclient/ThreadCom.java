package junglespeedclient;

// classe qui gère les échanges de messages avec l'application serveur.

import SharedData.Request;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ThreadCom implements Runnable{
    private JungleIG ig;
    private Synchro sync;
    private Socket sockComm = null;
    String pseudo = "";
    
    private ObjectOutputStream oos = null;
    private ObjectInputStream ois = null;
    
    private DataOutputStream dos = null;
    private DataInputStream dis = null;
    
    private boolean serverResponse = false;
    
    public  ThreadCom(Synchro s, JungleIG i){
        ig = i;
        sync = s;
    }
    
    
    public void run(){
        // Dans cette méthode run(), toutes les modifications de l'interface graphique
        // doivent être réalisées via la méthode invokeLater() de la classe SwingUtilities
        
        /*javax.swing.SwingUtilities.invokeLater( new Runnable() {
        public void run() {
        
        ig.setInitPanel();
        }
        });*/
        
        String ipServ = "";
        int portServ = 4000;
        
        // Création de la socket de communication avec le serveur
        
        try {
            // Partie 1
            do{
                sync.attendreDemandeConnexion();
                if(sockComm == null){
                    try{
                        sockComm = new Socket(ipServ, portServ);
                        System.out.println("Connexion OKAY");
                        // Création des flux (output en 1er)
                        oos = new ObjectOutputStream(new BufferedOutputStream(sockComm.getOutputStream()));
                        oos.flush();
                        ois = new ObjectInputStream(new BufferedInputStream(sockComm.getInputStream()));
                        dos = new DataOutputStream(new BufferedOutputStream(sockComm.getOutputStream()));
                        dos.flush();
                        dis = new DataInputStream(new BufferedInputStream(sockComm.getInputStream()));
                        System.out.println("Fulx ok");
                    }
                    catch(IOException e){
                        System.out.println("Connection erreur "+e.toString());
                        e.printStackTrace();
                        System.exit(0);
                    }
                }
                
                pseudo = ig.getPseudoConnexion();
                System.out.println("pseudo récup : "+pseudo);
                if(!pseudo.isEmpty()){
                    System.out.println("pseudo non vide");
                    System.out.println("envoie pseudo au serveur");
                    dos.writeUTF(pseudo);
                    dos.flush();
                    serverResponse = dis.readBoolean();
                    if(serverResponse == true){
                        sync.setConnecte();
                        System.out.println("pseudo accépté");
                    }
                    else{
                        System.out.println("Erreur de connexion");
                        javax.swing.SwingUtilities.invokeLater(new Runnable(){
                            public void run(){
                                ig.labelInfoPb.setText("Erreur de connexion");
                                ig.labelInfoPb.setVisible(true);
                                ig.pack();
                            }
                        });
                    }
                }
                else{
                    System.out.println("pseudo vide");
                    javax.swing.SwingUtilities.invokeLater(new Runnable(){
                        public void run(){
                            ig.labelInfoPb.setText("Le pseudo ne peut être vide...");
                            ig.labelInfoPb.setVisible(true);
                            ig.pack();
                        }
                    });
                }
                System.out.println(sync.getEstConnecte());
            }while(!sync.getEstConnecte());
            
            // ETAPE 2 
            
            /**
             * On est enregistré, on set l'affichage sur le panel Init
             */
            javax.swing.SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    ig.labelNomJ1.setText("Joueur : "+ pseudo);
                    ig.labelNomJ2.setText("Joueur : "+ pseudo);
                    ig.setInitPanel();
                    ig.pack();
                }
            });
            
            System.out.println("Attente demande action utilisateur");
            
            do{
                sync.attendreDemandeAction();
                System.out.println("action demandée "+sync.getNomAction());
                switch (sync.getNomAction()) {
                    case "CREATE":
                        System.out.println("Création d'une partie");
                        int nbJoueursCreationPartie = ig.getNombreJoueursCreationPartie();
                        Request request = new Request("CREATE", 
                                String.valueOf(nbJoueursCreationPartie));
                        //send request
                        break;
                    case "LIST":
                        System.out.println("Récupération de la liste des parties");
                        break;
                    case "JOIN":
                        System.out.println("Rejoindre une partie");
                        break;
                    default:
                        System.out.println("Action demandée imcomprise");
                        sync.setNomAction("");
                        sync.setDemandeActionFaux();
                        break;
                }
            }while(!sync.getWantToQuit());
            
        }
        catch(IOException e){
            System.out.println("Erreur de communication : "+e.toString());
            e.printStackTrace();
            System.exit(0);
        }
        finally{
            try{
                if(oos!=null){
                    oos.flush();
                    oos.close();
                }
                if(ois!=null){
                    ois.close();
                }
                if(dos!=null){
                    dos.flush();
                    dos.close();
                }
                if(dis!=null){
                    dis.close();
                }
                if(sockComm!=null){
                    sockComm.close();
                    sockComm=null;
                }
            }
            catch(IOException e){
                System.out.println("Closing error");
                e.printStackTrace();
            }
        }
        
    }
}
