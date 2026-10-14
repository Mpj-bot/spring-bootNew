document.addEventListener("DOMContentLoaded", () => {
    const dialog = document.querySelector("#signup-modal");
    const openButton = document.querySelector("#open-modal");
    const closeButton = document.querySelector("#close-modal");
    const form = document.querySelector("#registration-form");
    const status = document.querySelector("#form-status");
    const loginForm = document.querySelector("#login-form");
    const loginStatus = document.querySelector("#login-status");

    if (
        !(dialog instanceof HTMLDialogElement)
        || !(openButton instanceof HTMLButtonElement)
        || !(closeButton instanceof HTMLButtonElement)
        || !(form instanceof HTMLFormElement)
        || !(status instanceof HTMLElement)
        || !(loginForm instanceof HTMLFormElement)
        || !(loginStatus instanceof HTMLElement)
    ) {
        return;
    }

    const submitForm = async (submittedForm, messageElement, defaultErrorMessage) => {
        messageElement.textContent = "Please wait...";
        const body = new URLSearchParams();
        new FormData(submittedForm).forEach((value, key) => body.append(key, String(value)));

        try {
            const response = await fetch(submittedForm.action, {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
                body
            });
            const result = await response.json();
            if (!response.ok) {
                throw new Error(result.message || defaultErrorMessage);
            }
            messageElement.textContent = result.message;
            submittedForm.reset();
            return result;
        } catch (error) {
            messageElement.textContent = error instanceof Error ? error.message : defaultErrorMessage;
            return null;
        }
    };

    openButton.addEventListener("click", () => {
        dialog.showModal();
    });

    closeButton.addEventListener("click", () => {
        dialog.close();
    });

    dialog.addEventListener("click", (event) => {
        if (event.target === dialog) {
            dialog.close();
        }
    });

    form.addEventListener("submit", (event) => {
        event.preventDefault();
        submitForm(form, status, "Could not create your account.");
    });

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const result = await submitForm(loginForm, loginStatus, "Could not sign in.");
        if (result?.redirect) {
            window.location.assign(result.redirect);
        }
    });
});