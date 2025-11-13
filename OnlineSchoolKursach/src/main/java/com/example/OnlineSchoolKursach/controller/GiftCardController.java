package com.example.OnlineSchoolKursach.controller;

import com.example.OnlineSchoolKursach.model.GiftCardModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.service.AuthService;
import com.example.OnlineSchoolKursach.service.GiftCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/api/gift-cards")
@Tag(name = "Gift Card Management", description = "API для управления подарочными картами")
public class GiftCardController {

    @Autowired
    private GiftCardService giftCardService;

    @Autowired
    private AuthService authService;

    @GetMapping("/available")
    @Operation(summary = "Получить доступные подарочные карты", description = "Получение списка подарочных карт, доступных для покупки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список подарочных карт успешно получен",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GiftCardModel.class)))
    })
    public ResponseEntity<List<GiftCardModel>> getAvailableGiftCards() {
        List<GiftCardModel> giftCards = giftCardService.getAllAvailableGiftCards();
        return ResponseEntity.ok(giftCards);
    }

    @GetMapping("/my")
    @Operation(summary = "Получить мои подарочные карты", description = "Получение списка подарочных карт текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список подарочных карт успешно получен",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GiftCardModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<List<GiftCardModel>> getMyGiftCards(
            @Parameter(description = "Данные аутентификации")
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            List<GiftCardModel> giftCards = giftCardService.getUserGiftCards(user);
            return ResponseEntity.ok(giftCards);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/purchase")
    @Operation(summary = "Купить подарочную карту", description = "Покупка подарочной карты на указанную сумму")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Подарочная карта успешно куплена",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GiftCardModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<?> purchaseGiftCard(
            @Parameter(description = "Данные покупки")
            @RequestBody Map<String, Object> purchaseData,
            @Parameter(description = "Данные аутентификации")
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            
            Object amountObj = purchaseData.get("amount");
            if (amountObj == null) {
                return ResponseEntity.badRequest().body("Сумма не указана");
            }
            
            BigDecimal amount;
            if (amountObj instanceof Number) {
                amount = BigDecimal.valueOf(((Number) amountObj).doubleValue());
            } else if (amountObj instanceof String) {
                amount = new BigDecimal((String) amountObj);
            } else {
                return ResponseEntity.badRequest().body("Неверный формат суммы");
            }
            
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("Сумма должна быть больше нуля");
            }
            
            GiftCardModel giftCard = giftCardService.purchaseGiftCard(user, amount);
            return ResponseEntity.ok(giftCard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при покупке подарочной карты: " + e.getMessage());
        }
    }

    @PostMapping("/purchase/course/{courseId}")
    @Operation(summary = "Купить подарочную карту на курс", description = "Покупка подарочной карты на сумму равную цене курса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Подарочная карта успешно куплена",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GiftCardModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<?> purchaseGiftCardForCourse(
            @Parameter(description = "Идентификатор курса")
            @PathVariable Long courseId,
            @Parameter(description = "Данные аутентификации")
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            GiftCardModel giftCard = giftCardService.purchaseGiftCardForCourse(user, courseId);
            return ResponseEntity.ok(giftCard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при покупке подарочной карты: " + e.getMessage());
        }
    }

    @PostMapping("/use")
    @Operation(summary = "Использовать подарочную карту", description = "Использование подарочной карты для оплаты курса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Подарочная карта успешно использована",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GiftCardModel.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка в запросе")
    })
    public ResponseEntity<?> useGiftCard(
            @Parameter(description = "Данные использования карты")
            @RequestBody Map<String, Object> useData,
            @Parameter(description = "Данные аутентификации")
            Authentication authentication) {
        try {
            UserModel user = authService.getUserByEmail(authentication.getName());
            
            Object codeObj = useData.get("code");
            Object courseIdObj = useData.get("courseId");
            
            if (codeObj == null || courseIdObj == null) {
                return ResponseEntity.badRequest().body("Код карты и ID курса обязательны");
            }
            
            String code = codeObj.toString();
            Long courseId;
            if (courseIdObj instanceof Number) {
                courseId = ((Number) courseIdObj).longValue();
            } else if (courseIdObj instanceof String) {
                courseId = Long.parseLong((String) courseIdObj);
            } else {
                return ResponseEntity.badRequest().body("Неверный формат ID курса");
            }
            
            GiftCardModel giftCard = giftCardService.useGiftCard(code, courseId, user);
            return ResponseEntity.ok(giftCard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при использовании подарочной карты: " + e.getMessage());
        }
    }

    @GetMapping("/{giftCardId}")
    @Operation(summary = "Получить подарочную карту по ID", description = "Получение информации о подарочной карте")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Подарочная карта успешно получена",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GiftCardModel.class))),
            @ApiResponse(responseCode = "404", description = "Подарочная карта не найдена")
    })
    public ResponseEntity<GiftCardModel> getGiftCardById(
            @Parameter(description = "Идентификатор подарочной карты")
            @PathVariable Long giftCardId) {
        try {
            GiftCardModel giftCard = giftCardService.getGiftCardById(giftCardId);
            return ResponseEntity.ok(giftCard);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}

