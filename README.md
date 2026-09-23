# BÁO CÁO MÔ PHỎNG GIAO THỨC 2PC CHO GIAO DỊCH ĐẶT VÉ

## 1. Mô Tả Quy Trình 2PC

Giao thức Two-Phase Commit (2PC) gồm 2 giai đoạn chính:

- **Pha 1: Prepare (Chuẩn bị)**
  - Coordinator gửi yêu cầu `Prepare` đồng thời đến `TrainService` (giữ chỗ vé) và `WalletService` (kiểm tra và khóa số dư).
  - Các service kiểm tra tài nguyên và phản hồi trạng thái: `Ready` (Sẵn sàng) hoặc `Abort` (Thất bại).
- **Pha 2: Decision (Quyết định)**
  - Nếu tất cả service phản hồi `Ready`: Coordinator gửi lệnh `Commit` đến toàn bộ service để chốt giao dịch.
  - Nếu có ít nhất một service phản hồi `Abort`: Coordinator gửi lệnh `Rollback` đến toàn bộ service để hủy và hoàn tác dữ liệu đã khóa/giữ trước đó.

## 2. Dữ Liệu Đầu Vào

```json
{
  "bookingId": "TRAIN-2024-089",
  "trainCode": "SE5",
  "customerWalletId": "W-456",
  "price": 1200000
}
```

## 3. Mã Giả & Logic Hàm Coordinator

```typescript
function coordinator(bookingData):
    // Phase 1: Prepare
    trainStatus = TrainService.prepare(bookingData)
    walletStatus = WalletService.prepare(bookingData)

    // Phase 2: Decision
    if (trainStatus == "Ready" AND walletStatus == "Ready"):
        TrainService.commit(bookingData)
        WalletService.commit(bookingData)
        return "TRANSACTION_COMMITTED"
    else:
        TrainService.rollback(bookingData)
        WalletService.rollback(bookingData)
        return "TRANSACTION_ABORTED"
```

## 4. Kết Quả Chạy Thử Nghiệm

### Trường hợp 1: Cả 2 dịch vụ sẵn sàng (Thành công)

```text
=== PHASE 1: PREPARE ===
[TrainService] Prepare: Giữ chỗ cho chuyến SE5
[WalletService] Prepare: Kiểm tra số dư ví W-456 cho số tiền 1200000

=== PHASE 2: DECISION ===
Decision: COMMIT (Tất cả services đều sẵn sàng)
[TrainService] Commit: Xác nhận đặt chỗ thành công (TRAIN-2024-089)
[WalletService] Commit: Trừ tiền thành công (1200000 VND)
```

### Trường hợp 2: Ví không đủ tiền (Thất bại)

```text
=== PHASE 1: PREPARE ===
[TrainService] Prepare: Giữ chỗ cho chuyến SE5
[WalletService] Prepare: Số dư không đủ -> Trả về Abort

=== PHASE 2: DECISION ===
Decision: ROLLBACK (Có service trả về Abort)
[TrainService] Rollback: Hủy giữ chỗ (TRAIN-2024-089)
[WalletService] Rollback: Hoàn trả / Hủy giao dịch ví
```
