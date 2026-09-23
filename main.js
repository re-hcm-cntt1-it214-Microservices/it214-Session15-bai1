const bookingData = {
  bookingId: "TRAIN-2024-089",
  trainCode: "SE5",
  customerWalletId: "W-456",
  price: 1200000
};

class TrainService {
  prepare(booking) {
    console.log(`[TrainService] Prepare: Giữ chỗ cho chuyến ${booking.trainCode}`);
    return { status: "Ready" };
  }
  commit(booking) {
    console.log(`[TrainService] Commit: Xác nhận đặt chỗ thành công (${booking.bookingId})`);
  }
  rollback(booking) {
    console.log(`[TrainService] Rollback: Hủy giữ chỗ (${booking.bookingId})`);
  }
}

class WalletService {
  prepare(booking) {
    console.log(`[WalletService] Prepare: Kiểm tra số dư ví ${booking.customerWalletId} cho số tiền ${booking.price}`);
    return { status: "Ready" };
  }
  commit(booking) {
    console.log(`[WalletService] Commit: Trừ tiền thành công (${booking.price} VND)`);
  }
  rollback(booking) {
    console.log(`[WalletService] Rollback: Hoàn trả / Hủy giao dịch ví`);
  }
}

function coordinator(booking) {
  const trainService = new TrainService();
  const walletService = new WalletService();
  const participants = [trainService, walletService];

  console.log("=== PHASE 1: PREPARE ===");
  const trainRes = trainService.prepare(booking);
  const walletRes = walletService.prepare(booking);

  const allReady = trainRes.status === "Ready" && walletRes.status === "Ready";

  console.log("\n=== PHASE 2: DECISION ===");
  if (allReady) {
    console.log("Decision: COMMIT (Tất cả services đều sẵn sàng)");
    participants.forEach(service => service.commit(booking));
    return "TRANSACTION_COMMITTED";
  } else {
    console.log("Decision: ROLLBACK (Có service trả về Abort)");
    participants.forEach(service => service.rollback(booking));
    return "TRANSACTION_ABORTED";
  }
}

coordinator(bookingData);
