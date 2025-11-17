function getCurrentContext() {
    return document.body?.dataset?.context || '';
}

function getPageIdentifiers() {
    const dataset = document.body?.dataset || {};
    return {
        courseId: dataset.courseId || null,
        lessonId: dataset.lessonId || null,
        taskId: dataset.taskId || null,
        solutionId: dataset.solutionId || null
    };
}

function runIfAvailable(fn, fallbackSelector, navigationFallback) {
    if (fallbackSelector) {
        const element = document.querySelector(fallbackSelector);
        if (element) {
            element.click();
            return true;
        }
    }
    if (typeof fn === 'function') {
        fn();
        return true;
    }
    if (typeof navigationFallback === 'function') {
        return navigationFallback();
    }
    return false;
}

console.info('[Hotkeys] script loaded');

const hotkeys = {
    'ctrl+alt+keyc': {
        action: () => {
            if (!runIfAvailable(
                window.showAddCourseModal,
                '[data-action="create-course"]'
            )) {
                showHotkeyHint('Кнопка создания курса не найдена');
            }
        },
        description: 'Создать новый курс (Ctrl+Alt+C)',
        contexts: ['teacher-dashboard'],
        requirement: 'Откройте раздел "Мои курсы" преподавателя'
    },
    
    'ctrl+alt+keyl': {
        action: () => {
            if (!runIfAvailable(
                window.createLesson,
                '[data-action="create-lesson"]',
                () => {
                    const ids = getPageIdentifiers();
                    if (!ids.courseId) return false;
                    window.location.href = `/teacher/course/${ids.courseId}/create-lesson`;
                    return true;
                }
            )) {
                showHotkeyHint('На странице курса нет кнопки "Добавить урок"');
            }
        },
        description: 'Создать новый урок (Ctrl+Alt+L)',
        contexts: ['teacher-course'],
        requirement: 'Сначала откройте нужный курс'
    },
    
    'ctrl+alt+keyt': {
        action: () => {
            if (!runIfAvailable(
                window.createTask,
                '[data-action="create-task"]',
                () => {
                    const ids = getPageIdentifiers();
                    if (!ids.courseId || !ids.lessonId) return false;
                    window.location.href = `/teacher/course/${ids.courseId}/lesson/${ids.lessonId}/create-task`;
                    return true;
                }
            )) {
                showHotkeyHint('Откройте урок и используйте кнопку "Создать задание"');
            }
        },
        description: 'Создать новое задание (Ctrl+Alt+T)',
        contexts: ['teacher-lesson'],
        requirement: 'Сначала откройте урок, где нужно создать задание'
    },
    
    'ctrl+shift+alt+keyc': {
        action: () => {
            if (!runIfAvailable(
                window.editCourse,
                '[data-action="edit-course"]',
                () => {
                    const ids = getPageIdentifiers();
                    if (!ids.courseId) return false;
                    window.location.href = `/teacher/edit-course/${ids.courseId}`;
                    return true;
                }
            )) {
                showHotkeyHint('Функция редактирования курса недоступна');
            }
        },
        description: 'Редактировать курс (Ctrl+Alt+Shift+C)',
        contexts: ['teacher-course'],
        requirement: 'Откройте страницу нужного курса'
    },
    
    'ctrl+shift+alt+keyl': {
        action: () => {
            if (!runIfAvailable(
                window.editLesson,
                '[data-action="edit-lesson"]',
                () => {
                    const ids = getPageIdentifiers();
                    if (!ids.courseId || !ids.lessonId) return false;
                    window.location.href = `/teacher/course/${ids.courseId}/lesson/${ids.lessonId}/edit`;
                    return true;
                }
            )) {
                showHotkeyHint('Редактирование доступно только на странице урока');
            }
        },
        description: 'Редактировать урок (Ctrl+Alt+Shift+L)',
        contexts: ['teacher-lesson'],
        requirement: 'Откройте страницу урока'
    },
    
    'ctrl+shift+alt+keyt': {
        action: () => {
            if (!runIfAvailable(
                window.editTask,
                '[data-action="edit-task"]',
                () => {
                    const ids = getPageIdentifiers();
                    if (!ids.courseId || !ids.lessonId || !ids.taskId) return false;
                    window.location.href = `/teacher/course/${ids.courseId}/lesson/${ids.lessonId}/task/${ids.taskId}/edit`;
                    return true;
                }
            )) {
                showHotkeyHint('Редактирование задания доступно на странице задания');
            }
        },
        description: 'Редактировать задание (Ctrl+Alt+Shift+T)',
        contexts: ['teacher-task'],
        requirement: 'Откройте страницу задания'
    },
    
    'ctrl+alt+keyy': {
        action: () => {
            if (!runIfAvailable(
                window.copyCourse,
                '[data-action="copy-course"]',
                () => {
                    const ids = getPageIdentifiers();
                    if (!ids.courseId) return false;
                    window.location.href = `/teacher?copy=true&courseId=${ids.courseId}`;
                    return true;
                }
            )) {
                showHotkeyHint('Копирование доступно на странице курса');
            }
        },
        description: 'Копировать курс (Ctrl+Alt+Y)',
        contexts: ['teacher-course'],
        requirement: 'Откройте страницу курса, который хотите скопировать'
    },
    
    'ctrl+alt+keys': {
        action: (e) => {
            e.preventDefault();
            const saveBtn = document.querySelector('[data-action="save"]');
            if (saveBtn) {
                saveBtn.click();
            } else {
                const form = document.querySelector('form:not([style*="display: none"])');
                if (form) {
                    const submitBtn = form.querySelector('button[type="submit"]');
                    if (submitBtn) {
                        submitBtn.click();
                    }
                }
            }
        },
        description: 'Сохранить (Ctrl+Alt+S)',
        contexts: ['teacher-dashboard', 'teacher-course', 'teacher-lesson', 'teacher-task', 'teacher-solution'],
        requirement: 'Сохранение доступно на страницах преподавателя'
    },
    
    'escape': {
        action: () => {
            const activeModal = document.querySelector('.modal.show');
            if (activeModal) {
                const bsModal = bootstrap.Modal.getInstance(activeModal);
                if (bsModal) {
                    bsModal.hide();
                }
            }
        },
        description: 'Закрыть модальное окно'
    },
    
    'ctrl+alt+keyr': {
        action: (e) => {
            e.preventDefault();
            window.location.reload();
        },
        description: 'Обновить страницу (Ctrl+Alt+R)',
        contexts: ['teacher-dashboard', 'teacher-course', 'teacher-lesson', 'teacher-task', 'teacher-solution']
    },
    
    'ctrl+alt+keyf': {
        action: (e) => {
            e.preventDefault();
            const searchInput = document.querySelector('[data-action="search"]');
            if (searchInput) {
                searchInput.focus();
            }
        },
        description: 'Поиск (Ctrl+Alt+F)',
        contexts: ['teacher-dashboard', 'student-dashboard'],
        requirement: 'Поле поиска доступно в текущем разделе'
    },
    
    'ctrl+alt+keyd': {
        action: (e) => {
            e.preventDefault();
            if (window.userSettings && window.userSettings.toggleTheme) {
                window.userSettings.toggleTheme();
            } else if (typeof toggleTheme === 'function') {
                toggleTheme();
            }
        },
        description: 'Переключить тему (Ctrl+Alt+D)',
        contexts: ['teacher-dashboard', 'teacher-course', 'teacher-lesson', 'teacher-task', 'teacher-solution', 'student-dashboard', 'student-course', 'student-lesson', 'student-task']
    }
};

