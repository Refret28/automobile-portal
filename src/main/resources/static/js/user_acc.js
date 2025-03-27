function updateFileName() {
    const input = document.getElementById('fileInput');
    const fileName = document.getElementById('fileName');
    fileName.textContent = input.files.length > 0 ? input.files[0].name : 'Нет выбранного файла';
}

function getCSRFToken(button) {
    const form = button.closest("form");
    if (!form) {
        console.error("Ошибка: Форма не найдена.");
        return null;
    }

    const csrfInput = form.querySelector("input[name='_csrf']");
    if (!csrfInput) {
        console.error("Ошибка: CSRF-токен не найден.");
        return null;
    }

    return csrfInput.value;
}

document.querySelectorAll(".remove-from-favorites").forEach(button => {
    button.addEventListener("click", function(event) {
        event.preventDefault();

        let userId = this.getAttribute("data-userid"); 
        let carId = this.getAttribute("data-carid"); 
        
        if (!userId || !carId) {
            console.error("Ошибка: userId или carId не определены.");
            return;
        }

        const carItemId = `car-item-${carId}`;

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
                "X-CSRF-TOKEN": getCSRFToken(this) 
            },
            body: formData.toString()
        })
        .then(response => {
            if (!response.ok) {
                document.body.appendChild(carItemElement);
            } else {
                setTimeout(() => {
                    window.location.reload(); 
                }, 1000);
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
    }, 1000);
}

document.addEventListener("DOMContentLoaded", function() {
    fetch("/files/user-files")
        .then(response => response.json())
        .then(files => {
            let fileList = document.getElementById("fileList");
            fileList.innerHTML = "";
            let noFilesMessage = document.querySelector(".no-files");

            if (files.length === 0) {
                noFilesMessage.style.display = "block";
                return;
            } else {
                noFilesMessage.style.display = "none"; 
            }

            files.forEach(file => {
                let li = document.createElement("li");
                let a = document.createElement("a");
                let deleteBtn = document.createElement("button");

                a.href = file.filePath; 
                a.innerText = file.filename;
                a.target = "_blank"; 

                deleteBtn.innerText = "Удалить";
                deleteBtn.className = "btn delete-button"; 

                deleteBtn.onclick = function() {
                    let csrfToken = document.querySelector("input[name='_csrf']").value;  

                    fetch(`/files/delete/${file.id}`, { 
                        method: 'DELETE',
                        headers: {
                            'Content-Type': 'application/json',
                            'X-CSRF-TOKEN': csrfToken  
                        }
                    })
                    .then(response => {
                        if (response.ok) {
                            showNotification("Файл удалён!");
                            setTimeout(() => {
                                window.location.reload(); 
                            }, 500);
                        } else {
                            showNotification("Ошибка удаления файла");
                        }
                    })
                    .catch(error => {
                        console.error("Ошибка:", error);
                        showNotification("Ошибка удаления файла");
                    });
                };

                li.appendChild(a);
                li.appendChild(deleteBtn);
                fileList.appendChild(li);
            });
        })
        .catch(error => {
            console.error("Ошибка загрузки списка файлов:", error);
            showNotification("Ошибка загрузки файлов");
        });
});

document.getElementById('avatarInput').addEventListener('change', function () {
    let form = document.getElementById('avatarForm');
    let formData = new FormData(form);

    fetch(form.action, {
        method: 'POST',
        body: formData
    }).then(response => {
        if (response.ok) {
            setTimeout(() => {
                window.location.reload(); 
            }, 500);
        } else {
            console.error('Ошибка при загрузке:', response.statusText);
        }
    }).catch(error => console.error('Ошибка загрузки:', error));
});
