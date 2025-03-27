document.addEventListener("DOMContentLoaded", function () {
    refreshUsers();
    setTimeout(refreshNews, 500);  
});

function refreshUsers() {
    fetch("/api/users") 
        .then(response => response.json())
        .then(users => {
            let userList = document.getElementById("user-list");
            userList.innerHTML = ""; 

            let csrfInput = document.querySelector("input[name='_csrf']");
            if (!csrfInput) {
                console.error("CSRF-токен не найден!");
                return;
            }
            let csrfToken = csrfInput.value;

            users.forEach(user => {
                let row = document.createElement("tr");
                row.innerHTML = `
                    <td>${user.id}</td>
                    <td>${user.username}</td>
                    <td>${user.email}</td>
                    <td>${user.role.name}</td>
                    <td>
                        <form action="/update-role" method="post">
                            <input type="hidden" name="_csrf" value="${csrfToken}" />
                            <input type="hidden" name="userId" value="${user.id}" />
                            <select name="role" class="styled-select">
                                <option value="" disabled selected>Выберите роль</option>
                                <option value="USER">Пользователь</option>
                                <option value="MODERATOR">Модератор</option>
                                <option value="ADMIN">Администратор</option>
                            </select>
                            <button type="submit" class="btn edit-btn">Назначить роль</button>
                        </form>
                        <a href="/edit-user/${user.id}" class="btn edit-btn">Редактировать</a>
                        <form action="/delete-user" method="post">
                            <input type="hidden" name="_csrf" value="${csrfToken}" />
                            <input type="hidden" name="userId" value="${user.id}" />
                            <button type="submit" class="btn delete-btn">Удалить</button>
                        </form>
                    </td>
                `;
                userList.appendChild(row);
            });

            showNotification("Пользователи успешно обновлены!");
        })
        .catch(error => {
            console.error("Ошибка при обновлении пользователей:", error);
            showNotification("Ошибка при обновлении пользователей!", true);
        });
}

function showNotification(message, isError = false) {
    const existingNotification = document.querySelector(".notification");
    if (existingNotification) {
        existingNotification.remove(); 
    }

    const notification = document.createElement("div");
    notification.className = "notification"; 
    if (isError) {
        notification.classList.add("error"); 
    }
    notification.textContent = message;

    document.body.appendChild(notification); 

    notification.style.display = "block";
    notification.style.opacity = "0"; 

    setTimeout(() => {
        notification.style.opacity = "1"; 
    }, 10);

    setTimeout(() => {
        notification.style.opacity = "0"; 
        setTimeout(() => notification.remove(), 300); 
    }, 500); 
}

function refreshNews() {
    fetch('/news-list', {
        method: 'GET',
        headers: {
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        }
    })
    .then(response => response.json())
    .then(data => {
        const newsList = document.getElementById('news-list');
        newsList.innerHTML = ''; 

        data.forEach(news => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${news.id}</td>
                <td>${news.title}</td>
                <td>${news.content}</td>
                <td>
                    <a href="/edit-news/${news.id}" class="btn edit-btn">Редактировать</a>
                    <form action="/delete-news" method="post" style="display:inline;">
                        <input type="hidden" name="_csrf" value="${document.querySelector('meta[name="_csrf"]').content}" />
                        <input type="hidden" name="id" value="${news.id}" />
                        <button type="submit" class="btn delete-btn">Удалить</button>
                    </form>
                </td>
            `;
            newsList.appendChild(row);
        });

        showNotification("Новости успешно обновлены!"); 
    })
    .catch(error => {
        console.error('Ошибка при обновлении новостей:', error);
        showNotification("Ошибка при обновлении новостей!", true);
    });
}

function deleteNews(newsId) {
    fetch('/delete-news', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: new URLSearchParams({ id: newsId })
    })
    .then(response => {
        if (response.ok) {
            showNotification("Новость удалена!");
            refreshNews();
        } else {
            showNotification("Ошибка удаления!", true);
        }
    })
    .catch(error => console.error("Ошибка:", error));
}

function showTab(tabName) {
    localStorage.setItem("activeTab", tabName);

    const tabs = document.querySelectorAll('.tab-content');
    const buttons = document.querySelectorAll('.tab-link');

    tabs.forEach(tab => {
        tab.classList.remove('active');
        setTimeout(() => tab.style.display = 'none', 300);
    });

    buttons.forEach(button => button.classList.remove('active'));

    setTimeout(() => {
        const activeTab = document.getElementById(tabName);
        activeTab.style.display = 'block';
        setTimeout(() => activeTab.classList.add('active'), 10);

        if (tabName === "news-tab") {
            console.log("Открыта вкладка 'Новости', загружаем список...");
            refreshNews();
        }
    }, 300);

    document.querySelector(`.tab-link[onclick="showTab('${tabName}')"]`).classList.add('active');
}

document.addEventListener("DOMContentLoaded", function () {
    const savedTab = localStorage.getItem("activeTab") || "users-tab";
    showTab(savedTab);

    setTimeout(() => {
        if (savedTab === "news-tab") {
            refreshNews();
        }
    }, 100); 
});

document.addEventListener("submit", function (event) {
    if (event.target.matches("form[action='/update-role']")) {
        const select = event.target.querySelector("select[name='role']");
        if (!select.value) {
            event.preventDefault();
            showNotification("Выберите роль перед отправкой!", true);
        }
    }
});
