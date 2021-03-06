package junglespeedserver;

public class Card {
    char card;

    public Card(char card) throws IllegalArgumentException {
        if ((card == 'O') || (card == 'Q') || (card == 'B') || (card == 'P') || 
                (card == 'E') || (card == 'F') || (card == 'I') || (card == 'J')
                ||(card == 'C') || (card == 'G') || (card == 'T') 
                || (card == 'H')) {
            this.card = card;
        }
        else {
            throw new IllegalArgumentException();
        }
    }   
    
    @Override
    public String toString(){
        return card + "";
    }
}
