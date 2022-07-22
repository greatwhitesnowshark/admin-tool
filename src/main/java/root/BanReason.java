package root;

public enum BanReason {

    Harassment(1),
    Impersonation(1),
    Evasion(-1),
    AccountShare(30),
    MultiAccount(30),
    Hacking(-1),
    Botting(30),
    Exploit(-1),
    Scam(14),
    FalseBid(7),
    RealWorldTrade(-1),
    KillSteal(1),
    Debugger(-1, true),
    MemoryEdit(-1, true),
    WzEdit(-1, true),
    Injection(-1, true),
    Chargeback(-1, true),
    NotBanned;

    public int nReason;
    public int tDuration;
    public boolean bAutoBan;

    BanReason() {
        this.nReason = -1;
        this.tDuration = 0;
        this.bAutoBan = false;
    }

    BanReason(int tDuration) {
        this.nReason = ordinal();//nReason;
        this.tDuration = tDuration;
        this.bAutoBan = false;
    }

    BanReason(int tDuration, boolean bAutoBan) {
        this.nReason = ordinal();
        this.tDuration = tDuration;
        this.bAutoBan = bAutoBan;
    }

    public static BanReason GetReason(int nReason) {
        return nReason > 0 && BanReason.values().length > nReason ? BanReason.values()[nReason] : NotBanned;
    }
}