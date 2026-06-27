const state = {
    customers: [],
    cars: [],
    rentals: [],
    payments: []
};

const endpoints = {
    customers: "/api/customers",
    cars: "/api/cars",
    rentals: "/api/rentals",
    payments: "/api/payments"
};

document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    loadAll();
});

function bindEvents() {
    document.getElementById("refreshAllButton").addEventListener("click", loadAll);
    document.querySelectorAll("[data-load]").forEach((button) => {
        button.addEventListener("click", () => loadResource(button.dataset.load));
    });

    document.getElementById("customerForm").addEventListener("submit", (event) => {
        event.preventDefault();
        submitForm("customers", event.currentTarget, {
            name: getValue(event.currentTarget, "name"),
            phone: getValue(event.currentTarget, "phone"),
            email: getValue(event.currentTarget, "email"),
            licenseNumber: getValue(event.currentTarget, "licenseNumber")
        });
    });

    document.getElementById("carForm").addEventListener("submit", (event) => {
        event.preventDefault();
        submitForm("cars", event.currentTarget, {
            plateNumber: getValue(event.currentTarget, "plateNumber"),
            brand: getValue(event.currentTarget, "brand"),
            model: getValue(event.currentTarget, "model"),
            color: getValue(event.currentTarget, "color"),
            year: Number(getValue(event.currentTarget, "year")),
            status: getValue(event.currentTarget, "status"),
            dailyRate: Number(getValue(event.currentTarget, "dailyRate"))
        });
    });

    document.getElementById("rentalForm").addEventListener("submit", (event) => {
        event.preventDefault();
        const actualReturnDate = getValue(event.currentTarget, "actualReturnDate");
        submitForm("rentals", event.currentTarget, {
            customerId: Number(getValue(event.currentTarget, "customerId")),
            carId: Number(getValue(event.currentTarget, "carId")),
            rentalDate: getValue(event.currentTarget, "rentalDate"),
            returnDate: getValue(event.currentTarget, "returnDate"),
            actualReturnDate: actualReturnDate || null,
            totalAmount: Number(getValue(event.currentTarget, "totalAmount")),
            status: getValue(event.currentTarget, "status")
        });
    });

    document.getElementById("paymentForm").addEventListener("submit", (event) => {
        event.preventDefault();
        submitForm("payments", event.currentTarget, {
            rentalId: Number(getValue(event.currentTarget, "rentalId")),
            paymentDate: getValue(event.currentTarget, "paymentDate"),
            amount: Number(getValue(event.currentTarget, "amount")),
            paymentMethod: getValue(event.currentTarget, "paymentMethod")
        });
    });
}

async function loadAll() {
    await Promise.all([
        loadResource("customers"),
        loadResource("cars"),
        loadResource("rentals"),
        loadResource("payments")
    ]);
}

async function loadResource(resourceName) {
    try {
        const response = await fetch(endpoints[resourceName]);
        const data = await parseResponse(response);
        state[resourceName] = data;
        render();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function submitForm(resourceName, form, payload) {
    try {
        const response = await fetch(endpoints[resourceName], {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });
        await parseResponse(response);
        form.reset();
        await loadAll();
        showMessage("新增成功。");
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function parseResponse(response) {
    if (response.ok) {
        if (response.status === 204) {
            return null;
        }
        return response.json();
    }

    const errorBody = await response.json().catch(() => null);
    const details = errorBody?.details?.length ? ` ${errorBody.details.join(" ; ")}` : "";
    const message = errorBody?.message || `请求失败：${response.status}`;
    throw new Error(`${message}${details}`);
}

function render() {
    renderStats();
    renderCustomers();
    renderCars();
    renderRentals();
    renderPayments();
}

function renderStats() {
    document.getElementById("customerCount").textContent = state.customers.length;
    document.getElementById("carCount").textContent = state.cars.length;
    document.getElementById("rentalCount").textContent = state.rentals.length;
    document.getElementById("paymentCount").textContent = state.payments.length;
}

function renderCustomers() {
    document.getElementById("customerTable").innerHTML = state.customers.map((item) => `
        <tr>
            <td>${item.customerId}</td>
            <td>${escapeHtml(item.name)}</td>
            <td>${escapeHtml(item.phone)}</td>
            <td>${escapeHtml(item.email)}</td>
            <td>${escapeHtml(item.licenseNumber)}</td>
        </tr>
    `).join("");
}

function renderCars() {
    document.getElementById("carTable").innerHTML = state.cars.map((item) => `
        <tr>
            <td>${item.carId}</td>
            <td>${escapeHtml(item.plateNumber)}</td>
            <td>${escapeHtml(item.brand)} ${escapeHtml(item.model)}</td>
            <td>${escapeHtml(item.status)}</td>
            <td>${formatMoney(item.dailyRate)}</td>
        </tr>
    `).join("");
}

function renderRentals() {
    document.getElementById("rentalTable").innerHTML = state.rentals.map((item) => `
        <tr>
            <td>${item.rentalId}</td>
            <td>客户 ${item.customerId} / 车辆 ${item.carId}</td>
            <td>${escapeHtml(item.rentalDate)} ~ ${escapeHtml(item.returnDate)}</td>
            <td>${formatMoney(item.totalAmount)}</td>
            <td>${escapeHtml(item.status)}</td>
        </tr>
    `).join("");
}

function renderPayments() {
    document.getElementById("paymentTable").innerHTML = state.payments.map((item) => `
        <tr>
            <td>${item.paymentId}</td>
            <td>${item.rentalId}</td>
            <td>${escapeHtml(item.paymentDate)}</td>
            <td>${formatMoney(item.amount)}</td>
            <td>${escapeHtml(item.paymentMethod)}</td>
        </tr>
    `).join("");
}

function getValue(form, name) {
    return form.elements.namedItem(name).value.trim();
}

function formatMoney(value) {
    return Number(value).toFixed(2);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}

function showMessage(message, isError = false) {
    const box = document.getElementById("messageBox");
    box.textContent = message;
    box.classList.remove("hidden", "error");
    if (isError) {
        box.classList.add("error");
    }

    window.clearTimeout(showMessage.timerId);
    showMessage.timerId = window.setTimeout(() => {
        box.classList.add("hidden");
    }, 3200);
}
