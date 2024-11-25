const inputs = document.querySelectorAll('.otp-input');

inputs.forEach((input, index) => {
    input.addEventListener('input', (e) => {
        // Move to the next input box if a number is entered
        if (e.target.value.length > 0 && index < inputs.length - 1) {
            inputs[index + 1].focus();
        }
    });

    input.addEventListener('keydown', (e) => {
        // Allow backspace to move to the previous input box
        if (e.key === 'Backspace' && e.target.value === '' && index > 0) {
            inputs[index - 1].focus();
        }
    });
});

