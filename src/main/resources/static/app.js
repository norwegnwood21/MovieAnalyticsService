var currentReportId = null;

function byId(id) {
    return document.getElementById(id);
}

async function request(url, method, body) {
    if (!method) method = 'GET';
    var options = { method: method, headers: { 'Content-Type': 'application/json' } };
    if (body) options.body = JSON.stringify(body);

    var response = await fetch(url, options);
    var text = await response.text();
    var data = text ? JSON.parse(text) : null;

    if (!response.ok) {
        var message = data && data.error ? data.error : 'Ошибка запроса';
        throw new Error(message);
    }
    return data;
}

async function checkMe() {
    try {
        var user = await request('/api/auth/me');
        showApp(user);
        await loadHistory();
    } catch (e) {
        byId('authCard').classList.remove('hidden');
    }
}

function showApp(user) {
    byId('authCard').classList.add('hidden');
    byId('appCard').classList.remove('hidden');
    byId('historyCard').classList.remove('hidden');
    byId('userInfo').textContent = 'Вы вошли как: ' + user.username + ' (' + user.email + ')';
}

async function registerUser() {
    try {
        var user = await request('/api/auth/register', 'POST', {
            username: byId('regUsername').value,
            email: byId('regEmail').value,
            password: byId('regPassword').value
        });
        byId('authMessage').textContent = 'Регистрация выполнена';
        byId('authMessage').className = 'message success';
        showApp(user);
        await loadHistory();
    } catch (e) {
        byId('authMessage').textContent = e.message;
        byId('authMessage').className = 'message error';
    }
}

async function loginUser() {
    try {
        var user = await request('/api/auth/login', 'POST', {
            username: byId('loginUsername').value,
            password: byId('loginPassword').value
        });
        byId('authMessage').textContent = 'Вход выполнен';
        byId('authMessage').className = 'message success';
        showApp(user);
        await loadHistory();
    } catch (e) {
        byId('authMessage').textContent = e.message;
        byId('authMessage').className = 'message error';
    }
}

async function logoutUser() {
    await request('/api/auth/logout', 'POST');
    location.reload();
}

async function createReport() {
    var titles = byId('movieTitles').value.split('\n').map(function (x) { return x.trim(); }).filter(Boolean);
    try {
        byId('statusMessage').textContent = 'Анализ выполняется...';
        byId('statusMessage').className = 'message';
        var report = await request('/api/reports', 'POST', { titles: titles });
        byId('statusMessage').textContent = 'Анализ завершён';
        byId('statusMessage').className = 'message success';
        renderReport(report);
        await loadHistory();
    } catch (e) {
        byId('statusMessage').textContent = e.message;
        byId('statusMessage').className = 'message error';
    }
}

async function loadHistory() {
    var historyBox = byId('history');
    var historyMessage = byId('historyMessage');

    try {
        if (historyMessage) {
            historyMessage.textContent = 'Загружаем историю...';
            historyMessage.className = 'message';
        }

        var reports = await request('/api/reports');
        historyBox.innerHTML = '';

        if (!reports || reports.length === 0) {
            if (historyMessage) historyMessage.textContent = 'История пока пустая.';
            return;
        }

        if (historyMessage) {
            historyMessage.textContent = 'Найдено отчётов: ' + reports.length;
            historyMessage.className = 'message success';
        }

        for (var i = 0; i < reports.length; i++) {
            var report = reports[i];
            var div = document.createElement('div');
            div.className = 'historyItem';
            div.innerHTML =
                '<div>' +
                    '<p><b>Отчёт #' + report.id + '</b> <span class="badge ' + (report.status === 'COMPLETED' ? 'ok' : 'err') + '">' + report.status + '</span></p>' +
                    '<p>' + formatDate(report.createdAt) + '</p>' +
                    '<p>Фильмов в запросе: ' + (report.movies ? report.movies.length : 0) + '</p>' +
                '</div>' +
                '<div class="historyActions">' +
                    '<button onclick="loadReport(' + report.id + ')">Открыть</button>' +
                    '<button class="secondary" onclick="repeatReport(' + report.id + ')">Повторить без TMDB</button>' +
                '</div>';
            historyBox.appendChild(div);
        }
    } catch (e) {
        if (historyMessage) {
            historyMessage.textContent = e.message;
            historyMessage.className = 'message error';
        } else {
            historyBox.innerHTML = '<p class="error">' + escapeHtml(e.message) + '</p>';
        }
    }
}

async function loadReport(id) {
    try {
        var report = await request('/api/reports/' + id);
        renderReport(report);
    } catch (e) {
        var historyMessage = byId('historyMessage');
        if (historyMessage) {
            historyMessage.textContent = e.message;
            historyMessage.className = 'message error';
        }
    }
}

async function repeatCurrentReport() {
    if (!currentReportId) return;
    await repeatReport(currentReportId);
}

async function repeatReport(id) {
    var historyMessage = byId('historyMessage');
    try {
        if (historyMessage) {
            historyMessage.textContent = 'Выполняем повторный анализ без обращения к TMDB...';
            historyMessage.className = 'message';
        }
        var report = await request('/api/reports/' + id + '/repeat-local', 'POST');
        renderReport(report);
        await loadHistory();
    } catch (e) {
        if (historyMessage) {
            historyMessage.textContent = e.message;
            historyMessage.className = 'message error';
        }
    }
}

