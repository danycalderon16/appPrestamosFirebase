package calderon.appprestamos.models;

import java.io.Serializable;

public class Persona  implements Serializable {
    private int id;
    private String nombre;
    private int cantidadPrestada;
    private String fecha;
    private int saldo;
    private String tipo; // Semanal o quincenal
    private int abonos; // número de abonos dados
    private int monto; // monto del abono
    private int plazos; // número de abonos que se tienen que dar
    private int abonado; // cantidad acumulada de abonos
    private boolean expanded;

    public Persona() {
    }

    public static class ChildClass implements Serializable {

        public ChildClass() {}
    }

    public Persona(int cantidadPrestada, int saldo, int abonos, int monto, int plazos, int abonado) {
        this.cantidadPrestada = cantidadPrestada;
        this.saldo = saldo;
        this.abonos = abonos;
        this.monto = monto;
        this.plazos = plazos;
        this.abonado = abonado;
        this.expanded = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantidadPrestada() {
        return cantidadPrestada;
    }

    public void setCantidadPrestada(int cantidadPrestada) {
        this.cantidadPrestada = cantidadPrestada;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getAbonos() {
        return abonos;
    }

    public void setAbonos(int abonos) {
        this.abonos = abonos;
    }

    public int getMonto() {
        return monto;
    }

    public void setMonto(int monto) {
        this.monto = monto;
    }

    public int getPlazos() {
        return plazos;
    }

    public void setPlazos(int plazos) {
        this.plazos = plazos;
    }

    public int getAbonado() {
        return abonado;
    }

    public void setAbonado(int abonado) {
        this.abonado = abonado;
    }

    @Override
    public String toString() {
        return "Persona{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", cantidadPrestada=" + cantidadPrestada +
                ", fecha='" + fecha + '\'' +
                ", saldo=" + saldo +
                ", tipo='" + tipo + '\'' +
                ", abonos=" + abonos +
                ", monto=" + monto +
                ", plazos=" + plazos +
                ", abonado=" + abonado +
                '}';
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
