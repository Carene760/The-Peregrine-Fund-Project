package com.example.theperegrinefund;

public class Alerte {
    private int idAlerte;
    private Message message;
    private TypeAlerte type;

    public Alerte(int idAlerte, Message message, TypeAlerte type) {
        this.idAlerte = idAlerte;
        this.message = message;
        this.type = type;
    }

    public int getIdAlerte() {
        return idAlerte;
    }

    public Message getMessage() {
        return message;
    }

    public TypeAlerte getType() {
        return type;
    }

    public void setIdAlerte(int idAlerte) {
        this.idAlerte = idAlerte;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setType(TypeAlerte type) {
        this.type = type;
    }
}
