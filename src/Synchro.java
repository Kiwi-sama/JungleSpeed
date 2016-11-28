// instance de Synchro : objet partagé entre les instances de ThreadCom et JungleIG
// sert à la synchronisation des threads EDT (Event Dispatching Thread) et ThreadCom

public class Synchro
{
    private boolean demandeConnexion;
    
    public Synchro(){
        demandeConnexion = false;
    }
    
    public synchronized void attendreDemandeConnexion(){
        while (!demandeConnexion){
            try {
                wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        demandeConnexion = false;
        
    }
    public synchronized void signalerDemandeConnexion(){
        demandeConnexion = true;
        notify();
    }
}

