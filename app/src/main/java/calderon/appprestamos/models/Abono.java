package calderon.appprestamos.models;

public class Abono {

    private int id;
    private String fecha;
    private int abono;
    private int saldo;

    public Abono(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getAbono() {
        return abono;
    }

    public void setAbono(int abono) {
        this.abono = abono;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }

    @Override
    public String toString() {
        return "Abono{" +
                "id=" + id +
                ", fecha='" + fecha + '\'' +
                ", abono=" + abono +
                ", saldo=" + saldo +
                '}';
    }
}
