package com.example.OnlineSchoolKursach.service;

import com.example.OnlineSchoolKursach.model.UserModel;
import com.example.OnlineSchoolKursach.model.UserSettingsModel;
import com.example.OnlineSchoolKursach.repository.UserRepository;
import com.example.OnlineSchoolKursach.repository.UserSettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserSettingsService {

    private static final Logger logger = LoggerFactory.getLogger(UserSettingsService.class);

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public UserSettingsModel getOrCreateSettings(Long userId) {
        Optional<UserSettingsModel> settingsOpt = userSettingsRepository.findByUserUserId(userId);
        if (settingsOpt.isPresent()) {
            return settingsOpt.get();
        }

        UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        UserSettingsModel settings = new UserSettingsModel(user);
        return userSettingsRepository.save(settings);
    }

    @Transactional
    public UserSettingsModel saveSettings(Long userId, Map<String, Object> settingsData) {
        UserSettingsModel settings = getOrCreateSettings(userId);

        if (settingsData.containsKey("theme")) {
            String theme = (String) settingsData.get("theme");
            if ("light".equals(theme) || "dark".equals(theme)) {
                settings.setTheme(theme);
            }
        }

        if (settingsData.containsKey("itemsPerPage")) {
            Object itemsPerPageObj = settingsData.get("itemsPerPage");
            if (itemsPerPageObj instanceof Number) {
                int itemsPerPage = ((Number) itemsPerPageObj).intValue();
                if (itemsPerPage > 0 && itemsPerPage <= 100) {
                    settings.setItemsPerPage(itemsPerPage);
                }
            }
        }

        if (settingsData.containsKey("dateFormat")) {
            settings.setDateFormat((String) settingsData.get("dateFormat"));
        }

        if (settingsData.containsKey("savedFilters")) {
            Object filters = settingsData.get("savedFilters");
            if (filters instanceof String) {
                settings.setSavedFilters((String) filters);
            } else {
                settings.setSavedFilters(filters.toString());
            }
        }

        return userSettingsRepository.save(settings);
    }

    public Map<String, Object> getSettingsAsMap(Long userId) {
        UserSettingsModel settings = getOrCreateSettings(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("theme", settings.getTheme());
        result.put("itemsPerPage", settings.getItemsPerPage());
        result.put("dateFormat", settings.getDateFormat());
        result.put("savedFilters", settings.getSavedFilters());
        return result;
    }
}