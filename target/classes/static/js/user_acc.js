document.querySelectorAll(".remove-from-favorites").forEach(button => {
    button.addEventListener("click", function(event) {
        event.preventDefault();

        let userId = this.getAttribute("data-userid"); 
        let carId = this.getAttribute("data-carid"); 
        
        if (!userId || !carId) {
            console.error("Ошибка: userId или carId не определены.");
            return;
        }

        let csrfToken = document.getElementById("csrf_token").value;  

        const carItemId = `car-item-${carId}`;
        console.log(carItemId);
        const carItemElement = document.getElementById(carItemId);

        if (carItemElement) {
            carItemElement.remove();
        }

        let formData = new URLSearchParams();
        formData.append("userId", userId);
        formData.append("carId", carId);

        fetch("/remove-favorite", {
            method: "POST",
            headers: { 
                "Content-Type": "application/x-www-form-urlencoded",
                "X-CSRF-TOKEN": csrfToken
            },
            body: formData.toString()
        })
        .then(response => {
            if (!response.ok) {
                alert("Ошибка при удалении из избранного. Попробуйте снова.");
                document.body.appendChild(carItemElement);
            } else {
                return response.text();
            }
        })
        .then(message => {
            showNotification(message); 
        })
        .catch(error => {
            console.error("Ошибка:", error);
        });
    });
});

function showNotification(message) {
    let notification = document.getElementById("favorite-notification");
    notification.innerText = message;
    notification.classList.add("show");

    setTimeout(() => {
        notification.classList.remove("show");
        setTimeout(() => {
            notification.style.display = "none";
        }, 500);
    }, 2000);
}