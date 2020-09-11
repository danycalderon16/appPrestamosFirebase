package calderon.appprestamos.models;

public class Totales {
    int total;
    int totalRecuperar;
    int totalGanar;

    public Totales(int total, int totalRecuperar, int totalGanar) {
        this.total = total;
        this.totalRecuperar = totalRecuperar;
        this.totalGanar = totalGanar;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalRecuperar() {
        return totalRecuperar;
    }

    public void setTotalRecuperar(int totalRecuperar) {
        this.totalRecuperar = totalRecuperar;
    }

    public int getTotalGanar() {
        return totalGanar;
    }

    public void setTotalGanar(int totalGanar) {
        this.totalGanar = totalGanar;
    }


}
