package junglespeedserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe qui gére l'accés aux joueurs et aux parties partagés entre les threads
 * qui contient toutes les methodes pour en ajouter et en supprimmer
 */
public class Game {
    private List<Partie> parties; // toutes les parties crées
    private List<Joueur> joueurs; // tous les joueurs connéctés au serveur
    
    
    public Game(){
        parties = new ArrayList<Partie>();
        joueurs = new ArrayList<Joueur>();
    }
    
    /**
     * Crée un joueur si le pseudo est disponnible et l'ajoute à la liste des 
     * joueurs et le retourne, sinon retourne null.
     * @param pseudo 
     * @return le joueur créé ou null
     */
    public synchronized Joueur creerJoueur(String pseudo){
        boolean pseudoIsFree = true;
        for(Joueur joueur : joueurs){
            if (joueur.pseudo.equals(pseudo))
                pseudoIsFree = false;
        }
        if (pseudoIsFree){
            Joueur j = new Joueur(pseudo);
            joueurs.add(j);
            return j;
        }
        else{
            return null;
        }
    }
    
    public synchronized Partie creerPartie(Joueur createur, int nombreJoueurs){
        Partie p = new Partie(createur, nombreJoueurs);
        parties.add(p);
        return p;
    }
    
    /**
     * Si une partie correspond à l'identifiant passer en param on la retourne 
     * sinon retourne null.
     * @param partieId
     * @return 
     */
    public synchronized Partie getPartie(int partieId){
        Partie p = null;
        for(Partie partie : parties){
            if (partie.getPartieId()==partieId)
                p = partie;
        }
        return p;
    }
    
    /**
     * Retourne du texte contenant la déscription de chaque partie. 
     * @return 
     */
    public synchronized String listerParties(){
        if (parties.size()==0){
            return "Aucunes parties.";
        }
        else{
            String msg = "";
            for(Partie partie : parties){
                msg += partie+"\n";
            }
            return msg;
        }
    }
    
    /**
     * Supprime de la liste le joueur passer en parametre, retournbe vrai si le
     * joueur est bien supprimé sinon retourne faux.
     * @param joueur joueur à supprimer
     * @return 
     */
    public synchronized boolean supprimerJoueur(Joueur joueur){
        return joueurs.remove(joueur);
    }
    
    /**
     * 
     * @param partie
     * @return 
     */
    public synchronized boolean supprimerPartie(Partie partie){
        //procedure suppression partie
        return false;
    }
    
    public synchronized boolean joueurRejoinsPartie(Joueur joueur, int identifiantPartie){
        //getPartie(identifiantPartie).
        return false;
    }
    
}
