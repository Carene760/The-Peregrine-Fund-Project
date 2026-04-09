document.addEventListener("DOMContentLoaded", () => {
    // Écouteurs pour les boutons Éditer
    document.querySelectorAll(".js-edit-message").forEach(btn => {
        btn.addEventListener("click", function() {
            const messageId = parseInt(this.getAttribute("data-message-id"));
            enableEditMode(messageId);
        });
    });

    // Écouteurs pour les boutons Valider
    document.querySelectorAll(".js-validate-message").forEach(btn => {
        btn.addEventListener("click", function() {
            const messageId = parseInt(this.getAttribute("data-message-id"));
            validateAndSave(messageId);
        });
    });

    // Écouteurs pour les boutons Annuler
    document.querySelectorAll(".js-cancel-message").forEach(btn => {
        btn.addEventListener("click", function() {
            const messageId = parseInt(this.getAttribute("data-message-id"));
            disableEditMode(messageId);
        });
    });

    /**
     * Active le mode édition pour une ligne de message
     */
    function enableEditMode(messageId) {
        const row = document.querySelector(`[data-message-id="${messageId}"]`);
        if (!row) return;

        // Masquer les valeurs affichées et afficher les inputs
        row.querySelectorAll(".display-value").forEach(span => {
            span.classList.add("is-hidden");
        });
        row.querySelectorAll(".edit-input").forEach(input => {
            input.classList.remove("is-hidden");
        });

        // Basculer boutons
        row.querySelector(".js-edit-message").classList.add("is-hidden");
        row.querySelector(".js-validate-message").classList.remove("is-hidden");
        row.querySelector(".js-cancel-message").classList.remove("is-hidden");

        // Focus sur le premier input
        const firstInput = row.querySelector(".edit-input");
        if (firstInput) {
            setTimeout(() => firstInput.focus(), 100);
        }
    }

    /**
     * Désactive le mode édition pour une ligne de message
     */
    function disableEditMode(messageId) {
        const row = document.querySelector(`[data-message-id="${messageId}"]`);
        if (!row) return;

        // Afficher les valeurs affichées et masquer les inputs
        row.querySelectorAll(".display-value").forEach(span => {
            span.classList.remove("is-hidden");
        });
        row.querySelectorAll(".edit-input").forEach(input => {
            input.classList.add("is-hidden");
        });

        // Basculer boutons
        row.querySelector(".js-edit-message").classList.remove("is-hidden");
        row.querySelector(".js-validate-message").classList.add("is-hidden");
        row.querySelector(".js-cancel-message").classList.add("is-hidden");
    }

    /**
     * Valide et sauvegarde les modifications
     */
    function validateAndSave(messageId) {
        const row = document.querySelector(`[data-message-id="${messageId}"]`);
        if (!row) return;

        // Afficher dialogue de confirmation
        const confirmed = confirm("Ce message va être modifié. Êtes-vous sûr de vouloir accepter ? Cela aura un impact sur les statistiques.");
        if (!confirmed) {
            return;
        }

        // Collecter les valeurs des inputs
        const updateData = collectFormData(row);

        // Envoyer la requête POST
        fetch(`/history/messages/${messageId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(updateData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Mettre à jour les valeurs affichées et désactiver le mode édition
                updateDisplayValues(row, updateData);
                disableEditMode(messageId);
                alert("Message modifié avec succès!");
            } else {
                alert("Erreur: " + (data.message || "Mise à jour échouée"));
            }
        })
        .catch(error => {
            console.error("Erreur lors de la mise à jour:", error);
            alert("Erreur lors de la mise à jour du message");
        });
    }

    /**
     * Collecte les données des inputs modifiés
     */
    function collectFormData(row) {
        const updateData = {};
        const inputs = row.querySelectorAll(".edit-input");

        inputs.forEach((input, index) => {
            let value = null;
            
            if (input.tagName === "SELECT") {
                // Pour les selects
                value = input.value;
                if (input.value === "") {
                    value = null;
                } else if (["true", "false"].includes(input.value)) {
                    // Cas boolean
                    value = input.value === "true";
                } else {
                    // Essayer de convertir en nombre pour les IDs
                    const parsed = parseInt(input.value);
                    value = isNaN(parsed) ? input.value : parsed;
                }
            } else if (input.tagName === "TEXTAREA") {
                value = input.value;
            } else if (input.type === "number") {
                value = input.value === "" ? null : parseFloat(input.value);
            } else if (input.type === "datetime-local") {
                value = input.value ? input.value : null;
            } else {
                value = input.value;
            }

            // Déterminer le champ associé (par ordre de colonnes)
            const fieldName = getFieldNameByIndex(index);
            if (fieldName) {
                updateData[fieldName] = value;
            }
        });

        return updateData;
    }

    /**
     * Retourne le nom du champ selon l'index de l'input
     */
    function getFieldNameByIndex(index) {
        const fields = [
            "dateCommencement",
            "dateSignalement",
            "pointRepere",
            "surfaceApproximative",
            "description",
            "direction",
            "renfort",
            "longitude",
            "latitude",
            "idIntervention",
            "idEvenement",
            "idTypeAlerte",
            "idStatus"
        ];
        return fields[index] || null;
    }

    /**
     * Met à jour les valeurs affichées après une mise à jour réussie
     */
    function updateDisplayValues(row, updateData) {
        const displayValues = row.querySelectorAll(".display-value");
        const inputs = row.querySelectorAll(".edit-input");

        inputs.forEach((input, index) => {
            const fieldName = getFieldNameByIndex(index);
            if (fieldName && updateData.hasOwnProperty(fieldName)) {
                let displayValue = updateData[fieldName];

                // Formater la valeur pour l'affichage
                if (input.tagName === "SELECT") {
                    displayValue = input.options[input.selectedIndex] ? input.options[input.selectedIndex].text : "";
                } else if (fieldName === "renfort") {
                    displayValue = displayValue ? "Oui" : "Non";
                } else if (input.type === "number" && displayValue !== null) {
                    displayValue = displayValue.toString();
                }

                // Mettre à jour le texte affiché
                if (displayValues[index]) {
                    displayValues[index].textContent = displayValue || "";
                }
            }
        });
    }
});
