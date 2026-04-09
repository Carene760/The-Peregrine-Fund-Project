document.addEventListener("DOMContentLoaded", () => {
    const checkboxes = Array.from(document.querySelectorAll(".event-message-checkbox"));
    const createButton = document.getElementById("createEventBtn");
    const titleInput = document.getElementById("eventTitle");
    const dateInput = document.getElementById("eventDate");
    const descriptionInput = document.getElementById("eventDescription");
    const counter = document.getElementById("selectedEventCount");

    if (!checkboxes.length || !createButton || !titleInput || !dateInput || !descriptionInput || !counter) {
        return;
    }

    const today = new Date();
    const localDate = new Date(today.getTime() - (today.getTimezoneOffset() * 60000)).toISOString().slice(0, 10);
    if (!dateInput.value) {
        dateInput.value = localDate;
    }

    function getSelectedMessageIds() {
        return checkboxes
            .filter(checkbox => checkbox.checked)
            .map(checkbox => parseInt(checkbox.value, 10))
            .filter(Number.isFinite);
    }

    function refreshSelectionState() {
        const selectedCount = getSelectedMessageIds().length;
        counter.textContent = `${selectedCount} message${selectedCount > 1 ? "s" : ""} sélectionné${selectedCount > 1 ? "s" : ""}`;
        createButton.disabled = selectedCount === 0;
        createButton.classList.toggle("is-disabled", selectedCount === 0);
    }

    checkboxes.forEach(checkbox => {
        checkbox.addEventListener("change", refreshSelectionState);
    });

    createButton.addEventListener("click", async () => {
        const messageIds = getSelectedMessageIds();
        const nom = titleInput.value.trim();
        const date = dateInput.value.trim();
        const description = descriptionInput.value.trim();

        if (!messageIds.length) {
            window.alert("Sélectionnez au moins un message.");
            return;
        }

        if (!nom) {
            window.alert("Le titre de l'événement est obligatoire.");
            titleInput.focus();
            return;
        }

        if (!date) {
            window.alert("La date de l'événement est obligatoire.");
            dateInput.focus();
            return;
        }

        const confirmCreation = window.confirm(`Créer l'événement \"${nom}\" avec ${messageIds.length} message${messageIds.length > 1 ? "s" : ""} ?`);
        if (!confirmCreation) {
            return;
        }

        const previousLabel = createButton.textContent;
        createButton.disabled = true;
        createButton.textContent = "Création en cours...";

        try {
            const response = await fetch("/evenements/create", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify({
                    nom,
                    date,
                    description,
                    messageIds
                })
            });

            const contentType = response.headers.get("content-type") || "";
            if (!contentType.includes("application/json")) {
                const responseText = await response.text();
                const isHtmlResponse = responseText.includes("<html") || responseText.includes("<!DOCTYPE html");
                if (isHtmlResponse) {
                    throw new Error("Réponse inattendue du serveur (authentification ou redirection).");
                }
                throw new Error("Réponse invalide du serveur.");
            }

            const payload = await response.json();
            if (!response.ok || !payload.success) {
                window.alert(payload.message || "La création de l'événement a échoué.");
                return;
            }

            window.location.href = payload.redirectUrl || "/evenements";
        } catch (error) {
            console.error("Erreur lors de la création de l'événement", error);
            window.alert(error?.message || "Une erreur est survenue pendant la création de l'événement.");
        } finally {
            createButton.textContent = previousLabel;
            refreshSelectionState();
        }
    });

    refreshSelectionState();
});
