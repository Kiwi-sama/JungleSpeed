package junglespeedserver;

import java.util.ArrayList;
import java.util.List;

public class Joueur {
    
    String pseudo;
    
    // Partie dans la quelle le joueur est, null si le joueur ne joue pas une 
    // partie.
    Partie currentPartie;
    
    // Id dans la partie dans la quelle le joueur joue, -1 si le joueur n'a 
    // rejoint aucune partie 
    int id; 
    
    CardPacket cartesCahee;
    CardPacket cartesRevele;
    
    
    
    public Joueur(String pseudo){
        this.pseudo = pseudo;
        currentPartie = null;
        id = -1;
        cartesCahee = null;
        cartesRevele = null;
    }
    
    /**
     * Initialie le joueur pour la partie qu'il à rejoin
     * @param partie
     * @param id
     * @param pioche 
     */
    public void initialisationPartie(Partie partie, int id, ArrayList<Card> pioche){
       this.currentPartie = partie;
       this.id = id;
       cartesCahee = new CardPacket(pioche);
       cartesRevele = new CardPacket();
    }
    
    public void clearPartie(){
        currentPartie = null;
        id = -1;
        cartesCahee = null;
        cartesRevele = null;
    }
    
    /**
     * Retourne la première carte du tas de cartes caché (l'ajoute au tas de 
     * cartes retournées.
     * @return la carte retournée
     */
    public Card revealCard(){
        Card c = cartesCahee.removeFirst();
        cartesRevele.addFirst(c);
        return c;
    }
    
    /**
     * Retourne la dernier carte révélée par le joueur. Si il n'y en a aucune 
     * retourne null
     * @return 
     */
    public Card currentCard(){
        if (cartesRevele.isEmpty())
            return null;
        return cartesRevele.get(0);
    }
    
    /**
     * Quand le joueur prends des cartes, il mets les cartes du tas qu'il 
     * récupére et ses cartes révélées dans sa pile de cartes cachée.
     * @param tas Tas de carte que le joueur doit prendre.
     */
    public void takeCards(ArrayList<Card> tas){
        cartesCahee.addCards(tas);
        cartesCahee.addCards(cartesRevele);
        cartesRevele.clearCardPacket();
        cartesCahee.melanger();
    } 
    
    /**
     * Donne toutes les cartes révélées.
     * @return 
     */
    public ArrayList<Card> giveRevealedCards(){
        ArrayList<Card> tas = new ArrayList<Card>();
        tas.addAll(cartesRevele.getAllCards());
        cartesRevele.clearCardPacket();
        return tas;
    }
    
    public boolean checkIsWinner(){
        if((cartesRevele.size() == 0) && cartesCahee.size() == 0){
            return true;
        }
        else{
            return false;
        }
    }
    
    
}
