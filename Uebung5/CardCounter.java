import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


record Croupier(String name, InetAddress addr, int port){}

public class CardCounter {

    private record Card(String face, String suit, int value, int amount){}
    private record Player(String name, int bj, int win, InetAddress addr, int port){}
    private static int Decks;
    static String[][] Kartenset = {{"Pik", "Kreuz", "Herz", "Karo"} , {"2","3","4","5","6","7","8","9","10","Bube","Dame","Koenig","Ass"}};
    private static ArrayList<Card> Deck = new ArrayList<>();
    private static ArrayList<Player> table = new ArrayList<>();
    static byte[] buffer = new byte[4096];
    static DatagramPacket p = new DatagramPacket(buffer, buffer.length);
    static Croupier croupier;




    public static void main(String[] args){

        int port = Integer.parseInt(args[0]);

        try(DatagramSocket s = new DatagramSocket(port)) {

            String line;
            do {
                s.receive(p);

                InetAddress sender = p.getAddress();


                line = new String(buffer, 0, p.getLength(), StandardCharsets.UTF_8);
                line = line.replaceAll("\n", "");
                String[] code = line.split(":");

                switch(code[0].toLowerCase()){

                    case "registrier croupier" : // register Croupier:<name>:<port>:<Anzahl Decks>

                        croupier = new Croupier(code[1],sender,Integer.parseInt(code[2]));
                        Decks = Integer.parseInt(code[3]);
                        send(croupier.addr(),croupier.port(), croupier.name() );
                        generiereKartensatz();
                        break;
                    case "registrier spieler" : // register spieler:<Name>:<port>
                        Player player = new Player(code[1],0,0,sender,Integer.parseInt(code[2]));
                        table.add(player);
                        break;
                    case "neue runde" : // Ausgegebene Karten und Hand des Croupiers (New turn:<Karte1-Karte2-Karte3...-Karte n>:<Hand Cropuier>)
                        String[] karten1 = code[1].split("-");
                        //reduciere values von Deck hier
                        //speicher Hand des croupier
                        break;
                    case "erbitte vorschlag" ://erbitte vorschlag:<Spielername>:<Karte1-..-Karte n> (Theoretisch spielerermittlung durch IP adresse möglich)

                       String[] karten2 = code[2].split("-");
                       Card[] hand = new Card[8];
                       hand[0] = generiereKarte("4","Herz",1);
                         for(Player c : table){
                             if (c.name.equals(code[1])){
                                 Thread p =  new Thread( () -> {
                                     vorschlag(hand,c);
                                 });
                                 p.start();
                             }
                         }
                        break;
                    case "statistik": //statistik:<Spielername>
                       //hole statistik by name
                        //sende an croupier!
                        send(croupier.addr(), croupier.port(), "666 win 666 blackjack");
                }
            } while (true);
        } catch (IOException e) {
            System.err.println("Unable to receive message on port \"" + port + "\".");
        }
    }
   private static void generiereKartensatz(){
        for (String w : Kartenset[0]){
            for (String g : Kartenset[1]){

             Deck.add(generiereKarte(g,w,Decks));
            }
       }

        for (Card b : Deck){
            if (b.face.equals("Ass")){
                Deck.set(Deck.indexOf(b),new Card(b.face,b.suit, b.value, b.amount - 1));
            }
        }

        System.out.println(Deck.toString());
    }
     public static void vorschlag(Card[] hand, Player nun){
       send(nun.addr,nun.port,"Hit, 95% quit before winning"); //Kein eignetlicher vorschlagsalgorithmus vorhanden, platzhalter
     }

     public static Card generiereKarte(String face, String Suit, int amount){
         int value;

         if (face.equals("Bube") || face.equals("Dame") || face.equals("Koenig")){
             value = 10;
         } else if (face.equals("Ass")) {
             value = 11;
         }else {
             value = Integer.parseInt(face);
         }
         return new Card(face,Suit,value,Decks);
     }

    private static void send(InetAddress reciever, int recieveport, String message){
        try (DatagramSocket s = new DatagramSocket()) {
            byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket p = new DatagramPacket(buffer, buffer.length, reciever, recieveport);
            s.send(p);
        } catch (IOException e) {
            System.err.println("Unable to send message");
        }
    }

}


