package com.webserver.evrentalsystem.controller.staff;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webserver.evrentalsystem.model.dto.entitydto.RentalCheckDto;
import com.webserver.evrentalsystem.model.dto.entitydto.RentalDto;
import com.webserver.evrentalsystem.model.dto.entitydto.ReservationDto;
import com.webserver.evrentalsystem.model.dto.entitydto.ViolationDto;
import com.webserver.evrentalsystem.model.dto.request.*;
import com.webserver.evrentalsystem.model.dto.response.BillResponse;
import com.webserver.evrentalsystem.service.staff.RentalStaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/staff/rentals")
@Tag(name = "3.3. Staff Rental", description = "API quản lý thuê xe của nhân viên (Staff)")
@SecurityRequirement(name = "bearerAuth")
public class RentalStaffController {

    @Autowired
    private RentalStaffService rentalStaffService;

    @Operation(summary = "Lấy danh sách Reservation theo bộ lọc")
    @GetMapping("/reservations")
    public List<ReservationDto> getReservations(
            @Parameter(description = "ID của renter (người thuê)")
            @RequestParam(required = false) Long renterId,

            @Parameter(description = "ID của vehicle (xe)")
            @RequestParam(required = false) Long vehicleId,

            @Parameter(
                    description = "Trạng thái của Reservation",
                    example = "pending",
                    schema = @Schema(allowableValues = {"pending", "confirmed", "cancelled", "expired"})
            )
            @RequestParam(required = false) String status,

            @Parameter(description = "Thời gian bắt đầu (lọc từ)", example = "2025-09-23T08:00:00")
            @RequestParam(required = false) LocalDateTime startFrom,

            @Parameter(description = "Thời gian bắt đầu (lọc đến)")
            @RequestParam(required = false) LocalDateTime startTo
    ) {
        ReservationFilterRequest filter = new ReservationFilterRequest(
                renterId, vehicleId, status, startFrom, startTo
        );
        return rentalStaffService.getReservations(filter);
    }

    @Operation(
            summary = "Lấy danh sách Rental theo bộ lọc",
            description = "API cho phép staff lấy danh sách rental với các điều kiện lọc theo trạng thái, renter, vehicle, station pickup/return, khoảng thời gian."
    )
    @GetMapping
    public List<RentalDto> getRentals(
            @Parameter(description = "ID của renter (người thuê)")
            @RequestParam(required = false) Long renterId,

            @Parameter(description = "ID của vehicle (xe)")
            @RequestParam(required = false) Long vehicleId,

            @Parameter(description = "ID trạm pickup")
            @RequestParam(required = false) Long stationPickupId,

            @Parameter(description = "ID trạm return")
            @RequestParam(required = false) Long stationReturnId,

            @Parameter(
                    description = "Trạng thái của Rental",
                    example = "in_use",
                    schema = @Schema(allowableValues = {"booked", "in_use", "returned", "waiting_for_payment", "cancelled"})
            )
            @RequestParam(required = false) String status,

            @Parameter(description = "Thời gian bắt đầu (lọc từ)", example = "2025-09-23T08:00:00")
            @RequestParam(required = false) LocalDateTime startFrom,

            @Parameter(description = "Thời gian bắt đầu (lọc đến)", example = "2025-09-23T18:00:00")
            @RequestParam(required = false) LocalDateTime startTo
    ) {
        return rentalStaffService.getRentals(renterId, vehicleId, stationPickupId, stationReturnId, status, startFrom, startTo);
    }

    @Operation(summary = "Check-in nhận xe (booking hoặc walk-in)")
    @PostMapping("/check-in")
    public RentalDto checkIn(@Valid @RequestBody RentalCheckInRequest request) {
        return rentalStaffService.checkIn(request);
    }

    @Operation(summary = "Người dùng huỷ thuê xe")
    @PostMapping("/{id}/cancel")
    public RentalDto cancelRental(
            @Parameter(description = "ID của lượt thuê (rental)")
            @PathVariable Long id) {
        return rentalStaffService.cancelRental(id);
    }

    @Operation(
            summary = "Ghi nhận đặt cọc cho rental",
            description = "API cho phép staff xác nhận đã giữ tiền cọc (deposit) cho một rental."
    )
    @PostMapping("/{id}/hold-deposit")
    public RentalDto holdDeposit(
            @Parameter(description = "ID của rental cần ghi nhận đặt cọc")
            @PathVariable Long id
    ) {
        return rentalStaffService.holdDeposit(id);
    }

