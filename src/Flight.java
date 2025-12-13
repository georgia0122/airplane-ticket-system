import java.time.LocalDateTime;

public class Flight {
    private int id;
    private String code;
    private String origin;
    private String destination;
    private LocalDateTime departTime;
    private LocalDateTime arriveTime;
    private double price;
    private int seatsTotal;
    private int seatsLeft;
    private int duration; // 飞行时长（分钟）

    public Flight() {}

    // constructors, getters, setters
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDateTime getDepartTime() { return departTime; }
    public void setDepartTime(LocalDateTime departTime) { this.departTime = departTime; }
    public LocalDateTime getArriveTime() { return arriveTime; }
    public void setArriveTime(LocalDateTime arriveTime) { this.arriveTime = arriveTime; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getSeatsTotal() { return seatsTotal; }
    public void setSeatsTotal(int seatsTotal) { this.seatsTotal = seatsTotal; }
    public int getSeatsLeft() { return seatsLeft; }
    public void setSeatsLeft(int seatsLeft) { this.seatsLeft = seatsLeft; }
}
