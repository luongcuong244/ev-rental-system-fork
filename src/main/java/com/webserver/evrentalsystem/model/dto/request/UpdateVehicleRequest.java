package com.webserver.evrentalsystem.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVehicleRequest {

    @Schema(description = "Biển số xe", example = "30A-12345")
    private String licensePlate;

    @Schema(description = "Loại xe", example = "motorbike", allowableValues = {"car", "motorbike"})
    private String type;

    @Schema(description = "Hãng xe", example = "Honda")
    private String brand;

    @Schema(description = "Mẫu xe", example = "Air Blade")
    private String model;

    @Schema(description = "Dung lượng pin (kWh)", example = "20")
    private Integer capacity;

    @Schema(description = "Quãng đường di chuyển trên mỗi lần sạc đầy (km)", example = "250")
    private Integer rangePerFullCharge;

    @Schema(description = "Trạng thái xe", example = "available", allowableValues = {"reserved", "available", "reserved", "maintenance", "rented"})
    private String status;

    @Schema(description = "Giá thuê mỗi giờ", example = "50000")
    private BigDecimal pricePerHour;

    @Schema(description = "ID trạm", example = "2")
    private Long stationId;

    @Schema(description = "Ảnh xe")
    private MultipartFile image;

    @Schema(description = "Loại pin", example = "Lithium")
    private String batteryType;

    @Range(min = 0, max = 100, message = "Phần trăm pin không hợp lệ. Vui lòng nhập giá trị từ 0 đến 100")
    @Schema(description = "Phần trăm pin", example = "85")
    private Integer batteryLevel;

    @Positive(message = "Odometer vui lòng nhập giá trị >= 0")
    @Schema(description = "Odometer (km)", example = "100000")
    private Integer odo;

    @Positive(message = "Số chỗ ngồi vui lòng nhập giá trị >= 0")
    @Schema(description = "Số chỗ ngồi", example = "5")
    private Integer numberSeat;

}
