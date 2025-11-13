package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.GiftCardModel;
import com.example.OnlineSchoolKursach.model.GiftCardStatusModel;
import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.model.CheckModel;
import com.example.OnlineSchoolKursach.model.CourseModel;
import com.example.OnlineSchoolKursach.model.EnrollmentModel;
import com.example.OnlineSchoolKursach.repository.GiftCardRepository;
import com.example.OnlineSchoolKursach.repository.GiftCardStatusRepository;
import com.example.OnlineSchoolKursach.repository.CheckRepository;
import com.example.OnlineSchoolKursach.repository.PaymentStatusRepository;
import com.example.OnlineSchoolKursach.repository.CourseRepository;
import com.example.OnlineSchoolKursach.repository.EnrollmentRepository;
import com.example.OnlineSchoolKursach.model.PaymentStatusModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class GiftCardService {

    @Autowired
    private GiftCardRepository giftCardRepository;

    @Autowired
    private GiftCardStatusRepository giftCardStatusRepository;

    @Autowired
    private CheckRepository checkRepository;

    @Autowired
    private PaymentStatusRepository paymentStatusRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public List<GiftCardModel> getAllAvailableGiftCards() {
        // Get all gift cards that are available for purchase (not yet purchased)
        List<GiftCardStatusModel> statuses = giftCardStatusRepository.findAll();
        GiftCardStatusModel availableStatus = statuses.stream()
                .filter(s -> "Доступна".equals(s.getStatusName()) || "Available".equals(s.getStatusName()))
                .findFirst()
                .orElse(null);
        
        if (availableStatus != null) {
            return giftCardRepository.findByGiftCardStatusGiftCardStatusId(availableStatus.getGiftCardStatusId());
        }
        return List.of();
    }

    public List<GiftCardModel> getUserGiftCards(UserModel user) {
        return giftCardRepository.findByUserUserId(user.getUserId());
    }

    @Transactional
    public GiftCardModel purchaseGiftCard(UserModel user, BigDecimal amount) {
        // Generate unique card number and code
        String cardNumber = "GC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String code = UUID.randomUUID().toString().substring(0, 12).toUpperCase(); // Unique code for using when purchasing course
        
        // Find or create "Активна" status
        GiftCardStatusModel activeStatus = findOrCreateGiftCardStatus("Активна");
        
        // Create gift card
        GiftCardModel giftCard = new GiftCardModel();
        giftCard.setCardNumber(cardNumber);
        giftCard.setCode(code); // Set code for gift card
        giftCard.setBalance(amount);
        giftCard.setAmount(amount); // Set amount for gift card
        giftCard.setIssueDate(LocalDate.now());
        giftCard.setExpiryDate(LocalDate.now().plusYears(1)); // Valid for 1 year
        giftCard.setUser(user);
        giftCard.setGiftCardStatus(activeStatus);
        
        GiftCardModel savedCard = giftCardRepository.save(giftCard);
        
        // Create check for the purchase (without course)
        createCheckForGiftCard(user, amount);
        
        return savedCard;
    }

    private void createCheckForGiftCard(UserModel user, BigDecimal amount) {
        // Find or create payment status "Оплачено"
        PaymentStatusModel paidStatus = findOrCreatePaymentStatus("Оплачено");
        
        CheckModel check = new CheckModel();
        check.setUser(user);
        check.setCourse(null); // No course for gift card purchase
        check.setAmount(amount);
        check.setPaymentDate(LocalDate.now());
        check.setPaymentStatus(paidStatus);
        
        checkRepository.save(check);
    }

    private PaymentStatusModel findOrCreatePaymentStatus(String statusName) {
        List<PaymentStatusModel> statuses = paymentStatusRepository.findAll();
        for (PaymentStatusModel status : statuses) {
            if (statusName.equals(status.getStatusName())) {
                return status;
            }
        }
        
        // If not found, create it
        PaymentStatusModel newStatus = new PaymentStatusModel();
        newStatus.setStatusName(statusName);
        return paymentStatusRepository.save(newStatus);
    }

    private GiftCardStatusModel findOrCreateGiftCardStatus(String statusName) {
        List<GiftCardStatusModel> statuses = giftCardStatusRepository.findAll();
        for (GiftCardStatusModel status : statuses) {
            if (statusName.equals(status.getStatusName())) {
                return status;
            }
        }
        
        // If not found, create it
        GiftCardStatusModel newStatus = new GiftCardStatusModel();
        newStatus.setStatusName(statusName);
        return giftCardStatusRepository.save(newStatus);
    }

    @Transactional
    public GiftCardModel purchaseGiftCardForCourse(UserModel user, Long courseId) {
        // Get course
        CourseModel course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));
        
        // Check if course has started
        if (course.getStartDate() != null && !LocalDate.now().isBefore(course.getStartDate())) {
            throw new RuntimeException("Нельзя купить подарочную карту на курс, который уже начался");
        }
        
        // Check if course is full
        String statusName = course.getCourseStatus() != null ? course.getCourseStatus().getStatusName() : null;
        if ("Заполнен".equals(statusName)) {
            throw new RuntimeException("Нельзя купить подарочную карту на курс, который полностью заполнен");
        }
        
        // Check course capacity
        if (course.getCapacity() != null && course.getCapacity() > 0) {
            int currentEnrollments = enrollmentRepository.findByCourseCourseId(courseId).size();
            if (currentEnrollments >= course.getCapacity()) {
                throw new RuntimeException("Нельзя купить подарочную карту на курс, который полностью заполнен. Все места заняты.");
            }
        }
        
        // Get course price
        BigDecimal amount = course.getPrice();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Курс не имеет цены или цена равна нулю");
        }
        
        // Generate unique card number and code
        String cardNumber = "GC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String code = UUID.randomUUID().toString().substring(0, 12).toUpperCase(); // Unique code for using when purchasing course
        
        // Find or create "Активна" status
        GiftCardStatusModel activeStatus = findOrCreateGiftCardStatus("Активна");
        
        // Create gift card
        GiftCardModel giftCard = new GiftCardModel();
        giftCard.setCardNumber(cardNumber);
        giftCard.setCode(code); // Set code for gift card
        giftCard.setBalance(amount);
        giftCard.setAmount(amount); // Set amount for gift card
        giftCard.setIssueDate(LocalDate.now());
        giftCard.setExpiryDate(LocalDate.now().plusYears(1)); // Valid for 1 year
        giftCard.setUser(user);
        giftCard.setCourse(course); // Set course for gift card
        giftCard.setGiftCardStatus(activeStatus);
        
        GiftCardModel savedCard = giftCardRepository.save(giftCard);
        
        // Create check for the purchase (with course reference)
        createCheckForGiftCardCourse(user, course, amount);
        
        return savedCard;
    }

    private void createCheckForGiftCardCourse(UserModel user, CourseModel course, BigDecimal amount) {
        // Find or create payment status "Оплачено"
        PaymentStatusModel paidStatus = findOrCreatePaymentStatus("Оплачено");
        
        CheckModel check = new CheckModel();
        check.setUser(user);
        check.setCourse(course); // Set course for gift card purchase
        check.setAmount(amount);
        check.setPaymentDate(LocalDate.now());
        check.setPaymentStatus(paidStatus);
        
        checkRepository.save(check);
    }

    public GiftCardModel getGiftCardById(Long giftCardId) {
        return giftCardRepository.findById(giftCardId)
                .orElseThrow(() -> new RuntimeException("Подарочная карта не найдена"));
    }

    @Transactional
    public GiftCardModel useGiftCard(String code, Long courseId, UserModel user) {
        // Find gift card by code with all details
        GiftCardModel giftCard = giftCardRepository.findByCodeWithDetails(code);
        if (giftCard == null) {
            throw new RuntimeException("Подарочная карта с таким кодом не найдена");
        }

        // Check if card is active
        if (giftCard.getGiftCardStatus() == null) {
            throw new RuntimeException("Статус подарочной карты не определен");
        }
        
        String statusName = giftCard.getGiftCardStatus().getStatusName();
        if (!"Активна".equals(statusName)) {
            throw new RuntimeException("Подарочная карта неактивна или уже использована. Текущий статус: " + statusName);
        }

        // Check if card is expired
        if (giftCard.getExpiryDate() != null && LocalDate.now().isAfter(giftCard.getExpiryDate())) {
            throw new RuntimeException("Срок действия подарочной карты истек");
        }

        // Check if card is for the correct course
        if (giftCard.getCourse() == null) {
            throw new RuntimeException("Подарочная карта не привязана к курсу");
        }
        
        if (!giftCard.getCourse().getCourseId().equals(courseId)) {
            String courseName = giftCard.getCourse().getTitle() != null ? giftCard.getCourse().getTitle() : "ID: " + giftCard.getCourse().getCourseId();
            throw new RuntimeException("Подарочная карта предназначена для другого курса: " + courseName);
        }

        // Get course
        CourseModel course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        // Check if card balance is sufficient
        if (giftCard.getBalance() == null || course.getPrice() == null) {
            throw new RuntimeException("Ошибка: баланс карты или цена курса не определены");
        }
        
        if (giftCard.getBalance().compareTo(course.getPrice()) < 0) {
            throw new RuntimeException("Недостаточно средств на подарочной карте. Баланс: " + giftCard.getBalance() + " руб., требуется: " + course.getPrice() + " руб.");
        }

        // Update card status to "Использована"
        GiftCardStatusModel usedStatus = findOrCreateGiftCardStatus("Использована");
        
        // Ensure the status is persisted before using it
        if (usedStatus.getGiftCardStatusId() == null) {
            usedStatus = giftCardStatusRepository.save(usedStatus);
        }
        
        giftCard.setGiftCardStatus(usedStatus);
        giftCard.setBalance(BigDecimal.ZERO); // Set balance to zero after use
        
        // Flush to ensure all changes are persisted
        GiftCardModel savedCard = giftCardRepository.saveAndFlush(giftCard);
        
        // Note: Enrollment and check creation should be handled by the caller
        // The check was already created when the gift card was purchased
        
        return savedCard;
    }
}

