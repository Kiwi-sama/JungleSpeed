// classe qui gère les échanges de messages avec l'application serveur.

public class ThreadCom implements Runnable{
    private JungleIG ig;
    private Synchro sync;
    
    public  ThreadCom(Synchro s, JungleIG i){
        ig = i;
        sync = s;
    }
    public void run(){
        // Dans cette méthode run(), toutes les modifications de l'interface graphique
        // doivent être réalisées via la méthode invokeLater() de la classe SwingUtilities
        sync.attendreDemandeConnexion();
        
        //ici créer socket && protocole communication
        
        javax.swing.SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                
                ig.setInitPanel();
            }
        });
    }
}