    @Operation(
            summary = "Xác nhận đã giao xe cho khách (confirm pickup)",
            description = "API cho phép staff xác nhận giao xe, kèm theo báo cáo tình trạng và 3 file ảnh (photo, chữ ký staff, chữ ký khách)."
    )
    @PostMapping(value = "/confirm-pickup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RentalCheckDto confirmPickup(
            @Parameter(
                    description = "JSON dữ liệu ConfirmRentalRequest (string JSON)",
                    example = "{\"rentalId\":101,\"checkType\":\"pickup\",\"conditionReport\":\"Xe có trầy xước nhẹ\"}"
            )
            @RequestParam("data") String dataJson,   // Nhận JSON dưới dạng String

            @Parameter(description = "Ảnh xe (JPG/PNG)")
            @RequestPart(value = "photo") MultipartFile photo,

            @Parameter(description = "Chữ ký nhân viên")
            @RequestPart(value = "staff_signature") MultipartFile staffSignature,

            @Parameter(description = "Chữ ký khách hàng")
            @RequestPart(value = "customer_signature") MultipartFile customerSignature
    ) throws JsonProcessingException {
        // Parse JSON string sang object
        ConfirmRentalRequest request = new ObjectMapper().readValue(dataJson, ConfirmRentalRequest.class);

        return rentalStaffService.confirmPickup(request, photo, staffSignature, customerSignature);
    }

    @Operation(
            summary = "Xác nhận đã nhận xe từ khách (confirm return)",
            description = "API cho phép staff xác nhận trả xe, kèm theo báo cáo tình trạng và 3 file ảnh (photo, chữ ký staff, chữ ký khách)."
    )
    @PostMapping(value = "/confirm-return", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RentalCheckDto confirmReturn(
            @Parameter(
                    description = "JSON dữ liệu ConfirmRentalRequest (string JSON)",
                    example = "{\"rentalId\":101,\"checkType\":\"return\",\"conditionReport\":\"Xe có trầy xước nhẹ\"}"
            )
            @RequestParam("data") String dataJson,   // Nhận JSON dưới dạng String

            @Parameter(description = "Ảnh xe (JPG/PNG)")
            @RequestPart(value = "photo") MultipartFile photo,

            @Parameter(description = "Chữ ký nhân viên")
            @RequestPart(value = "staff_signature") MultipartFile staffSignature,

            @Parameter(description = "Chữ ký khách hàng")
            @RequestPart(value = "customer_signature") MultipartFile customerSignature
    ) throws JsonProcessingException {
        // Parse JSON string sang object
        ConfirmRentalRequest request = new ObjectMapper().readValue(dataJson, ConfirmRentalRequest.class);

        return rentalStaffService.confirmReturn(request, photo, staffSignature, customerSignature);
    }

    @Operation(
            summary = "Thêm chi phí phát sinh (violation) cho rental",
            description = "API cho phép staff ghi nhận chi phí phát sinh (vi phạm, hư hỏng, phí bổ sung) cho một lượt thuê xe."
    )
    @PostMapping("/add-violation")
    public ViolationDto addViolation(
            @Valid @RequestBody ViolationRequest request
    ) {
        return rentalStaffService.addViolation(request);
    }

    @Operation(
            summary = "Lấy danh sách violation của rental",
            description = "API cho phép staff xem tất cả chi phí phát sinh / vi phạm đã ghi nhận cho một lượt thuê."
    )
    @GetMapping("/{rentalId}/violations")
    public List<ViolationDto> getViolationsByRentalId(
            @Parameter(description = "ID của rental", example = "101")
            @PathVariable Long rentalId
    ) {
        return rentalStaffService.getViolationsByRentalId(rentalId);
    }

    @Operation(
            summary = "Tính tổng bill của rental",
            description = "API cho phép staff tính tổng chi phí (tiền thuê xe + vi phạm) dựa trên thời gian trả."
    )
    @PostMapping("/{rentalId}/bill")
    public BillResponse calculateBill(
            @Parameter(description = "ID của rental", example = "101")
            @PathVariable Long rentalId,
            @Valid @RequestBody BillRequest request
    ) {
        return rentalStaffService.calculateBill(rentalId, request);
    }

    @Operation(
            summary = "Xác nhận đã thanh toán bill",
            description = "API cho phép staff xác nhận một rental đã được thanh toán đầy đủ (bao gồm tiền thuê + violation)."
    )
    @PostMapping("/{rentalId}/payment")
    public void confirmPayment(
            @Parameter(description = "ID của rental", example = "101")
            @PathVariable Long rentalId
    ) {
        rentalStaffService.confirmPayment(rentalId);
    }

    @Operation(
            summary = "Ghi nhận trả cọc cho rental",
            description = "API cho phép staff xác nhận đã trả tiền cọc (deposit) cho một rental."
    )
    @PostMapping("/{id}/return-deposit")
    public RentalDto returnDeposit(
            @Parameter(description = "ID của rental cần ghi nhận trả cọc")
            @PathVariable Long id
    ) {
        return rentalStaffService.returnDeposit(id);
    }
}
