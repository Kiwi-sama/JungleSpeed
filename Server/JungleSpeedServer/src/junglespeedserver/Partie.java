package junglespeedserver;

import java.util.*;

public class Partie {
    
    private static Random rand = new Random(Calendar.getInstance().getTimeInMillis());
    
    // Les différents états de la partie
    final static int STATE_BEFORESTART = 0;
    final static int STATE_PLAYING = 1;
    final static int STATE_ENDWIN = 2;
    final static int STATE_ENDBROKEN = 3; // quand un client à quitter la partie 
    int state; // etat courant de la partie 
    
    //attributs généraux de la partie
    static int NEXT_PARTIE_ID = 1;
    int id;    
    Joueur createur;
    private int nbJoueurs;
    List<Joueur> joueurs;
    private CardPacket allCards; // le packet de carte contien les cartes pour tous les joueurs (12*nbJoueurs)
    private List<Card> underTotem; // cartes sous le totem

    //Semaphores
    Semaphore semAttenteDebut; //barriere pour attendre le bon nomb de joueur pour commencer la partie
    int nbJoueursAttenteDebut; //nombre de threads joueurs atttendant le début de la partie

    Semaphore semAttenteRevelCarte;
    int nbJoueursAttenteRevelCarte;
    
    //attributs pour les cartes
   
    //attributs pour la gestion d'un tour
    /* NOTE: parmis ces attributs seuls quelques uns doivent être accessibles
       par les threads, notamment currentPlayer, AllRevealedCards, resultMsg, partywinner
       vous n'avez donc pas besoin de faire des getters pour tous les attributs.
     */
    public Joueur currentJoueur; //joueur qui dois rtourner une carte durant le tour
    public int currentJoueurId;
    private Card derniereCarteJouee; 
    private String cartesRevele; //cartes rélélées à envoyer aux clients avant de jouer
    
    
    //attributs pour la  gestion du resultat d'un tour
    private List<Joueur> aJoue; // joueur qui ont déjà jouer ce tour ci
    private List<Integer> result; // the result of the order of players.
    private boolean totemTaken; // if the totem has been taken during the current turn
    private boolean totemHand; // if a player has put his hand on the totem during the current turn
    private String resultMsg; // the message containing the reuslt of the current turn that threads send to their client.
    private Joueur gagnantDeLaPartie; // winner of the party (must stay at null until there is effectively a winner)
    private Joueur gagnantDuTour; 
    
    // attribute to manage the end of the party
        private int nbLeaveParty; // the number of threads that have currently leaved the party
    
    public Partie(Joueur createur, int nbPlayer){
        this.id = Partie.NEXT_PARTIE_ID;
        Partie.NEXT_PARTIE_ID++;
        this.createur = createur;
        this.nbJoueurs = nbPlayer;
        gagnantDeLaPartie = null;
        nbLeaveParty = 0;

        semAttenteDebut = new Semaphore(0);
        nbJoueursAttenteDebut = 0;
        
        semAttenteRevelCarte = new Semaphore(0);
        nbJoueursAttenteRevelCarte = 0;
        
        state = STATE_BEFORESTART;
        
        joueurs = new ArrayList<Joueur>();
        underTotem = new ArrayList<Card>();
        aJoue = new ArrayList<Joueur>();
        result = new ArrayList<Integer>();
        
        allCards = new CardPacket(nbJoueurs);
        
        ajouterJoueur(createur);
    }
    
    /**
     * Ajoute le joueur j à la partie si cela est possible, et lui distribue
     * ses cartes
     * @param j Joueur à ajouter à la partie.
     * @return 
     */
    public synchronized boolean ajouterJoueur(Joueur j){
        if(joueurs.size()==nbJoueurs){
            return false;
        }
        else{
            joueurs.add(j);
            ArrayList<Card> pioche = allCards.Draw(12);
            j.initialisationPartie(this, joueurs.size(), pioche);
            return true;
        }
    }
    
    //sémaphores
    /**
     * Permet d'attendre que le nombre de joueurs souhaiter soit  atteint 
     * avant de débuter la partie.
     */
    public void AttendreDebutPatie(){
        nbJoueursAttenteDebut += 1;
        if (nbJoueursAttenteDebut == nbJoueurs){
            nbJoueursAttenteDebut = 0;

            //le nombre de joueurs est atteint, on commence la partie maj état
            setState(STATE_PLAYING);
            
            //déterminer id premier joueur
            currentJoueurId = rand.nextInt(nbJoueurs);
            currentJoueur = getJoueur(currentJoueurId);
            
            
            semAttenteDebut.put(nbJoueurs);
            System.out.println("La partie commence");
        }
        semAttenteDebut.get(1);
    }
 
