const form = document.querySelector("#filter-form");

const orgId = form.dataset.orgId;
const licenseId = form.dataset.licenseId;

const licenseContainer = document.querySelector("#license-table");

form.addEventListener("submit", async (event) => {
    event.preventDefault()

    const params = new URLSearchParams(new FormData(form));

    try {
        const url = `http://localhost:8080/org/${orgId}/licenses?${params}`;
        const resp = await fetch(url, {
                method: "GET",
                headers: {
                    "Accept": "application/json"
                }
            }
        );

        if (!resp.ok) {
            throw new Error("Не удалось получить лицензии");
        }

        renderLicenses(await resp.json());
    } catch (error) {
        console.log(error);
    }
})

function renderLicenses(licenses) {
    licenseContainer.replaceChildren();

    for (const l of licenses) {
        const tableRow = document.createElement("tr");

        const title = document.createElement("td");
        const status = document.createElement("td");
        const expires = document.createElement("td");
        const linkData = document.createElement("td");
        const link = document.createElement("a")

        title.textContent = l.title;
        status.textContent = l.status;
        expires.textContent = l.expiresAt;
        link.href = `http://localhost:8080/org/${orgId}/licenses/${l.id}`;
        link.textContent = "Открыть";

        linkData.append(link);

        tableRow.append(title, status, expires, linkData);
        licenseContainer.append(tableRow);
    }
}