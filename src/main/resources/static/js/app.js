(() => {
    "use strict";

    const STORAGE_KEYS = {
        accessToken: "accessToken",
        refreshToken: "refreshToken",
        theme: "educore-theme"
    };

    const state = {
        selectedLoginRole: "ROLE_USER",
        user: null, // { username, role }
        page: 0,
        size: 10,
        sortBy: "id",
        direction: "asc",
        search: "",
        course: "",
        totalPages: 0,
        totalElements: 0,
        students: [],
        editingStudentId: null,
        deletingStudent: null,
        searchTimer: null
    };

    /* ================= ELEMENTS ================= */
    const el = {
        loginView: document.getElementById("loginView"),
        dashboardView: document.getElementById("dashboardView"),
        loginForm: document.getElementById("loginForm"),
        loginUsername: document.getElementById("loginUsername"),
        loginPassword: document.getElementById("loginPassword"),
        togglePasswordBtn: document.getElementById("togglePasswordBtn"),
        loginError: document.getElementById("loginError"),
        loginSubmitBtn: document.getElementById("loginSubmitBtn"),
        roleTabs: document.querySelectorAll(".role-tab"),
        themeToggles: document.querySelectorAll("[data-theme-toggle]"),

        navUsername: document.getElementById("navUsername"),
        navUserRole: document.getElementById("navUserRole"),
        logoutBtn: document.getElementById("logoutBtn"),
        totalCountBadge: document.getElementById("totalCountBadge"),
        addStudentBtn: document.getElementById("addStudentBtn"),

        searchInput: document.getElementById("searchInput"),
        courseFilter: document.getElementById("courseFilter"),
        sortSelect: document.getElementById("sortSelect"),

        studentTable: document.getElementById("studentTable"),
        studentTableBody: document.getElementById("studentTableBody"),
        actionsHeader: document.getElementById("actionsHeader"),
        loadingSkeleton: document.getElementById("loadingSkeleton"),
        emptyState: document.getElementById("emptyState"),
        emptyStateTitle: document.getElementById("emptyStateTitle"),
        emptyStateText: document.getElementById("emptyStateText"),
        clearFiltersBtn: document.getElementById("clearFiltersBtn"),
        emptyAddBtn: document.getElementById("emptyAddBtn"),

        paginationFooter: document.getElementById("paginationFooter"),
        paginationSummary: document.getElementById("paginationSummary"),
        pageSizeSelect: document.getElementById("pageSizeSelect"),
        prevPageBtn: document.getElementById("prevPageBtn"),
        nextPageBtn: document.getElementById("nextPageBtn"),
        pageNumbers: document.getElementById("pageNumbers"),

        studentDialog: document.getElementById("studentDialog"),
        studentDialogTitle: document.getElementById("studentDialogTitle"),
        studentDialogSubtitle: document.getElementById("studentDialogSubtitle"),
        closeStudentDialogBtn: document.getElementById("closeStudentDialogBtn"),
        cancelStudentDialogBtn: document.getElementById("cancelStudentDialogBtn"),
        studentForm: document.getElementById("studentForm"),
        studentFormError: document.getElementById("studentFormError"),
        saveStudentBtn: document.getElementById("saveStudentBtn"),
        studentName: document.getElementById("studentName"),
        studentEmail: document.getElementById("studentEmail"),
        studentPhone: document.getElementById("studentPhone"),
        studentCity: document.getElementById("studentCity"),
        studentCourse: document.getElementById("studentCourse"),
        studentMarks: document.getElementById("studentMarks"),

        deleteDialog: document.getElementById("deleteDialog"),
        deleteDialogMessage: document.getElementById("deleteDialogMessage"),
        cancelDeleteBtn: document.getElementById("cancelDeleteBtn"),
        confirmDeleteBtn: document.getElementById("confirmDeleteBtn"),

        toastRegion: document.getElementById("toastRegion")
    };

    /* ================= THEME ================= */
    function toggleTheme() {
        const current = document.documentElement.getAttribute("data-theme") === "dark" ? "dark" : "light";
        const next = current === "dark" ? "light" : "dark";
        document.documentElement.setAttribute("data-theme", next);
        localStorage.setItem(STORAGE_KEYS.theme, next);
    }

    el.themeToggles.forEach((btn) => btn.addEventListener("click", toggleTheme));

    /* ================= TOASTS ================= */
    function showToast(message, type = "success") {
        const toast = document.createElement("div");
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        el.toastRegion.appendChild(toast);

        window.setTimeout(() => {
            toast.remove();
        }, 3500);
    }

    /* ================= JWT HELPERS ================= */
    function parseJwtPayload(token) {
        if (!token || typeof token !== "string") {
            return null;
        }
        try {
            const parts = token.split(".");
            if (parts.length !== 3) {
                return null;
            }
            const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
            const padded = base64.padEnd(base64.length + (4 - (base64.length % 4)) % 4, "=");
            return JSON.parse(atob(padded));
        } catch {
            return null;
        }
    }

    function isTokenExpired(payload) {
        if (!payload || typeof payload.exp !== "number") {
            return true;
        }
        return payload.exp * 1000 <= Date.now();
    }

    function isAdmin() {
        return state.user && state.user.role === "ROLE_ADMIN";
    }

    function clearAuthTokens() {
        localStorage.removeItem(STORAGE_KEYS.accessToken);
        localStorage.removeItem(STORAGE_KEYS.refreshToken);
        state.user = null;
    }

    async function refreshAccessToken() {
        const refreshToken = localStorage.getItem(STORAGE_KEYS.refreshToken);
        if (!refreshToken) {
            return false;
        }

        try {
            const response = await fetch("/auth/refresh", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ refreshToken })
            });
            if (!response.ok) {
                return false;
            }
            const data = await response.json();
            if (!data.token) {
                return false;
            }
            localStorage.setItem(STORAGE_KEYS.accessToken, data.token);
            if (data.refreshToken) {
                localStorage.setItem(STORAGE_KEYS.refreshToken, data.refreshToken);
            }
            const payload = parseJwtPayload(data.token);
            if (payload) {
                state.user = {
                    username: payload.sub || "user",
                    role: payload.role || "ROLE_USER"
                };
            }
            return true;
        } catch {
            return false;
        }
    }

    async function apiFetch(url, options = {}, allowRetry = true) {
        const token = localStorage.getItem(STORAGE_KEYS.accessToken);
        const headers = new Headers(options.headers || {});
        if (token) {
            headers.set("Authorization", `Bearer ${token}`);
        }
        if (options.body && !headers.has("Content-Type")) {
            headers.set("Content-Type", "application/json");
        }

        const response = await fetch(url, { ...options, headers });

        if (response.status === 401 && allowRetry) {
            const refreshed = await refreshAccessToken();
            if (refreshed) {
                return apiFetch(url, options, false);
            }
            handleSessionExpired();
            throw new Error("SESSION_EXPIRED");
        }

        return response;
    }

    function handleSessionExpired() {
        clearAuthTokens();
        showLoginView();
        showToast("Session expired. Please sign in again.", "error");
    }

    /* ================= VIEW SWITCHING ================= */
    function showLoginView() {
        el.dashboardView.hidden = true;
        el.loginView.hidden = false;
        el.loginForm.reset();
        hideInlineError(el.loginError);
        el.loginUsername.focus();
    }

    function showDashboardView() {
        el.loginView.hidden = true;
        el.dashboardView.hidden = false;

        el.navUsername.textContent = state.user.username;
        el.navUserRole.textContent = isAdmin() ? "ADMIN" : "USER";
        el.addStudentBtn.hidden = !isAdmin();
        el.actionsHeader.hidden = !isAdmin();

        loadCourses();
        loadStudents();
    }

    function showInlineError(node, message) {
        node.textContent = message;
        node.hidden = false;
    }

    function hideInlineError(node) {
        node.textContent = "";
        node.hidden = true;
    }

    function setButtonLoading(button, loading) {
        button.disabled = loading;
        button.classList.toggle("is-loading", loading);
    }

    /* ================= LOGIN FLOW ================= */
    el.roleTabs.forEach((tab) => {
        tab.addEventListener("click", () => {
            state.selectedLoginRole = tab.dataset.role;
            el.roleTabs.forEach((t) => {
                const active = t === tab;
                t.classList.toggle("is-active", active);
                t.setAttribute("aria-selected", String(active));
            });
            hideInlineError(el.loginError);
        });
    });

    el.togglePasswordBtn.addEventListener("click", () => {
        const isShowing = el.loginPassword.type === "text";
        el.loginPassword.type = isShowing ? "password" : "text";
        el.togglePasswordBtn.setAttribute("aria-pressed", String(!isShowing));
        el.togglePasswordBtn.setAttribute("aria-label", isShowing ? "Show password" : "Hide password");
    });

    el.loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        hideInlineError(el.loginError);

        const username = el.loginUsername.value.trim();
        const password = el.loginPassword.value;

        if (!username || !password) {
            showInlineError(el.loginError, "Please enter both username and password.");
            return;
        }

        setButtonLoading(el.loginSubmitBtn, true);

        try {
            const response = await fetch("/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                showInlineError(el.loginError, "Invalid username or password.");
                return;
            }

            const data = await response.json();
            const payload = parseJwtPayload(data.token);
            const actualRole = payload && payload.role ? payload.role : "ROLE_USER";

            if (actualRole !== state.selectedLoginRole) {
                const expectedLabel = state.selectedLoginRole === "ROLE_ADMIN" ? "Admin" : "User";
                const actualLabel = actualRole === "ROLE_ADMIN" ? "Admin" : "User";
                showInlineError(
                    el.loginError,
                    `This account has ${actualLabel} access. Switch to the ${actualLabel} tab instead of ${expectedLabel}.`
                );
                return;
            }

            localStorage.setItem(STORAGE_KEYS.accessToken, data.token);
            localStorage.setItem(STORAGE_KEYS.refreshToken, data.refreshToken);

            state.user = {
                username: (payload && payload.sub) || username,
                role: actualRole
            };
            state.page = 0;
            state.search = "";
            state.course = "";
            el.searchInput.value = "";
            el.courseFilter.value = "";

            showDashboardView();
            showToast(`Signed in as ${state.user.username}`);
        } catch {
            showInlineError(el.loginError, "Unable to reach the server. Please try again.");
        } finally {
            setButtonLoading(el.loginSubmitBtn, false);
        }
    });

    el.logoutBtn.addEventListener("click", () => {
        clearAuthTokens();
        showLoginView();
        showToast("Signed out");
    });

    /* ================= DATA FETCHING ================= */
    async function loadCourses() {
        try {
            const response = await apiFetch("/studentmgmt/courses");
            if (!response.ok) {
                return;
            }
            const courses = await response.json();
            const previous = el.courseFilter.value;

            el.courseFilter.innerHTML = "";
            const defaultOpt = document.createElement("option");
            defaultOpt.value = "";
            defaultOpt.textContent = "All courses";
            el.courseFilter.appendChild(defaultOpt);

            courses.forEach((course) => {
                const opt = document.createElement("option");
                opt.value = course;
                opt.textContent = course;
                el.courseFilter.appendChild(opt);
            });

            if (courses.includes(previous)) {
                el.courseFilter.value = previous;
            }
        } catch (err) {
            if (err.message !== "SESSION_EXPIRED") {
                // Non-fatal: filter list stays with default option
            }
        }
    }

    function setLoadingState(loading) {
        el.loadingSkeleton.hidden = !loading;
        if (loading) {
            el.studentTable.hidden = true;
            el.emptyState.hidden = true;
            el.paginationFooter.hidden = true;
        }
    }

    async function loadStudents() {
        setLoadingState(true);

        const params = new URLSearchParams({
            page: String(state.page),
            size: String(state.size),
            sortBy: state.sortBy,
            direction: state.direction
        });

        if (state.search.trim()) {
            params.set("search", state.search.trim());
        }
        if (state.course) {
            params.set("course", state.course);
        }

        try {
            const response = await apiFetch(`/studentmgmt?${params.toString()}`);
            if (!response.ok) {
                throw new Error("FETCH_FAILED");
            }

            const pageData = await response.json();
            state.students = pageData.content || [];
            state.totalPages = pageData.totalPages || 0;
            state.totalElements = pageData.totalElements || 0;
            state.page = pageData.number || 0;

            if (state.students.length === 0 && state.page > 0 && state.totalPages > 0) {
                state.page = state.totalPages - 1;
                return loadStudents();
            }

            renderStudents();
            renderPagination();
        } catch (err) {
            if (err.message !== "SESSION_EXPIRED") {
                setLoadingState(false);
                showEmptyState(
                    "Unable to load students",
                    "There was a problem reaching the server. Please try again."
                );
                showToast("Failed to load student records", "error");
            }
        }
    }

    /* ================= RENDERING ================= */
    function getInitials(name) {
        if (!name) {
            return "?";
        }
        const parts = name.trim().split(/\s+/);
        if (parts.length === 1) {
            return parts[0].slice(0, 2).toUpperCase();
        }
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }

    function getMarksTierClass(marks) {
        if (marks >= 80) return "marks-high";
        if (marks >= 50) return "marks-mid";
        return "marks-low";
    }

    function showEmptyState(title, text) {
        el.studentTable.hidden = true;
        el.paginationFooter.hidden = true;
        el.emptyStateTitle.textContent = title;
        el.emptyStateText.textContent = text;

        const hasActiveFilter = Boolean(state.search.trim() || state.course);
        el.clearFiltersBtn.hidden = !hasActiveFilter;
        el.emptyAddBtn.hidden = hasActiveFilter || !isAdmin();
        el.emptyState.hidden = false;
    }

    function renderStudents() {
        setLoadingState(false);
        el.totalCountBadge.textContent = String(state.totalElements);
        el.studentTableBody.innerHTML = "";

        if (state.students.length === 0) {
            const hasActiveFilter = Boolean(state.search.trim() || state.course);
            showEmptyState(
                hasActiveFilter ? "No matching students" : "No students yet",
                hasActiveFilter
                    ? "Try adjusting your search query or course filter."
                    : isAdmin()
                        ? "Add your first student record to populate the directory."
                        : "No student records are available at the moment."
            );
            return;
        }

        el.emptyState.hidden = true;
        el.studentTable.hidden = false;

        const fragment = document.createDocumentFragment();

        state.students.forEach((student) => {
            const tr = document.createElement("tr");

            const tdId = document.createElement("td");
            tdId.className = "col-id";
            tdId.textContent = `#${student.id}`;
            tr.appendChild(tdId);

            const tdStudent = document.createElement("td");
            const studentCell = document.createElement("div");
            studentCell.className = "student-cell";

            const avatar = document.createElement("span");
            avatar.className = "avatar";
            avatar.setAttribute("aria-hidden", "true");
            avatar.textContent = getInitials(student.name);

            const meta = document.createElement("div");
            meta.className = "student-meta";

            const nameSpan = document.createElement("span");
            nameSpan.className = "student-name";
            nameSpan.textContent = student.name;

            const emailSpan = document.createElement("span");
            emailSpan.className = "student-email";
            emailSpan.textContent = student.email;

            meta.append(nameSpan, emailSpan);
            studentCell.append(avatar, meta);
            tdStudent.appendChild(studentCell);
            tr.appendChild(tdStudent);

            const tdPhone = document.createElement("td");
            tdPhone.textContent = student.phone;
            tr.appendChild(tdPhone);

            const tdCity = document.createElement("td");
            tdCity.textContent = student.city;
            tr.appendChild(tdCity);

            const tdCourse = document.createElement("td");
            const courseBadge = document.createElement("span");
            courseBadge.className = "course-badge";
            courseBadge.textContent = student.course;
            tdCourse.appendChild(courseBadge);
            tr.appendChild(tdCourse);

            const tdMarks = document.createElement("td");
            tdMarks.className = "col-num";
            const marksPill = document.createElement("span");
            marksPill.className = `marks-pill ${getMarksTierClass(Number(student.marks))}`;
            marksPill.textContent = `${student.marks}%`;
            tdMarks.appendChild(marksPill);
            tr.appendChild(tdMarks);

            if (isAdmin()) {
                const tdActions = document.createElement("td");
                tdActions.className = "col-actions";

                const actionWrap = document.createElement("div");
                actionWrap.className = "row-actions";

                const editBtn = document.createElement("button");
                editBtn.type = "button";
                editBtn.className = "icon-btn";
                editBtn.setAttribute("aria-label", `Edit ${student.name}`);
                editBtn.title = "Edit student";
                editBtn.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 20h9"></path><path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"></path></svg>`;
                editBtn.addEventListener("click", () => openEditDialog(student));

                const deleteBtn = document.createElement("button");
                deleteBtn.type = "button";
                deleteBtn.className = "icon-btn is-danger";
                deleteBtn.setAttribute("aria-label", `Delete ${student.name}`);
                deleteBtn.title = "Delete student";
                deleteBtn.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 6h18"></path><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path></svg>`;
                deleteBtn.addEventListener("click", () => openDeleteDialog(student));

                actionWrap.append(editBtn, deleteBtn);
                tdActions.appendChild(actionWrap);
                tr.appendChild(tdActions);
            }

            fragment.appendChild(tr);
        });

        el.studentTableBody.appendChild(fragment);
    }

    function renderPagination() {
        if (state.totalElements === 0) {
            el.paginationFooter.hidden = true;
            return;
        }

        el.paginationFooter.hidden = false;
        const start = state.page * state.size + 1;
        const end = Math.min((state.page + 1) * state.size, state.totalElements);
        el.paginationSummary.textContent = `Showing ${start}–${end} of ${state.totalElements}`;

        el.prevPageBtn.disabled = state.page <= 0;
        el.nextPageBtn.disabled = state.page >= state.totalPages - 1;

        el.pageNumbers.innerHTML = "";
        const maxButtons = 5;
        let startPage = Math.max(0, state.page - Math.floor(maxButtons / 2));
        let endPage = Math.min(state.totalPages - 1, startPage + maxButtons - 1);
        startPage = Math.max(0, endPage - maxButtons + 1);

        for (let p = startPage; p <= endPage; p++) {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = `page-num-btn${p === state.page ? " is-active" : ""}`;
            btn.textContent = String(p + 1);
            btn.setAttribute("aria-label", `Page ${p + 1}`);
            if (p === state.page) {
                btn.setAttribute("aria-current", "page");
            }
            btn.addEventListener("click", () => {
                if (p !== state.page) {
                    state.page = p;
                    loadStudents();
                }
            });
            el.pageNumbers.appendChild(btn);
        }
    }

    /* ================= SEARCH, FILTER & PAGINATION EVENTS ================= */
    el.searchInput.addEventListener("input", () => {
        window.clearTimeout(state.searchTimer);
        state.searchTimer = window.setTimeout(() => {
            state.search = el.searchInput.value;
            state.page = 0;
            loadStudents();
        }, 250);
    });

    el.courseFilter.addEventListener("change", () => {
        state.course = el.courseFilter.value;
        state.page = 0;
        loadStudents();
    });

    el.sortSelect.addEventListener("change", () => {
        const [sortBy, direction] = el.sortSelect.value.split(":");
        state.sortBy = sortBy || "id";
        state.direction = direction || "asc";
        state.page = 0;
        loadStudents();
    });

    el.pageSizeSelect.addEventListener("change", () => {
        state.size = Number(el.pageSizeSelect.value) || 10;
        state.page = 0;
        loadStudents();
    });

    el.prevPageBtn.addEventListener("click", () => {
        if (state.page > 0) {
            state.page -= 1;
            loadStudents();
        }
    });

    el.nextPageBtn.addEventListener("click", () => {
        if (state.page < state.totalPages - 1) {
            state.page += 1;
            loadStudents();
        }
    });

    el.clearFiltersBtn.addEventListener("click", () => {
        state.search = "";
        state.course = "";
        state.page = 0;
        el.searchInput.value = "";
        el.courseFilter.value = "";
        loadStudents();
    });

    /* ================= ADD / EDIT DIALOG ================= */
    function clearFormValidation() {
        hideInlineError(el.studentFormError);
        el.studentForm.querySelectorAll("[data-error-for]").forEach((span) => {
            span.textContent = "";
        });
        el.studentForm.querySelectorAll("input").forEach((input) => {
            input.removeAttribute("aria-invalid");
        });
    }

    function applyFieldErrors(errors) {
        if (!errors) return;
        Object.entries(errors).forEach(([field, message]) => {
            const errSpan = el.studentForm.querySelector(`[data-error-for="${field}"]`);
            const input = el.studentForm.querySelector(`[name="${field}"]`);
            if (errSpan) errSpan.textContent = message;
            if (input) input.setAttribute("aria-invalid", "true");
        });
    }

    function openAddDialog() {
        if (!isAdmin()) {
            showToast("Administrator access required", "error");
            return;
        }
        state.editingStudentId = null;
        el.studentForm.reset();
        clearFormValidation();
        el.studentDialogTitle.textContent = "Add Student";
        el.studentDialogSubtitle.textContent = "Create a new student record in Educore.";
        el.saveStudentBtn.querySelector(".btn-label").textContent = "Create Student";
        el.studentDialog.showModal();
        el.studentName.focus();
    }

    function openEditDialog(student) {
        if (!isAdmin()) {
            showToast("Administrator access required", "error");
            return;
        }
        state.editingStudentId = student.id;
        clearFormValidation();
        el.studentDialogTitle.textContent = "Edit Student";
        el.studentDialogSubtitle.textContent = `Update details for ${student.name}.`;
        el.saveStudentBtn.querySelector(".btn-label").textContent = "Save Changes";

        el.studentName.value = student.name;
        el.studentEmail.value = student.email;
        el.studentPhone.value = student.phone;
        el.studentCity.value = student.city;
        el.studentCourse.value = student.course;
        el.studentMarks.value = String(student.marks);

        el.studentDialog.showModal();
        el.studentName.focus();
    }

    function closeStudentDialog() {
        el.studentDialog.close();
    }

    el.addStudentBtn.addEventListener("click", openAddDialog);
    el.emptyAddBtn.addEventListener("click", openAddDialog);
    el.closeStudentDialogBtn.addEventListener("click", closeStudentDialog);
    el.cancelStudentDialogBtn.addEventListener("click", closeStudentDialog);

    el.studentDialog.addEventListener("click", (event) => {
        if (event.target === el.studentDialog) {
            closeStudentDialog();
        }
    });

    el.studentForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearFormValidation();

        const payload = {
            name: el.studentName.value.trim(),
            email: el.studentEmail.value.trim(),
            phone: el.studentPhone.value.trim(),
            city: el.studentCity.value.trim(),
            course: el.studentCourse.value.trim(),
            marks: el.studentMarks.value === "" ? null : Number(el.studentMarks.value)
        };

        const clientErrors = {};
        if (!payload.name) clientErrors.name = "Name cannot be empty";
        if (!payload.email) clientErrors.email = "Email cannot be empty";
        if (!/^[0-9]{10}$/.test(payload.phone)) clientErrors.phone = "Phone must contain exactly 10 digits";
        if (!payload.city) clientErrors.city = "City cannot be blank";
        if (!payload.course) clientErrors.course = "Course cannot be blank";
        if (payload.marks === null || Number.isNaN(payload.marks) || payload.marks < 0 || payload.marks > 100) {
            clientErrors.marks = "Marks must be between 0 and 100";
        }

        if (Object.keys(clientErrors).length > 0) {
            applyFieldErrors(clientErrors);
            return;
        }

        const isEdit = state.editingStudentId !== null;
        const url = isEdit ? `/studentmgmt/${state.editingStudentId}` : "/studentmgmt";
        const method = isEdit ? "PUT" : "POST";

        setButtonLoading(el.saveStudentBtn, true);

        try {
            const response = await apiFetch(url, {
                method,
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                let errBody = null;
                try {
                    errBody = await response.json();
                } catch {
                    errBody = null;
                }

                if (errBody && errBody.validationErrors) {
                    applyFieldErrors(errBody.validationErrors);
                } else {
                    showInlineError(
                        el.studentFormError,
                        (errBody && errBody.message) || "Unable to save student. Please check your input."
                    );
                }
                return;
            }

            closeStudentDialog();
            showToast(isEdit ? "Student updated successfully" : "Student added successfully");
            await Promise.all([loadCourses(), loadStudents()]);
        } catch (err) {
            if (err.message !== "SESSION_EXPIRED") {
                showInlineError(el.studentFormError, "Unable to connect to the server.");
            }
        } finally {
            setButtonLoading(el.saveStudentBtn, false);
        }
    });

    /* ================= DELETE DIALOG ================= */
    function openDeleteDialog(student) {
        if (!isAdmin()) {
            showToast("Administrator access required", "error");
            return;
        }
        state.deletingStudent = student;
        el.deleteDialogMessage.textContent = `Delete "${student.name}" (${student.email})? This action cannot be undone.`;
        el.deleteDialog.showModal();
    }

    function closeDeleteDialog() {
        state.deletingStudent = null;
        el.deleteDialog.close();
    }

    el.cancelDeleteBtn.addEventListener("click", closeDeleteDialog);

    el.deleteDialog.addEventListener("click", (event) => {
        if (event.target === el.deleteDialog) {
            closeDeleteDialog();
        }
    });

    el.confirmDeleteBtn.addEventListener("click", async () => {
        if (!state.deletingStudent) return;
        const target = state.deletingStudent;

        setButtonLoading(el.confirmDeleteBtn, true);
        try {
            const response = await apiFetch(`/studentmgmt/${target.id}`, {
                method: "DELETE"
            });

            if (!response.ok) {
                showToast("Unable to delete student", "error");
                return;
            }

            closeDeleteDialog();
            showToast(`Deleted ${target.name}`);
            await Promise.all([loadCourses(), loadStudents()]);
        } catch (err) {
            if (err.message !== "SESSION_EXPIRED") {
                showToast("Unable to connect to the server", "error");
            }
        } finally {
            setButtonLoading(el.confirmDeleteBtn, false);
        }
    });

    /* ================= BOOTSTRAP ================= */
    async function init() {
        const token = localStorage.getItem(STORAGE_KEYS.accessToken);
        const payload = parseJwtPayload(token);

        if (payload && !isTokenExpired(payload)) {
            state.user = {
                username: payload.sub || "user",
                role: payload.role || "ROLE_USER"
            };
            showDashboardView();
            return;
        }

        if (localStorage.getItem(STORAGE_KEYS.refreshToken)) {
            const refreshed = await refreshAccessToken();
            if (refreshed && state.user) {
                showDashboardView();
                return;
            }
        }

        clearAuthTokens();
        showLoginView();
    }

    init();
})();
