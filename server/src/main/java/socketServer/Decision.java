package socketServer;

import game.Client;
import game.Game;

public class Decision {
    private Client client;
    private int loan;
    private int FCount;

    public void setLoan(int loan) {
        this.loan = loan;
    }

    public void setFCount(int FCount) {
        this.FCount = FCount;
    }

    public void setADCount(int ADCount) {
        this.ADCount = ADCount;
    }

    public void setUFCount(int UFCount) {
        this.UFCount = UFCount;
    }

    private int ADCount;
    private int UFCount;

    public int getESM() {
        return ESM;
    }

    public int getEGP() {
        return EGP;
    }

    private int ESM;
    private int EGP;
    private int cash;

    public void setESM(int ESM) {
        this.ESM = ESM;
    }

    public void setEGP(int EGP) {
        this.EGP = EGP;
    }

    public void setCash(int cash) {
        this.cash = cash;
    }

    public Decision(int sessionId) {
        this.client = Game.getInstance().getClient(sessionId);
        ESM = 0;
        EGP = 0;
        cash = 0;
    }

    public int getCash() {
        return cash;
    }

    public int getLoan() {
        return loan;
    }

    public int getFCount() {
        return FCount;
    }

    public int getADCount() {
        return ADCount;
    }

    public int getUFCount() {
        return UFCount;
    }
}
