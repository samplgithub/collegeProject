import java.util.Scanner;
import java.util.regex.Pattern;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

class Vehicle {
    String licensePlate;
    String type;
    long entryTime;
    long exitTime;

    public Vehicle(String licensePlate, String type) {
        this.licensePlate=licensePlate;
        this.type=type;
        new SectionAdbConnection(licensePlate,type);
        //this.type = type;
        this.entryTime = 0;
        this.exitTime = 0;


    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getType() {
        return type;
    }

    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }

    public void setExitTime(long exitTime) {
        this.exitTime = exitTime;
    }

    public long getEntryTime() {
        return entryTime;
    }

    public long getExitTime() {
        return exitTime;
    }

    public double calculateParkingCharge(double hourlyRate) {
        long duration = (exitTime - entryTime) / 1000; // Convert milliseconds to hours
        return duration * hourlyRate;
    }
}
class ParkingLot {
    Vehicle[] parkingSpaces;
    int capacity;
    int occupiedSpaces;

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        this.parkingSpaces = new Vehicle[capacity];
        this.occupiedSpaces = 0;
    }

    public boolean addVehicle(Vehicle vehicle) {
        if (occupiedSpaces < capacity) {
            for (int i = 0; i < capacity; i++) {
                if (parkingSpaces[i] == null) {
                    parkingSpaces[i] = vehicle;
                    occupiedSpaces++;
                    System.out.println("Vehicle parked successfully.");
                    return true;
                }
            }
        }
        System.out.println("Parking lot is full. Unable to park the vehicle.");
        return false;
    }

    public boolean removeVehicle(String licensePlate) {
        for (int i = 0; i < capacity; i++) {
            if (parkingSpaces[i] != null && parkingSpaces[i].getLicensePlate().equals(licensePlate)) {
                parkingSpaces[i] = null;
                occupiedSpaces--;
                System.out.println("Vehicle removed successfully.");
                return true;
            }
        }
        System.out.println("Vehicle not found in the parking lot.");
        return false;
    }

    public Vehicle getVehicle(String licensePlate) {
        for (int i = 0; i < capacity; i++) {
            if (parkingSpaces[i] != null && parkingSpaces[i].getLicensePlate().equals(licensePlate)) {
                return parkingSpaces[i];
            }
        }
        return null;
    }

    public void displayStatus() {
        System.out.println("Parking Lot Status:");
        for (int i = 0; i < capacity; i++) {
            System.out.print("Space " + (i + 1) + ": ");
            if (parkingSpaces[i] != null) {
                System.out.println(parkingSpaces[i].getLicensePlate() + " - " + parkingSpaces[i].getType());
            } else {
                System.out.println("Empty");
            }
        }
    }
}
class SectionAdbConnection {
    Connection con;
    Statement stm;
    ResultSet rst;
    SectionAdbConnection(String Vehicleplate,String type){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver"); // driver register
            con= DriverManager.getConnection("jdbc:mysql://localhost:3306/vehicleparking","root","");
            stm=con.createStatement();

            String query= "CREATE DATABASE IF NOT EXISTS vehicleparking"; // statement
            stm.executeUpdate(query);

            String tbl_query = "CREATE TABLE IF NOT EXISTS vehicle_info(ID INT PRIMARY KEY AUTO_INCREMENT,plate_no VARCHAR(30),type VARCHAR(30))";
            stm.executeUpdate(tbl_query);

            String info_query="INSERT INTO vehicle_info(plate_no,type) VALUES('"+Vehicleplate+"','"+type+"')";
            stm.executeUpdate(info_query);

            /*String r_query ="SELECT * FROM vehicle_info";
            rst= stm.executeQuery(r_query);
            int i=1;
            while(rst.next()){
                System.out.println("Record: "+i);
                System.out.println("Vehicle Id: "+rst.getString("ID"));
                System.out.println("License Plate no: "+rst.getString("plate_no"));
                System.out.println("Vehicle Type: "+rst.getString("type"));
            }
            */

           // System.out.println("Vehicle data insert successfully!");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

}


public class ParkingManagementSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ParkingLot parkingLot = new ParkingLot(10);

        boolean loggedIn = false; // Track operator login status

        while (!loggedIn) {
            System.out.print("Enter operator name: ");
            String operatorName = scanner.nextLine();
            System.out.print("Enter operator Password: ");
            String operatorId = scanner.nextLine();


            if (validateOperator(operatorId, operatorName)) {
                loggedIn = true;
                System.out.println("Login successful. Welcome, " + operatorName + "!");
            } else {
                System.out.println("Login failed. Invalid operator ID or name. Please try again.");
            }
        }

        while (true) {
            System.out.println("\n---- Vehicle Parking Management System ----");
            System.out.println("1. Add Vehicle");
            System.out.println("2. Remove Vehicle");
            System.out.println("3. Display Parking Lot Status");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            switch (choice) {
                case 1:
                    String licensePlate;
                    do {
                        System.out.print("Enter vehicle license plate: ");
                        licensePlate = scanner.nextLine();

                        if (!isValidLicensePlate(licensePlate)) {
                            System.out.println("Invalid license plate format. Please try again.");
                        }
                    } while (!isValidLicensePlate(licensePlate));

                    System.out.print("Enter vehicle type (car/motorcycle): ");
                    String type = scanner.nextLine().toLowerCase();

                    if (type.equals("car") || type.equals("motorcycle")) {
                        Vehicle vehicle = new Vehicle(licensePlate, type);
                        vehicle.setEntryTime(System.currentTimeMillis());
                        parkingLot.addVehicle(vehicle);
                    } else {
                        System.out.println("Invalid vehicle type. Please try again.");
                    }
                    break;
                // ... (unchanged)
                case 2:
                    System.out.print("Enter vehicle license plate: ");
                    licensePlate = scanner.nextLine();
                    Vehicle exitingVehicle = parkingLot.getVehicle(licensePlate);
                    if (exitingVehicle != null) {
                        exitingVehicle.setExitTime(System.currentTimeMillis());
                        double parkingCharge = exitingVehicle.calculateParkingCharge(500.0); // Assuming hourly rate is $5.0
                        System.out.println("Parking Charge: Rs." + parkingCharge);
                        parkingLot.removeVehicle(licensePlate);
                    } else {
                        System.out.println("Vehicle not found in the parking lot.");
                    }
                    break;
                case 3:
                    parkingLot.displayStatus();
                    break;
                case 4:
                    System.out.println("Exiting the System.");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

    }
    public static boolean validateOperator(String operatorId, String operatorName) {
        if(operatorId.equals("mohan@123")&& operatorName.equals("mohan")|| operatorId.equals("456")&&operatorName
                .equals("sandip")){
            return true;
        }else {
            return false;
        }
    }

    public static boolean isValidLicensePlate(String licensePlate) {
        String pattern = "[a-zA-Z]{2}+[0-9]{2}+[a-zA-Z]{2}+[0-9]{4}";
        return Pattern.matches(pattern, licensePlate);
    }
}