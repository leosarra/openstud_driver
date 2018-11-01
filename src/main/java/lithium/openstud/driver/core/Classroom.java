package lithium.openstud.driver.core;

public class Classroom {
    private double latitude;
    private double longitude;
    private String where;
    private String name;
    private String fullName;
    private int internalId;
    private String roomId;
    private boolean occupied;
    private boolean willBeOccupied;
    private int weight;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getInternalId() {
        return internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public boolean isWillBeOccupied() {
        return willBeOccupied;
    }

    public void setWillBeOccupied(boolean willBeOccupied) {
        this.willBeOccupied = willBeOccupied;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Classroom{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", where='" + where + '\'' +
                ", name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", internalId=" + internalId +
                ", roomId='" + roomId + '\'' +
                ", occupied=" + occupied +
                ", willBeOccupied=" + willBeOccupied +
                ", weight=" + weight +
                '}';
    }
}
