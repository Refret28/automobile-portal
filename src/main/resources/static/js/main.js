document.addEventListener("DOMContentLoaded", function() {
    document.querySelectorAll(".add-to-favorites").forEach(button => {
        button.addEventListener("click", function(event) {
            event.preventDefault();

            let userId = this.getAttribute("data-userid");
            let carId = this.getAttribute("data-carid");
            let csrfToken = document.getElementById("csrf_token")?.value; 

            if (!userId || !carId) {
                console.error("Ошибка: userId или carId не определены.");
                return;
            }

            let formData = new URLSearchParams();
            formData.append("userId", userId);
            formData.append("carId", carId);

            fetch("/add-to-favorites", {
                method: "POST",
                headers: { 
                    "Content-Type": "application/x-www-form-urlencoded",
                    "X-CSRF-TOKEN": csrfToken 
                },
                body: formData.toString()
            })
            .then(response => response.text().then(text => ({ status: response.status, text })))
            .then(({ status, text }) => {
                if (status === 200) {
                    showNotification(text);
                } else if (status === 400) {
                    showNotification(text); 
                } else {
                    throw new Error(`Ошибка запроса: ${status}`);
                }
            })
            .catch(error => console.error("Ошибка:", error));
        });
    });
});

function showNotification(message) {
    let notification = document.getElementById("favorite-notification");
    notification.innerText = message;
    notification.style.display = "block";
    notification.classList.add("show");

    setTimeout(() => {
        notification.classList.remove("show");
        setTimeout(() => {
            notification.style.display = "none";
        }, 500);
    }, 1500);
}

function toggleSidebar() {
    const sidebar = document.getElementById("sidebar");
    sidebar.classList.toggle("open");
}

function closeSidebar() {
    const sidebar = document.getElementById("sidebar");
    sidebar.classList.remove("open");
}

function showTab(tabName) {
    const contents = document.querySelectorAll('.tab-content');
    const tabs = document.querySelectorAll('.tab');

    tabs.forEach(tab => tab.classList.remove('active'));

    contents.forEach(content => {
        content.style.opacity = '0';
        content.style.visibility = 'hidden';
        content.style.height = '0';
        content.classList.remove('active');
    });

    document.querySelector(`.tab[data-tab="${tabName}"]`)?.classList.add('active');

    const selectedTabContent = document.getElementById(tabName);
    if (selectedTabContent) {
        selectedTabContent.classList.add('active');
        selectedTabContent.style.height = 'auto';
        selectedTabContent.style.visibility = 'visible';
        setTimeout(() => {
            selectedTabContent.style.opacity = '1'; 
        }, 50);
    }
}