    /**
     * Permet d'attendre que le joueur qui doit piocher ai pioché avant 
     * de continuer.
     */
    public  void AttendreRevelationCarte(){
        nbJoueursAttenteRevelCarte += 1;
        if (nbJoueursAttenteRevelCarte == nbJoueurs){
            nbJoueursAttenteRevelCarte = 0;
            
            semAttenteRevelCarte.put(nbJoueurs);
            System.out.println("tous les joueurs ont attendu que la carte "
                    + "soit révélé");
        }
        semAttenteRevelCarte.get(1);
    }
    
    //états de la partie
    /**
     * Retourne l'identifiant de l'état courant de la partie
     * @return 
     */
    public int getCurrentState(){
        return state;
    }
    
    /**
     * Mets à jour l'état de la partie, si la mise à jour est la suite logique 
     * de l'état courant
     * @param stateId 
     */
    public synchronized void setState(int stateId){
        boolean changementEtat = false;
        if (stateId == STATE_ENDBROKEN){
            changementEtat = true;
        }
        else {
            if (state == STATE_BEFORESTART && stateId == STATE_PLAYING){
                changementEtat = true;
            }
            else if (state == STATE_PLAYING && stateId == STATE_ENDWIN) {
                changementEtat = true;
            }
        }
        if (changementEtat)
            state = stateId;
    }
    
    /**
     * Révéle la carte du joueur courant, et construit le résumé des dernieres 
     * cartes jouées.
     */
    public synchronized void revealCard(){
        derniereCarteJouee = currentJoueur.revealCard();
        cartesRevele = "";
        for(Joueur j : joueurs){
            if (j.cartesRevele.size() > 0){
                cartesRevele += j.cartesRevele.cartes.get(0) +";";
            }
            else{
                cartesRevele += ";";
            }
        }
    }
    
    /**
     * Verifie si la carte retournée devant le joueur passé en param (si elle 
     * existe) correspond à celle d'au moins un autre joueur, si oui 
     * elle retourne true.
     * @param j
     * @return 
     */
    public boolean checkDuplicateCards(Joueur joueur){
        if (joueur.currentCard()==null){
            return false;
        }
        for(Joueur j : joueurs){
            if (!joueur.equals(j)){
                if (joueur.currentCard().equals(j.currentCard())){
                    return true;
                }
            }
        }
        return false;
    }
    
    
    
    //autres
    /**
     * Indique l'identifiant d'une partie.
     * @return 
     */
    public int getPartieId(){
        return this.id;
    }
    
    /**
     * Retourne le créateur de la partie.
     * @return 
     */
    public Joueur getCreateur(){
        return this.createur;
    }
    
    
    /**
     * Retourne le joueur avec l'identifiant dans la partie correspondant 
     * à celui passé en param, sinon retourne null.
     * @param id
     * @return 
     */
    public Joueur getJoueur(int id){
        Joueur joueur = null;
        for (Joueur j : joueurs){
            if (j.id == id)
                joueur = j;
        }
        
        return joueur;
    }
    
    /**
     * Mets à jour l'id et le joueur courant et le retourne.
     * @return 
     */
    public Joueur getJoueurProchainTour(){
        Joueur joueur = null;
        if (currentJoueurId == nbJoueurs){
            currentJoueurId = 1;
        }
        else{
            currentJoueurId++;
        }
        joueur = getJoueur(currentJoueurId);
        currentJoueur = joueur;
        return joueur;
    }
    
    public synchronized void setProchainJoueur(int joueurId){
        
    }
    
    
    /**
     * Retourne le nombre de place libre dans la partie.
     * @return 
     */
    public int getNbPlaceLibre(){
        return this.nbJoueurs-this.joueurs.size();           
    }
    
    //get set 
    public String getCartesRevele(){
        return cartesRevele;
    }
    
    
    /**
     * Retourne un descriptif de la partie.
     * @return 
     */
    @Override
    public String toString(){
        if(getNbPlaceLibre() > 1)
            return "["+getPartieId()+"] - partie créée par "+createur.pseudo+", reste "+getNbPlaceLibre()+" places disponible";
        else if(getNbPlaceLibre() == 1)
            return getPartieId()+" - partie créée par "+createur.pseudo+", reste "+getNbPlaceLibre()+" place disponible";
        else
           return getPartieId()+" - partie créée par "+createur.pseudo+", Plus de places disponible";
    }
    
}
