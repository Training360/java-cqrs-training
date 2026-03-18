const API_URL = '/api/courses/enrollment-counts';

const tableBody = document.getElementById('table-body');
const lastUpdated = document.getElementById('last-updated');
const refreshBtn = document.getElementById('refresh-btn');
const spinner = document.getElementById('spinner');

function setLoading(loading) {
  refreshBtn.disabled = loading;
  spinner.style.display = loading ? 'block' : 'none';
}

function formatTime(date) {
  return date.toLocaleTimeString('hu-HU', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

function renderError(message) {
  tableBody.innerHTML = `
    <tr id="status-row">
      <td colspan="3" class="error-msg">⚠️ ${message}</td>
    </tr>`;
  lastUpdated.textContent = 'Hiba a betöltés során';
}

function renderEmpty() {
  tableBody.innerHTML = `
    <tr id="status-row">
      <td colspan="3">Nincs megjeleníthető adat.</td>
    </tr>`;
}

function renderRows(data) {
  if (!data || data.length === 0) {
    renderEmpty();
    return;
  }

  tableBody.innerHTML = data
    .map((item, index) => `
      <tr>
        <td>${index + 1}</td>
        <td><strong>${item.courseId}</strong></td>
        <td>
          <span class="badge ${item.enrollmentCount === 0 ? 'zero' : ''}">
            ${item.enrollmentCount}
          </span>
        </td>
      </tr>`)
    .join('');

  lastUpdated.textContent = `Utoljára frissítve: ${formatTime(new Date())}`;
}

async function fetchEnrollmentCounts() {
  setLoading(true);
  try {
    const response = await fetch(API_URL);
    if (!response.ok) {
      throw new Error(`HTTP ${response.status} – ${response.statusText}`);
    }
    const data = await response.json();
    renderRows(data);
  } catch (err) {
    renderError(err.message || 'Ismeretlen hiba');
  } finally {
    setLoading(false);
  }
}

refreshBtn.addEventListener('click', fetchEnrollmentCounts);

fetchEnrollmentCounts();

