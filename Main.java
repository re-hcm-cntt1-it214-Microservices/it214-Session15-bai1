import java.util.Arrays;
import java.util.List;

class BookingData {
    String bookingId;
    String trainCode;
    String customerWalletId;
    long price;

    public BookingData(String bookingId, String trainCode, String customerWalletId, long price) {
        this.bookingId = bookingId;
        this.trainCode = trainCode;
        this.customerWalletId = customerWalletId;
        this.price = price;
    }
}

interface ParticipantService {
    boolean prepare(BookingData booking);
    void commit(BookingData booking);
    void rollback(BookingData booking);
}

class TrainService implements ParticipantService {
    @Override
    public boolean prepare(BookingData booking) {
        System.out.println("[TrainService] Prepare: Giu cho cho chuyen " + booking.trainCode);
        return true;
    }

    @Override
    public void commit(BookingData booking) {
        System.out.println("[TrainService] Commit: Xac nhan dat cho thanh cong (" + booking.bookingId + ")");
    }

    @Override
    public void rollback(BookingData booking) {
        System.out.println("[TrainService] Rollback: Huy giu cho (" + booking.bookingId + ")");
    }
}

class WalletService implements ParticipantService {
    @Override
    public boolean prepare(BookingData booking) {
        System.out.println("[WalletService] Prepare: Kiem tra so du vi " + booking.customerWalletId + " cho so tien " + booking.price);
        return true;
    }

    @Override
    public void commit(BookingData booking) {
        System.out.println("[WalletService] Commit: Tru tien thanh cong (" + booking.price + " VND)");
    }

    @Override
    public void rollback(BookingData booking) {
        System.out.println("[WalletService] Rollback: Hoan tra / Huy giao dich vi");
    }
}

public class Main {
    public static String coordinator(BookingData booking) {
        List<ParticipantService> participants = Arrays.asList(new TrainService(), new WalletService());

        System.out.println("=== PHASE 1: PREPARE ===");
        boolean allReady = true;
        for (ParticipantService service : participants) {
            if (!service.prepare(booking)) {
                allReady = false;
                break;
            }
        }

        System.out.println("\n=== PHASE 2: DECISION ===");
        if (allReady) {
            System.out.println("Decision: COMMIT (Tat ca services deu san sang)");
            for (ParticipantService service : participants) {
                service.commit(booking);
            }
            return "TRANSACTION_COMMITTED";
        } else {
            System.out.println("Decision: ROLLBACK (Co service tra ve Abort)");
            for (ParticipantService service : participants) {
                service.rollback(booking);
            }
            return "TRANSACTION_ABORTED";
        }
    }

    public static void main(String[] args) {
        BookingData booking = new BookingData("TRAIN-2024-089", "SE5", "W-456", 1200000L);
        coordinator(booking);
    }
}
