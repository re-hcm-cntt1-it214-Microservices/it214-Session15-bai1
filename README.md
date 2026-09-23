# BÁO CÁO MÔ PHỎNG GIAO THỨC 2PC CHO GIAO DỊCH ĐẶT VÉ

## 1. Mô Tả Quy Trình 2PC

Giao thức Two-Phase Commit (2PC) gồm 2 giai đoạn chính:

- **Pha 1: Prepare (Chuẩn bị)**
  - Coordinator gửi yêu cầu `prepare()` đến `TrainService` (giữ chỗ vé) và `WalletService` (kiểm tra và khóa số dư).
  - Các service kiểm tra tài nguyên và phản hồi trạng thái: `true` (Ready/Sẵn sàng) hoặc `false` (Abort/Thất bại).
- **Pha 2: Decision (Quyết định)**
  - Nếu tất cả service phản hồi `true`: Coordinator gửi lệnh `commit()` đến toàn bộ service để chốt giao dịch.
  - Nếu có ít nhất một service phản hồi `false`: Coordinator gửi lệnh `rollback()` đến toàn bộ service để hủy và hoàn tác dữ liệu đã khóa/giữ trước đó.

## 2. Dữ Liệu Đầu Vào

```json
{
  "bookingId": "TRAIN-2024-089",
  "trainCode": "SE5",
  "customerWalletId": "W-456",
  "price": 1200000
}
```

## 3. Mã Giả & Logic Hàm Coordinator (Java)

```java
public static String coordinator(BookingData booking) {
    List<ParticipantService> participants = Arrays.asList(new TrainService(), new WalletService());

    // Phase 1: Prepare
    boolean allReady = true;
    for (ParticipantService service : participants) {
        if (!service.prepare(booking)) {
            allReady = false;
            break;
        }
    }

    // Phase 2: Decision
    if (allReady) {
        for (ParticipantService service : participants) {
            service.commit(booking);
        }
        return "TRANSACTION_COMMITTED";
    } else {
        for (ParticipantService service : participants) {
            service.rollback(booking);
        }
        return "TRANSACTION_ABORTED";
    }
}
```

## 4. Kết Quả Chạy Thử Nghiệm

### Trường hợp 1: Cả 2 dịch vụ sẵn sàng (Thành công)

```text
=== PHASE 1: PREPARE ===
[TrainService] Prepare: Giu cho cho chuyen SE5
[WalletService] Prepare: Kiem tra so du vi W-456 cho so tien 1200000

=== PHASE 2: DECISION ===
Decision: COMMIT (Tat ca services deu san sang)
[TrainService] Commit: Xac nhan dat cho thanh cong (TRAIN-2024-089)
[WalletService] Commit: Tru tien thanh cong (1200000 VND)
```

### Trường hợp 2: Ví không đủ tiền (Thất bại)

```text
=== PHASE 1: PREPARE ===
[TrainService] Prepare: Giu cho cho chuyen SE5
[WalletService] Prepare: So du khong du -> Tra ve Abort

=== PHASE 2: DECISION ===
Decision: ROLLBACK (Co service tra ve Abort)
[TrainService] Rollback: Huy giu cho (TRAIN-2024-089)
[WalletService] Rollback: Hoan tra / Huy giao dich vi
```
