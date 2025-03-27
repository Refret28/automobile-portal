document.getElementById('avatarInput').addEventListener('change', function (event) {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            document.querySelector('.avatar').src = e.target.result;
        };
        reader.readAsDataURL(file);
    }
});