function renderReport(report) {
    currentReportId = report.id;
    byId('resultCard').classList.remove('hidden');

    var repeatButton = byId('repeatCurrentButton');
    if (repeatButton) {
        repeatButton.classList.remove('hidden');
        repeatButton.disabled = report.status !== 'COMPLETED';
    }

    byId('reportStatus').innerHTML =
        '<p><b>Отчёт #' + report.id + '</b></p>' +
        '<p><b>Статус:</b> <span class="badge ' + (report.status === 'COMPLETED' ? 'ok' : 'err') + '">' + report.status + '</span></p>' +
        (report.errorMessage ? '<p class="error">' + escapeHtml(report.errorMessage) + '</p>' : '');

    byId('matches').innerHTML = '';
    var movies = report.movies || [];
    for (var i = 0; i < movies.length; i++) {
        var movie = movies[i];
        var div = document.createElement('div');
        div.className = 'movie';

        var candidates = '';
        var candidateList = movie.candidates || [];
        for (var j = 0; j < candidateList.length; j++) {
            var c = candidateList[j];
            candidates += '<li>' + escapeHtml(c.title) + ' (' + escapeHtml(c.releaseDate || 'дата неизвестна') + ')</li>';
        }

        div.innerHTML =
            '<b>' + escapeHtml(movie.originalTitle) + '</b> ' +
            '<span class="badge ' + (movie.success ? 'ok' : 'err') + '">' + (movie.success ? 'найден' : 'ошибка') + '</span><br>' +
            (movie.success
                ? 'Выбран: ' + escapeHtml(movie.matchedTitle) + ' (' + escapeHtml(movie.releaseDate || 'дата неизвестна') + ')'
                : '<span class="error">' + escapeHtml(movie.errorMessage || 'Ошибка') + '</span>') +
            '<details><summary>Найденные варианты TMDB</summary><ul>' + candidates + '</ul></details>';
        byId('matches').appendChild(div);
    }

    updateDescription('genreDescription', report.genreStats, 'жанров');
    updateDescription('yearDescription', report.yearStats, 'годов выпуска');
    updateDescription('castDescription', report.castStats, 'актёров');

    drawHtmlBarChart('genreChart', report.genreStats, 12);
    drawHtmlBarChart('yearChart', report.yearStats, 12);
    drawHtmlBarChart('castChart', report.castStats, 10);
}

function updateDescription(elementId, dataMap, unitName) {
    var description = byId(elementId);
    if (!description) return;

    var entries = Object.entries(dataMap || {});
    if (entries.length === 0) {
        description.textContent = 'Нет данных для отображения.';
        return;
    }

    entries.sort(function (a, b) { return b[1] - a[1]; });

    var shownCount = Math.min(entries.length, 15);
    var topValue = entries[0][1];
    var leaders = entries.filter(function (entry) {
        return entry[1] === topValue;
    });

    var text = 'Показано ' + shownCount + ' ' + unitName + '.';

    if (topValue > 1 && leaders.length === 1) {
        text += ' Самое частое значение: ' + entries[0][0] + ' — ' + topValue + '.';
    } else if (topValue === 1) {
        text += ' Все значения встречаются одинаково часто.';
    } else {
        var leaderNames = leaders.map(function (entry) { return entry[0]; }).join(', ');
        text += ' Несколько значений встречаются одинаково часто: ' + leaderNames + ' — ' + topValue + '.';
    }

    description.textContent = text;
}

function drawHtmlBarChart(containerId, dataMap, limit) {
    var container = byId(containerId);
    if (!container) return;

    container.innerHTML = '';

    var entries = Object.entries(dataMap || {});
    if (entries.length === 0) {
        container.innerHTML = '<p class="emptyChart">Нет данных для отображения.</p>';
        return;
    }

    entries.sort(function (a, b) { return b[1] - a[1]; });
    entries = entries.slice(0, limit);

    var maxValue = entries[0][1];

    for (var i = 0; i < entries.length; i++) {
        var name = entries[i][0];
        var value = entries[i][1];
        var percent = maxValue === 0 ? 0 : Math.round((value / maxValue) * 100);

        var row = document.createElement('div');
        row.className = 'barRow';
        row.innerHTML =
            '<div class="barLabel" title="' + escapeHtml(name) + '">' + escapeHtml(name) + '</div>' +
            '<div class="barTrack">' +
                '<div class="barFill" style="width: ' + percent + '%;"></div>' +
            '</div>' +
            '<div class="barValue">' + value + '</div>';

        container.appendChild(row);
    }
}

function formatDate(value) {
    if (!value) return '';
    try {
        return new Date(value).toLocaleString('ru-RU');
    } catch (e) {
        return value;
    }
}

function escapeHtml(value) {
    return String(value || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Явно прикрепляем функции к window, чтобы inline onclick точно видел их.
window.registerUser = registerUser;
window.loginUser = loginUser;
window.logoutUser = logoutUser;
window.createReport = createReport;
window.loadHistory = loadHistory;
window.loadReport = loadReport;
window.repeatReport = repeatReport;
window.repeatCurrentReport = repeatCurrentReport;

checkMe();
