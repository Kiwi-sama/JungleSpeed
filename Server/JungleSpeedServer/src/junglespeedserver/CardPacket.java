package junglespeedserver;

import java.util.*;

public class CardPacket {

    /**
     * Valeur aléatoire sert pour mélanger le paquêt
     */
    private static Random loto = new Random(Calendar.getInstance().getTimeInMillis());
    
    /**
     * liste des cartes 
     */
    ArrayList<Card> cartes;
   
    /**
     * Crée un paquet de carte vide pour un joueur
     */
    public CardPacket(){
        cartes = new ArrayList<Card>();
    }
    
    /**
     * Crée un paquet pour un joueur à partir d'une liste de cartes.
     * @param tas 
     */
    public CardPacket(ArrayList<Card> tas){
        cartes = new ArrayList<Card>();
        cartes.addAll(tas);
    }
    
    /**
     * Crée un paquet de carte contenant toutes les cartes pour le nombre de 
     * joueurs passé en param.
     * @param nbJoueurs 
     */
    public CardPacket(int nbJoueurs){
        Card carte;
        cartes = new ArrayList<Card>();
        for(int i=0;i<nbJoueurs;i++) {
            carte = new Card('O');
            cartes.add(carte);
            carte = new Card('Q');
            cartes.add(carte);
           carte = new Card('B');
            cartes.add(carte);
            carte = new Card('P');
            cartes.add(carte);
           carte = new Card('E');
            cartes.add(carte);
            carte = new Card('F');
            cartes.add(carte);
            carte = new Card('I');
            cartes.add(carte);
            carte = new Card('J');
            cartes.add(carte);
            carte = new Card('C');
            cartes.add(carte);
            carte = new Card('G');
            cartes.add(carte);
            carte = new Card('T');
            cartes.add(carte);
            carte = new Card('H');
            cartes.add(carte);
        }
        melanger();
    }
    
    /**
     * Retourne le nombre de carte présent dans le paquet.
     * @return 
     */
    public int size(){
        return cartes.size();
    }
    
    /**
     * Ajoute au paquet de cartes le tas passé en param.
     * @param tas liste de cartes
     */
    public void addCards(ArrayList<Card> tas){
        cartes.addAll(tas);
    }
    
    /**
     * Ajoute au paquet de cartes le tas passé en param.
     * @param tas paquet de cartes
     */
    public void addCards(CardPacket tas){
        cartes.addAll(tas.cartes);
    }
    
    /**
     * retire du paquet toutes les cartes dans le tas passé en param.
     * @param tas
     */
    public void removeCards(ArrayList<Card> tas){
        cartes.removeAll(tas);
    }
    
    /**
     * Vide le paquet de cartes.
     */
    public void clearCardPacket(){
        cartes.clear();
    }
    
    /**
     * Ajoute une carte au dessus du paquet.
     * @param c 
     */
    public void addFirst(Card c) {
        cartes.add(0,c);
    }
    
    /**
     * Retire et retourne la première carte du paquet.
     * @return 
     */
    public Card removeFirst(){
        return cartes.remove(0);
    }
    
    /**
     * retourne et retire le nombre de carte passé en param, sil il n'y a pas 
     * suffisament de cartes, retourne le nombre de carte restant dans le 
     * paquets.
     * @param nbCards
     * @return 
     */
    public ArrayList<Card> Draw(int nbCards){
        ArrayList<Card> tas = new ArrayList<Card>();
        Card carte;
        for (int i = 0; i < nbCards; i++) {
            carte = cartes.remove(0);
            tas.add(carte);
        }
        return tas;
    }
    
    /**
     * Retourne la cartes à l'index passé en param ou null si l'index est 
     * plus grand que le nombre de carte restant ou inferieur à 0.
     * @param index
     * @return 
     */
    public Card get(int index) {
        if ((index <0) || (index >= cartes.size())) return null;

        return cartes.get(index);
    }
    
    /**
     * Retourne toutes les cartes du paquet.
     * @return 
     */
    public List<Card> getAllCards(){
        return cartes;
    }
    
    /**
     * Indique si le paquet est vide ou non.
     * @return 
     */
    public boolean isEmpty(){
        if (cartes.isEmpty()) return true;
        return false;
    }
    
    /**
     * Mélange le paquet de carte.
     */
    public void melanger(){
        int index;
        for (int i = 0; i < 200; i++) {
            index = loto.nextInt(cartes.size());
            Card carte = cartes.remove(index);
            cartes.add(carte);
        }
    }
}
