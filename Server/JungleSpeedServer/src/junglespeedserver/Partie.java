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
    private Joueur createur;
    private Joueur gagnant;
    private int nbJoueurs;
    List<Joueur> joueurs;
    private CardPacket allCards; // le packet de carte contien les cartes pour tous les joueurs (12*nbJoueurs)
    private CardPacket underTotem; // cartes sous le totem
    private int idJoueurProchainTour;
    
    
    //Semaphores
    Semaphore semAttenteDebut; 
    int nbJoueursAttenteDebut; 

    Semaphore semAttenteRevelCarte;
    int nbJoueursAttenteRevelCarte;
    
    Semaphore semAttendreActionJoueurs;
    int nbJoueurAttendreActionJoueurs;
    
    
    //attributs pour les cartes
  
    public Joueur currentJoueur; //joueur qui dois rtourner une carte durant le tour
    public int currentJoueurId;
    private Card derniereCarteJouee; 
    private String cartesRevele; //cartes rélélées à envoyer aux clients avant de jouer
    
    
    //attributs pour la  gestion du resultat d'un tour
    private ArrayList<Joueur> aJoue; // joueur qui ont déjà jouer ce tour ci
    private ArrayList<Integer> result; // the result of the order of players.
    private boolean totemTaken; // if the totem has been taken during the current turn
    private boolean totemHand; // if a player has put his hand on the totem during the current turn
    private String resultMsg; // the message containing the reuslt of the current turn that threads send to their client.
    private Joueur gagnantDeLaPartie; // winner of the party (must stay at null until there is effectively a winner)
    private Joueur gagnantDuTour; 
    
    public Partie(Joueur createur, int nbPlayer){
        this.id = Partie.NEXT_PARTIE_ID;
        Partie.NEXT_PARTIE_ID++;
        
        this.createur = createur;
        gagnant = null;
        this.nbJoueurs = nbPlayer;
        gagnantDeLaPartie = null;
        
        //semaphore
        semAttenteDebut = new Semaphore(0);
        nbJoueursAttenteDebut = 0;
        
        semAttenteRevelCarte = new Semaphore(0);
        nbJoueursAttenteRevelCarte = 0;
        
        semAttendreActionJoueurs = new Semaphore(0);
        nbJoueurAttendreActionJoueurs = 0;
        
        state = STATE_BEFORESTART;
        
        cartesRevele = "";
        
        joueurs = new ArrayList<Joueur>();
        underTotem = new CardPacket();
        aJoue = new ArrayList<Joueur>();
        result = new ArrayList<Integer>();
        
        totemHand = false;
        totemTaken = false;
        
        allCards = new CardPacket(nbJoueurs);
        
        ajouterJoueur(createur);
        idJoueurProchainTour = createur.id;
    }
    
    public Joueur getGagnant(){
        return this.gagnant;
    }
    
    public synchronized void setGagnant(Joueur joueur){
        this.gagnant = joueur;
    }
    
    public int getIdJoueurProchainTour(){
        return idJoueurProchainTour;
    }
    
    public synchronized void setJoueurProchainTour(int idJoueur){
        this.idJoueurProchainTour = idJoueur;
        this.currentJoueur = getJoueur(idJoueur);
    }
    
    /**
     * Ajoute le joueur j à la partie si cela est possible, et lui distribue
     * ses cartes
     * @param j Joueur à ajouter à la partie.
     * @return 
     */
    public synchronized boolean ajouterJoueur(Joueur joueur){
        if(joueurs.size()==nbJoueurs){
            return false;
        }
        else{
            joueurs.add(joueur);
            ArrayList<Card> pioche = allCards.Draw(12);
            joueur.initialisationPartie(this, joueurs.size(), pioche);
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
            debugReport("La partie commence");
        }
        semAttenteDebut.get(1);
    }
 
    /**
     * Permet d'attendre que le joueur qui doit piocher ai pioché avant 
     * de continuer.
     */
    public void AttendreRevelationCarte(){
        nbJoueursAttenteRevelCarte += 1;
        if (nbJoueursAttenteRevelCarte == nbJoueurs){
            nbJoueursAttenteRevelCarte = 0;
            semAttenteRevelCarte.put(nbJoueurs);
        }
        semAttenteRevelCarte.get(1);
    }
    
    public void AttendteActionJoueur(){
        nbJoueurAttendreActionJoueurs += 1;
        if (nbJoueurAttendreActionJoueurs == nbJoueurs){
            nbJoueurAttendreActionJoueurs = 0;
            semAttendreActionJoueurs.put(nbJoueurs);
        }
        semAttendreActionJoueurs.get(1);
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
                cartesRevele += j.id+","+j.cartesRevele.cartes.get(0) +";";
            }
            else{
                cartesRevele += j.id+",X;";
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
    public boolean checkSameCards(Joueur joueur){
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
    
    /**
     * Pour le joueur passé en param, et en fonction de l'action indiqué par 
     * le client vas ajouter le joueur dans le tableau des joueur ayant joué
     * (ce qui permet de conserver l'ordre de rapidité de réponse) et dans un
     * tableau d'entier stock au même index le resultat cd ce joueur.
     * 
     * Les ordres peuvent être :
     * N : ne rien faire
     * TT : prendre le totem
     * HT : mettre la main sur le totem
     * 
     * Les resultats peuvent être :
     * -2 : le joueur à fait une erreur
     * -1 : le joueur n'a pas pris le totem quand il devait
     * 0  : le joueur à bien réagit mais trop tard
     * 1  : le joueur à gagné le tour
     * 
     * Les erreur peuvent être :
     *  - le joueur ne fait rien alors que la carte révélé était H.
     *  - le joueur prend le totem alors que la carte révélé était H.
     *  - le joueur met sa main sur le totem alors que la carte révélé était T.
     *  - le joueur prends le totem alors qu'il n'avait pas la même carte 
     * qu'un autre joueur et que la carte révélé n'était ni H, ni T.
     * 
     * @param joueur
     * @param ordre 
     */
    public synchronized void IntegrationOrdreJoueur(Joueur joueur, String ordre){
        aJoue.add(joueur);
        if (derniereCarteJouee.card == 'H'){
            if (ordre == "N"){
                result.add(-2); // le joueur a fait une érreur, il devait HT
            }
            else if (ordre == "TT"){
                result.add(-2); // le joueur a fait une érreur, il devait HT
            }
            else if (ordre == "HT"){
                if (!totemHand){
                    result.add(1); // il est le premier a HT
                    totemHand = true;
                }
                else{
                    if (joueurs.size() == nbJoueurs){
                        result.add(-1); // perdant car dernier a HT
                    }
                    else{
                        result.add(0); // ni premier, ni dernier;
                    }
                } 
            }
        }
        else if (derniereCarteJouee.card == 'T'){
            if (ordre == "N"){
                result.add(-1);//joueur perds mais pas grave
            }
            else if (ordre == "TT"){
                if(!totemTaken){
                    result.add(1);//joueur premier a TT => gagne
                    totemTaken = true;
                }
                else{
                    result.add(0);//joueur ni gagnant, ni perdant
                }
            }
            else if (ordre == "HT"){
                result.add(-2);//joueur erreur devait faire TT
            }
        }
        else{ //cas ou la derniere carte joué n'est ni T ni H
            if (ordre == "N"){
                if(checkSameCards(joueur)){
                    result.add(-1);//joueur perd, devait TT en 1er
                }
                else{
                    result.add(0);//ni perdant ni gagnant car ne devait rien faire
                }
            }
            else if (ordre == "TT"){
                if (checkSameCards(joueur)){
                    if (!totemTaken){
                        result.add(1);//gagne a des carte en commun avec autre joueur et 1er à TT
                        totemTaken = true;
                    }
                    else{
                        result.add(-1);//perd, n'a pas TT en 1er
                    }
                }
                else{
                    result.add(-2);//joueur ne devait pas prendre totem => erreur
                }
            }
            else if (ordre == "HT"){
               result.add(-2);//joueur a fait erreur, mauvaise action
            }
        }
    }
    
    /**
     * analyse les resultat de la partie
     */
    public synchronized void analyseResultats(){
        resultMsg = "";
        ArrayList<Joueur> erreurs = new ArrayList<Joueur>();
        ArrayList<Joueur> perdants = new ArrayList<Joueur>();
        for (int i = 0; i<nbJoueurs;i++ ){
            if(result.get(i)==-2){
                erreurs.add(aJoue.get(i));
            }
            if(result.get(i)==-1){
                erreurs.add(aJoue.get(i));
            }
        }
        
        //si au moins un joueur a fait une erreur
        if (!erreurs.isEmpty()){
            //les joueurs qui font des erreurs on le pire cas, 
            //on récupére toutes les cartes révélé de chaque joueurs et sous
            //le totem pour les distribuer entre les joueurs perdant
            CardPacket tasErreur = getToutesCartesRevele();
            tasErreur.melanger();
            int nb = (tasErreur.size()+1)/erreurs.size();
            for (int i=0; i<erreurs.size()+1;i++ ){
                Joueur j = erreurs.get(i);
                if (i < erreurs.size()-1){
                    j.takeCards(tasErreur.Draw(nb));
                    resultMsg += j.pseudo+" à fait une erreur, il prends "+nb+" cartes";
                }
                else{
                    resultMsg += j.pseudo + " à fait une erreur, il prends "+tasErreur.size()+" cartes de tous les joueurs";
                    j.takeCards(tasErreur.getAllCards());
                }
            }
            
            //on regarde si quelqu'un à gagné
            for(Joueur j : joueurs){
                if(j.checkIsWinner()){
                    resultMsg += j.pseudo+" à gagné la partie";
                    setState(STATE_ENDWIN);
                    return;
                }
            }
            //on choisit le prochain joueur
            currentJoueur = erreurs.get(rand.nextInt(erreurs.size()));
            resultMsg += "Prochain joueur "+ currentJoueur.pseudo;
            this.resultMsg = resultMsg;
        }
        else{
            //si aucun joueur n'a fait d'erreurs
            int indexGagnant = -1;
            for(Integer r : result){
                if (r==1){
                    indexGagnant = r;
                    break;
                }
            }
            
            //si personne ne gagne le tour
            if(indexGagnant==-1){
                resultMsg += "Personne n'a gagné le tour\n";
                currentJoueur = joueurs.get(currentJoueur.id%nbJoueurs);
                resultMsg += "\n prochain joueur "+currentJoueur.pseudo;
            }
            else{
                //si un joueur est gagnant, le resultat dépends de la la dernière carte révélé
                gagnantDuTour = joueurs.get(indexGagnant);
                resultMsg += gagnantDuTour.pseudo + " à gagné le tour\n";
                
                if (derniereCarteJouee.card == 'T'){
                    //gagnant à gagné sur Take totem
                    resultMsg += "Il mets ses cartes sous le totem\n";
                    underTotem.addCards(gagnantDuTour.cartesRevele.getAllCards());
                    gagnantDuTour.cartesRevele.clearCardPacket();
                }
                else if (derniereCarteJouee.card == 'H'){
                    //gagnant à gagné sur hand totem
                    Joueur perdant = perdants.get(0);
                    resultMsg += "Il donne ses cartes et celles sous le totem à "+perdant.pseudo+"\n";
                    perdant.takeCards(getCartesGagnant().getAllCards());
                }
                else{
                    //le gagnant gagne car il avait la même carte qu'un autre joueur
                    //distribue les cartes du gagnants aux perdants
                    CardPacket tasGagnant = getCartesGagnant();
                    tasGagnant.melanger();
                    int nb = (tasGagnant.size()+1)/perdants.size();
                    for (int i=0;i<perdants.size();i++){
                        Joueur joueur = perdants.get(i);
                        if (i < perdants.size()-1){
                            joueur.takeCards(tasGagnant.Draw(nb));
                            resultMsg += joueur.pseudo+ " perds son duel avec "+gagnant.pseudo+" il prends "+nb+" cartes.\n";
                        }
                        else{
                            joueur.takeCards(tasGagnant.getAllCards());
                            resultMsg += joueur.pseudo+" perds son duel avec "+gagnant.pseudo+" il prends "+tasGagnant.size()+"cartes.\n";
                        }
                    }
                }
                for(Joueur joueur : joueurs){
                    if(joueur.checkIsWinner()){
                        resultMsg += joueur.pseudo+" gagne la partie.\n";
                        setState(STATE_ENDWIN);
                    }
                }
                currentJoueur = perdants.get(rand.nextInt(perdants.size()));
                resultMsg += "\n Prochain joueur : "+currentJoueur.pseudo;
                this.resultMsg = resultMsg;
            }
        }
        initNouveauTour();
    }
    
    public String getResultatPartie(){
        return this.resultMsg;
    }
    
    public void initNouveauTour(){
        aJoue.clear();
        result.clear();
        totemTaken = false;
        totemHand = false;
        
    }
    
    public CardPacket getCartesGagnant(){
        CardPacket tas = new CardPacket();
        tas.addCards(gagnant.giveRevealedCards());
        tas.addCards(underTotem);
        return tas;
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
    
    /**
     * 
     * @param joueurId 
     */
    public synchronized void setProchainJoueur(int joueurId){
        
    }
    
    
    /**
     * Retourne le nombre de place libre dans la partie.
     * @return 
     */
    public int getNbPlaceLibre(){
        return this.nbJoueurs-this.joueurs.size();           
    }
    
    /**
     * Crée un paquêt a partir de toutes les cartes révélé par tous les joueurs
     * et celles sous le totem. (Sert quand un joueur fait une erreur)
     * @return 
     */
    public CardPacket getToutesCartesRevele(){
        CardPacket tas = new CardPacket();
        for (Joueur joueur : joueurs){
            tas.addCards(joueur.giveRevealedCards());
        }
        tas.addCards(underTotem);
        return tas;
    }
    
    //get set 
    public String getCartesRevele(){
        return cartesRevele;
    }
    
    public synchronized boolean joueurQuittePartie(Joueur joueur){
        return false;
    }
    
   
    private void debugReport(String msg) {
        System.err.println("Partie ["+id+"] - "+msg);
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
            return "["+getPartieId()+"] - partie créée par "+createur.pseudo+", reste "+getNbPlaceLibre()+" place disponible";
        else
           return "["+getPartieId()+"] - partie créée par "+createur.pseudo+", Plus de places disponible";
    }
    
}
