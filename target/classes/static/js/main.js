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
    contents.forEach(function(content) {
        content.classList.remove('active'); 
        content.style.opacity = '0'; 
        content.style.visibility = 'hidden'; 
    });

    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(function(tab) {
        tab.classList.remove('active'); 
    });

    const tabContent = document.getElementById(tabName);
    if (tabContent) {
        tabContent.classList.add('active'); 
        tabContent.style.visibility = 'visible'; 
        setTimeout(() => {
            tabContent.style.opacity = '1'; 
        }, 10); 
    }

    const activeTab = Array.from(tabs).find(tab => tab.textContent.trim().toLowerCase() === tabName.toLowerCase());
    if (activeTab) {
        activeTab.classList.add('active'); 
    }
}