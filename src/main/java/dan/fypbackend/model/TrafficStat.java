package dan.fypbackend.model;

public class TrafficStat {
    private double delay;
    private long date;
    private String weather, carParkName, direction;

    public TrafficStat() {

    }

    public double getDelay() {
        return delay;
    }

    public void setDelay(double delay) {
        this.delay = delay;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getCarParkName() {
        return carParkName;
    }

    public void setCarParkName(String carParkName) {
        this.carParkName = carParkName;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "TrafficStat{" +
                "delay=" + delay +
                ", date=" + date +
                ", weather='" + weather + '\'' +
                ", carParkName='" + carParkName + '\'' +
                ", direction='" + direction + '\'' +
                '}';
    }
}
