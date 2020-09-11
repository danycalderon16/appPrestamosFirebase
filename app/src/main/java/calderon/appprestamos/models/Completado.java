package calderon.appprestamos.models;

public class Completado {

    private int id;
    private String nombre;
    private int cantidadPrestada;
    private int ganancia;
    private String fecha_prestamo;
    private String fecha_final;

    public Completado() {
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

    public int getGanancia() {
        return ganancia;
    }

    public void setGanancia(int ganancia) {
        this.ganancia = ganancia;
    }

    public String getFecha_prestamo() {
        return fecha_prestamo;
    }

    public void setFecha_prestamo(String fecha_prestamo) {
        this.fecha_prestamo = fecha_prestamo;
    }

    public String getFecha_final() {
        return fecha_final;
    }

    public void setFecha_final(String fecha_final) {
        this.fecha_final = fecha_final;
    }

    @Override
    public String toString() {
        return "Completado{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", cantidadPrestada=" + cantidadPrestada +
                ", ganancia=" + ganancia +
                ", fecha_prestamo='" + fecha_prestamo + '\'' +
                ", fecha_final='" + fecha_final + '\'' +
                '}';
    }
}