document.addEventListener('keydown', function(e) {
    if (e.target.tagName === 'INPUT' || 
        e.target.tagName === 'TEXTAREA' || 
        e.target.isContentEditable) {
        if (e.key === 'Escape' || (e.ctrlKey && e.altKey)) {
        } else {
            return;
        }
    }
    
    let keyCombo = '';
    if (e.ctrlKey) keyCombo += 'ctrl+';
    if (e.shiftKey) keyCombo += 'shift+';
    if (e.altKey) keyCombo += 'alt+';
    
    if (e.key === 'Escape') {
        keyCombo = 'escape';
    } else {
        const code = e.code ? e.code.toLowerCase() : '';
        if (code) {
            keyCombo += code;
        } else {
            keyCombo += e.key.toLowerCase();
        }
    }
    
    console.debug('[Hotkeys] combo detected:', keyCombo, 'context:', getCurrentContext());
    
    const handler = hotkeys[keyCombo];
    if (handler) {
        const contextAllowed = !handler.contexts || handler.contexts.includes(getCurrentContext());
        if (!contextAllowed) {
            showHotkeyHint(handler.requirement || 'Эта комбинация доступна в другом разделе');
            return;
        }
        handler.action(e);
        showHotkeyHint(handler.description);
    }
});

function showHotkeyHint(description) {
    const existingHint = document.getElementById('hotkeyHint');
    if (existingHint) {
        existingHint.remove();
    }
    
    const hint = document.createElement('div');
    hint.id = 'hotkeyHint';
    hint.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: rgba(0, 0, 0, 0.8);
        color: white;
        padding: 10px 20px;
        border-radius: 5px;
        z-index: 10000;
        font-size: 14px;
        animation: fadeInOut 2s ease-in-out;
    `;
    hint.textContent = description;
    document.body.appendChild(hint);
    
    setTimeout(() => {
        if (hint.parentNode) {
            hint.remove();
        }
    }, 2000);
}

const style = document.createElement('style');
style.textContent = `
    @keyframes fadeInOut {
        0% { opacity: 0; transform: translateY(-10px); }
        20% { opacity: 1; transform: translateY(0); }
        80% { opacity: 1; transform: translateY(0); }
        100% { opacity: 0; transform: translateY(-10px); }
    }
`;
document.head.appendChild(style);

function showHotkeysHelp() {
    let html = '<div class="modal fade" id="hotkeysHelpModal" tabindex="-1">';
    html += '<div class="modal-dialog modal-lg">';
    html += '<div class="modal-content">';
    html += '<div class="modal-header">';
    html += '<h5 class="modal-title">Горячие клавиши</h5>';
    html += '<button type="button" class="btn-close" data-bs-dismiss="modal"></button>';
    html += '</div>';
    html += '<div class="modal-body">';
    html += '<table class="table table-striped">';
    html += '<thead><tr><th>Комбинация</th><th>Действие</th></tr></thead>';
    html += '<tbody>';
    
    Object.entries(hotkeys).forEach(([key, handler]) => {
        html += `<tr><td><kbd>${key.toUpperCase()}</kbd></td><td>${handler.description}</td></tr>`;
    });
    
    html += '</tbody></table>';
    html += '</div>';
    html += '<div class="modal-footer">';
    html += '<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Закрыть</button>';
    html += '</div>';
    html += '</div></div></div>';
    
    const existing = document.getElementById('hotkeysHelpModal');
    if (existing) {
        existing.remove();
    }
    
    document.body.insertAdjacentHTML('beforeend', html);
    const modal = new bootstrap.Modal(document.getElementById('hotkeysHelpModal'));
    modal.show();
}

document.addEventListener('DOMContentLoaded', function() {
    const helpBtn = document.createElement('button');
    helpBtn.className = 'btn btn-outline-info btn-sm';
    helpBtn.innerHTML = '<i class="fas fa-keyboard"></i> Горячие клавиши';
    helpBtn.onclick = showHotkeysHelp;
    helpBtn.style.marginLeft = '10px';
    
    const navbar = document.querySelector('.navbar-nav');
    if (navbar) {
        navbar.appendChild(helpBtn);
    }
});

window.hotkeys = hotkeys;
window.showHotkeysHelp = showHotkeysHelp;

