document.addEventListener("DOMContentLoaded", () => {
    const exportBtn = document.getElementById("exportBtn");
    const importBtn = document.getElementById("importBtn");
    const csvFile = document.getElementById("csvFile");
    const exportFormat = document.getElementById("exportFormat");
    const filterYear = document.getElementById("filterYear");
    const filterMonth = document.getElementById("filterMonth");
    const filterAlerte = document.getElementById("filterAlerte");
    const filterSite = document.getElementById("filterSite");
    const filterStatus = document.getElementById("filterStatus");
    const filterEvenement = document.getElementById("filterEvenement");
    const importForm = document.getElementById("importForm");
    const importProgress = document.getElementById("importProgress");
    const importStatus = document.getElementById("importStatus");

    // Écouteur pour le bouton Export
    if (exportBtn) {
        exportBtn.addEventListener("click", function() {
            handleExport();
        });
    }

    // Écouteur pour le bouton Import
    if (importBtn) {
        importBtn.addEventListener("click", function() {
            csvFile.click();
        });
    }

    // Écouteur pour le changement de fichier
    if (csvFile) {
        csvFile.addEventListener("change", function(e) {
            if (e.target.files && e.target.files.length > 0) {
                handleImport(e.target.files[0]);
                // Réinitialiser le fichier pour permettre de réuploader le même fichier
                csvFile.value = "";
            }
        });
    }

    /**
     * Gère l'export des messages dans le format choisi
     */
    function handleExport() {
        const params = new URLSearchParams();
        const selectedFormat = exportFormat ? exportFormat.value : "csv";

        if (filterYear && filterYear.value) params.set("year", filterYear.value);
        if (filterMonth && filterMonth.value) params.set("month", filterMonth.value);
        if (filterAlerte && filterAlerte.value) params.set("alerte", filterAlerte.value);
        if (filterSite && filterSite.value) params.set("site", filterSite.value);
        if (filterEvenement && filterEvenement.value) params.set("eventId", filterEvenement.value);
        if (filterStatus && filterStatus.value) params.set("status", filterStatus.value);
        params.set("format", selectedFormat);

        exportBtn.disabled = true;
        exportBtn.textContent = "Téléchargement...";

        fetch("/history/export?" + params.toString(), { method: "GET" })
        .then(response => {
            if (!response.ok) {
                throw new Error("Erreur lors du téléchargement");
            }
            return response.blob();
        })
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            const extension = selectedFormat === "xlsx" ? "xlsx" : selectedFormat === "pdf" ? "pdf" : "csv";
            link.download = "historique_filtre_" + new Date().toISOString().split('T')[0] + "." + extension;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);

            exportBtn.disabled = false;
            exportBtn.textContent = "📥 Exporter";
        })
        .catch(error => {
            console.error("Erreur:", error);
            exportBtn.disabled = false;
            exportBtn.textContent = "📥 Exporter";
            alert("Erreur lors de l'export: " + error.message);
        });
    }

    /**
     * Gère l'import des messages depuis un fichier CSV
     */
    function handleImport(file) {
        const lowerName = file.name.toLowerCase();
        if (!lowerName.endsWith(".csv") && !lowerName.endsWith(".xlsx") && !lowerName.endsWith(".xls")) {
            alert("Veuillez sélectionner un fichier CSV ou XLSX");
            return;
        }

        importProgress.style.display = "block";
        importStatus.textContent = "Chargement du fichier...";
        importBtn.disabled = true;

        // Créer un FormData pour l'upload
        const formData = new FormData();
        formData.append("file", file);

        fetch("/history/import", {
            method: "POST",
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                importStatus.innerHTML = `Import réussi: ${data.importedCount} message(s) importé(s).`;
                importStatus.className = "import-status success";
            } else {
                let errorMsg = `${data.message || "Import échoué"}`;
                
                if (data.errorCount && data.errorCount > 0) {
                    errorMsg += `<br/><strong>Erreurs (${data.errorCount})</strong><ul>`;
                    if (data.errors && data.errors.length > 0) {
                        data.errors.slice(0, 10).forEach(err => {
                            errorMsg += `<li>${formatError(err)}</li>`;
                        });
                        if (data.errors.length > 10) {
                            errorMsg += `<li>... et ${data.errors.length - 10} erreur(s) supplémentaire(s)</li>`;
                        }
                    }
                    errorMsg += `</ul>`;
                    errorMsg += `<br/><strong>Valides:</strong> ${data.validCount}`;
                }

                importStatus.innerHTML = errorMsg;
                importStatus.className = "import-status error";
            }

            // Masquer après 5 secondes
            setTimeout(() => {
                importProgress.style.display = "none";
            }, 5000);
        })
        .catch(error => {
            console.error("Erreur:", error);
            importStatus.innerHTML = `❌ Erreur lors de l'import: ${error.message}`;
            importStatus.className = "import-status error";

            setTimeout(() => {
                importProgress.style.display = "none";
            }, 5000);
        })
        .finally(() => {
            importBtn.disabled = false;
        });
    }

    function formatError(error) {
        if (typeof error === "string") {
            return error;
        }

        if (!error) {
            return "Erreur inconnue";
        }

        const line = error.line ? `Ligne ${error.line}` : null;
        const column = error.column ? `colonne ${error.column}` : null;
        const location = [line, column].filter(Boolean).join(", ");
        const message = error.message || "Erreur de validation";
        const value = error.value ? ` (valeur: ${error.value})` : "";

        return location ? `${location} : ${message}${value}` : `${message}${value}`;
    }
});
