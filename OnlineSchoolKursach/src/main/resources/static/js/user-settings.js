let userSettings = {
    theme: 'light',
    itemsPerPage: 10,
    dateFormat: 'dd.MM.yyyy',
    savedFilters: null
};

function applySettings() {
    console.log('applySettings called, theme:', userSettings.theme);
    if (userSettings.theme === 'dark') {
        document.body.classList.add('dark-theme');
        document.documentElement.setAttribute('data-theme', 'dark');
        console.log('Dark theme applied');
    } else {
        document.body.classList.remove('dark-theme');
        document.documentElement.setAttribute('data-theme', 'light');
        console.log('Light theme applied');
    }

    const themeToggle = document.getElementById('themeToggle');
    const themeToggleFloat = document.getElementById('themeToggleFloat');
    if (themeToggle) {
        themeToggle.innerHTML = userSettings.theme === 'dark' ? '☀️' : '🌙';
    }
    if (themeToggleFloat) {
        themeToggleFloat.innerHTML = userSettings.theme === 'dark' ? '☀️' : '🌙';
    }

    if (userSettings.itemsPerPage) {
        const itemsPerPageSelect = document.getElementById('itemsPerPage');
        if (itemsPerPageSelect) {
            itemsPerPageSelect.value = userSettings.itemsPerPage;
            console.log('Items per page applied:', userSettings.itemsPerPage);
        }
        
        if (typeof window !== 'undefined' && window.itemsPerPage !== undefined) {
            window.itemsPerPage = parseInt(userSettings.itemsPerPage);
        }
    }

    if (userSettings.savedFilters) {
        try {
            const filters = JSON.parse(userSettings.savedFilters);
            console.log('Saved filters found:', filters);
        } catch (e) {
            console.error('Error parsing saved filters:', e);
        }
    }
}

async function saveUserSettings() {
    try {
        const token = localStorage.getItem('token');
        if (!token) return;

        const response = await fetch('/v1/api/settings', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userSettings)
        });

        if (!response.ok) {
            console.error('Error saving user settings');
        }
    } catch (error) {
        console.error('Error saving user settings:', error);
    }
}

function toggleTheme() {
    console.log('toggleTheme called, current theme:', userSettings.theme);
    userSettings.theme = userSettings.theme === 'light' ? 'dark' : 'light';
    console.log('new theme:', userSettings.theme);
    
    localStorage.setItem('userTheme', userSettings.theme);
    
    applySettings();
    
    saveUserSettings();
}

window.toggleTheme = toggleTheme;

(function() {
    function init() {
        const savedTheme = localStorage.getItem('userTheme');
        if (savedTheme === 'dark') {
            userSettings.theme = 'dark';
            document.body.classList.add('dark-theme');
            document.documentElement.setAttribute('data-theme', 'dark');
        }
        
        const savedItemsPerPage = localStorage.getItem('userItemsPerPage');
        if (savedItemsPerPage) {
            userSettings.itemsPerPage = parseInt(savedItemsPerPage);
        }
        
        const savedFilters = localStorage.getItem('userSavedFilters');
        if (savedFilters) {
            try {
                userSettings.savedFilters = savedFilters;
            } catch (e) {
                console.error('Error loading saved filters from localStorage:', e);
            }
        }
        
        function setupThemeToggle() {
            const themeToggle = document.getElementById('themeToggle');
            if (themeToggle) {
                themeToggle.onclick = null;
                themeToggle.addEventListener('click', function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    toggleTheme();
                });
                console.log('Theme toggle button found and handler attached');
            } else {
                console.log('Theme toggle button not found');
            }
        }
        
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', function() {
                setupThemeToggle();
                loadUserSettings();
            });
        } else {
            setupThemeToggle();
            loadUserSettings();
        }
    }
    
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();

async function loadUserSettings() {
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            applySettings();
            return Promise.resolve();
        }

        const response = await fetch('/v1/api/settings', {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });

        if (response.ok) {
            const settings = await response.json();
            userSettings = { ...userSettings, ...settings };
            localStorage.setItem('userTheme', userSettings.theme || 'light');
            if (userSettings.itemsPerPage) {
                localStorage.setItem('userItemsPerPage', userSettings.itemsPerPage);
            }
            if (userSettings.savedFilters) {
                localStorage.setItem('userSavedFilters', userSettings.savedFilters);
            }
            applySettings();
        } else {
            applySettings();
        }
        return Promise.resolve();
    } catch (error) {
        console.error('Error loading user settings:', error);
        applySettings();
        return Promise.resolve();
    }
}

function setItemsPerPage(itemsPerPageValue) {
    userSettings.itemsPerPage = parseInt(itemsPerPageValue);
    localStorage.setItem('userItemsPerPage', userSettings.itemsPerPage);
    applySettings();
    saveUserSettings();
    
    if (typeof loadAllCourses === 'function') {
        loadAllCourses();
    }
}

function saveFilters(filters) {
    console.log('Saving filters:', filters);
    userSettings.savedFilters = JSON.stringify(filters);
    localStorage.setItem('userSavedFilters', userSettings.savedFilters);
    saveUserSettings();
}

function applyFilters(filters) {
    console.log('Applying filters:', filters);
    if (filters.categoryId) {
        const categorySelect = document.getElementById('categoryFilter');
        if (categorySelect) {
            categorySelect.value = filters.categoryId;
            console.log('Category filter applied:', filters.categoryId);
        }
    }
    
    if (filters.minPrice !== undefined && filters.minPrice !== null && filters.minPrice !== '') {
        const minPriceInput = document.getElementById('minPrice');
        if (minPriceInput) {
            minPriceInput.value = filters.minPrice;
            console.log('Min price filter applied:', filters.minPrice);
        }
    }
    
    if (filters.maxPrice !== undefined && filters.maxPrice !== null && filters.maxPrice !== '') {
        const maxPriceInput = document.getElementById('maxPrice');
        if (maxPriceInput) {
            maxPriceInput.value = filters.maxPrice;
            console.log('Max price filter applied:', filters.maxPrice);
        }
    }
    
    if (filters.status) {
        const statusSelect = document.getElementById('statusFilter');
        if (statusSelect) {
            statusSelect.value = filters.status;
            console.log('Status filter applied:', filters.status);
        }
    }
}

function getCurrentFilters() {
    return {
        categoryId: document.getElementById('categoryFilter')?.value || '',
        minPrice: document.getElementById('minPrice')?.value || '',
        maxPrice: document.getElementById('maxPrice')?.value || '',
        status: document.getElementById('statusFilter')?.value || ''
    };
}

window.userSettings = {
    load: loadUserSettings,
    save: saveUserSettings,
    toggleTheme: toggleTheme,
    setItemsPerPage: setItemsPerPage,
    saveFilters: saveFilters,
    applyFilters: applyFilters,
    getCurrentFilters: getCurrentFilters,
    getSettings: () => userSettings,
    applySettings: applySettings
};

