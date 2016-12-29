package junglespeedclient;
// instance de Synchro : objet partagé entre les instances de ThreadCom et JungleIG
// sert à la synchronisation des threads EDT (Event Dispatching Thread) et ThreadCom

public class Synchro
{
    private boolean demandeConnexion;
    private boolean estConnecte;
    
    private boolean demandeAction;
    private String nomAction;
    
    private boolean wantToQuit;
    
    public Synchro(){
        demandeConnexion = false;
        estConnecte = false;
        
        demandeAction = false;
        nomAction = "";
        
        wantToQuit = false;
    }
    
    public synchronized void attendreDemandeConnexion(){
        while (!demandeConnexion){
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Attente demande connexion error");
                e.printStackTrace();
            }
        }
        demandeConnexion = false;
    }
    
    public synchronized void attendreDemandeAction(){
        while(!demandeAction){
            try{
                wait();
            }
            catch (InterruptedException e) {
                System.out.println("Attente demande d'action error");
                e.printStackTrace();
            }
        }
    }
    
    public synchronized void signalerDemandeConnexion(){
        demandeConnexion = true;
        notify();
    }
    
    public synchronized void setConnecte(){
        this.estConnecte = true;
    }
    
    public synchronized void setNonConnecte(){
        this.estConnecte = false;
    }
    
    public synchronized boolean getEstConnecte(){
        return this.estConnecte;
    }
    
    public synchronized void SignalerdemandeAction(){
        this.demandeAction = true;
    }
    
    public synchronized void setDemandeActionFaux(){
        this.demandeAction = false;
    }
    
    public synchronized boolean getDemandeAction(){
        return this.demandeAction;
    }
    
    public synchronized void setNomAction(String nomAction){
        this.nomAction = nomAction;
    }
    
    public synchronized String getNomAction(){
        return this.nomAction;
    }
    
    public synchronized void setWantToQuit(){
        this.wantToQuit = true;
    }
    
    public synchronized boolean getWantToQuit(){
        return this.wantToQuit;
    }
}

