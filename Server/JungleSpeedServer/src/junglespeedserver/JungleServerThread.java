package junglespeedserver;

import SharedData.Request;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JungleServerThread extends Thread {
    
    // Socket et flux 
    private Socket sockComm = null;
    
    // Status des étapes pour le joueur connécté
    private boolean pseudoEnregistre;
    private boolean quitter;
    private boolean rejoindrePartie;
    
    // Objets partagés 
    Game game;
    
    Joueur joueur;
    Partie currentPartie;
    
    //Flux
    ObjectInputStream ois = null;
    ObjectOutputStream oos = null;
    DataInputStream dis = null;
    DataOutputStream dos = null;
    
    
    public JungleServerThread(Socket sockComm, Game game){
        this.sockComm = sockComm;
        this.game = game;
        joueur = null;
        currentPartie = null;
        
        // Mise à false des étapes
        this.pseudoEnregistre = false;
        this.quitter = false;
        this.rejoindrePartie = false;
    } 
   
    
    @Override
    public void run(){
        System.out.println(this.getName()+" run");
       
        
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
            initLoop();
            
            // Etape 2
            requestLoop();
            
        }
        catch(IOException e){
            debugReport("IOException");
            System.out.println( this.getName()+" IOException");
            System.out.println(e.toString());
            e.printStackTrace();
            
            //gestion déconnexion client
            if (joueur != null){
                debugReport("A été déco, suppression du joueur de la liste des joueurs");
                debugReport(game.listerJoueur());
                this.game.supprimerJoueur(joueur);
                debugReport(game.listerJoueur());
            }
                
            
            
        } catch (ClassNotFoundException ex) {
            debugReport("ClassNotFoundException");
            System.out.println(this.getName()+"Class not foud exception");
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            debugReport("InterruptedException");
            Logger.getLogger(JungleServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally{
            debugReport("Dans finally");
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
    
    public void initLoop() throws IOException{
        do{
                // Reception pseudo
                String pseudo = dis.readUTF();
                
                
                // Pseudo non present dans la liste des joueurs, on l'ajoute donc
                // à cette liste et envoie true au client
                Joueur j = game.creerJoueur(pseudo);
                if (j!=null){
                    this.joueur = j;
                    dos.writeBoolean(true);
                    dos.flush();
                    pseudoEnregistre=true;
                    System.out.println(this.getName()+" pseudo ajouter à la liste des joueurs connécté");
                }
                else{
                    // joueur déjà présent dans la liste des joueurs
                    // On envoie false au client
                    dos.writeBoolean(false);
                    dos.flush();
                    System.out.println(this.getName()+" pseudo déjà présent dans la liste.");
                }
            }while(!pseudoEnregistre);   
    } 

    public void requestLoop() throws IOException, ClassNotFoundException, InterruptedException{
        do{
             do{
                // Récéption requête
                System.out.println(this.getName()+" Attente reception requete pour client "+ joueur.pseudo);
                Request request = (Request)ois.readObject();
                switch(request.getType()){
                    case "CREATE":
                        debugReport("Requête reçu : CREATE");
                        requestCreatePartie(request);
                        break;
                        
                    case "LIST":
                        System.out.println(this.getName()+" Requête reçu : LIST");
                        requestListParties();
                        break;
                        
                    case "JOIN":
                        System.out.println(this.getName()+" Requête reçu : JOIN");
                        requestJoinPartie(request);
                        break;
                    default:
                        System.out.println(this.getName()+"Requête non comprise");
                        break;
                }
            }while(!quitter);
        }while(!quitter);
    }
    
    public void requestCreatePartie(Request req) throws IOException, InterruptedException {
        debugReport("Requête création de partie");
        int nbJoueurs = Integer.parseInt(req.getArgs());
        Partie p = game.creerPartie(joueur, nbJoueurs);
        if (p!=null){
            dos.writeBoolean(true);
            dos.flush();
            currentPartie = p;
            debugReport(joueur.pseudo+" à créé la partie "+p.id);
            partieLoop();
            clearPartieLoop();
        }
        else {
            dos.writeBoolean(false);
            dos.flush();
        }
    }
    
    public void requestJoinPartie(Request req) throws IOException, InterruptedException{
        debugReport("requête rejoindre partie");
        int idPartie = Integer.parseInt(req.getArgs());
        debugReport("requête rejoindre partie"+idPartie);
        Partie p = game.getPartie(idPartie);
        if (p.ajouterJoueur(joueur)){
            debugReport(joueur.pseudo+" à rejoint la partie "+idPartie);
            dos.writeBoolean(true);
            dos.flush();
            currentPartie = p;
            partieLoop();
            clearPartieLoop();
        }
        else{
            //n'as pas rejoint la partie
            dos.writeBoolean(false);
            dos.flush();
        }
    }
    
    public void requestListParties() throws IOException{
        debugReport("requête list partie");
        String resume = game.listerParties();
        dos.writeUTF(resume);
        dos.flush();
        debugReport("liste des parties envoyé");
    }
    
    public void partieLoop() throws IOException, InterruptedException{
        debugReport("entré dans la boucle de jeu");
        //envoie de l'id au sein de la partie à l'application cliente
        dos.writeInt(joueur.id);
        dos.flush();
        debugReport("j'attends début de partie");
        currentPartie.AttendreDebutPatie();
        debugReport("le nombre de joueur est atteint la partie commence");
        dos.writeBoolean(true);
        dos.flush();
        
        boolean stop = false;
        
        while(!stop){
            
            try {
                //on regarde si la partie est terminée
                
                if (currentPartie.getCurrentState() == Partie.STATE_ENDWIN ||
                        currentPartie.getCurrentState() == Partie.STATE_ENDBROKEN){
                    stop = true;
                    informerJoueurFinDePartie();
                    return;
                }
                
                
                //on récupére le joueur courant
                Joueur currentJoueurTour = currentPartie.currentJoueur;
                
                //si ce thread est le joueur courant, on révéle une carte puis on
                //attends
                if (joueur == currentJoueurTour){
                    sleep(2000);
                    currentPartie.revealCard();
                }
                
                //on attends que la carte du tours soit tirée et que tous les
                //joueurs attendent
                currentPartie.AttendreRevelationCarte();
                
                //On récupére la liste des dernières cartes révélé par les joueurs
                String cartesRevele = currentPartie.getCartesRevele();
                debugReport(cartesRevele);
                dos.writeUTF(cartesRevele);
                dos.flush();
                
                if (currentPartie.getCurrentState() == Partie.STATE_ENDWIN ||
                        currentPartie.getCurrentState() == Partie.STATE_ENDBROKEN){
                    stop = true;
                    informerJoueurFinDePartie();
                    return;
                }
                
                String clientOrder = dis.readUTF();
                debugReport(clientOrder);
                currentPartie.IntegrationOrdreJoueur(joueur, clientOrder);
                currentPartie.AttendteActionJoueur();
                //analyseResults
                debugReport(currentPartie.getResultatPartie());
                dos.writeUTF(currentPartie.getResultatPartie());
                dos.flush();
                debugReport("Fin tour");
            }
            catch(IOException e){
                currentPartie.setState(Partie.STATE_ENDBROKEN);
                currentPartie.joueurQuittePartie(joueur);
                throw e;
            }   
        }
    }
    
    public void clearPartieLoop(){
        joueur.clearPartie();
        currentPartie = null;
        
    }
    
    public void informerJoueurFinDePartie() throws IOException{
        dos.writeUTF("END");
        dos.flush();
        
        if(currentPartie.getCurrentState() == Partie.STATE_ENDWIN){
            dos.writeUTF("WIN");
            dos.flush();
            dos.writeUTF(currentPartie.getGagnant().pseudo);
            dos.flush();
        }
        else{
            dos.writeUTF("LEAVER");
            dos.flush();
        }
    }
    
    private void debugReport(String msg) {
        if (joueur.pseudo != null){
            System.err.println("Thread ["+this.getName()+"-"+joueur.pseudo+"] - "+msg);
        }
        else{
            System.err.println("Thread ["+this.getName()+"] - "+msg);
        }
    }    
}
