import java.time.LocalDateTime;


public class Order {
    public enum Status {
        PENDING, PAID, CANCELLED, REFUNDED
    }
    public enum PayType {
        NONE, WECHAT, ALIPAY, UNIONPAY
    }
    private int id;
    private int userId;
    private int flightId;
    private int seatCount;
    private String seatNumber; // 新增：座位号
    private LocalDateTime orderTime;
    private Status status = Status.PENDING;
    private PayType payType = PayType.NONE;
    private boolean refundRequest = false; // 用户是否提交退票申请
    private boolean rescheduleRequest = false; // 用户是否提交改签申请
    // 用户建议的改签目标（可选）
    private int requestedFlightId = -1; // 建议改签到的航班ID
    private String requestedSeatNumber; // 建议座位号（逗号分隔）
    private String requestReason; // 用户填写的备注/理由

    public Order() {}
    public Order(int userId, int flightId, int seatCount, String seatNumber, LocalDateTime orderTime) {
        this.userId = userId;
        this.flightId = flightId;
        this.seatCount = seatCount;
        this.seatNumber = seatNumber;
        this.orderTime = orderTime;
        this.status = Status.PENDING;
        this.payType = PayType.NONE;
    }

    public boolean isRefundRequest() { return refundRequest; }
    public void setRefundRequest(boolean refundRequest) { this.refundRequest = refundRequest; }
    public boolean isRescheduleRequest() { return rescheduleRequest; }
    public void setRescheduleRequest(boolean rescheduleRequest) { this.rescheduleRequest = rescheduleRequest; }
    public int getRequestedFlightId() { return requestedFlightId; }
    public void setRequestedFlightId(int requestedFlightId) { this.requestedFlightId = requestedFlightId; }
    public String getRequestedSeatNumber() { return requestedSeatNumber; }
    public void setRequestedSeatNumber(String requestedSeatNumber) { this.requestedSeatNumber = requestedSeatNumber; }
    public String getRequestReason() { return requestReason; }
    public void setRequestReason(String requestReason) { this.requestReason = requestReason; }
    // getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getFlightId() { return flightId; }
    public void setFlightId(int flightId) { this.flightId = flightId; }
    public int getSeatCount() { return seatCount; }
    public void setSeatCount(int seatCount) { this.seatCount = seatCount; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public PayType getPayType() { return payType; }
    public void setPayType(PayType payType) { this.payType = payType; }
}
