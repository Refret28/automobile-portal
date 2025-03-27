function refreshNews() {

    const csrfInput = document.querySelector("input[name='_csrf']");
    if (!csrfInput) {
        return;
    }
    const csrfToken = csrfInput.value;

    fetch("/news-list")
        .then(response => {
            if (!response.ok) {
                throw new Error("Ошибка при обновлении новостей");
            }
            return response.json();
        })
        .then(data => {
            const newsContainer = document.querySelector("#news-list");
            if (!newsContainer) {
                return;
            }

            newsContainer.innerHTML = ""; 

            data.forEach(news => {
                const newsRow = document.createElement("tr");
                newsRow.innerHTML = `
                    <td>${news.id}</td>
                    <td>${news.title}</td>
                    <td>${news.content}</td>
                    <td>
                        <a href="/edit-news/${news.id}" class="btn edit-btn">Редактировать</a>
                        <form action="/delete-news" method="post" style="display:inline;">
                            <input type="hidden" name="_csrf" value="${csrfToken}" /> 
                            <input type="hidden" name="id" value="${news.id}" />
                            <button type="submit" class="btn delete-btn">Удалить</button>
                        </form>
                    </td>
                `;
                newsContainer.appendChild(newsRow);
            });

            showNotification("Новости успешно обновлены!");
        })
        .catch(error => {
            console.error("Ошибка при обновлении новостей:", error);
            showNotification("Ошибка при обновлении новостей!", true);
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
    }, 3000); 
}