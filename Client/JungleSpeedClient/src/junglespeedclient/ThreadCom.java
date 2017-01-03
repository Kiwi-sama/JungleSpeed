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
    
    private String pseudo;
    private int idDansPartie;
    private String etatPartie;
    private String resultaltTour;
    
    private ObjectOutputStream oos = null;
    private ObjectInputStream ois = null;
    
    private DataOutputStream dos = null;
    private DataInputStream dis = null;
    
    private boolean serverResponse = false;
    
    public  ThreadCom(Synchro s, JungleIG i){
        ig = i;
        sync = s;
        pseudo = "";
        idDansPartie = -1;
    }
    
    
    public void run(){
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
                        System.out.println("Flux ok");
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
                Request request = null;
                System.out.println("action demandée "+sync.getNomAction());
                switch (sync.getNomAction()) {
                    case "CREATE":
                        // Création et envoie de la requête/ 
                        System.out.println("Création d'une partie");
                        System.out.println("Création de la requête");
                        int nbJoueursCreationPartie = ig.getNombreJoueursCreationPartie();
                        request = new Request("CREATE", 
                                String.valueOf(nbJoueursCreationPartie));
                        oos.writeObject(request);
                        oos.flush();
                        System.out.println("Requête envoyée");    
                        boolean partieCree = dis.readBoolean();
                        // Retour du serveur, traitement 
                        // Faire rejoindre à ce joueur la partie crée
                        if (partieCree){
                            System.out.println("partie crée");
                            partieLoop();
                        }
                        else{
                            System.out.println("partie non crée");
                        }
                        break;
                        
                    case "LIST":
                        System.out.println("Récupération de la liste des parties");
                        System.out.println("Création de la requête");
                        request = new Request("LIST", "");
                        oos.writeObject(request);
                        oos.flush();
                        System.out.println("Requête envoyée");    
                        String infoParties = dis.readUTF();
                        javax.swing.SwingUtilities.invokeLater(new Runnable(){
                            public void run(){
                                ig.textInfoInit.setText("");
                                ig.textInfoInit.setText(infoParties);
                                ig.pack();
                            }
                        });
                        System.out.println("Info parties affichés");
                        break;
                        
                    case "JOIN":
                        System.out.println("Rejoindre une partie");
                        String idPartieaRejoindre = ig.getIdPartieARejoindre();
                        request = new Request("JOIN", idPartieaRejoindre);
                        oos.writeObject(request);
                        oos.flush();
                        System.out.println("requête envoyée");
                        boolean estDansPartie = dis.readBoolean();
                        if (estDansPartie){
                            partieLoop();
                        }
                        else{
                            javax.swing.SwingUtilities.invokeLater(new Runnable(){
                                public void run(){
                                    ig.textInfoInit.setText("");
                                    ig.textInfoInit.setText("Erreur, ne peut pas rejoindre la partie n°"+idPartieaRejoindre);
                                    ig.pack();
                                }
                            });
                            System.out.println("Erreur, ne peut pas rejoindre la partie n°"+idPartieaRejoindre);
                        }
                        break;
                    
                    default:
                        System.out.println("Action demandée imcomprise");
                        break;
                }
                sync.setDemandeActionFaux();
                sync.setNomAction("");
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
    
    public void partieLoop() throws IOException{
        //recuperation de l'id du joueur au sein de la partie.
        idDansPartie = dis.readInt();
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                ig.setPartyPanel();
                ig.labelNomJ2.setText(pseudo+" joueur n°"+idDansPartie);
                ig.textInfoParty.setText("Attente début partie");
            }
        });
        System.out.println("Attente début de partie");
        dis.readBoolean();
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                ig.textInfoParty.setText("La partie commence");
            }
        });
        boolean finDePartie = false;
        while (!finDePartie){
            
            //etape 1-1 attente etat de la partie.
            etatPartie = dis.readUTF();            
            //Etape 1-2 affichage etat de la partie
            javax.swing.SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    ig.textInfoParty.setText("");
                    ig.textInfoParty.setText(etatPartie);
                    ig.butHandT.setEnabled(true);
                    ig.butTakeT.setEnabled(true);
                }
            });
            
            //Etape 2 
            long timestamp = System.currentTimeMillis();
            sync.signalerDemandeOrdreJoueur("N");
            while (System.currentTimeMillis()<=timestamp+3000){
                String ordre = sync.getOrdre();
                if (!ordre.equals("N")){
                    //Etape 3
                    javax.swing.SwingUtilities.invokeLater(new Runnable(){
                        public void run(){
                            ig.butHandT.setEnabled(false);
                            ig.butTakeT.setEnabled(false);
                        }
                    });
                    dos.writeUTF(ordre);
                    dos.flush();
                    break;
                }
            }
            //Etape 4
            String ordre = sync.getOrdre();
            if (ordre.equals("N")){
                //Etape 3
                javax.swing.SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        ig.butHandT.setEnabled(false);
                        ig.butTakeT.setEnabled(false);
                    }
                });
                dos.writeUTF(ordre);
                dos.flush();
            }
            
            
            sync.attendreReceptionResultatTour();
            resultaltTour = "";
            while (resultaltTour.equals("")){
                resultaltTour = dis.readUTF();
                if(!resultaltTour.equals(""))
                    sync.signalerReceptionEtatPartie();
            }
            
        } 
    }
    
}
